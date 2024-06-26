package org.smartregister.chw.cecap.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.cecap.CecapLibrary;
import org.smartregister.chw.cecap.contract.CecapProfileContract;
import org.smartregister.chw.cecap.domain.MemberObject;
import org.smartregister.chw.cecap.domain.Visit;
import org.smartregister.chw.cecap.util.AppExecutors;
import org.smartregister.chw.cecap.util.Constants;
import org.smartregister.chw.cecap.util.CecapUtil;

public class BaseCecapProfileInteractor implements CecapProfileContract.Interactor {
    protected AppExecutors appExecutors;

    @VisibleForTesting
    BaseCecapProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public BaseCecapProfileInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void refreshProfileInfo(MemberObject memberObject, CecapProfileContract.InteractorCallBack callback) {
        Runnable runnable = () -> appExecutors.mainThread().execute(() -> {
            callback.refreshMedicalHistory(getVisit(Constants.EVENT_TYPE.CECAP_FOLLOW_UP_VISIT, memberObject) != null);
        });
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveRegistration(final String jsonString, final CecapProfileContract.InteractorCallBack callback) {

        Runnable runnable = () -> {
            try {
                CecapUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

        };
        appExecutors.diskIO().execute(runnable);
    }

    public static Visit getVisit(String eventType, MemberObject memberObject) {
        try {
            return CecapLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), eventType);
        } catch (Exception e) {
            return null;
        }
    }
}
