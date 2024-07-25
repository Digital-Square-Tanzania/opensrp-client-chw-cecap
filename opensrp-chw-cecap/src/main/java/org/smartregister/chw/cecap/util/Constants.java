package org.smartregister.chw.cecap.util;

public interface Constants {

    int REQUEST_CODE_GET_JSON = 2244;
    String ENCOUNTER_TYPE = "encounter_type";
    String STEP_ONE = "step1";
    String STEP_TWO = "step2";

    interface JSON_FORM_EXTRA {
        String JSON = "json";
        String ENCOUNTER_TYPE = "encounter_type";

        String DELETE_EVENT_ID = "deleted_event_id";

        String DELETE_FORM_SUBMISSION_ID = "deleted_form_submission_id";
    }

    interface EVENT_TYPE {
        String CECAP_REGISTRATION = "CECAP Enrollment";

        String CECAP_FOLLOW_UP_VISIT = "CECAP Follow-up Visit";

        String CECAP_HOME_VISIT = "CECAP Home Visit";

        String CECAP_HEALTH_EDUCATION_MOBILIZATION = "CECAP Health Education Mobilization";

        String DELETE_EVENT = "Delete Event";
    }

    interface FORMS {
        String CECAP_ENROLLMENT = "cecap_enrollment";

        String CECAP_INDIVIDUAL_COUNSELING_FOR_CERVICAL_CANCER = "cecap_individual_counseling_for_cervical_cancer";

        String CECAP_INDIVIDUAL_COUNSELING_FOR_BREAST_CANCER = "cecap_individual_counseling_for_breast_cancer";

        String CECAP_INDIVIDUAL_COUNSELING_FOR_PROSTATE_CANCER = "cecap_individual_counseling_for_prostate_cancer";

        String CECAP_CLINICAL_BREAST_EXAMINATION = "cecap_clinical_breast_examination";

        String CECAP_VAGINAL_SPECULUM_EXAMINATION = "cecap_vaginal_speculum_examination_results";

        String CECAP_SCREENING_METHOD = "cecap_screening_method";

        String CECAP_VIA_APPROACH = "cecap_via_approach";

        String CECAP_VIA_APPROACH_FOR_HPV_DNA = "cecap_via_approach_for_hpv_dna";

        String CECAP_TREATMENT = "cecap_treatment";

        String CECAP_HPV_DNA_SAMPLE_COLLECTION = "cecap_hpv_dna_sample_collection";

        String CECAP_PAP_SAMPLE_COLLECTION = "cecap_pap_sample_collection";

        String HPV_DNA_FINDINGS = "cecap_hpv_dna_findings";

        String PAP_FINDINGS = "cecap_pap_findings";

        String CECAP_MOBILIZATION_SESSION = "cecap_mobilization_session";

        String CECAP_COMMUNITY_VISIT = "cecap_community_visit";
    }

    interface TABLES {
        String CECAP_REGISTER = "ec_cecap_register";

        String CECAP_FOLLOW_UP = "ec_cecap_visit";

        String CECAP_TEST_RESULTS = "ec_cecap_test_results";

        String CECAP_MOBILIZATION_SESSIONS = "ec_cecap_mobilization_session";

    }

    interface ACTIVITY_PAYLOAD {
        String BASE_ENTITY_ID = "BASE_ENTITY_ID";

        String FAMILY_BASE_ENTITY_ID = "FAMILY_BASE_ENTITY_ID";

        String ACTION = "ACTION";

        String CECAP_FORM_NAME = "CECAP_FORM_NAME";

        String CECAP_FORM = "CECAP_FORM";

        String PARENT_FORM_ENTITY_ID = "PARENT_FORM_ENTITY_ID";

        String EDIT_MODE = "editMode";

        String IS_VIA_FOLLOWUP_TEST = "isViaFollowupTest";

        String MEMBER_PROFILE_OBJECT = "MemberObject";

    }

    interface ACTIVITY_PAYLOAD_TYPE {
        String REGISTRATION = "REGISTRATION";
        String FOLLOW_UP_VISIT = "FOLLOW_UP_VISIT";
    }

    interface CONFIGURATION {
        String CECAP_REGISTRATION_CONFIGURATION = "cecap_registration";
    }

}