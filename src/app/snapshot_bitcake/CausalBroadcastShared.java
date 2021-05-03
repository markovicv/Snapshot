package app.snapshot_bitcake;

import servent.message.Message;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class CausalBroadcastShared {

    public static Map<Integer,Integer> vectorClock = new ConcurrentHashMap<>();
    private static List<Message> commitedCausalMessages = new CopyOnWriteArrayList<>();
    private static Queue<Message> pendingMessagesQueue = new ConcurrentLinkedQueue<>();
    private static Object pendingMessagesQueueLock = new Object();


}
