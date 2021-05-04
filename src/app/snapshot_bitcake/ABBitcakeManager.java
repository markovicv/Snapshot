package app.snapshot_bitcake;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.snapshot.ABMarkerMessage;
import servent.message.snapshot.ABTellDirectlyCollectorMessage;
import servent.message.util.MessageUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
//        recordedAmount = getCurrentBitcakeAmount();

//        Message abMarkerMessage = new ABMarkerMessage(AppConfig.myServentInfo,null,AppConfig.myServentInfo.getId(),CausalBroadcastShared.vectorClock);

        Map<Integer,Integer> myClock = CausalBroadcastShared.getVectorClock();
        Map<Integer,Integer> myClockCopy = new ConcurrentHashMap<>(myClock);
        ServentInfo myInfo = AppConfig.myServentInfo;
        for(Integer neighbor : AppConfig.myServentInfo.getNeighbors()){

            Message abMarkerMessage = new ABMarkerMessage(myInfo,AppConfig.getInfoById(neighbor),AppConfig.myServentInfo.getId(),myClockCopy);
            MessageUtil.sendMessage(abMarkerMessage);

            /*
                for not complete graph
             */
//            abMarkerMessage = abMarkerMessage.changeReceiver(neighbor);
//            System.out.println(abMarkerMessage);
//            MessageUtil.sendMessage(abMarkerMessage);
        }


        Message commitMessageLocaly = new ABMarkerMessage(myInfo,myInfo,AppConfig.myServentInfo.getId(),myClockCopy);
        MessageUtil.sendMessage(commitMessageLocaly);
        CausalBroadcastShared.commitCausalMessage(commitMessageLocaly);


//        CausalBroadcastShared.sendSnapshotResult(snapshotCollector.getBitcakeManager(), snapshotCollector);





    }


    public void handleMarker(Message clientMessage,SnapshotCollector snapshotCollector,int currentBitcake){
        int collectorId = Integer.parseInt(clientMessage.getMessageText());
//        System.out.println("id: "+clientMessage.getOriginalSenderInfo().getId()+" rec: "+clientMessage.getReceiverInfo().getId()+ " msg: "+clientMessage.getMessageText());

        ABSnapshotResult snapshotResult = new ABSnapshotResult(AppConfig.myServentInfo.getId(),currentBitcake);
        System.out.println("Pre recordovanja : "+currentBitcake);
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
