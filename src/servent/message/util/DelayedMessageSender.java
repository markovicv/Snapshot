package servent.message.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.CausalBroadcastShared;
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
        System.out.println("RUN METHOD: " + messageToSend.getVectorClock());
        /*
         * A random sleep before sending.
         * It is important to take regular naps for health reasons.
         */
        try {
            Thread.sleep((long) (Math.random() * 1000) + 500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

//        ServentInfo receiverInfo = messageToSend.getReceiverInfo();
        System.out.println("RUN METHOD2: " + messageToSend.getVectorClock());
        if (MessageUtil.MESSAGE_UTIL_PRINTING) {
            AppConfig.timestampedStandardPrint("Sending message " + messageToSend);
        }

        try {

            synchronized (AppConfig.msgLock){
                if(messageToSend.getMessageType() == MessageType.AB_MARKER){
                    int sent = AppConfig.sendMessagesToNeighbors.incrementAndGet();
                    if(sent>=AppConfig.myServentInfo.getNeighbors().size()){
                        AppConfig.sendMessagesToNeighbors.set(0);
                        CausalBroadcastShared.commitCausalMessage(new ABMarkerMessage(AppConfig.myServentInfo,AppConfig.myServentInfo,AppConfig.myServentInfo.getId(),CausalBroadcastShared.getVectorClock()));

                    }
                }
            }
//            synchronized (AppConfig.msgLock){
//                if(messageToSend.getMessageType() == MessageType.AB_MARKER){
//                    if(AppConfig.sendMessagesToNeighbors.get() < 6){
//                        AppConfig.sendMessagesToNeighbors.getAndIncrement();
//                    }
//                    else{
//                        CausalBroadcastShared.commitCausalMessage(new ABMarkerMessage(AppConfig.myServentInfo,AppConfig.myServentInfo,AppConfig.myServentInfo.getId(),CausalBroadcastShared.getVectorClock()));
//                        AppConfig.sendMessagesToNeighbors.set(0);
//                    }
//                }
//            }

            Socket sendSocket = new Socket(messageToSend.getReceiverInfo().getIpAddress(), messageToSend.getReceiverInfo().getListenerPort());

            ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
            System.out.println(messageToSend);
            oos.writeObject(messageToSend);
            oos.flush();
            sendSocket.close();

            messageToSend.sendEffect();


//			}
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.toString());
        }
    }
}
