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


        if (MessageUtil.MESSAGE_UTIL_PRINTING) {
            AppConfig.timestampedStandardPrint("Sending message " + messageToSend);
        }

        try {



            Socket sendSocket = new Socket(messageToSend.getReceiverInfo().getIpAddress(), messageToSend.getReceiverInfo().getListenerPort());

            ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
            System.out.println(messageToSend);
            oos.writeObject(messageToSend);
            oos.flush();
            sendSocket.close();

            /*
                SENT logika, za AB algoritam
             */
            if(AppConfig.SNAPSHOT_TYPE == SnapshotType.AB){
                synchronized (CausalBroadcastShared.sentLock){
                    int receiverId = messageToSend.getReceiverInfo().getId();
                    List<Integer> sentMessagesList =  CausalBroadcastShared.SENT.getOrDefault(receiverId,new ArrayList<>());
                    sentMessagesList.add(Integer.parseInt(messageToSend.getMessageText()));
                    CausalBroadcastShared.SENT.put(receiverId,sentMessagesList);
                }
            }
            /*
             sent logika, za AV algoritam
             */
//            if(AppConfig.SNAPSHOT_TYPE==SnapshotType.AV){
//                synchronized (AppConfig.avLock){
//                    if(AppConfig.isAVMarkerSent.get())
//                        messageToSend.setMsgStatus(true);
//                }
//            }


            messageToSend.sendEffect();


//			}
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.toString());
//            AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.getRoute());
            AppConfig.timestampedErrorPrint(e.getMessage());
        }
    }
}
