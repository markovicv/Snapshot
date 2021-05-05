package servent.message.snapshot;

import app.ServentInfo;
import app.snapshot_bitcake.ABSnapshotResult;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class ABTellMessage extends BasicMessage {



    public ABTellMessage(ServentInfo sender, ServentInfo receiver, ABSnapshotResult abSnapshotResult,int collectorId){
        super(MessageType.AB_TELL,sender,receiver);
        this.abSnapshotResult = abSnapshotResult;
        this.collectorId =collectorId;
    }


}
