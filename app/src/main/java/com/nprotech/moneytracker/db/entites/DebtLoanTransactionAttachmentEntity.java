package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "debt_loan_transactions_attachment",
        indices = {
                @Index(value = {"tempDebtLoanServerId"}),
                @Index(value = {"attachmentPath", "tempDebtLoanServerId"}),
                @Index(value = {"serverId"})
        }
)
public class DebtLoanTransactionAttachmentEntity implements Serializable {

    // Sync Status
    public static final int SYNC_PENDING = 0;
    public static final int SYNCED = 1;
    public static final int SYNC_FAILED = 2;
    public static final int DELETE_PENDING = 3;

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String tempDebtLoanServerId;
    public int debtLoanId;
    public int debtLoanPaymentId;
    public int debtLoanPaymentTransactionId;
    public int serverId;
    public String attachmentPath;
    public String attachmentName;
    public String attachmentExtension;
    public long attachmentSize;
    public long createdAt;
    public long updatedAt;
    public int syncStatus = SYNC_PENDING;
}