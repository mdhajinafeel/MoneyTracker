package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "transactions_attachment",
        indices = {
                @Index("serverId"),
                @Index("tempTransactionServerId"),
                @Index("attachmentPath")
        }
)
public class TransactionAttachmentEntity implements Serializable {

    // Sync Status
    public static final int SYNC_PENDING = 0;
    public static final int SYNCED = 1;
    public static final int SYNC_FAILED = 2;
    public static final int DELETE_PENDING = 3;

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String tempTransactionServerId;
    public long serverId;
    public String attachmentPath;
    public String attachmentName;
    public String attachmentExtension;
    public long attachmentSize;
    public long createdAt;
    public long updatedAt;
    public int syncStatus = SYNC_PENDING;
}