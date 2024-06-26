package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.AppConfig;
import app.Cancellable;
import app.snapshot_bitcake.SnapshotCollector;
import app.snapshot_bitcake.SnapshotType;
import servent.handler.MessageHandler;
import servent.handler.NullHandler;
import servent.handler.PingHandler;
import servent.handler.PongHandler;
import servent.handler.TransactionHandler;

import servent.handler.snapshot.ABMarkerMessageHandler;
import servent.handler.snapshot.ABTellMessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class SimpleServentListener implements Runnable, Cancellable {

    private volatile boolean working = true;

    private SnapshotCollector snapshotCollector;

    public SimpleServentListener(SnapshotCollector snapshotCollector) {
        this.snapshotCollector = snapshotCollector;
    }

    /*
     * Thread pool for executing the handlers. Each client will get it's own handler thread.
     */
    private final ExecutorService threadPool = Executors.newWorkStealingPool();


    @Override
    public void run() {
        ServerSocket listenerSocket = null;
        try {
            listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
            /*
             * If there is no connection after 1s, wake up and see if we should terminate.
             */
            listenerSocket.setSoTimeout(1000);
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
            System.exit(0);
        }


        while (working) {
            try {
                Message clientMessage;

                /*
                 * Lai-Yang stuff. Process any red messages we got before we got the marker.
                 * The marker contains the collector id, so we need to process that as our first
                 * red message.
                 */

                /*
                 * This blocks for up to 1s, after which SocketTimeoutException is thrown.
                 */
                Socket clientSocket = listenerSocket.accept();

                //GOT A MESSAGE! <3
                clientMessage = MessageUtil.readMessage(clientSocket);


                MessageHandler messageHandler = new NullHandler(clientMessage);

                if(clientMessage.getMessageType()==MessageType.AB_MARKER){
                    AppConfig.timestampedStandardPrint("DOSO marker");
                }
                /*
                 * Each message type has it's own handler.
                 * If we can get away with stateless handlers, we will,
                 * because that way is much simpler and less error prone.
                 */
                switch (clientMessage.getMessageType()) {
                    case PING:
                        messageHandler = new PingHandler(clientMessage);
                        break;
                    case PONG:
                        messageHandler = new PongHandler(clientMessage);
                        break;
                    case TRANSACTION:
                        messageHandler = new TransactionHandler(clientMessage, snapshotCollector.getBitcakeManager());
                        break;
                    case AB_MARKER:
                        messageHandler = new ABMarkerMessageHandler(clientMessage,snapshotCollector);
                        break;

                    case AB_TELL:
                        messageHandler = new ABTellMessageHandler(clientMessage,snapshotCollector);
                        break;
                    case POISON:
                        break;
                }

                threadPool.submit(messageHandler);
            } catch (SocketTimeoutException timeoutEx) {
                //Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        this.working = false;
    }

}
