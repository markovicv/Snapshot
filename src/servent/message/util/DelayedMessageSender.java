package servent.message.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotType;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.ABMarkerMessage;

/**
 * This worker sends a message asynchronously. Doing this in a separate thread
 * has the added benefit of being able to delay without blocking main or somesuch.
 *
 * @author bmilojkovic
 */
public class DelayedMessageSender implements Runnable {

    private Message messageToSend;
    private static int counter = 0;

    public DelayedMessageSender(Message messageToSend) {
        this.messageToSend = messageToSend;
    }

    @Override
    public void run() {

        try {
            Thread.sleep((long) (Math.random() * 1000) + 500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        ServentInfo receiverInfo = messageToSend.getReceiverInfo();
        if (MessageUtil.MESSAGE_UTIL_PRINTING) {
            AppConfig.timestampedStandardPrint("Sending message " + messageToSend);
        }



        try {

            Socket sendSocket = new Socket(receiverInfo.getIpAddress(), receiverInfo.getListenerPort());
            ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
            oos.writeObject(messageToSend);
            oos.flush();
            sendSocket.close();



            /*
                SENT logika, za AB algoritam
             */
            if (AppConfig.SNAPSHOT_TYPE == SnapshotType.AB) {
                synchronized (CausalBroadcastShared.sentLock) {
                    if (messageToSend.getMessageType() == MessageType.TRANSACTION) {
                        int receiverId = messageToSend.getReceiverInfo().getId();
                        List<Integer> sentMessagesList = CausalBroadcastShared.SENT.getOrDefault(receiverId, new ArrayList<>());
                        sentMessagesList.add(Integer.parseInt(messageToSend.getMessageText()));
                        CausalBroadcastShared.SENT.put(receiverId, sentMessagesList);
                    }

                }
            }



            messageToSend.sendEffect();


//			}
        } catch (IOException e) {
            System.err.println("IP: " + messageToSend.getReceiverInfo().getIpAddress() + " : " + messageToSend.getReceiverInfo().getListenerPort());
            AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.toString());
            AppConfig.timestampedErrorPrint("counter: "+counter+" : "+e.getMessage());
//            try {
//                Thread.sleep((long) (Math.random() * 1000) + 500);
//            } catch (InterruptedException e1) {
//                e1.printStackTrace();
//            }
//            Thread t = new Thread(new DelayedMessageSender(messageToSend));
//            t.start();

        }
        counter++;

    }
}
