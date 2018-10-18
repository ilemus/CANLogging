package CANDB;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;

public class CanDb implements Serializable {
    private static final long serialVersionUID = 1L;

    public HashMap<Long, Message> messages;
    public CanDb(HashMap<Long, Message> messages) {
        this.messages = messages;
    }
    public CanDb() {
        this.messages = new HashMap<Long, Message>();
    }

    public static class Message {
        public long msgId;
        public String msgName = "";
        public int msgLength;
        public String msgSender = "";
        public ArrayList<Signal> signals = new ArrayList<Signal>();

        public Message() {
        }

        public Message(long msgId, String msgName, int msgLength, String msgSender) {
            this.msgId = msgId;
            this.msgName = msgName;
            this.msgLength = msgLength;
            this.msgSender = msgSender;
        }
    }

    public static class Signal {
        public String name = "";
        public int startBit;
        public int length;
        public long mask;

        public Signal() {
        }

        public Signal(String name, int startBit, int length) {
            this.name = name;
            this.startBit = startBit;
            this.length = length;
            this.mask = 0;
            for (int i = 0; i < this.length; i++) {
                this.mask = this.mask << 1;
                this.mask++;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append(" ").append(startBit).append(" ").append(length).append(" ").append(String.format("%08X", mask));
            return sb.toString();
        }
    }
}
