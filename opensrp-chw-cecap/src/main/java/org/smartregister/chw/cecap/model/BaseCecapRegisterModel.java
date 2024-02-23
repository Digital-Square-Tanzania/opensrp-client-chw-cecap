package org.smartregister.chw.cecap.model;

import org.json.JSONObject;
import org.smartregister.chw.cecap.contract.CecapRegisterContract;
import org.smartregister.chw.cecap.util.CecapJsonFormUtils;

public class BaseCecapRegisterModel implements CecapRegisterContract.Model {

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = CecapJsonFormUtils.getFormAsJson(formName);
        CecapJsonFormUtils.getRegistrationForm(jsonObject, entityId, currentLocationId);

        return jsonObject;
    }

}
