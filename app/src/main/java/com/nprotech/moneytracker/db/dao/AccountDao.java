package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.AccountEntity;

import java.util.List;

@Dao
public interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AccountEntity accountEntity);

    @Query("SELECT * FROM accounts " +
            "WHERE isDeleted = 0 AND isArchived = 0 " +
            "ORDER BY isDefault DESC, ordering ASC")
    List<AccountEntity> getAllAccounts();

    @Query("SELECT * FROM accounts " +
            "WHERE id = :accountId " +
            "AND isDeleted = 0 AND isArchived = 0")
    LiveData<AccountEntity> getAccountById(int accountId);

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    AccountEntity getAccountDetailById(int accountId);

    @Query("SELECT COALESCE(MAX(ordering), 0) FROM accounts")
    int getLastAccountOrder();

    @Query("UPDATE accounts SET balance = :amount WHERE id = :accountId")
    void updateAccountById(int accountId, double amount);

    @Query("UPDATE accounts SET balance = :amount WHERE id = :accountId")
    int updateAccountBalanceById(int accountId, double amount);

    @Update
    void updateAccount(AccountEntity account);

    @Query(" SELECT * FROM accounts WHERE isDeleted = 0 AND isArchived = 0 ORDER BY " +
            "CASE WHEN :sortType = 1 THEN isDefault END DESC, " +
            "CASE WHEN :sortType = 2 THEN ordering END DESC, " +
            "CASE WHEN :sortType = 3 THEN ordering END ASC, " +
            "CASE WHEN :sortType = 4 THEN name END COLLATE NOCASE ASC, " +
            "CASE WHEN :sortType = 5 THEN name END COLLATE NOCASE DESC, " +
            "CASE WHEN :sortType = 6 THEN balance END DESC, " +
            "CASE WHEN :sortType = 7 THEN balance END ASC")
    LiveData<List<AccountEntity>> getFilteredAccounts(int sortType);

    @Query(" SELECT * FROM accounts WHERE isDeleted = 0 AND isArchived = 1 ORDER BY " +
            "CASE WHEN :sortType = 2 THEN archivedAt END DESC, " +
            "CASE WHEN :sortType = 3 THEN archivedAt END ASC, " +
            "CASE WHEN :sortType = 4 THEN name END COLLATE NOCASE ASC, " +
            "CASE WHEN :sortType = 5 THEN name END COLLATE NOCASE DESC, " +
            "CASE WHEN :sortType = 6 THEN balance END DESC, " +
            "CASE WHEN :sortType = 7 THEN balance END ASC")
    LiveData<List<AccountEntity>> getFilteredArchivedAccounts(int sortType);

    @Query("UPDATE accounts SET isDefault = 0")
    void clearDefaultAccount();

    @Query("UPDATE accounts SET isDefault = 1 WHERE id = :accountId")
    void setDefaultAccount(int accountId);

    @Query("UPDATE accounts SET archivedAt = :archivedAt, isArchived = :isArchived WHERE id = :accountId")
    void archiveAccount(int accountId, boolean isArchived, long archivedAt);

    @Query("SELECT COUNT(*) FROM accounts " +
            "WHERE id = :accountId AND isDefault = 0 AND isDeleted = 0")
    int canDeleteAccount(int accountId);

    @Query("UPDATE accounts SET isDeleted = 1 " +
            "WHERE id = :accountId AND isDefault = 0 AND isDeleted = 0")
    int deleteAccount(int accountId);

    @Query("SELECT id FROM accounts " +
            "WHERE isDefault = 1 AND isDeleted = 0 LIMIT 1")
    int getDefaultAccountId();

    @Query("SELECT COUNT(*) FROM accounts " +
            "WHERE id = :accountId " +
            "AND isDeleted = 0 AND isArchived = 0")
    int isAccountAvailable(int accountId);
}