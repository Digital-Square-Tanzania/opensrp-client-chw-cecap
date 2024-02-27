package org.smartregister.chw.cecap.dao;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.cecap.CecapLibrary;
import org.smartregister.chw.cecap.domain.MemberObject;
import org.smartregister.chw.cecap.model.CecapMobilizationSessionModel;
import org.smartregister.chw.cecap.util.Constants;
import org.smartregister.chw.cecap.util.DBConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.dao.AbstractDao;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class CecapDao extends AbstractDao {
    public static void closeCecapMemberFromRegister(String baseEntityID) {
        String sql = "update ec_cecap_register set is_closed = 1 where base_entity_id = '" + baseEntityID + "'";
        updateDB(sql);
    }

    public static boolean isRegisteredForCecap(String baseEntityID) {
        String sql = "SELECT count(p.base_entity_id) count FROM " + Constants.TABLES.CECAP_REGISTER + " p " + "WHERE p.base_entity_id = '" + baseEntityID + "' AND p.is_closed = 0";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return false;

        return res.get(0) > 0;
    }

    public static MemberObject getMember(String baseEntityID) {
        String sql = "select m.base_entity_id , m.unique_id , m.relational_id , m.dob , m.first_name , m.middle_name , m.last_name , m.gender , m.phone_number , m.other_phone_number , f.first_name family_name ,f.primary_caregiver , f.family_head , f.village_town ,fh.first_name family_head_first_name , fh.middle_name family_head_middle_name , fh.last_name family_head_last_name, fh.phone_number family_head_phone_number ,  pcg.first_name pcg_first_name , pcg.last_name pcg_last_name , pcg.middle_name pcg_middle_name , pcg.phone_number  pcg_phone_number , mr.* from ec_family_member m inner join ec_family f on m.relational_id = f.base_entity_id inner join " + Constants.TABLES.CECAP_REGISTER + " mr on mr.base_entity_id = m.base_entity_id left join ec_family_member fh on fh.base_entity_id = f.family_head left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver where m.base_entity_id ='" + baseEntityID + "' ";
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        DataMap<MemberObject> dataMap = cursor -> {
            MemberObject memberObject = new MemberObject();

            memberObject.setFirstName(getCursorValue(cursor, "first_name", ""));
            memberObject.setMiddleName(getCursorValue(cursor, "middle_name", ""));
            memberObject.setLastName(getCursorValue(cursor, "last_name", ""));
            memberObject.setAddress(getCursorValue(cursor, "village_town"));
            memberObject.setGender(getCursorValue(cursor, "gender"));
            memberObject.setUniqueId(getCursorValue(cursor, "unique_id", ""));
            memberObject.setDob(getCursorValue(cursor, "dob"));
            memberObject.setFamilyBaseEntityId(getCursorValue(cursor, "relational_id", ""));
            memberObject.setRelationalId(getCursorValue(cursor, "relational_id", ""));
            memberObject.setPrimaryCareGiver(getCursorValue(cursor, "primary_caregiver"));
            memberObject.setFamilyName(getCursorValue(cursor, "family_name", ""));
            memberObject.setPhoneNumber(getCursorValue(cursor, "phone_number", ""));
            memberObject.setBaseEntityId(getCursorValue(cursor, "base_entity_id", ""));
            memberObject.setFamilyHead(getCursorValue(cursor, "family_head", ""));
            memberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "pcg_phone_number", ""));
            memberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "family_head_phone_number", ""));
            memberObject.setHivStatus(getCursorValue(cursor, "hiv_status", ""));
            memberObject.setParity(getCursorValue(cursor, "parity", ""));

            String familyHeadName = getCursorValue(cursor, "family_head_first_name", "") + " " + getCursorValue(cursor, "family_head_middle_name", "");

            familyHeadName = (familyHeadName.trim() + " " + getCursorValue(cursor, "family_head_last_name", "")).trim();
            memberObject.setFamilyHeadName(familyHeadName);

            String familyPcgName = getCursorValue(cursor, "pcg_first_name", "") + " " + getCursorValue(cursor, "pcg_middle_name", "");

            familyPcgName = (familyPcgName.trim() + " " + getCursorValue(cursor, "pcg_last_name", "")).trim();
            memberObject.setPrimaryCareGiverName(familyPcgName);

            return memberObject;
        };

        List<MemberObject> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return null;

        return res.get(0);
    }

    public static Event getEventByFormSubmissionId(String formSubmissionId) {
        String sql = "select json from event where formSubmissionId = '" + formSubmissionId + "'";
        DataMap<Event> dataMap = (c) -> {
            Event event;
            try {
                event = (Event) CecapLibrary.getInstance().getEcSyncHelper().convert(new JSONObject(getCursorValue(c, "json")), Event.class);
            } catch (JSONException e) {
                Timber.e(e);
                return null;
            }

            return event;
        };
        return (Event) AbstractDao.readSingleValue(sql, dataMap);
    }


    public static int getClientAge(String baseEntityID) {
        String sql = "SELECT  dob  FROM ec_family_member WHERE base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "dob");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return 0;


        int age;
        try {
            age = (new Period(new DateTime(res.get(0)), new DateTime())).getYears();
        } catch (Exception e) {
            Timber.e(e);
            return 0;
        }
        return age;
    }

    public static String getClientSex(String baseEntityID) {
        String sql = "SELECT  gender  FROM ec_family_member WHERE base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "gender");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return null;

        return res.get(0);
    }

    public static String getServicesEnrolled(String baseEntityID) {
        String sql = "SELECT  services_enrolled  FROM " + Constants.TABLES.CECAP_REGISTER + " WHERE base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "services_enrolled");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return null;

        return res.get(0);
    }


    public static boolean hasTestResults(String baseEntityID) {
        String sql = "SELECT  pap_smear_sample_id, hpv_dna_specimen_sample_id   FROM " + Constants.TABLES.CECAP_FOLLOW_UP + " WHERE entity_id = '" + baseEntityID + "' AND (pap_smear_sample_id IS NOT NULL OR hpv_dna_specimen_sample_id IS NOT NULL) ORDER BY last_interacted_with DESC LIMIT 1";

        DataMap<String> papSmearDataMap = cursor -> getCursorValue(cursor, "pap_smear_sample_id");
        DataMap<String> hpvDataMap = cursor -> getCursorValue(cursor, "hpv_dna_specimen_sample_id");

        List<String> papSmearRes = readData(sql, papSmearDataMap);
        List<String> hpvRes = readData(sql, hpvDataMap);

        return (papSmearRes != null && papSmearRes.size() > 0) || (hpvRes != null && hpvRes.size() > 0);
    }


    public static String getScreenTestPerformed(String baseEntityID) {
        String sql = "SELECT  screening_test_performed  FROM " + Constants.TABLES.CECAP_FOLLOW_UP + " WHERE entity_id = '" + baseEntityID + "' ORDER BY last_interacted_with DESC LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "screening_test_performed");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return null;

        return res.get(0);
    }


    public static String getHpvFindings(String baseEntityID) {
        String sql = "SELECT  hpv_dna_findings  FROM " + Constants.TABLES.CECAP_FOLLOW_UP + " INNER JOIN " + Constants.TABLES.CECAP_TEST_RESULTS + " ON " + Constants.TABLES.CECAP_TEST_RESULTS + "." + DBConstants.KEY.CECAP_TEST_RESULTS_FOLLOWUP_FORM_SUBMISSION_ID + " = " + Constants.TABLES.CECAP_FOLLOW_UP + "." + DBConstants.KEY.BASE_ENTITY_ID + " WHERE " + Constants.TABLES.CECAP_FOLLOW_UP + ".entity_id = '" + baseEntityID + "' ORDER BY " + Constants.TABLES.CECAP_FOLLOW_UP + ".last_interacted_with DESC LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hpv_dna_findings");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return null;

        return res.get(0);
    }

    public static List<CecapMobilizationSessionModel> getCecapMobilizationSessions() {
        String sql = "SELECT * FROM " + Constants.TABLES.CECAP_MOBILIZATION_SESSIONS;

        DataMap<CecapMobilizationSessionModel> dataMap = cursor -> {
            CecapMobilizationSessionModel sbcMobilizationSessionModel = new CecapMobilizationSessionModel();
            sbcMobilizationSessionModel.setSessionId(cursor.getString(cursor.getColumnIndex("id")));

            sbcMobilizationSessionModel.setSessionDate(cursor.getString(cursor.getColumnIndex("mobilization_date")));

            sbcMobilizationSessionModel.setSessionParticipants(computeSessionParticipants(cursor.getString(cursor.getColumnIndex(DBConstants.KEY.FEMALE_CLIENTS_REACHED)), cursor.getString(cursor.getColumnIndex(DBConstants.KEY.MALE_CLIENTS_REACHED))));

            sbcMobilizationSessionModel.setHealthEducation(cursor.getString(cursor.getColumnIndex("health_education_provided")));

            return sbcMobilizationSessionModel;
        };

        List<CecapMobilizationSessionModel> res = readData(sql, dataMap);
        if (res == null || res.size() == 0) return null;
        return res;
    }

    public static void updateData(String baseEntityID, String mobilization_date, String femaleClientsReached, String maleClientsReached, String healthEducationProvided) {
        String sql = "INSERT INTO "+ Constants.TABLES.CECAP_MOBILIZATION_SESSIONS +
                "           (id, mobilization_date, female_clients_reached, male_clients_reached, health_education_provided) " +
                "           VALUES (" +
                "                   '" + baseEntityID + "', " +
                "                   '" + mobilization_date + "', " +
                "                   '" + femaleClientsReached + "', " +
                "                   '" + maleClientsReached + "', " +
                "                   '" + healthEducationProvided + "') " +
                "              " +
                " ON CONFLICT (id) DO UPDATE SET mobilization_date      = '" + mobilization_date + "',      " +
                "                               female_clients_reached = '" + femaleClientsReached + "',  " +
                "                               male_clients_reached   = '" + maleClientsReached + "',    " +
                "                               health_education_provided  = '" + healthEducationProvided + "'    ";
        updateDB(sql);
    }

    private static String computeSessionParticipants(String femaleParticipants, String maleParticipants) {
        int sum = Integer.parseInt(femaleParticipants) + Integer.parseInt(maleParticipants);
        return String.valueOf(sum);
    }

}
