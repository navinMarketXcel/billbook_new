package com.billbook.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class ExtraInputViewModel extends AndroidViewModel {
    private LiveData<List<String>> brandList;

    public ExtraInputViewModel(Application application) {
        super(application);

    }

    public LiveData<List<String>> getInputList() {
        return brandList;
    }

}
