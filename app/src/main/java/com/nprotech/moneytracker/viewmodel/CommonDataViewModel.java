package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.CommonDataEntity;
import com.nprotech.moneytracker.repositories.CommonDataRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CommonDataViewModel extends ViewModel {

    private final CommonDataRepository commonDataRepository;

    @Inject
    public CommonDataViewModel(CommonDataRepository commonDataRepository) {
        this.commonDataRepository = commonDataRepository;
    }

    public List<CommonDataEntity> getDataByType(int type) {
        return commonDataRepository.getDataByType(type);
    }

    public void updateSelectedData(int type, int id) {
        commonDataRepository.updateSelectedData(type, id);
    }
}