package servent.handler.snapshot;

import app.AppConfig;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.ABTellMessage;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ABTellMessageHandler implements MessageHandler {

    private Message clientMessage;
    private SnapshotCollector snapshotCollector;

    public ABTellMessageHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.AB_TELL){
            // TODO napravljen konkurentni set MessageHandler

            boolean didPut = AppConfig.seen.add(clientMessage);


            if(didPut){
                AppConfig.timestampedErrorPrint("my id: "+AppConfig.myServentInfo.getId()+" originalSender: "+clientMessage.getCollectorId());
                if(AppConfig.myServentInfo.getId() == clientMessage.getCollectorId()){
                    snapshotCollector.addAcharyaBadrinathInfo(clientMessage.getOriginalSenderInfo().getId(),clientMessage.getABSnapshotResult());
                }
                else{
                    for(Integer neighbor:AppConfig.myServentInfo.getNeighbors()){
                        clientMessage = clientMessage.changeReceiver(neighbor).makeMeASender();
                        MessageUtil.sendMessage(clientMessage);
                    }
                }
            }
            else {
                AppConfig.timestampedStandardPrint("Dont rebroadcast, seen this message");
            }


        }
    }
}
