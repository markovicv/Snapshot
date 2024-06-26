package servent.handler;

import app.AppConfig;
import app.snapshot_bitcake.AVBitcakeManager;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotType;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionHandler implements MessageHandler {

    private Message clientMessage;
    private BitcakeManager bitcakeManager;

    //TODO ako je meni namenjena obradim i posaljem drugima, inace samo broadcastujem

    public TransactionHandler(Message clientMessage, BitcakeManager bitcakeManager) {
        this.clientMessage = clientMessage;
        this.bitcakeManager = bitcakeManager;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TRANSACTION) {
            if (clientMessage.getOriginalSenderInfo().getId() != AppConfig.myServentInfo.getId()) {
                boolean didPut = CausalBroadcastShared.seenMessages.add(clientMessage);
                if (didPut) {
                    clientMessage.setBitcakeManager(this.bitcakeManager);
                    CausalBroadcastShared.addPendingMessages(clientMessage);
                    CausalBroadcastShared.checkPandingMessages();


                    AppConfig.timestampedStandardPrint("Rebroadcasting");
                    for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                        MessageUtil.sendMessage(clientMessage.changeReceiver(neighbor).makeMeASender());


                    }
                } else {
                    AppConfig.timestampedStandardPrint("Seen this transaction");
                }
            }

        } else {
            AppConfig.timestampedErrorPrint("Transaction handler got: " + clientMessage);
        }
    }

    public static void handleTransaction(Message clientMessage) {
        String amountString = clientMessage.getMessageText();
        BitcakeManager bitcakeManager = clientMessage.getBitcakeManager();

        int amountNumber = 0;
        try {
            amountNumber = Integer.parseInt(amountString);
        } catch (NumberFormatException e) {
            AppConfig.timestampedErrorPrint("Couldn't parse amount: " + amountString);
            return;
        }

        bitcakeManager.addSomeBitcakes(amountNumber);


        if (AppConfig.SNAPSHOT_TYPE == SnapshotType.AB) {
            synchronized (CausalBroadcastShared.recdLock) {
                int senderId = clientMessage.getOriginalSenderInfo().getId();
                List<Integer> receivedListMessages = CausalBroadcastShared.RECD.getOrDefault(senderId, new ArrayList<>());
                receivedListMessages.add(Integer.parseInt(clientMessage.getMessageText()));
                CausalBroadcastShared.RECD.put(senderId, receivedListMessages);
            }
        }


		/*
		 provera da li je poruka stara ili nova
		 */
        if (AppConfig.SNAPSHOT_TYPE == SnapshotType.AV) {
            synchronized (AppConfig.avLock) {
                if (CausalBroadcastShared.avMarkerMessage != null &&
                        !CausalBroadcastShared.otherClockGreates(clientMessage.getVectorClock(), CausalBroadcastShared.avMarkerMessage.getVectorClock())) {
                    String chanelKey = String.valueOf(AppConfig.myServentInfo.getId()) + "-" + clientMessage.getOriginalSenderInfo().getId();
                    List<Integer> channelMsg = ((AVBitcakeManager) bitcakeManager).channels.getOrDefault(chanelKey, new ArrayList<>());
                    channelMsg.add(Integer.parseInt(clientMessage.getMessageText()));
                    ((AVBitcakeManager) bitcakeManager).channels.put(chanelKey, channelMsg);
                }

            }

        }

    }

}
