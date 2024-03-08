package org.smartregister.chw.cecap.interactor;


import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.cecap.CecapLibrary;
import org.smartregister.chw.cecap.R;
import org.smartregister.chw.cecap.actionhelper.CecapVisitActionHelper;
import org.smartregister.chw.cecap.actionhelper.ClinicalBreastExaminationActionHelper;
import org.smartregister.chw.cecap.actionhelper.HpvDnaSampleCollectionActionHelper;
import org.smartregister.chw.cecap.actionhelper.IndividualCounselingForBreastCancerActionHelper;
import org.smartregister.chw.cecap.actionhelper.IndividualCounselingForCervicalCancerActionHelper;
import org.smartregister.chw.cecap.actionhelper.IndividualCounselingForProstateCancerActionHelper;
import org.smartregister.chw.cecap.actionhelper.PapSampleCollectionActionHelper;
import org.smartregister.chw.cecap.actionhelper.ScreeningMethodActionHelper;
import org.smartregister.chw.cecap.actionhelper.TreatmentActionHelper;
import org.smartregister.chw.cecap.actionhelper.VaginalSpeculumExaminationActionHelper;
import org.smartregister.chw.cecap.actionhelper.ViaApproachActionHelper;
import org.smartregister.chw.cecap.contract.BaseCecapVisitContract;
import org.smartregister.chw.cecap.dao.CecapDao;
import org.smartregister.chw.cecap.domain.MemberObject;
import org.smartregister.chw.cecap.domain.Visit;
import org.smartregister.chw.cecap.domain.VisitDetail;
import org.smartregister.chw.cecap.model.BaseCecapVisitAction;
import org.smartregister.chw.cecap.repository.VisitRepository;
import org.smartregister.chw.cecap.util.AppExecutors;
import org.smartregister.chw.cecap.util.Constants;
import org.smartregister.chw.cecap.util.JsonFormUtils;
import org.smartregister.chw.cecap.util.NCUtils;
import org.smartregister.chw.cecap.util.VisitUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;


public class BaseCecapVisitInteractor implements BaseCecapVisitContract.Interactor {

    private final CecapLibrary cecapLibrary;
    private final LinkedHashMap<String, BaseCecapVisitAction> actionList;
    protected AppExecutors appExecutors;
    private ECSyncHelper syncHelper;
    private Context mContext;
    private Map<String, List<VisitDetail>> details = null;

    private BaseCecapVisitContract.InteractorCallBack callBack;

    private Boolean isViaFollowupTest;

    @VisibleForTesting
    public BaseCecapVisitInteractor(AppExecutors appExecutors, CecapLibrary CecapLibrary, ECSyncHelper syncHelper, Boolean isViaFollowupTest) {
        this.appExecutors = appExecutors;
        this.cecapLibrary = CecapLibrary;
        this.syncHelper = syncHelper;
        this.actionList = new LinkedHashMap<>();
        this.isViaFollowupTest = isViaFollowupTest;
    }

    public BaseCecapVisitInteractor(Boolean isViaFollowupTest) {
        this(new AppExecutors(), CecapLibrary.getInstance(), CecapLibrary.getInstance().getEcSyncHelper(), isViaFollowupTest);
    }

    @Override
    public void reloadMemberDetails(String memberID, BaseCecapVisitContract.InteractorCallBack callBack) {
        MemberObject memberObject = getMemberClient(memberID);
        if (memberObject != null) {
            final Runnable runnable = () -> {
                appExecutors.mainThread().execute(() -> callBack.onMemberDetailsReloaded(memberObject));
            };
            appExecutors.diskIO().execute(runnable);
        }
    }

    /**
     * Override this method and return actual member object for the provided user
     *
     * @param memberID unique identifier for the user
     * @return MemberObject wrapper for the user's data
     */
    @Override
    public MemberObject getMemberClient(String memberID) {
        return CecapDao.getMember(memberID);
    }

    @Override
    public void saveRegistration(String jsonString, boolean isEditMode, BaseCecapVisitContract.InteractorCallBack callBack) {
        Timber.v("saveRegistration");
    }

    @Override
    public void calculateActions(final BaseCecapVisitContract.View view, MemberObject memberObject, final BaseCecapVisitContract.InteractorCallBack callBack) {
        mContext = view.getContext();
        this.callBack = callBack;

        if (view.getEditMode()) {
            Visit lastVisit = cecapLibrary.visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.CECAP_FOLLOW_UP_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(cecapLibrary.visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        final Runnable runnable = () -> {
            if (!isViaFollowupTest) {
                try {
                    String servicesEnrolled = CecapDao.getServicesEnrolled(memberObject.getBaseEntityId());
                    if (servicesEnrolled != null) {
                        if (servicesEnrolled.contains("cervical_cancer")) {
                            evaluateIndividualCounsellingForCervicalCancer(memberObject, details);
                        }

                        if (servicesEnrolled.contains("breast_cancer")) {
                            evaluateIndividualCounsellingForBreastCancer(memberObject, details);
                        }

                        if (servicesEnrolled.contains("prostate_cancer")) {
                            evaluateIndividualCounsellingForProstateCancer(memberObject, details);
                        }
                    } else {
                        evaluateIndividualCounsellingForCervicalCancer(memberObject, details);
                        evaluateIndividualCounsellingForBreastCancer(memberObject, details);
                        evaluateIndividualCounsellingForProstateCancer(memberObject, details);
                    }
                } catch (BaseCecapVisitAction.ValidationException e) {
                    Timber.e(e);
                }
            } else {
                try {
                    evaluateViaApproach(memberObject, details);
                } catch (BaseCecapVisitAction.ValidationException e) {
                    Timber.e(e);
                }
            }
            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    protected void evaluateIndividualCounsellingForCervicalCancer(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseCecapVisitAction.ValidationException {
        CecapVisitActionHelper actionHelper = new IndividualCounselingForCervicalCancerActionHelper(mContext, memberObject) {
            @Override
            public void processIndividualCounselingForCervicalCancer(String eligibleForCervicalCancerScreening) {
                if (Boolean.parseBoolean(eligibleForCervicalCancerScreening)) {
                    try {
                        evaluateVaginalSpeculumExamination(memberObject, details);
                    } catch (BaseCecapVisitAction.ValidationException e) {
                        Timber.e(e);
                    }
                } else {
                    actionList.remove(mContext.getString(R.string.cecap_vaginal_speculum_examination));
                    actionList.remove(mContext.getString(R.string.cecap_screening_method));
                    actionList.remove(mContext.getString(R.string.cecap_via_approach));
                    actionList.remove(mContext.getString(R.string.cecap_hpv_dna_sample_collection));
                    actionList.remove(mContext.getString(R.string.cecap_pap_sample_collection));
                }

                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };

        String actionName = mContext.getString(R.string.cecap_individual_counseling_for_cervical_cancer);
        BaseCecapVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.CECAP_INDIVIDUAL_COUNSELING_FOR_CERVICAL_CANCER).build();
        actionList.put(actionName, action);
    }

    protected void evaluateIndividualCounsellingForBreastCancer(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseCecapVisitAction.ValidationException {
        CecapVisitActionHelper actionHelper = new IndividualCounselingForBreastCancerActionHelper(mContext, memberObject) {
            @Override
            public void processIndividualCounselingForBreastCancer(String eligibleForBreastCancerScreening) {
                if (Boolean.parseBoolean(eligibleForBreastCancerScreening)) {
                    try {
                        evaluateBreastCounselling(memberObject, details);
                    } catch (BaseCecapVisitAction.ValidationException e) {
                        Timber.e(e);
                    }
                } else {
                    actionList.remove(mContext.getString(R.string.cecap_clinical_breast_examination));
                }

                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };
        String actionName = mContext.getString(R.string.cecap_individual_counseling_for_breast_cancer);
        BaseCecapVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.CECAP_INDIVIDUAL_COUNSELING_FOR_BREAST_CANCER).build();
        actionList.put(actionName, action);
    }

    protected void evaluateIndividualCounsellingForProstateCancer(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseCecapVisitAction.ValidationException {
        CecapVisitActionHelper actionHelper = new IndividualCounselingForProstateCancerActionHelper(mContext, memberObject);
        String actionName = mContext.getString(R.string.cecap_individual_counseling_for_prostate_cancer);
        BaseCecapVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.CECAP_INDIVIDUAL_COUNSELING_FOR_PROSTATE_CANCER).build();
        actionList.put(actionName, action);
    }

    protected void evaluateBreastCounselling(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseCecapVisitAction.ValidationException {
        CecapVisitActionHelper actionHelper = new ClinicalBreastExaminationActionHelper(mContext, memberObject);
        String actionName = mContext.getString(R.string.cecap_clinical_breast_examination);
        BaseCecapVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.CECAP_CLINICAL_BREAST_EXAMINATION).build();
        actionList.put(actionName, action);
    }

    protected void evaluateVaginalSpeculumExamination(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseCecapVisitAction.ValidationException {
        CecapVisitActionHelper actionHelper = new VaginalSpeculumExaminationActionHelper(mContext, memberObject) {
            @Override
            public void processVaginalSpeculumExamination(String eligibleForCervicalCancerScreening) {
                if (Boolean.parseBoolean(eligibleForCervicalCancerScreening)) {
                    try {
                        evaluateScreeningMethod(memberObject, details);
                    } catch (BaseCecapVisitAction.ValidationException e) {
                        Timber.e(e);
                    }
                } else {
                    actionList.remove(mContext.getString(R.string.cecap_screening_method));
                    actionList.remove(mContext.getString(R.string.cecap_via_approach));
                    actionList.remove(mContext.getString(R.string.cecap_hpv_dna_sample_collection));
                    actionList.remove(mContext.getString(R.string.cecap_pap_sample_collection));
                }

                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };
        String actionName = mContext.getString(R.string.cecap_vaginal_speculum_examination);
        BaseCecapVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.CECAP_VAGINAL_SPECULUM_EXAMINATION).build();
        actionList.put(actionName, action);
    }

    protected void evaluateScreeningMethod(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseCecapVisitAction.ValidationException {
        CecapVisitActionHelper actionHelper = new ScreeningMethodActionHelper(mContext, memberObject) {
            @Override
            public void processScreeningMethod(String screeningMethods) {
                if (screeningMethods.contains("hpv_dna")) {
                    try {
                        evaluateHpvDnaSampleCollection(memberObject, details);
                    } catch (BaseCecapVisitAction.ValidationException e) {
                        Timber.e(e);
                    }
                } else {
                    actionList.remove(mContext.getString(R.string.cecap_hpv_dna_sample_collection));
                }

                if (screeningMethods.contains("via")) {
                    try {
                        evaluateViaApproach(memberObject, details);
                    } catch (BaseCecapVisitAction.ValidationException e) {
                        Timber.e(e);
                    }
                } else {
                    actionList.remove(mContext.getString(R.string.cecap_via_approach));
                }

                if (screeningMethods.contains("pap")) {
                    try {
                        evaluatePapSampleCollection(memberObject, details);
                    } catch (BaseCecapVisitAction.ValidationException e) {
                        Timber.e(e);
                    }
                } else {
                    actionList.remove(mContext.getString(R.string.cecap_pap_sample_collection));
                }

                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };
        String actionName = mContext.getString(R.string.cecap_screening_method);
        BaseCecapVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.CECAP_SCREENING_METHOD).build();
        actionList.put(actionName, action);
    }

    protected void evaluateViaApproach(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseCecapVisitAction.ValidationException {
        CecapVisitActionHelper actionHelper = new ViaApproachActionHelper(mContext, memberObject) {
            @Override
            public void processViaFindings(String viaFindings) {
                if (viaFindings.contains("positive")) {
                    try {
                        evaluateTreatment(memberObject, details, viaFindings);
                    } catch (BaseCecapVisitAction.ValidationException e) {
                        Timber.e(e);
                    }
                } else {
                    actionList.remove(mContext.getString(R.string.cecap_hpv_dna_sample_collection));
                    actionList.remove(mContext.getString(R.string.cecap_treatment));
                }

                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };
        String actionName = mContext.getString(R.string.cecap_via_approach);
        BaseCecapVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.CECAP_VIA_APPROACH).build();
        actionList.put(actionName, action);
    }

    protected void evaluateTreatment(MemberObject memberObject, Map<String, List<VisitDetail>> details, String viaFindings) throws BaseCecapVisitAction.ValidationException {
        CecapVisitActionHelper actionHelper = new TreatmentActionHelper(mContext, memberObject, viaFindings);
        String actionName = mContext.getString(R.string.cecap_treatment);
        BaseCecapVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.CECAP_TREATMENT).build();
        actionList.put(actionName, action);
    }

    protected void evaluateHpvDnaSampleCollection(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseCecapVisitAction.ValidationException {
        CecapVisitActionHelper actionHelper = new HpvDnaSampleCollectionActionHelper(mContext, memberObject);
        String actionName = mContext.getString(R.string.cecap_hpv_dna_sample_collection);
        BaseCecapVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.CECAP_HPV_DNA_SAMPLE_COLLECTION).build();
        actionList.put(actionName, action);
    }

    protected void evaluatePapSampleCollection(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseCecapVisitAction.ValidationException {
        CecapVisitActionHelper actionHelper = new PapSampleCollectionActionHelper(mContext, memberObject);
        String actionName = mContext.getString(R.string.cecap_pap_sample_collection);
        BaseCecapVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.CECAP_PAP_SAMPLE_COLLECTION).build();
        actionList.put(actionName, action);
    }

    public BaseCecapVisitAction.Builder getBuilder(String title) {
        return new BaseCecapVisitAction.Builder(mContext, title);
    }

    @Override
    public void submitVisit(final boolean editMode, final String memberID, final Map<String, BaseCecapVisitAction> map, final BaseCecapVisitContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {
            boolean result = true;
            try {
                submitVisit(editMode, memberID, map, "");
            } catch (Exception e) {
                Timber.e(e);
                result = false;
            }

            final boolean finalResult = result;
            appExecutors.mainThread().execute(() -> callBack.onSubmitted(finalResult));
        };

        appExecutors.diskIO().execute(runnable);
    }

    protected void submitVisit(final boolean editMode, final String memberID, final Map<String, BaseCecapVisitAction> map, String parentEventType) throws Exception {
        // create a map of the different types

        Map<String, BaseCecapVisitAction> externalVisits = new HashMap<>();
        Map<String, String> combinedJsons = new HashMap<>();
        String payloadType = null;
        String payloadDetails = null;

        // aggregate forms to be processed
        for (Map.Entry<String, BaseCecapVisitAction> entry : map.entrySet()) {
            String json = entry.getValue().getJsonPayload();
            if (StringUtils.isNotBlank(json)) {
                // do not process events that are meant to be in detached mode
                // in a similar manner to the the aggregated events

                BaseCecapVisitAction action = entry.getValue();
                BaseCecapVisitAction.ProcessingMode mode = action.getProcessingMode();

                if (mode == BaseCecapVisitAction.ProcessingMode.SEPARATE && StringUtils.isBlank(parentEventType)) {
                    externalVisits.put(entry.getKey(), entry.getValue());
                } else {
                    if (action.getActionStatus() != BaseCecapVisitAction.Status.PENDING)
                        combinedJsons.put(entry.getKey(), json);
                }

                payloadType = action.getPayloadType().name();
                payloadDetails = action.getPayloadDetails();
            }
        }

        String type = StringUtils.isBlank(parentEventType) ? getEncounterType() : getEncounterType();

        // persist to database
        Visit visit = saveVisit(editMode, memberID, type, combinedJsons, parentEventType);
        if (visit != null) {
            saveVisitDetails(visit, payloadType, payloadDetails);
            processExternalVisits(visit, externalVisits, memberID);
        }

        if (cecapLibrary.isSubmitOnSave()) {
            List<Visit> visits = new ArrayList<>(1);
            visits.add(visit);
            VisitUtils.processVisits(visits, cecapLibrary.visitRepository(), cecapLibrary.visitDetailsRepository());

            Context context = cecapLibrary.getInstance().context().applicationContext();

        }
    }

    /**
     * recursively persist visits to the db
     *
     * @param visit
     * @param externalVisits
     * @param memberID
     * @throws Exception
     */
    protected void processExternalVisits(Visit visit, Map<String, BaseCecapVisitAction> externalVisits, String memberID) throws Exception {
        if (visit != null && !externalVisits.isEmpty()) {
            for (Map.Entry<String, BaseCecapVisitAction> entry : externalVisits.entrySet()) {
                Map<String, BaseCecapVisitAction> subEvent = new HashMap<>();
                subEvent.put(entry.getKey(), entry.getValue());

                String subMemberID = entry.getValue().getBaseEntityID();
                if (StringUtils.isBlank(subMemberID)) subMemberID = memberID;

                submitVisit(false, subMemberID, subEvent, visit.getVisitType());
            }
        }
    }

    protected @Nullable Visit saveVisit(boolean editMode, String memberID, String encounterType, final Map<String, String> jsonString, String parentEventType) throws Exception {

        AllSharedPreferences allSharedPreferences = cecapLibrary.getInstance().context().allSharedPreferences();

        String derivedEncounterType = StringUtils.isBlank(parentEventType) ? encounterType : "";
        Event baseEvent = JsonFormUtils.processVisitJsonForm(allSharedPreferences, memberID, derivedEncounterType, jsonString, getTableName());

        // only tag the first event with the date
        if (StringUtils.isBlank(parentEventType)) {
            prepareEvent(baseEvent);
        } else {
            prepareSubEvent(baseEvent);
        }

        if (baseEvent != null) {
            baseEvent.setFormSubmissionId(JsonFormUtils.generateRandomUUIDString());
            JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);

            String visitID = (editMode) ? visitRepository().getLatestVisit(memberID, getEncounterType()).getVisitId() : JsonFormUtils.generateRandomUUIDString();

            // reset database
            if (editMode) {
                Visit visit = visitRepository().getVisitByVisitId(visitID);
                if (visit != null) baseEvent.setEventDate(visit.getDate());

                VisitUtils.deleteProcessedVisit(visitID, memberID);
                deleteOldVisit(visitID);
            }

            Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
            visit.setPreProcessedJson(new Gson().toJson(baseEvent));
            visit.setParentVisitID(getParentVisitEventID(visit, parentEventType));

            visitRepository().addVisit(visit);
            return visit;
        }
        return null;
    }

    protected String getParentVisitEventID(Visit visit, String parentEventType) {
        return visitRepository().getParentVisitEventID(visit.getBaseEntityId(), parentEventType, visit.getDate());
    }

    @VisibleForTesting
    public VisitRepository visitRepository() {
        return CecapLibrary.getInstance().visitRepository();
    }

    protected void deleteOldVisit(String visitID) {
        visitRepository().deleteVisit(visitID);
        CecapLibrary.getInstance().visitDetailsRepository().deleteVisitDetails(visitID);

        List<Visit> childVisits = visitRepository().getChildEvents(visitID);
        for (Visit v : childVisits) {
            visitRepository().deleteVisit(v.getVisitId());
            CecapLibrary.getInstance().visitDetailsRepository().deleteVisitDetails(v.getVisitId());
        }
    }


    protected void saveVisitDetails(Visit visit, String payloadType, String payloadDetails) {
        if (visit.getVisitDetails() == null) return;

        for (Map.Entry<String, List<VisitDetail>> entry : visit.getVisitDetails().entrySet()) {
            if (entry.getValue() != null) {
                for (VisitDetail d : entry.getValue()) {
                    d.setPreProcessedJson(payloadDetails);
                    d.setPreProcessedType(payloadType);
                    CecapLibrary.getInstance().visitDetailsRepository().addVisitDetails(d);
                }
            }
        }
    }

    /**
     * Injects implementation specific changes to the event
     *
     * @param baseEvent object
     */
    protected void prepareEvent(Event baseEvent) {
        if (baseEvent != null) {
            // add sbc date obs and last
            List<Object> list = new ArrayList<>();
            list.add(new Date());
            baseEvent.addObs(new Obs("concept", "text", "cecap_visit_date", "", list, new ArrayList<>(), null, "cecap_visit_date"));
        }
    }

    /**
     * injects additional meta data to the event
     *
     * @param baseEvent object
     */
    protected void prepareSubEvent(Event baseEvent) {
        Timber.v("You can add information to sub events");
    }

    protected String getEncounterType() {
        return Constants.EVENT_TYPE.CECAP_FOLLOW_UP_VISIT;
    }

    protected String getTableName() {
        return Constants.TABLES.CECAP_REGISTER;
    }
}