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
                kada smo poslali poruku komsiji, poveacamo SENTi[j] za vrednost koju saljemo !
             */
            synchronized (CausalBroadcastShared.sentLock){
                CausalBroadcastShared.SENT[messageToSend.getReceiverInfo().getId()] += Integer.parseInt(messageToSend.getMessageText());
            }

            messageToSend.sendEffect();


//			}
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.toString());
//            AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.getRoute());
            AppConfig.timestampedErrorPrint(e.getMessage());
        }
    }
}
