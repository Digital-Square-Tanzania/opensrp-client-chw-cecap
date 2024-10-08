package org.smartregister.chw.cecap.util;

import static org.smartregister.client.utils.constants.JsonFormConstants.JSON_FORM_KEY.GLOBAL;
import static org.smartregister.client.utils.constants.JsonFormConstants.V_REGEX;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.cecap.CecapLibrary;
import org.smartregister.chw.cecap.dao.CecapDao;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.FormUtils;

import java.util.Calendar;

import timber.log.Timber;

public class CecapJsonFormUtils extends org.smartregister.util.JsonFormUtils {
    public static final String METADATA = "metadata";

    public static Triple<Boolean, JSONObject, JSONArray> validateParameters(String jsonString) {

        JSONObject jsonForm = toJSONObject(jsonString);
        JSONArray fields = malariaFormFields(jsonForm);

        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = Triple.of(jsonForm != null && fields != null, jsonForm, fields);
        return registrationFormParams;
    }

    public static JSONArray malariaFormFields(JSONObject jsonForm) {
        try {
            JSONArray fieldsOne = fields(jsonForm, Constants.STEP_ONE);
            JSONArray fieldsTwo = fields(jsonForm, Constants.STEP_TWO);
            if (fieldsTwo != null) {
                for (int i = 0; i < fieldsTwo.length(); i++) {
                    fieldsOne.put(fieldsTwo.get(i));
                }
            }
            return fieldsOne;

        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    public static JSONArray fields(JSONObject jsonForm, String step) {
        try {

            JSONObject step1 = jsonForm.has(step) ? jsonForm.getJSONObject(step) : null;
            if (step1 == null) {
                return null;
            }

            return step1.has(FIELDS) ? step1.getJSONArray(FIELDS) : null;

        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    public static Event processJsonForm(AllSharedPreferences allSharedPreferences, String
            jsonString) {

        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

        if (!registrationFormParams.getLeft()) {
            return null;
        }

        JSONObject jsonForm = registrationFormParams.getMiddle();
        JSONArray fields = registrationFormParams.getRight();
        String entityId = getString(jsonForm, ENTITY_ID);
        String encounter_type = jsonForm.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);

        if (Constants.EVENT_TYPE.CECAP_REGISTRATION.equals(encounter_type)) {
            encounter_type = Constants.TABLES.CECAP_REGISTER;
        } else if (Constants.EVENT_TYPE.CECAP_FOLLOW_UP_VISIT.equals(encounter_type)) {
            encounter_type = Constants.TABLES.CECAP_FOLLOW_UP;
        }
        return org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, METADATA), formTag(allSharedPreferences), entityId, getString(jsonForm, Constants.ENCOUNTER_TYPE), encounter_type);
    }

    protected static FormTag formTag(AllSharedPreferences allSharedPreferences) {
        FormTag formTag = new FormTag();
        formTag.providerId = allSharedPreferences.fetchRegisteredANM();
        formTag.appVersion = CecapLibrary.getInstance().getApplicationVersion();
        formTag.databaseVersion = CecapLibrary.getInstance().getDatabaseVersion();
        return formTag;
    }

    public static void tagEvent(AllSharedPreferences allSharedPreferences, Event event) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        event.setProviderId(providerId);
        event.setLocationId(locationId(allSharedPreferences));
        event.setChildLocationId(allSharedPreferences.fetchCurrentLocality());
        event.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        event.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));

        event.setClientApplicationVersion(CecapLibrary.getInstance().getApplicationVersion());
        event.setClientDatabaseVersion(CecapLibrary.getInstance().getDatabaseVersion());
    }

    private static String locationId(AllSharedPreferences allSharedPreferences) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        String userLocationId = allSharedPreferences.fetchUserLocalityId(providerId);
        if (StringUtils.isBlank(userLocationId)) {
            userLocationId = allSharedPreferences.fetchDefaultLocalityId(providerId);
        }

        return userLocationId;
    }

    public static void getRegistrationForm(JSONObject jsonObject, String entityId, String
            currentLocationId) throws JSONException {
        jsonObject.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);
        jsonObject.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, entityId);
        jsonObject.put(DBConstants.KEY.RELATIONAL_ID, entityId);

        jsonObject.getJSONObject(GLOBAL).put("age", CecapDao.getClientAge(entityId));
        jsonObject.getJSONObject(GLOBAL).put("sex", CecapDao.getClientSex(entityId));

        JSONArray fields = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
        JSONObject reproductiveCancerId = JsonFormUtils.getFieldJSONObject(fields, "reproductive_cancer_id");

        if (reproductiveCancerId != null) {
            try {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                reproductiveCancerId.getJSONObject(V_REGEX).put(VALUE, generateRegex(currentYear));
            } catch (Exception e) {
                Timber.e(e);
            }
        }


    }
    public static String generateRegex(int year) {
        // Get the last digit of the year
        int lastDigit = year % 10;

        // Construct the regex based on the last digit
        return String.format("(\\d{4}-(0[1-9]|1[0-2])-(00|0[1-9]|1[0-9]|2[0-%d]))?", lastDigit);
    }

    public static JSONObject getFormAsJson(String formName) throws Exception {
        return FormUtils.getInstance(CecapLibrary.getInstance().context().applicationContext()).getFormJson(formName);
    }

}
