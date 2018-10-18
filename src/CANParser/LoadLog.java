package CANParser;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import CANDB.CanDb;
import CANDB.CanDb.Message;
import CANDB.CanDb.Signal;
import CANParser.Progress;
import CANParser.LogHistory;
import CANParser.LogHistory.Event;

/**
Begin Triggerblock Wed Aug 29 06:26:42.271 pm 2018
  92.391838 1  200             Tx   d 8 00 00 00 00 00 10 00 00  Length = 245987 BitCount = 126 ID = 512
  92.392028 1  2B0             Tx   d 5 00 00 00 07 00  Length = 183987 BitCount = 95 ID = 688
End TriggerBlock
*/
// Noise:
//  125.005370 2  Statistic: D 145 R 0 XD 0 XR 0 E 0 O 0 B 18.14%
public class LoadLog {
    private File mFile;
    private CanDb mCcan;
    private CanDb mBcan;
    private CanDb mMMcan;

    public static final byte CCAN_LINE = 1;
    public static final byte BCAN_LINE = 2;
    public static final byte MMCAN_LINE = 3;

    public int fileCount;
    public int countSoFar = 0;
    public int progressMilestone = 1000;
    private ArrayList<Progress> mProg = new ArrayList<Progress>();

    public LoadLog() {

    }

    public LoadLog(File file, CanDb ccan, CanDb bcan, CanDb mmcan) {
        mFile = file;
        mCcan = ccan;
        mBcan = bcan;
        mMMcan = mmcan;
        try {
            fileCount = countLines(file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LogHistory readFile() {
        LogHistory lh = new LogHistory();

        try {
            BufferedReader br = new BufferedReader(new FileReader(mFile));
            readHeader(lh, br);
            readHistory(lh, br);
        } catch (Exception e) {
            // TODO: make more accurate exception catching
            e.printStackTrace();
        }

        return lh;
    }

    // Begin Triggerblock Wed Aug 29 06:26:42.271 pm 2018
    public void readHeader(LogHistory history, BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            countSoFar++;
            if (line.startsWith("Begin")) {
                setHeader(history, line);
                break;
            }
        }
    }

    public void setHeader(LogHistory lh, String line) {
        String date = line.substring(19, line.length());
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss.SSS aa yyyy");
            Date d = sdf.parse(date);
            lh.startTime = d.getTime();
        } catch (ParseException e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss aa yyyy");
                Date d = sdf.parse(date);
                lh.startTime = d.getTime();
            } catch (ParseException e1) {
                e.printStackTrace();
                e1.printStackTrace();
            }
        }
        

    }

    //   92.392028 1  2B0             Tx   d 5 00 00 00 07 00  Length = 183987 BitCount = 95 ID = 688
    //  125.005370 2  Statistic: D 145 R 0 XD 0 XR 0 E 0 O 0 B 18.14%
    public void readHistory(LogHistory history, BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            countSoFar++;
            if (countSoFar % progressMilestone == 0) notifyProgress();
            if (line.startsWith(" ")) {
                parseEvent(line, history);
            } else if (line.startsWith("End TriggerBlock")) {
                break;
            }
        }
    }

    public void parseEvent(String line, LogHistory history) {
        String[] arr = line.split(" +");
        // If [4] == Tx || Rx it is a frame
        if (!arr[4].equals("Tx") && !arr[4].equals("Rx")) return;

        // [1] is always time difference
        int t = (int) (1000.0 * Float.parseFloat(arr[1]));
        byte len = Byte.parseByte(arr[6]);
        // [6] == length of message, to find ID
        long id = Long.parseLong(arr[6 + len + 9]);
        byte canLine = Byte.parseByte(arr[2]);
        long data = getMessage(arr, len);

        switch (canLine) {
            case CCAN_LINE:
                if (mCcan.messages.containsKey(id)) {
                    parseMessage(mCcan.messages.get(id), t, data, history);
                }
                break;
            case BCAN_LINE:
                if (mBcan.messages.containsKey(id)) {
                    parseMessage(mBcan.messages.get(id), t, data, history);
                }
                break;
            case MMCAN_LINE:
                if (mMMcan.messages.containsKey(id)) {
                    parseMessage(mMMcan.messages.get(id), t, data, history);
                }
                break;
            default:
                break;
        }
    }

    // 00 00 00 07 00 -> (long) 0000000700
    public long getMessage(String[] arr, byte len) {
        long data = 0;
        int l1 = len - 1;
        for (int i = 0; i < len; i++) {
            // 6 7  8  9  A  B
            // 5 00 00 00 07 00
            //               ^
            //            ^
            data |= (long) (Long.parseLong(arr[7 + i], 16) << ((l1 - i) * 8));
        }
        return data;
    }

    public void parseMessage(Message msg, int time, long data, LogHistory history) {
        StringBuilder sb = new StringBuilder(msg.msgName);
        int len = msg.msgName.length();

        // For each signal in message
        for (int i = 0; i < msg.signals.size(); i++) {
            // get signal value
            long value = parseSignal(msg.signals.get(i), data, msg.msgLength);

            // check if we have history
            sb.append(":").append(msg.signals.get(i).name);
            String key = sb.toString();
            if (history.events.containsKey(key)) {
                // Add to existing history if value is changed
                ArrayList<Event> events = history.events.get(key);
                if (events.get(events.size() - 1).val != value) {
                    events.add(new Event(time, value));
                }
            } else {
                // First event for signal
                ArrayList<Event> list = new ArrayList<Event>();
                list.add(new Event(time, value));
                history.events.put(key, list);
            }

            sb.setLength(len);
        }
    }

    // 0000000700 -> 7
    // signal, 000000 0000000700, 5
    // signal.startBit = 30
    // signal.length = 4
    public long parseSignal(Signal signal, long frame, int length) {
        return (frame >> ((8 * length) - (signal.startBit + signal.length))) & signal.mask;
    }

    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            int count = 0;
            while (readChars == 1024) {
                for (int i=0; i<1024;) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                System.out.println(readChars);
                for (int i=0; i<readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        } finally {
            is.close();
        }
    }

    private void notifyProgress() {
        for (int i = 0; i < mProg.size(); i++) {
            mProg.get(i).onUpdate();
        }
    }

    public void registerProgress(Progress listener) {
        mProg.add(listener);
    }

    public void unRegisterProgress(Progress listener) {
        for (int i = 0; i < mProg.size(); i++) {
            if (mProg.get(i) == listener) {
                mProg.remove(i);
                break;
            }
        }
    }
}
