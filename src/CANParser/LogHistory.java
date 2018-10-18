package CANParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class LogHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    public long startTime;
    public HashMap<String, ArrayList<Event> > events = new HashMap<String, ArrayList<Event> >();

    public LogHistory() {

    }

    public static class Event implements Serializable {
        private static final long serialVersionUID = 1L;
        public int time;
        public long val;

        public Event(int time, long val) {
            this.time = time;
            this.val = val;
        }
    }
}
