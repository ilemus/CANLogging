package UI;

import java.io.File;

import java.util.Scanner;
import java.util.Map;
import java.util.ArrayList;

import CANDB.CanDbLoader;
import CANDB.CanDb.Message;
import CANDB.CanDb.Signal;
import CANDB.CanDb;

import CANParser.LoadLog;
import CANParser.LogHistory;
import CANParser.LogHistory.Event;
import CANParser.Progress;

import Cacher.ReadAndSave;

public class MainView {
    private CanDb mCcan = null;
    private CanDb mBcan = null;
    private CanDb mMMcan = null;

    public void start() {
        mCcan = promptUser("CCAN");
        mBcan = promptUser("BCAN");
        mMMcan = promptUser("MMCAN");

        
        /*for (Map.Entry<Long, Message> entry : mCcan.messages.entrySet()) {
            System.out.print("Message: " + String.format("%03X", entry.getKey()) + ", "
                + ((Message) entry.getValue()).msgName + ":");
            for (Signal signal: ((Message) entry.getValue()).signals) {
                System.out.print(" " + signal.name);
            }
            System.out.println("");
        }*/
        

        // Get TMU11
        /*Message msg = mCcan.messages.get((long) 0x587);
        if (msg != null) {
            System.out.println("Size of TMU11 signals: " + msg.signals.size());
            for (int i = 0; i < msg.signals.size(); i++) {
                System.out.println(msg.signals.get(i));
            }
        } else {
            System.out.println("ERROR, TMU11 null");
        }*/

        LogHistory lh = promptUserEnterLog();
        System.out.println("");
        printSignal(lh, "EMS11:N");
        System.out.println("Saving...");
        ReadAndSave.saveHistory(lh);
    }

    private void printSignal(LogHistory lh, String name) {
        if (!lh.events.containsKey(name)) {
            System.out.println("does not have history of: " + name);
            return;
        }

        ArrayList<Event> events = lh.events.get(name);
        for (int i = 0; i < events.size(); i++) {
            System.out.println(name + " " + String.format("%X", events.get(i).val));
        }
    }

    private LogHistory promptUserEnterLog() {
        System.out.print("Input log path: ");
        String line = readLine();

        File file = new File(line);

        if (!file.exists()) {
            // TODO: some meaningful continue
            System.err.println("File does not exist: " + line);
            return new LogHistory();
        }

        final LoadLog loader = new LoadLog(file, mCcan, mBcan, mMMcan);
        loader.registerProgress(new Progress() {
            public int prevLength = 0;
            @Override
            public void onUpdate() {
                String s = new String(loader.countSoFar + " / " + loader.fileCount);
                for (int i = 0; i < prevLength; i++) {
                    System.out.print("\b");
                }
                prevLength = s.length();
                System.out.print(s);
            }
        });

        return loader.readFile();
    }

    private CanDb promptUser(String type) {
        System.out.print("Input DB path [" + type + "]: ");
        String line = readLine();

        File file = new File(line);

        if (!file.exists()) {
            // TODO: use old DB or no DB
            System.err.println("Using default DB");
            return new CanDb();
        }

        return CanDbLoader.loadFromFile(file);
    }

    private String readLine() {
        Scanner input = new Scanner(System.in);
        if (input.hasNextLine()) {
            return input.nextLine();
        }
        return "";
    }
}
