package cli.command;

import app.AppConfig;
import app.snapshot_bitcake.SnapshotCollector;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.util.CausalWorker;

public class StopCommand implements CLICommand {

	private CLIParser parser;
	private SimpleServentListener listener;
	private SnapshotCollector snapshotCollector;
	private CausalWorker sendSnapshots;
	
	public StopCommand(CLIParser parser, SimpleServentListener listener,
					   SnapshotCollector snapshotCollector, CausalWorker sendSnapshots) {
		this.parser = parser;
		this.listener = listener;
		this.snapshotCollector = snapshotCollector;
		this.sendSnapshots = sendSnapshots;
	}
	
	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Stopping...");
		parser.stop();
		listener.stop();
		snapshotCollector.stop();
		sendSnapshots.stop();
	}

}
