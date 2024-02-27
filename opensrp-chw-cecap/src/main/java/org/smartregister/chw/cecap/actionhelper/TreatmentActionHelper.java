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
public class TreatmentActionHelper extends CecapVisitActionHelper {
    protected Context context;

    protected MemberObject memberObject;

    protected String cryotherapyThermocoagulation;

    protected String leepTreatment;

    protected String viaFindings;

    private JSONObject jsonForm;

    public TreatmentActionHelper(Context context, MemberObject memberObject, String viaFindings) {
        this.context = context;
        this.memberObject = memberObject;
        this.viaFindings = viaFindings;
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
     * set preprocessed status to be inert
     *
     * @return null
     */
    @Override
    public String getPreProcessed() {
        try {
            JSONObject global = jsonForm.getJSONObject(GLOBAL);
            global.put("viaFindings", viaFindings);
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
            cryotherapyThermocoagulation = JsonFormUtils.getValue(jsonObject, "cryotherapy_thermocoagulation");
            leepTreatment = JsonFormUtils.getValue(jsonObject, "leep_treatment");
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
        if (StringUtils.isNotBlank(cryotherapyThermocoagulation) || StringUtils.isNotBlank(leepTreatment)) {
            return BaseCecapVisitAction.Status.COMPLETED;
        } else {
            return BaseCecapVisitAction.Status.PENDING;
        }
    }
}