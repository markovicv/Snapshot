package servent.message;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.ABSnapshotResult;
import app.snapshot_bitcake.BitcakeManager;

/**
 * A default message implementation. This should cover most situations.
 * If you want to add stuff, remember to think about the modificator methods.
 * If you don't override the modificators, you might drop stuff.
 * @author bmilojkovic
 *
 */
public class BasicMessage implements Message {

	private static final long serialVersionUID = -9075856313609777945L;
	private final MessageType type;
	private final ServentInfo originalSenderInfo;
	private final ServentInfo receiverInfo;
	private final List<ServentInfo> routeList;
	private  String messageText;
	private final boolean white;
	
	//This gives us a unique id - incremented in every natural constructor.
	private static AtomicInteger messageCounter = new AtomicInteger(0);
	private final int messageId;

	protected Map<Integer,Integer> senderVectorClock;

	protected ABSnapshotResult abSnapshotResult;
	protected int collectorId;
	protected transient BitcakeManager bitcakeManager;

	/*
	 av
	 */
	protected boolean isNew;


	public BasicMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo) {
		this.type = type;
		this.originalSenderInfo = originalSenderInfo;
		this.receiverInfo = receiverInfo;
		this.white = AppConfig.isWhite.get();
		this.routeList = new ArrayList<>();
		this.messageText = "";
		senderVectorClock = new HashMap<>();
		
		this.messageId = messageCounter.getAndIncrement();
	}
	
	public BasicMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
			String messageText) {
		this.type = type;
		this.originalSenderInfo = originalSenderInfo;
		this.receiverInfo = receiverInfo;
		this.white = AppConfig.isWhite.get();
		this.routeList = new ArrayList<>();
		this.messageText = messageText;
		senderVectorClock = new HashMap<>();

		this.messageId = messageCounter.getAndIncrement();
	}
	
	@Override
	public MessageType getMessageType() {
		return type;
	}

	@Override
	public ServentInfo getOriginalSenderInfo() {
		return originalSenderInfo;
	}

	@Override
	public ServentInfo getReceiverInfo() {
		return receiverInfo;
	}
	
	@Override
	public boolean isWhite() {
		return white;
	}
	
	@Override
	public List<ServentInfo> getRoute() {
		return routeList;
	}
	
	@Override
	public String getMessageText() {
		return messageText;
	}

	@Override
	public void setMessageText(String message) {
		this.messageText = message;
	}

	@Override
	public int getMessageId() {
		return messageId;
	}

	@Override
	public Map<Integer, Integer> getVectorClock() {
		return senderVectorClock;
	}

	@Override
	public void setVectorClock(Map<Integer, Integer> clock) {
		this.senderVectorClock = clock;
	}

	@Override
	public void setAbSnapshot(ABSnapshotResult abSnapshotResult) {
		this.abSnapshotResult = abSnapshotResult;
	}

	@Override
	public int getCollectorId() {
		return collectorId;
	}

	@Override
	public ABSnapshotResult getABSnapshotResult() {
		return abSnapshotResult;
	}

	@Override
	public void setCollectorId(int id) {
		this.collectorId = id;
	}

	@Override
	public BitcakeManager getBitcakeManager() {
		return bitcakeManager;
	}

	@Override
	public void setBitcakeManager(BitcakeManager bitcakeManager) {
		this.bitcakeManager = bitcakeManager;
	}

	@Override
	public boolean isMsgNew() {
		return isNew;
	}

	@Override
	public void setMsgStatus(boolean status) {
		this.isNew = status;
	}

	protected BasicMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
						   boolean white, List<ServentInfo> routeList, String messageText, int messageId) {
		this.type = type;
		this.originalSenderInfo = originalSenderInfo;
		this.receiverInfo = receiverInfo;
		this.white = white;
		this.routeList = routeList;
		this.messageText = messageText;
		
		this.messageId = messageId;
	}

	/**
	 * Used when resending a message. It will not change the original owner
	 * (so equality is not affected), but will add us to the route list, so
	 * message path can be retraced later.
	 */
	@Override
	public Message makeMeASender() {
		ServentInfo newRouteItem = AppConfig.myServentInfo;
		
		List<ServentInfo> newRouteList = new ArrayList<>(routeList);
		newRouteList.add(newRouteItem);
		Message toReturn = new BasicMessage(getMessageType(), getOriginalSenderInfo(),
				getReceiverInfo(), isWhite(), newRouteList, getMessageText(), getMessageId());

		if(getMessageType() == MessageType.AB_MARKER)
			toReturn.setVectorClock(getVectorClock());



		if(getMessageType()==MessageType.AB_TELL){
			toReturn.setAbSnapshot(getABSnapshotResult());
			toReturn.setCollectorId(getCollectorId());
		}
		if(getMessageType()==MessageType.TRANSACTION){
			toReturn.setMessageText(getMessageText());
			toReturn.setVectorClock(getVectorClock());
			toReturn.setBitcakeManager(getBitcakeManager());
		}
		if(getMessageType()==MessageType.AV_MARKER){
			toReturn.setVectorClock(getVectorClock());

		}
		if(getMessageType() == MessageType.DONE){
			toReturn.setCollectorId(getCollectorId());
		}

		
		return toReturn;
	}
	
	/**
	 * Change the message received based on ID. The receiver has to be our neighbor.
	 * Use this when you want to send a message to multiple neighbors, or when resending.
	 */
	@Override
	public Message changeReceiver(Integer newReceiverId) {
		if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId) || AppConfig.myServentInfo.getId() == newReceiverId) {
			ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);
			
			Message toReturn = new BasicMessage(getMessageType(), getOriginalSenderInfo(),
					newReceiverInfo, isWhite(), getRoute(), getMessageText(), getMessageId());


			if(getMessageType() == MessageType.AB_MARKER) {
				toReturn.setVectorClock(getVectorClock());
			}
			if(getMessageType() == MessageType.AB_TELL){
				toReturn.setAbSnapshot(getABSnapshotResult());
				toReturn.setCollectorId(getCollectorId());
			}
			if(getMessageType()==MessageType.TRANSACTION) {
				toReturn.setMessageText(getMessageText());
				toReturn.setVectorClock(getVectorClock());
				toReturn.setBitcakeManager(getBitcakeManager());

			}
			if(getMessageType()==MessageType.AV_MARKER){
				toReturn.setVectorClock(getVectorClock());
			}
			if(getMessageType() == MessageType.DONE){
				toReturn.setCollectorId(getCollectorId());
			}


			
			return toReturn;
		} else {
			AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");
			
			return null;
		}
		
	}
	
	@Override
	public Message setRedColor() {
		Message toReturn = new BasicMessage(getMessageType(), getOriginalSenderInfo(),
				getReceiverInfo(), false, getRoute(), getMessageText(), getMessageId());
		
		return toReturn;
	}
	
	@Override
	public Message setWhiteColor() {
		Message toReturn = new BasicMessage(getMessageType(), getOriginalSenderInfo(),
				getReceiverInfo(), true, getRoute(), getMessageText(), getMessageId());
		
		return toReturn;
	}
	
	/**
	 * Comparing messages is based on their unique id and the original sender id.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicMessage) {
			BasicMessage other = (BasicMessage)obj;
			
			if (getMessageId() == other.getMessageId() &&
				getOriginalSenderInfo().getId() == other.getOriginalSenderInfo().getId()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Hash needs to mirror equals, especially if we are gonna keep this object
	 * in a set or a map. So, this is based on message id and original sender id also.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getMessageId(), getOriginalSenderInfo().getId());
	}
	
	/**
	 * Returns the message in the format: <code>[sender_id|message_id|text|type|receiver_id]</code>
	 */
	@Override
	public String toString() {
		return "[" + getOriginalSenderInfo().getId() + "|" + getMessageId() + "|" +
					getMessageText() + "|" + getMessageType() + "|" +
					getReceiverInfo().getId() + "]";
	}

	/**
	 * Empty implementation, which will be suitable for most messages.
	 */
	@Override
	public void sendEffect() {
		
	}
}
