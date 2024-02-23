package org.smartregister.chw.cecap.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.cecap.contract.CecapRegisterContract;
import org.smartregister.chw.cecap.util.CecapUtil;
import org.smartregister.chw.cecap.util.AppExecutors;

public class BaseCecapRegisterInteractor implements CecapRegisterContract.Interactor {

    private AppExecutors appExecutors;

    @VisibleForTesting
    BaseCecapRegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public BaseCecapRegisterInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void saveRegistration(final String jsonString, final CecapRegisterContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            try {
                CecapUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            appExecutors.mainThread().execute(() -> callBack.onRegistrationSaved());
        };
        appExecutors.diskIO().execute(runnable);
    }
}
