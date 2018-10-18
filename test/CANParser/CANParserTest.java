package CANParser;

import java.util.ArrayList;
import java.util.Map;

import CANParser.LoadLog;
import CANParser.LogHistory;
import CANParser.LogHistory.Event;

import CANDB.CanDb.Message;
import CANDB.CanDb.Signal;

import junit.framework.TestCase;

public class CANParserTest extends TestCase {
    LoadLog log = new LoadLog();
    public CANParserTest(String testName) {
        super(testName);

        System.out.println("WE ARE TESTING!!!");
    }

    public void testSignal() {
        Signal signal = new Signal("testSignal", 0, 4);
        long data = 0xFFFF;
        long value = log.parseSignal(signal, data, 2);
        assertEquals((long)0x0F, value);

        data = 0x3FFF;
        value = log.parseSignal(signal, data, 2);
        assertEquals((long)0x03, value);

        signal = new Signal("testSignal", 8, 8);
        data = 0xFF30;
        value = log.parseSignal(signal, data, 2);
        assertEquals((long) 0x30, value);
    }

    public void testMessage() {
        String[] arr = { "", "", "", "", "", "", "3", "00", "70", "08" };
        long value = log.getMessage(arr, (byte) 3);
        assertEquals((long) 0x007008, value);
    }

    public void testMessageParser() {
        Message msg = new Message();
        LogHistory history = new LogHistory();
        int time = 19;
        long data = 0x007058;

        msg.msgName = "EMS1";
        msg.msgLength = 3;
        msg.msgSender = "EMS";

        Signal sig = new Signal("N", 8, 8);
        msg.signals.add(sig);
        sig = new Signal("Vanz", 16, 4);
        msg.signals.add(sig);
        sig = new Signal("DispSpeed", 20, 4);
        msg.signals.add(sig);

        assertEquals(3, msg.signals.size());

        log.parseMessage(msg, time, data, history);

        assertNotNull(history.events.get("EMS1:N"));
        assertEquals(0x70, history.events.get("EMS1:N").get(0).val);
        assertNotNull(history.events.get("EMS1:Vanz"));
        assertEquals(0x05, history.events.get("EMS1:Vanz").get(0).val);
        assertNotNull(history.events.get("EMS1:DispSpeed"));
        assertEquals(0x08, history.events.get("EMS1:DispSpeed").get(0).val);
        assertEquals(19, history.events.get("EMS1:DispSpeed").get(0).time);

        // 0x007058
        data = 0x007048;
        time = 20;
        log.parseMessage(msg, time, data, history);

        assertNotNull(history.events.get("EMS1:Vanz"));
        assertEquals(0x04, history.events.get("EMS1:Vanz").get(1).val);
        assertEquals(20, history.events.get("EMS1:Vanz").get(1).time);

        // 0x007048
        data = 0x007048;
        time = 21;
        log.parseMessage(msg, time, data, history);

        assertNotNull(history.events.get("EMS1:Vanz"));
        assertEquals(2, history.events.get("EMS1:Vanz").size());
    }
}
