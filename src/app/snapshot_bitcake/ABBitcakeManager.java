package app.snapshot_bitcake;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.snapshot.ABMarkerMessage;
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
    public void markEvent(ServentInfo collectorInfo,SnapshotCollector snapshotCollector){
        recordedAmount = getCurrentBitcakeAmount();

        Message abMarkerMessage = new ABMarkerMessage(AppConfig.myServentInfo,null,AppConfig.myServentInfo.getId(),CausalBroadcastShared.vectorClock);
        for(Integer neighbor : AppConfig.myServentInfo.getNeighbors()){
            abMarkerMessage = abMarkerMessage.changeReceiver(neighbor);
            MessageUtil.sendMessage(abMarkerMessage);
        }



    }


    public void handleMarker(Message clientMessage,SnapshotCollector snapshotCollector){


    }
}
