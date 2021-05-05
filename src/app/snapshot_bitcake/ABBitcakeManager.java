package app.snapshot_bitcake;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.snapshot.ABMarkerMessage;
import servent.message.snapshot.ABTellDirectlyCollectorMessage;
import servent.message.snapshot.ABTellMessage;
import servent.message.util.MessageUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ABBitcakeManager implements BitcakeManager{


    private AtomicInteger currentAmount = new AtomicInteger(1000);


    @Override
    public void takeSomeBitcakes(int amount) {
        currentAmount.getAndAdd(-amount);
    }

    @Override
    public void addSomeBitcakes(int amount) {
        currentAmount.getAndAdd(amount);
    }

    @Override
    public int getCurrentBitcakeAmount() {
        return currentAmount.get();
    }


    /*
        initiates causal broadcast marker to all neighbors
     */
    public void markEvent(){

        Map<Integer,Integer> myClock = CausalBroadcastShared.getVectorClock();
        Map<Integer,Integer> myClockCopy = new ConcurrentHashMap<>(myClock);

        ServentInfo myInfo = AppConfig.myServentInfo;
        Message abMarkerMessage = new ABMarkerMessage(AppConfig.myServentInfo,null,AppConfig.myServentInfo.getId(),myClockCopy);

        for(Integer neighbor : AppConfig.myServentInfo.getNeighbors()){
          notCompleteGraphSend(neighbor,abMarkerMessage);


        }


        abMarkerMessage.changeReceiver(AppConfig.myServentInfo.getId());
        MessageUtil.sendMessage(abMarkerMessage);
        CausalBroadcastShared.commitCausalMessage(abMarkerMessage);


//        CausalBroadcastShared.sendSnapshotResult(snapshotCollector.getBitcakeManager(), snapshotCollector);

    }
    private void completeGraphSend(Integer neighbor,Map<Integer,Integer> myClockCopy,ServentInfo myInfo){
        Message abMarkerMessage = new ABMarkerMessage(myInfo,AppConfig.getInfoById(neighbor),AppConfig.myServentInfo.getId(),myClockCopy);
        MessageUtil.sendMessage(abMarkerMessage);

    }
    private void notCompleteGraphSend(Integer neighbor,Message abMarkerMessage){
        abMarkerMessage = abMarkerMessage.changeReceiver(neighbor);
        MessageUtil.sendMessage(abMarkerMessage);
    }


    public void handleMarker(Message clientMessage,SnapshotCollector snapshotCollector,int currentBitcake){


        ABSnapshotResult snapshotResult = new ABSnapshotResult(AppConfig.myServentInfo.getId(),currentBitcake);
        if(AppConfig.myServentInfo.getId() == clientMessage.getOriginalSenderInfo().getId())
            snapshotCollector.addAcharyaBadrinathInfo(clientMessage.getOriginalSenderInfo().getId(),snapshotResult);
        else{
            // bad way, sending message directly to collector
//            sendMessageToCollector(clientMessage,snapshotResult);

            // good way, broadcast snapshot result to neighbors
            broadcastTellMessage(clientMessage,snapshotResult);
        }


    }
//    private void sendMessageToCollector(Message clientMessage,ABSnapshotResult snapshotResult){
//        int collectorId = Integer.parseInt(clientMessage.getMessageText());
//        Message directMessage = new ABTellDirectlyCollectorMessage(AppConfig.myServentInfo,AppConfig.getInfoById(clientMessage.getOriginalSenderInfo().getId()),snapshotResult);
//        MessageUtil.sendMessage(directMessage);
//    }
    private void broadcastTellMessage(Message clientMessage,ABSnapshotResult snapshotResult){
        Message abTellMessage = new ABTellMessage(AppConfig.myServentInfo,null,snapshotResult,clientMessage.getOriginalSenderInfo().getId());

        for(Integer neighbor:AppConfig.myServentInfo.getNeighbors()){
            // TODO kopija napravljena
            abTellMessage = abTellMessage.changeReceiver(neighbor);
            MessageUtil.sendMessage(abTellMessage);
        }
    }

}
