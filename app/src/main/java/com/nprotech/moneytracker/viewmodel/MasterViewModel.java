package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
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

    public CurrencyEntity getFirstCurrencyForWallet(int accountId) {
        return masterRepository.getFirstCurrencyForWallet(accountId);
    }

    public CurrencyEntity getFirstCurrencyForAccount(int accountId) {
        return masterRepository.getFirstCurrencyForAccount(accountId);
    }

    public List<CurrencyEntity> getCurrenciesForWallet(int accountId) {
        return masterRepository.getCurrenciesForWallet(accountId);
    }

    public CurrencyEntity getCurrencyByCode(String code) {
        return masterRepository.getCurrencyByCode(code);
    }
}