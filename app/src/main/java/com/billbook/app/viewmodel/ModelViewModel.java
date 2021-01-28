package com.billbook.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.billbook.app.database.models.Model;
import com.billbook.app.repository.AppRepository;

import java.util.List;

public class ModelViewModel extends AndroidViewModel {

    private final LiveData<List<Model>> modelList;

    public ModelViewModel(Application application) {
        super(application);
        modelList = AppRepository.getInstance().getModels();
    }

    public LiveData<List<Model>> getModels() {
        return modelList;
    }



}
