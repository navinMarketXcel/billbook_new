package com.billbook.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.billbook.app.database.models.Expense;
import com.billbook.app.repository.AppRepository;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final LiveData<List<Expense>> modelList;

    public ExpenseViewModel(Application application) {
        super(application);
        modelList = AppRepository.getInstance().getExpenses();
    }

    public LiveData<List<Expense>> getModels() {
        return modelList;
    }



}
