package servent.message.snapshot;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class TerminateMessage extends BasicMessage {

    public TerminateMessage(ServentInfo sender,ServentInfo receiver,int nothing){
        super(MessageType.TERMINATE,sender,receiver,String.valueOf(nothing));
    }
}
