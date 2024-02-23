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
        String CECAP_REGISTRATION = "CECAP Registration";

        String CECAP_FOLLOW_UP_VISIT = "CECAP Follow-up Visit";

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

        String CECAP_HPV_DNA_SAMPLE_COLLECTION = "cecap_hpv_dna_sample_collection";

        String CECAP_PAP_SAMPLE_COLLECTION = "cecap_pap_sample_collection";
    }

    interface TABLES {
        String CECAP_REGISTER = "ec_cecap_register";

        String CECAP_FOLLOW_UP = "ec_cecap_follow_up_visit";

    }

    interface ACTIVITY_PAYLOAD {
        String BASE_ENTITY_ID = "BASE_ENTITY_ID";
        String FAMILY_BASE_ENTITY_ID = "FAMILY_BASE_ENTITY_ID";
        String ACTION = "ACTION";
        String CECAP_FORM_NAME = "CECAP_FORM_NAME";
        String EDIT_MODE = "editMode";
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