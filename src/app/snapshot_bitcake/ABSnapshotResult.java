package app.snapshot_bitcake;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ABSnapshotResult implements Serializable {

    private final int serventId;
    private final int recordedAmount;
    private Map<Integer,List<Integer>> SEND;
    private Map<Integer, List<Integer>> RECD;

    public ABSnapshotResult(int serventId, int recordedAmount, Map<Integer, List<Integer>> SEND, Map<Integer, List<Integer>> RECD) {
        this.serventId = serventId;
        this.recordedAmount = recordedAmount;
        this.SEND = SEND;
        this.RECD = RECD;
    }



    public int getServentId() {
        return serventId;
    }

    public int getRecordedAmount() {
        return recordedAmount;
    }

    public Map<Integer, List<Integer>> getSEND() {
        return SEND;
    }

    public Map<Integer, List<Integer>> getRECD() {
        return RECD;
    }
}
