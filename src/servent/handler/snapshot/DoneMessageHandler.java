package servent.handler.snapshot;

import app.AppConfig;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.DoneMessage;
import servent.message.util.MessageUtil;

public class DoneMessageHandler implements MessageHandler {
    private Message clientMessage;
    private SnapshotCollector snapshotCollector;

    public DoneMessageHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {

        if(clientMessage.getMessageType() == MessageType.DONE){

            boolean didPut = AppConfig.seen.add(clientMessage);
            if(didPut){
                if(AppConfig.myServentInfo.getId() == clientMessage.getCollectorId()){
                    /*
                     dodaj u collector da sam primio done poruku od cvoda | DONE!
                     */
                    snapshotCollector.addAVInfo(AppConfig.myServentInfo.getId(),(DoneMessage) clientMessage);
                }
                else {
                    for(Integer neighbor:AppConfig.myServentInfo.getNeighbors()){
                        clientMessage = clientMessage.changeReceiver(neighbor).makeMeASender();
                        MessageUtil.sendMessage(clientMessage);
                    }
                }
            }
            else{
                AppConfig.timestampedStandardPrint("Dont rebroadcast, seen this message");
            }
        }

    }
}
