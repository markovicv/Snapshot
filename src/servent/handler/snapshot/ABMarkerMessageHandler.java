package servent.handler.snapshot;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.ABMarkerMessage;
import servent.message.util.MessageUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ABMarkerMessageHandler implements MessageHandler {


    private Message clientMessage;
    private SnapshotCollector snapshotCollector;

    public ABMarkerMessageHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {

        /*
            1 - add marker to queue
            2 - check if we can commit causal message
            3 - send snapshot result back to original node
         */


        if(clientMessage.getMessageType() == MessageType.AB_MARKER){

            if(clientMessage.getOriginalSenderInfo().getId() != AppConfig.myServentInfo.getId()){

                boolean didPut = CausalBroadcastShared.seenMessages.add(clientMessage);

                if(didPut){
                    CausalBroadcastShared.addPendingMessages(clientMessage);
                    CausalBroadcastShared.checkPandingMessages();

                    AppConfig.timestampedStandardPrint("Rebroadcasting");

                    for(Integer neighbor:AppConfig.myServentInfo.getNeighbors()){
                        MessageUtil.sendMessage(clientMessage.changeReceiver(neighbor).makeMeASender());


                    }


                }
                else
                    AppConfig.timestampedStandardPrint("Has this message. No rebroadcasting");

            }
        }
    }

    private void rebroadcast() {

    }
}
