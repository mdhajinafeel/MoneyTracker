package com.nprotech.moneytracker.repositories;

import com.nprotech.moneytracker.db.dao.CommonDataDao;
import com.nprotech.moneytracker.db.entites.CommonDataEntity;

import java.util.List;

public class CommonDataRepository {

    private final CommonDataDao commonDataDao;

    public CommonDataRepository(CommonDataDao commonDataDao) {
        this.commonDataDao = commonDataDao;
    }

    public List<CommonDataEntity> getDataByType(int type) {
        return commonDataDao.getDataByType(type);
    }

    public void updateSelectedData(int type, int id) {
        commonDataDao.updateDeSelectedData(type);
        commonDataDao.updateSelectedData(id);
    }
}