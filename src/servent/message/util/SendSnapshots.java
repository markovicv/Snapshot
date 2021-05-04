package servent.message.util;

import app.Cancellable;
import app.snapshot_bitcake.ABBitcakeManager;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PoisonMessage;

import java.util.concurrent.TimeUnit;

public class SendSnapshots implements Runnable, Cancellable {

    SnapshotCollector snapshotCollector;
    BitcakeManager bitcakeManager;

    public SendSnapshots(SnapshotCollector snapshotCollector) {
        this.snapshotCollector = snapshotCollector;
        this.bitcakeManager = snapshotCollector.getBitcakeManager();
    }

    @Override
    public void run() {

        while (true){



            try {
                Message message = CausalBroadcastShared.commitedCausalMessages.take();
                if(message.getMessageType()==MessageType.POISON)
                    break;
                if(message.getMessageType()==MessageType.AB_MARKER){
                    ABBitcakeManager abBitcakeManager = (ABBitcakeManager) bitcakeManager;
                    abBitcakeManager.handleMarker(message,snapshotCollector,abBitcakeManager.getCurrentBitcakeAmount());
                }


//                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void stop() {
        CausalBroadcastShared.commitedCausalMessages.add(new PoisonMessage());
    }
}
