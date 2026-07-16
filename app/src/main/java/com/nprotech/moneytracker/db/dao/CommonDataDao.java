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
}