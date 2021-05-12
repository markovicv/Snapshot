package servent.handler.snapshot;

import app.AppConfig;
import app.snapshot_bitcake.AVBitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.util.List;
import java.util.Map;

public class TerminateHandler implements MessageHandler {

    private Message clientMessage;
    private SnapshotCollector snapshotCollector;


    public TerminateHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.TERMINATE){
            boolean didPut = AppConfig.seen.add(clientMessage);
            if(didPut){
                AppConfig.timestampedStandardPrint(String.valueOf(AppConfig.avSnapshotResult.getRecordedAmount()));

                AVBitcakeManager avBitcakeManager = (AVBitcakeManager) snapshotCollector;

                for(Map.Entry<String, List<Integer>> entry : avBitcakeManager.channels.entrySet()){
                    int channelSum = 0;
                    for(Integer val:entry.getValue()){
                        channelSum+=val;
                    }
                    AppConfig.timestampedStandardPrint("Channel bitcake for " + entry.getKey() +
                            ": " + entry.getValue() + " with channel bitcake sum: " + channelSum);
                }


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
