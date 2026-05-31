package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.CurrencyEntity;
import com.nprotech.moneytracker.repositories.MasterRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MasterViewModel extends ViewModel {

    private final MasterRepository masterRepository;

    @Inject
    public MasterViewModel(MasterRepository masterRepository) {
        this.masterRepository = masterRepository;
    }

    public List<CurrencyEntity> getAllCurrencies() {
        return masterRepository.getAllCurrencies();
    }

    public CurrencyEntity getDefaultCurrency() {
        return masterRepository.getDefaultCurrency();
    }
}