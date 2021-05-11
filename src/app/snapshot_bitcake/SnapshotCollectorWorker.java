package app.snapshot_bitcake;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import app.AppConfig;
import servent.message.Message;
import servent.message.snapshot.DoneMessage;
import servent.message.snapshot.TerminateMessage;
import servent.message.util.MessageUtil;

/**
 * Main snapshot collector class. Has support for Naive, Chandy-Lamport
 * and Lai-Yang snapshot algorithms.
 *
 * @author bmilojkovic
 */
public class SnapshotCollectorWorker implements SnapshotCollector {

    private volatile boolean working = true;

    private AtomicBoolean collecting = new AtomicBoolean(false);


    private SnapshotType snapshotType = SnapshotType.NAIVE;

    private BitcakeManager bitcakeManager;
    private Map<Integer,ABSnapshotResult> collectedABValues = new ConcurrentHashMap<>();

    private Map<Integer,DoneMessage> doneMessages = new ConcurrentHashMap<>();

    public SnapshotCollectorWorker(SnapshotType snapshotType) {
        this.snapshotType = snapshotType;

        switch (snapshotType) {
            case AB:
                bitcakeManager = new ABBitcakeManager();
                break;


            case NONE:
                AppConfig.timestampedErrorPrint("Making snapshot collector without specifying type. Exiting...");
                System.exit(0);
        }
    }

    @Override
    public BitcakeManager getBitcakeManager() {
        return bitcakeManager;
    }

    @Override
    public void run() {
        while (working) {

            /*
             * Not collecting yet - just sleep until we start actual work, or finish
             */
            while (collecting.get() == false) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (working == false) {
                    return;
                }
            }

            /*
             * Collecting is done in three stages:
             * 1. Send messages asking for values
             * 2. Wait for all the responses
             * 3. Print result
             */

            //1 send asks
            switch (snapshotType) {

                case AB:
                    ((ABBitcakeManager)bitcakeManager).initSnapshot();
                    break;
                case AV:
                    ((AVBitcakeManager)bitcakeManager).initSnapshot();

                case NAIVE:
                    break;

            }

            //2 wait for responses or finish
            boolean waiting = true;
            while (waiting) {
                switch (snapshotType) {

                    case AB:
                        System.out.println("collected: "+collectedABValues.size()+ " - "+ "max: "+AppConfig.getServentCount());
                        if(collectedABValues.size() == AppConfig.getServentCount()){
                            waiting=false;
                        }
                        break;
                    case AV:
                        /*
                         waiting for done response
                         */
                        if(doneMessages.size() == AppConfig.getServentCount())
                            waiting=false;
                        break;

                    case NONE:
                        //Shouldn't be able to come here. See constructor.
                        break;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (working == false) {
                    return;
                }
            }

            //print
            int sum;
            switch (snapshotType) {
                case AB:
                    sum = 0;
                    for(Entry<Integer,ABSnapshotResult> entry:this.collectedABValues.entrySet()){
                        sum+=entry.getValue().getRecordedAmount();
                        AppConfig.timestampedStandardPrint(
                                "Recorded bitcake amount for " + entry.getKey() + " = " + entry.getValue().getRecordedAmount());
                    }

                    for(int i=0; i<AppConfig.getServentCount();i++){
                        for(int j=0;j<AppConfig.getServentCount();j++){
                            if(i!=j){
                                if(AppConfig.getInfoById(i).getNeighbors().contains(j)){
                                    /*
                                        uzmi sve poruke koje sam poslao j
                                     */
                                    int iSentMsgsSize = collectedABValues.get(i).getSEND().get(j).size();
                                    /*
                                        uzmi sve poruke koje sam primio od i
                                     */
                                    int jRecdMsgsSize = collectedABValues.get(j).getRECD().get(i).size();

                                    for(int k=jRecdMsgsSize+1;k<=iSentMsgsSize;k++){
                                        int amount = collectedABValues.get(i).getSEND().get(i).get(k);
                                        sum+=amount;
                                        AppConfig.timestampedStandardPrint("canal msg from "+i+" to "+j+" : "+ amount);
                                    }
                                }
                            }
                        }
                    }

                    AppConfig.timestampedStandardPrint("System bitcake count: " + sum);
                    collectedABValues.clear();
                    break;
                case AV:
                    Message terminateMessage = new TerminateMessage(AppConfig.myServentInfo,null,-1);
                    for(Integer neighbor : AppConfig.myServentInfo.getNeighbors()){
                        terminateMessage = terminateMessage.changeReceiver(neighbor);
                        MessageUtil.sendMessage(terminateMessage);
                    }

                case NONE:
                    //Shouldn't be able to come here. See constructor.
                    break;
            }
            collecting.set(false);
        }

    }

    @Override
    public void addNaiveSnapshotInfo(String snapshotSubject, int amount) {

    }

    //TODO
    @Override
    public void addAcharyaBadrinathInfo(int id,ABSnapshotResult snapshotResult) {
        if(collectedABValues.containsKey(id))
            return;
        System.out.println("UBACIO JE :::: "+id +" kolicina ::::"+snapshotResult.getRecordedAmount());
        this.collectedABValues.put(id,snapshotResult);

    }

    @Override
    public void addAVInfo(int id, DoneMessage doneMessage) {
        this.doneMessages.put(id,doneMessage);
    }

    @Override
    public void startCollecting() {
        boolean oldValue = this.collecting.getAndSet(true);

        if (oldValue == true) {
            AppConfig.timestampedErrorPrint("Tried to start collecting before finished with previous.");
        }
    }

    @Override
    public void stop() {
        working = false;
    }

}
