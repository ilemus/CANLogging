package CANParser;

import java.util.ArrayList;
import java.util.HashMap;

public class LogHistory {
    public long startTime;
    public HashMap<String, ArrayList<Event> > events = new HashMap<String, ArrayList<Event> >();

    public LogHistory() {

    }

    public static class Event {
        public int time;
        public long val;

        public Event(int time, long val) {
            this.time = time;
            this.val = val;
        }
    }
}
