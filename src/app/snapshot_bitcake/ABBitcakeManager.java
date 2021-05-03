package app.snapshot_bitcake;

import app.AppConfig;
import servent.message.Message;
import servent.message.snapshot.ABMarkerMessage;
import servent.message.snapshot.ABTellDirectlyCollectorMessage;
import servent.message.util.MessageUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class ABBitcakeManager implements BitcakeManager{


    private AtomicInteger currentAmount = new AtomicInteger(1000);
    private int recordedAmount = 0;


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
        recordedAmount = getCurrentBitcakeAmount();

        Message abMarkerMessage = new ABMarkerMessage(AppConfig.myServentInfo,null,AppConfig.myServentInfo.getId(),CausalBroadcastShared.vectorClock);
        for(Integer neighbor : AppConfig.myServentInfo.getNeighbors()){
            abMarkerMessage = abMarkerMessage.changeReceiver(neighbor);
            System.out.println(abMarkerMessage);
            MessageUtil.sendMessage(abMarkerMessage);
        }

        // commit localy

        Message commitMessageLocaly = new ABMarkerMessage(AppConfig.myServentInfo,AppConfig.myServentInfo,AppConfig.myServentInfo.getId(),CausalBroadcastShared.vectorClock);
        System.out.println(commitMessageLocaly);
//        MessageUtil.sendMessage(commitMessageLocaly);

        CausalBroadcastShared.commitCausalMessage(commitMessageLocaly);



    }


    public void handleMarker(Message clientMessage,SnapshotCollector snapshotCollector){
        int collectorId = Integer.parseInt(clientMessage.getMessageText());
        System.out.println("id: "+clientMessage.getOriginalSenderInfo().getId()+" rec: "+clientMessage.getReceiverInfo().getId()+ " msg: "+clientMessage.getMessageText());

        ABSnapshotResult snapshotResult = new ABSnapshotResult(AppConfig.myServentInfo.getId(),recordedAmount);
        if(AppConfig.myServentInfo.getId() == collectorId)
            snapshotCollector.addAcharyaBadrinathInfo(collectorId,snapshotResult);
        else{
            // bad way, sending message directly to collector
            Message directMessage = new ABTellDirectlyCollectorMessage(AppConfig.myServentInfo,AppConfig.getInfoById(collectorId),snapshotResult);
            MessageUtil.sendMessage(directMessage);

            // good way, broadcast snapshot result to neighbors
        }

        recordedAmount = 0;

    }
}
