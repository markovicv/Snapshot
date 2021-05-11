package servent.message.snapshot;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class DoneMessage extends BasicMessage {

    public DoneMessage(ServentInfo sender,ServentInfo receiver,int collectorId){
        super(MessageType.DONE,sender,receiver);
        this.collectorId = collectorId;
    }
}
