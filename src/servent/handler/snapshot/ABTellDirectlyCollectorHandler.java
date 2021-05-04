package servent.handler.snapshot;

import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.ABTellDirectlyCollectorMessage;

public class ABTellDirectlyCollectorHandler implements MessageHandler {

    private Message clientMessage;
    private SnapshotCollector snapshotCollector;

    public ABTellDirectlyCollectorHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType()== MessageType.AB_TELL_DIRECT){

            ABTellDirectlyCollectorMessage abTellDirectlyCollector = (ABTellDirectlyCollectorMessage) clientMessage;
            System.out.println("Recordovano aaa: "+abTellDirectlyCollector.getAbSnapshotResult().getRecordedAmount());


            snapshotCollector.addAcharyaBadrinathInfo(abTellDirectlyCollector.getOriginalSenderInfo().getId(),
                    abTellDirectlyCollector.getAbSnapshotResult());

        }
    }
}
