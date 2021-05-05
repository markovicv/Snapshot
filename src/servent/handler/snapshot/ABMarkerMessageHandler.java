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

                // TODO uradjen konkurentan set za Causal
                boolean didPut = CausalBroadcastShared.seenMessages.add(clientMessage);

                if(didPut){
                    CausalBroadcastShared.seenMessages.add(clientMessage);
                    CausalBroadcastShared.addPendingMessages(clientMessage);
                    CausalBroadcastShared.checkPandingMessages();
//                    CausalBroadcastShared.sendSnapshotResult(snapshotCollector.getBitcakeManager(),snapshotCollector);

                    AppConfig.timestampedStandardPrint("Rebroadcasting");
                    Map<Integer,Integer> myClockCopy = clientMessage.getVectorClock();
                    for(Integer neighbor:AppConfig.myServentInfo.getNeighbors()){
//                        clientMessage.setVectorClock(myClockCopy);
                        MessageUtil.sendMessage(clientMessage.changeReceiver(neighbor).makeMeASender());


                    }

                    CausalBroadcastShared.commitCausalMessage(new ABMarkerMessage(AppConfig.myServentInfo,AppConfig.myServentInfo,AppConfig.myServentInfo.getId(),myClockCopy));

                }
                else
                    AppConfig.timestampedStandardPrint("Has this message. No rebroadcasting");

            }
        }
    }

    private void rebroadcast() {

    }
}
