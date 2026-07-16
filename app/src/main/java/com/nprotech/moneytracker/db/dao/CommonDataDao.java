package com.nprotech.moneytracker.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.moneytracker.db.entites.CommonDataEntity;

import java.util.List;

@Dao
public interface CommonDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CommonDataEntity> list);

    @Query("SELECT COUNT(*) FROM common_data")
    int getCommonDataCount();

    @Query("SELECT * FROM common_data WHERE type = :type AND active = 1")
    List<CommonDataEntity> getDataByType(int type);

    @Query("UPDATE common_data SET selected = 0 WHERE type = :type")
    void updateDeSelectedData(int type);
    @Query("UPDATE common_data SET selected = 1 WHERE id = :id")
    void updateSelectedData(int id);
}