package app.snapshot_bitcake;

import app.AppConfig;
import servent.message.Message;
import servent.message.snapshot.ABMarkerMessage;
import servent.message.snapshot.AVMarkerMessage;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class CausalBroadcastShared {

    public static Map<Integer,Integer> vectorClock = new ConcurrentHashMap<>();
    public static BlockingQueue<Message> commitedCausalMessages = new LinkedBlockingQueue();
    public static Set<Message> seenMessages = Collections.newSetFromMap(new ConcurrentHashMap<Message, Boolean>());

    public static Queue<Message> pendingMessagesQueue = new ConcurrentLinkedQueue<>();
    public static Object pendingMessagesQueueLock = new Object();
    public static Object causalMessageLock = new Object();


    public static Map<Integer,List<Integer>> SENT = new ConcurrentHashMap<>();
    public static Map<Integer,List<Integer>> RECD = new ConcurrentHashMap<>();

    public static Object sentLock = new Object();
    public static Object recdLock = new Object();


    public static AVMarkerMessage avMarkerMessage;


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
        AppConfig.isAVMarkerSent.set(true);
        checkPandingMessages();
    }

    public static boolean otherClockGreates(Map<Integer,Integer> clock1,Map<Integer,Integer> clock2){
        if(clock1.size()!=clock2.size())
            throw new IllegalArgumentException("clocks are not same size");
        for(int i=0;i<clock1.size();i++){
            if(clock2.get(i)>clock1.get(i))
                return true;
        }
        return false;
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
                    System.out.println(pendingMsg.getMessageType());

                    System.out.println("vrtim se unutra ");
                    boolean clockGreater = otherClockGreates(myVectorClock,pendingMsg.getVectorClock());
                    System.out.println(myVectorClock);
                    System.out.println(pendingMsg.getVectorClock());
                    if(!clockGreater){
                        gotWork=true;

                        AppConfig.timestampedStandardPrint("Commiting "+pendingMsg);
                        commitedCausalMessages.add(pendingMsg);
                        incrementClock(pendingMsg.getOriginalSenderInfo().getId());
                        iterator.remove();
                        break;
                    }
                }

            }
        }

    }

}
