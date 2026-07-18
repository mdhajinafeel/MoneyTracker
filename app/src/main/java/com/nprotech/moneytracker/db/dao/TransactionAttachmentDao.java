package com.nprotech.moneytracker.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;

import java.util.List;

@Dao
public interface TransactionAttachmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<TransactionAttachmentEntity> transactionAttachmentEntities);

    @Query("SELECT * FROM transactions_attachment WHERE tempTransactionServerId = :tempTransactionServerId")
    List<TransactionAttachmentEntity> getAttachments(String tempTransactionServerId);

    @Query("DELETE FROM transactions_attachment WHERE attachmentPath = :attachmentPath AND tempTransactionServerId = :tempTransactionServerId")
    void deleteAttachment(String attachmentPath, String tempTransactionServerId);
}