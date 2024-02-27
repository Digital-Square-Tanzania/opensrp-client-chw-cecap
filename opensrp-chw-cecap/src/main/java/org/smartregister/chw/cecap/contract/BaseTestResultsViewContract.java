package org.smartregister.chw.cecap.contract;

import com.vijay.jsonwizard.domain.Form;

public interface BaseTestResultsViewContract {
    interface View {
        TestResultsFragmentContract.Presenter presenter();

        Form getFormConfig();
    }

}
