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
public class IndividualCounselingForProstateCancerActionHelper extends CecapVisitActionHelper {
    protected Context context;
    protected MemberObject memberObject;
    protected String familyHistoryProstateCancer;

    public IndividualCounselingForProstateCancerActionHelper(Context context, MemberObject memberObject) {
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
            familyHistoryProstateCancer = JsonFormUtils.getValue(jsonObject, "family_history_prostate_cancer");
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseCecapVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isNotBlank(familyHistoryProstateCancer)) {
            return BaseCecapVisitAction.Status.COMPLETED;
        } else {
            return BaseCecapVisitAction.Status.PENDING;
        }
    }
}