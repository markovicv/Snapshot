package servent.message.snapshot;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.util.Map;

/*
    Message that we causal broadcast
 */
public class ABMarkerMessage extends BasicMessage {



    public ABMarkerMessage(ServentInfo sender, ServentInfo receiver, int collectorId, Map<Integer,Integer> vectorClock){
        super(MessageType.AB_MARKER,sender,receiver,String.valueOf(collectorId));
        this.senderVectorClock = vectorClock;
    }



}
