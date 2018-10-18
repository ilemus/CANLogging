package Cacher;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import CANParser.LogHistory;

public class ReadAndSave {
    public static final String HISTORY_FILE = "history.dat";

    public static void saveHistory(LogHistory lh) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(HISTORY_FILE));
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(lh);

            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean historyExists() {
        File f = new File(HISTORY_FILE);
        return f.exists();
    }

    public static LogHistory loadHistory() {
        LogHistory lh;
        try {
            FileInputStream fis = new FileInputStream(new File(HISTORY_FILE));
            ObjectInputStream ois = new ObjectInputStream(fis);

            lh = (LogHistory) ois.readObject();

            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            lh = new LogHistory();
        }

        return lh;
    }
}
