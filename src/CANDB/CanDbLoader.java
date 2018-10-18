package CANDB;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import CANDB.CanDb.Message;
import CANDB.CanDb.Signal;


public class CanDbLoader {
    public static CanDb loadFromFile(File file) {
        HashMap<Long, Message> db = new HashMap<Long, Message>();

        try {
            Message current = new Message();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            Object result;
            while ((line = br.readLine()) != null) {
                result = parseLine(line);
                if (result instanceof Message) {
                    current = (Message) result;
                    db.put(current.msgId, (Message) current);
                } else if (result instanceof Signal) {
                    current.signals.add((Signal) result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CanDb(db);
    }

    private static Object parseLine(String line) {
        if (line.startsWith("BO_")) {
            return parseMessage(line);
        } else if (line.startsWith(" SG_")) {
            return parseSignal(line);
        } else {
            return null;
        }
    }

    // BO_ 1952 DiagReqBCM: 8 IBOX
    // 7A0, DiagReqBCM, 8, IBOX
    private static Message parseMessage(String line) {
        // Split by spaces
        String[] arr = line.split(" ");
        // Default constructor
        if (arr.length < 5) return new Message();

        long msgId = Long.parseLong(arr[1]);
        String msgName = arr[2].substring(0, arr[2].length() - 1);
        int msgLength = Integer.parseInt(arr[3]);
        String msgSender = arr[4];

        return new Message(msgId, msgName, msgLength, msgSender);
    }

    //  SG_ ReqBCM_B8 : 56|8@1+ (1,0) [0|0] ""  BCM
    // ReqBCM_B8, 56, 8
    private static Signal parseSignal(String line) {
        String[] arr = line.split(" ");
        if (arr.length < 5) return new Signal();

        String signalName = arr[2];
        
        List<String> signalData = new ArrayList<String>();
        Matcher m = Pattern.compile("[0-9]+").matcher(arr[4]);
        while (m.find()) {
            signalData.add(m.group(0));
        }

        int startBit = Integer.parseInt(signalData.get(0));
        int length = Integer.parseInt(signalData.get(1));

        return new Signal(signalName, startBit, length);
    }
}
