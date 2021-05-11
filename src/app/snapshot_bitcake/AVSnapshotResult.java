package app.snapshot_bitcake;

import java.io.Serializable;

public class AVSnapshotResult implements Serializable {

    private final int serventId;
    private final int recordedAmount;

    public AVSnapshotResult(int serventId, int recordedAmount) {
        this.serventId = serventId;
        this.recordedAmount = recordedAmount;
    }

    public int getServentId() {
        return serventId;
    }

    public int getRecordedAmount() {
        return recordedAmount;
    }
}
