package app.snapshot_bitcake;

import app.AppConfig;
import servent.message.Message;
import servent.message.snapshot.ABMarkerMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;

public class CausalBroadcastShared {

    public static Map<Integer,Integer> vectorClock = new ConcurrentHashMap<>();
    public static List<Message> commitedCausalMessages = new CopyOnWriteArrayList<>();
    public static Queue<Message> pendingMessagesQueue = new ConcurrentLinkedQueue<>();
    public static Object pendingMessagesQueueLock = new Object();
    public static Object causalMessageLock = new Object();


    public static void initVectorClock(int serventCount){
        for(int i=0;i<serventCount;i++){
            vectorClock.put(i,0);
        }
    }

    public static void incrementClock(int serventId){
        vectorClock.computeIfPresent(serventId, (key, oldValue) -> oldValue + 1);
    }
    public static List<Message> getCommitedCausalMessages(){
        return  new CopyOnWriteArrayList<>(commitedCausalMessages);
    }

    public static Map<Integer, Integer> getVectorClock() {
        return vectorClock;
    }

    public static void addPendingMessages(Message message){
        pendingMessagesQueue.add(message);
    }

    public static void commitCausalMessage(Message causalMessage){
        commitedCausalMessages.add(causalMessage);
        incrementClock(causalMessage.getOriginalSenderInfo().getId());
        checkPandingMessages();
    }

    private static boolean otherClockGreates(Map<Integer,Integer> clock1,Map<Integer,Integer> clock2){
        if(clock1.size()!=clock2.size())
            throw new IllegalArgumentException("clocks are not same size");
        for(int i=0;i<clock1.size();i++){
            if(clock2.get(i)>clock1.get(i))
                return true;
        }
        return false;
    }

    public static void sendSnapshotResult(BitcakeManager bitcakeManager,SnapshotCollector snapshotCollector){
        System.out.println("SNAP RESULT ");
        synchronized (causalMessageLock){
            for(Message message:commitedCausalMessages){
                ABBitcakeManager abBitcakeManager = (ABBitcakeManager) bitcakeManager;
                abBitcakeManager.handleMarker(message,snapshotCollector);
            }
        }


    }

    public static void checkPandingMessages(){
        boolean gotWork = true;

        while(gotWork){
            gotWork=false;
            System.out.println("vrtim se u petlji");
            synchronized (pendingMessagesQueueLock){
                Iterator<Message> iterator = pendingMessagesQueue.iterator();
                Map<Integer,Integer> myVectorClock = getVectorClock();
                while(iterator.hasNext()){

                    Message pendingMsg = iterator.next();
                    ABMarkerMessage abMarkerMessage = (ABMarkerMessage)pendingMsg;
                    System.out.println("vrtim se unutra ");
                    if(!otherClockGreates(myVectorClock,abMarkerMessage.getVectorClock())){
                        gotWork=true;

                        AppConfig.timestampedStandardPrint("Commiting "+pendingMsg);
                        commitedCausalMessages.add(pendingMsg);
                        incrementClock(pendingMsg.getOriginalSenderInfo().getId());
                        iterator.remove();
                        break;
                    }
                    System.out.println("izasao sam iz unutrasnje ");
                }

            }
        }

    }

}
