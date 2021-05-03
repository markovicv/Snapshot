package servent.message.snapshot;

import app.ServentInfo;
import app.snapshot_bitcake.ABSnapshotResult;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class ABTellDirectlyCollectorMessage extends BasicMessage {

    private ABSnapshotResult abSnapshotResult;

    public ABTellDirectlyCollectorMessage(ServentInfo sender, ServentInfo receiver, ABSnapshotResult abSnapshotResult){
        super(MessageType.AB_TELL_DIRECT,sender,receiver);
        this.abSnapshotResult = abSnapshotResult;

    }

    public ABSnapshotResult getAbSnapshotResult() {
        return abSnapshotResult;
    }
}
