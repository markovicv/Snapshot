package servent.handler.snapshot;

import app.AppConfig;
import app.snapshot_bitcake.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AVMarkerMessageHandler implements MessageHandler {

    private Message clientMessage;
    private SnapshotCollector snapshotCollector;

    public AVMarkerMessageHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.AV_MARKER){
            if(clientMessage.getOriginalSenderInfo().getId()!= AppConfig.myServentInfo.getId()){
                boolean didPut = CausalBroadcastShared.seenMessages.add(clientMessage);
                if(didPut){
                    CausalBroadcastShared.addPendingMessages(clientMessage);
                    CausalBroadcastShared.checkPandingMessages();

                    AppConfig.timestampedStandardPrint("Rebroadcasting");

                    for(Integer neighbor:AppConfig.myServentInfo.getNeighbors()){
                        MessageUtil.sendMessage(clientMessage.changeReceiver(neighbor).makeMeASender());


                    }
                }
                else{
                    AppConfig.timestampedStandardPrint("Has this message. No rebroadcasting");
                }
            }
        }

    }
}
