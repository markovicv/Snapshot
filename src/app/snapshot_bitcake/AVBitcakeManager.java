package app.snapshot_bitcake;

import app.AppConfig;
import servent.message.Message;
import servent.message.snapshot.AVMarkerMessage;
import servent.message.snapshot.DoneMessage;
import servent.message.util.MessageUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AVBitcakeManager implements BitcakeManager{

    private AtomicInteger currentAmount = new AtomicInteger(1000);


    @Override
    public void takeSomeBitcakes(int amount) {
        this.currentAmount.getAndAdd(-amount);
    }

    @Override
    public void addSomeBitcakes(int amount) {
        this.currentAmount.getAndAdd(amount);
    }

    @Override
    public int getCurrentBitcakeAmount() {
        return currentAmount.get();
    }

    /*
        sve poruke moraju da budu old ili new, new marker!!!
     */

    public Map<String, List<Integer>> channels = new ConcurrentHashMap<>();

    public void initSnapshot(){
        Map<Integer,Integer> myClock = CausalBroadcastShared.getVectorClock();
        Map<Integer,Integer> myClockCopy = new ConcurrentHashMap<>(myClock);
        Message avMarkerMessage = new AVMarkerMessage(AppConfig.myServentInfo,null,AppConfig.myServentInfo.getId(),myClockCopy);

        for(Integer neighbor:AppConfig.myServentInfo.getNeighbors()){
            avMarkerMessage = avMarkerMessage.changeReceiver(neighbor);
            MessageUtil.sendMessage(avMarkerMessage);
        }
        avMarkerMessage.changeReceiver(AppConfig.myServentInfo.getId());
        MessageUtil.sendMessage(avMarkerMessage);
        CausalBroadcastShared.commitCausalMessage(avMarkerMessage);

    }
    public void handleMarker(Message clientMessage,SnapshotCollector snapshotCollector,int currentBitcake){
        AVSnapshotResult avSnapshotResult = new AVSnapshotResult(AppConfig.myServentInfo.getId(),currentBitcake);
        /*
          dodaj u svoje stanje lokalni rezultat | DONE!
         */

        AppConfig.avSnapshotResult = avSnapshotResult;

        channels.clear();

        /*
            vrati Done poruku inicijatoru | DONE!
         */
        Message doneMessage = new DoneMessage(AppConfig.myServentInfo,null,clientMessage.getOriginalSenderInfo().getId());
        for(Integer neighbor:AppConfig.myServentInfo.getNeighbors()){
            doneMessage = doneMessage.changeReceiver(neighbor);
            MessageUtil.sendMessage(doneMessage);
        }

    }
}
