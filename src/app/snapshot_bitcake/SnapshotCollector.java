package app.snapshot_bitcake;

import app.Cancellable;
import servent.message.snapshot.DoneMessage;

/**
 * Describes a snapshot collector. Made not-so-flexibly for readability.
 * 
 * @author bmilojkovic
 *
 */
public interface SnapshotCollector extends Runnable, Cancellable {

	BitcakeManager getBitcakeManager();

	void addNaiveSnapshotInfo(String snapshotSubject, int amount);
	void addAcharyaBadrinathInfo(int id,ABSnapshotResult snapshotResult);
	void addAVInfo(int id, DoneMessage doneMessage);

	void startCollecting();

}