package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.models.DebtLoanWithDetails;

import java.util.List;

@Dao
public interface DebtLoanDao {

    @Insert
    long insert(DebtLoanEntity entity);

    @Update
    int update(DebtLoanEntity entity);

    @Query("""
    SELECT *,
        CASE
            -- Fully paid
            WHEN remainingAmount <= 0 THEN 3

            -- Due date has passed
            WHEN dueDate IS NOT NULL
                 AND date(dueDate / 1000, 'unixepoch', 'localtime')
                     < date(:currentTime / 1000, 'unixepoch', 'localtime')
            THEN 2

            -- Ongoing / due today / future due date
            ELSE 1
        END AS statusType

    FROM debt_loans
    WHERE id = :id
    LIMIT 1
    """)
    LiveData<DebtLoanWithDetails> getDebtLoanById(int id, long currentTime);

    @Query("""
SELECT *,
    CASE
        -- Fully paid
        WHEN remainingAmount <= 0 THEN 3

        -- Due date has passed
        WHEN dueDate IS NOT NULL
             AND date(dueDate / 1000, 'unixepoch', 'localtime')
                 < date(:currentTime / 1000, 'unixepoch', 'localtime')
        THEN 2

        -- Due today, future due date, or no due date
        ELSE 1
    END AS statusType

FROM debt_loans

WHERE type = :type
  AND isDeleted = 0

  AND (
        -- Ongoing
        (
            :statusType = 1
            AND remainingAmount > 0
            AND (
                dueDate IS NULL
                OR date(dueDate / 1000, 'unixepoch', 'localtime')
                   >= date(:currentTime / 1000, 'unixepoch', 'localtime')
            )
        )

        OR

        -- Overdue
        (
            :statusType = 2
            AND remainingAmount > 0
            AND dueDate IS NOT NULL
            AND date(dueDate / 1000, 'unixepoch', 'localtime')
                < date(:currentTime / 1000, 'unixepoch', 'localtime')
        )

        OR

        -- Completed
        (
            :statusType = 3
            AND remainingAmount <= 0
        )
  )

ORDER BY
    CASE WHEN :sortType = 1 THEN createdAt END DESC,
    CASE WHEN :sortType = 2 THEN createdAt END ASC,
    CASE WHEN :sortType = 3 THEN totalAmount END DESC,
    CASE WHEN :sortType = 4 THEN totalAmount END ASC

LIMIT :limit OFFSET :offset
""")
    List<DebtLoanWithDetails> getAllDebtLoans(int type, int sortType, int statusType, long currentTime, int limit, int offset);

    @Query("UPDATE debt_loans SET updatedAt = :updatedAt, isDeleted = 1 WHERE id = :debtLoanId AND isDeleted = 0")
    int delete(int debtLoanId, long updatedAt);

    @Query("""
        SELECT * FROM debt_loans
        WHERE reminderEnabled = 1
        AND isDeleted = 0
        """)
    List<DebtLoanEntity> getLoansWithReminders();

    @Query("SELECT * FROM debt_loans WHERE id = :id LIMIT 1")
    DebtLoanEntity getDebtLoanByIdSync(int id);

    @Query("""
        SELECT COUNT(*)
        FROM debt_loans
        WHERE accountId = :accountId
          AND isDeleted = 0
        """)
    LiveData<Integer> getActiveDebtCount(int accountId);
}