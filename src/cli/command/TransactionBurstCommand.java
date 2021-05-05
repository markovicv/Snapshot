package cli.command;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.CausalBroadcastShared;
import servent.message.Message;
import servent.message.TransactionMessage;
import servent.message.util.MessageUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionBurstCommand implements CLICommand {

	private static final int TRANSACTION_COUNT = 5;
	private static final int BURST_WORKERS = 10;
	private static final int MAX_TRANSFER_AMOUNT = 10;
	
	//Chandy-Lamport
//	private static final int TRANSACTION_COUNT = 3;
//	private static final int BURST_WORKERS = 5;
//	private static final int MAX_TRANSFER_AMOUNT = 10;
	
	private BitcakeManager bitcakeManager;
	
	public TransactionBurstCommand(BitcakeManager bitcakeManager) {
		this.bitcakeManager = bitcakeManager;
	}
	
	private class TransactionBurstWorker implements Runnable {
		
		@Override
		public void run() {
			for (int i = 0; i < TRANSACTION_COUNT; i++) {
				//TODO transkacija resena
				Map<Integer,Integer> vectorClockCopy = new ConcurrentHashMap<>(CausalBroadcastShared.getVectorClock());
				Message transactionMessage = new TransactionMessage(AppConfig.myServentInfo,null,0,bitcakeManager,vectorClockCopy);
				for (int neighbor : AppConfig.myServentInfo.getNeighbors()) {
					int amount = 1 + (int)(Math.random() * MAX_TRANSFER_AMOUNT);

					transactionMessage.setMessageText(String.valueOf(amount));
					transactionMessage = transactionMessage.changeReceiver(neighbor);


					
					MessageUtil.sendMessage(transactionMessage);
				}
				// povecati vector clock posle svake poruke
				transactionMessage = transactionMessage.changeReceiver(AppConfig.myServentInfo.getId());
				CausalBroadcastShared.commitCausalMessage(transactionMessage);
			}
		}
	}
	
	@Override
	public String commandName() {
		return "transaction_burst";
	}

	@Override
	public void execute(String args) {
		for (int i = 0; i < BURST_WORKERS; i++) {
			Thread t = new Thread(new TransactionBurstWorker());
			
			t.start();
		}
	}

	
}
