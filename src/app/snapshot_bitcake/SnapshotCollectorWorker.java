package app.snapshot_bitcake;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import app.AppConfig;
import servent.message.Message;
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
                    ((ABBitcakeManager)bitcakeManager).markEvent();
                    break;

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
                    AppConfig.timestampedStandardPrint("System bitcake count: " + sum);
                    collectedABValues.clear();
                    break;

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
        this.collectedABValues.put(id,snapshotResult);

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
