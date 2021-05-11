package servent.message.snapshot;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.util.Map;

public class AVMarkerMessage extends BasicMessage {

    public AVMarkerMessage(ServentInfo sender, ServentInfo receiver,int collectorId, Map<Integer,Integer> vectorClock){
        super(MessageType.AV_MARKER,sender,receiver,String.valueOf(collectorId));
        this.senderVectorClock =vectorClock;
    }
}
