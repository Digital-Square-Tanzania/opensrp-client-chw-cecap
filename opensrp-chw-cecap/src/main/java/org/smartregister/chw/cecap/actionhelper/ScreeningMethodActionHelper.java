package org.smartregister.chw.cecap.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.cecap.domain.MemberObject;
import org.smartregister.chw.cecap.model.BaseCecapVisitAction;
import org.smartregister.chw.cecap.util.JsonFormUtils;

import timber.log.Timber;

/**
 * CECAP Services Survey Action Helper
 */
public abstract class ScreeningMethodActionHelper extends CecapVisitActionHelper {
    protected Context context;

    protected MemberObject memberObject;

    protected String canTheFacilityPerformHpvDnaTest;

    protected String screeningTestPerformed;

    public ScreeningMethodActionHelper(Context context, MemberObject memberObject) {
        this.context = context;
        this.memberObject = memberObject;
    }

    /**
     * set preprocessed status to be inert
     *
     * @return null
     */
    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            canTheFacilityPerformHpvDnaTest = JsonFormUtils.getValue(jsonObject, "can_the_facility_perform_hpv_dna_test");
            screeningTestPerformed = JsonFormUtils.getValue(jsonObject, "screening_test_performed");
        } catch (Exception e) {
            Timber.e(e);
        }

        processScreeningMethod(screeningTestPerformed);
    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseCecapVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isNotBlank(canTheFacilityPerformHpvDnaTest)) {
            return BaseCecapVisitAction.Status.COMPLETED;
        } else {
            return BaseCecapVisitAction.Status.PENDING;
        }
    }


    public abstract void processScreeningMethod(String screeningMethods);
}