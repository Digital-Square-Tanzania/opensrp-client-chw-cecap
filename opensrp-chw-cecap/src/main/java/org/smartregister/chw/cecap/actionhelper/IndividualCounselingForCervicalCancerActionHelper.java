package org.smartregister.chw.cecap.actionhelper;

import static org.smartregister.client.utils.constants.JsonFormConstants.GLOBAL;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.cecap.domain.MemberObject;
import org.smartregister.chw.cecap.domain.VisitDetail;
import org.smartregister.chw.cecap.model.BaseCecapVisitAction;
import org.smartregister.chw.cecap.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * CECAP Services Survey Action Helper
 */
public abstract class IndividualCounselingForCervicalCancerActionHelper extends CecapVisitActionHelper {
    protected Context context;
    protected MemberObject memberObject;

    protected String clientPregnancyStatus;

    private JSONObject jsonForm;

    public IndividualCounselingForCervicalCancerActionHelper(Context context, MemberObject memberObject) {
        this.context = context;
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        super.onJsonFormLoaded(jsonString, context, details);
        try {
            jsonForm = new JSONObject(jsonString);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /**
     * Update the preprocessed form to pass client age as global parameter
     *
     * @return the updated form
     */
    @Override
    public String getPreProcessed() {
        try {
            JSONObject global = jsonForm.getJSONObject(GLOBAL);
            global.put("age", memberObject.getAge());

            if (memberObject.getParity() != null) {
                try {
                    global.put("parity", Integer.parseInt(memberObject.getParity()));
                } catch (Exception e) {
                    Timber.e(e);
                    global.put("parity", -1);
                }
            }
            return jsonForm.toString();
        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            clientPregnancyStatus = JsonFormUtils.getValue(jsonObject, "client_pregnancy_status");
            processIndividualCounselingForCervicalCancer(JsonFormUtils.getValue(jsonObject, "eligible_for_cervical_cancer_screening_after_individual_counselling_for_cervical_cancer"));
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
        if (StringUtils.isNotBlank(clientPregnancyStatus)) {
            return BaseCecapVisitAction.Status.COMPLETED;
        } else {
            return BaseCecapVisitAction.Status.PENDING;
        }
    }


    public abstract void processIndividualCounselingForCervicalCancer(String eligibleForCervicalCancerScreening);
}