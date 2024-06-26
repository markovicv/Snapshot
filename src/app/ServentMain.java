package app;

import app.snapshot_bitcake.ABBitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import app.snapshot_bitcake.SnapshotCollectorWorker;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.util.MessageUtil;
import servent.message.util.CausalWorker;

/**
 * Describes the procedure for starting a single Servent
 *
 * @author bmilojkovic
 */
public class ServentMain {

    /**
     * Command line arguments are:
     * 0 - path to servent list file
     * 1 - this servent's id
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            AppConfig.timestampedErrorPrint("Please provide servent list file and id of this servent.");
        }

        int serventId = -1;
        int portNumber = -1;

        String serventListFile = args[0];

        AppConfig.readConfig(serventListFile);

        try {
            serventId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            AppConfig.timestampedErrorPrint("Second argument should be an int. Exiting...");
            System.exit(0);
        }

        if (serventId >= AppConfig.getServentCount()) {
            AppConfig.timestampedErrorPrint("Invalid servent id provided");
            System.exit(0);
        }

        AppConfig.myServentInfo = AppConfig.getInfoById(serventId);

        try {
            portNumber = AppConfig.myServentInfo.getListenerPort();

            if (portNumber < 1000 || portNumber > 2000) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            AppConfig.timestampedErrorPrint("Port number should be in range 1000-2000. Exiting...");
            System.exit(0);
        }

        MessageUtil.initializePendingMessages();

        AppConfig.timestampedStandardPrint("Starting servent " + AppConfig.myServentInfo);

        SnapshotCollector snapshotCollector;

        snapshotCollector = new SnapshotCollectorWorker(AppConfig.SNAPSHOT_TYPE,new ABBitcakeManager());

        Thread snapshotCollectorThread = new Thread(snapshotCollector);
        snapshotCollectorThread.start();

        SimpleServentListener simpleListener = new SimpleServentListener(snapshotCollector);
        Thread listenerThread = new Thread(simpleListener);
        listenerThread.start();

        CausalWorker sendSnapshots = new CausalWorker(snapshotCollector);
        Thread sendSnapshotThread = new Thread(sendSnapshots);
        sendSnapshotThread.start();

        CLIParser cliParser = new CLIParser(simpleListener, snapshotCollector,sendSnapshots);
        Thread cliThread = new Thread(cliParser);
        cliThread.start();

    }
}
