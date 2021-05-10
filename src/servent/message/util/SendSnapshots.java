package servent.message.util;

import app.AppConfig;
import app.Cancellable;
import app.snapshot_bitcake.ABBitcakeManager;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.TransactionHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PoisonMessage;

import java.util.ArrayList;
import java.util.List;
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
                if(message.getMessageType() == MessageType.TRANSACTION){
                    if(message.getReceiverInfo().getId() == AppConfig.myServentInfo.getId()){
                        TransactionHandler.handleTransaction(message);


                    }

                }


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
