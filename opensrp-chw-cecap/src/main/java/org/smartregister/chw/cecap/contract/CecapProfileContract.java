package org.smartregister.chw.cecap.contract;

import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.cecap.domain.MemberObject;

public interface CecapProfileContract {
    interface View extends InteractorCallBack {

        void setProfileViewWithData();

        void setOverDueColor();

        void openMedicalHistory();

        void recordCecap(MemberObject memberObject);

        void showProgressBar(boolean status);

        void hideView();
    }

    interface Presenter {

        void fillProfileData(@Nullable MemberObject memberObject);

        void saveForm(String jsonString);

        @Nullable
        View getView();

        void refreshProfileBottom();

        void recordCecapButton(String visitState);
    }

    interface Interactor {

        void refreshProfileInfo(MemberObject memberObject, InteractorCallBack callback);

        void saveRegistration(String jsonString, final CecapProfileContract.InteractorCallBack callBack);
    }


    interface InteractorCallBack {
        void refreshMedicalHistory(boolean hasHistory);
    }
}