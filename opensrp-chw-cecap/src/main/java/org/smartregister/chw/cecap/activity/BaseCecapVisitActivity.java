package org.smartregister.chw.cecap.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.cecap.CecapLibrary;
import org.smartregister.chw.cecap.R;
import org.smartregister.chw.cecap.adapter.BaseCecapVisitAdapter;
import org.smartregister.chw.cecap.contract.BaseCecapVisitContract;
import org.smartregister.chw.cecap.dao.CecapDao;
import org.smartregister.chw.cecap.domain.MemberObject;
import org.smartregister.chw.cecap.interactor.BaseCecapVisitInteractor;
import org.smartregister.chw.cecap.model.BaseCecapVisitAction;
import org.smartregister.chw.cecap.presenter.BaseCecapVisitPresenter;
import org.smartregister.chw.cecap.util.Constants;
import org.smartregister.view.activity.SecuredActivity;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

public class BaseCecapVisitActivity extends SecuredActivity implements BaseCecapVisitContract.View, View.OnClickListener {

    private static final String TAG = BaseCecapVisitActivity.class.getCanonicalName();
    protected Map<String, BaseCecapVisitAction> actionList = new LinkedHashMap<>();
    protected BaseCecapVisitContract.Presenter presenter;
    protected MemberObject memberObject;
    protected String baseEntityID;
    protected Boolean isEditMode = false;

    protected Boolean isViaFollowupTest = false;

    protected RecyclerView.Adapter mAdapter;
    protected ProgressBar progressBar;
    protected TextView tvSubmit;
    protected TextView tvTitle;
    protected String current_action;
    protected String confirmCloseTitle;
    protected String confirmCloseMessage;

    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode, boolean isViaFollowupTest) {
        Intent intent = new Intent(activity, BaseCecapVisitActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, isEditMode);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.IS_VIA_FOLLOWUP_TEST, isViaFollowupTest);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_cecap_visit);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isEditMode = getIntent().getBooleanExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, false);
            isViaFollowupTest = getIntent().getBooleanExtra(Constants.ACTIVITY_PAYLOAD.IS_VIA_FOLLOWUP_TEST, false);
            baseEntityID = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
            memberObject = getMemberObject(baseEntityID);
        }

        confirmCloseTitle = getString(R.string.confirm_form_close);
        confirmCloseMessage = getString(R.string.confirm_form_close_explanation);
        setUpView();
        displayProgressBar(true);
        registerPresenter();
        if (presenter != null) {
            if (StringUtils.isNotBlank(baseEntityID)) {
                presenter.reloadMemberDetails(baseEntityID);
            } else {
                presenter.initialize();
            }
        }
    }

    protected MemberObject getMemberObject(String baseEntityId) {
        return CecapDao.getMember(baseEntityId);
    }

    public void setUpView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        findViewById(R.id.close).setOnClickListener(this);
        tvSubmit = findViewById(R.id.customFontTextViewSubmit);
        tvSubmit.setOnClickListener(this);
        tvTitle = findViewById(R.id.customFontTextViewName);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new BaseCecapVisitAdapter(this, this, (LinkedHashMap) actionList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        redrawVisitUI();
    }

    protected void registerPresenter() {
        presenter = new BaseCecapVisitPresenter(memberObject, this, new BaseCecapVisitInteractor(isViaFollowupTest));
    }

    @Override
    public void initializeActions(LinkedHashMap<String, BaseCecapVisitAction> map) {
        actionList.clear();

        //Necessary evil to rearrange the actions according to a specific arrangement
        if (map.containsKey(getString(R.string.cecap_individual_counseling_for_cervical_cancer))) {
            actionList.put(getString(R.string.cecap_individual_counseling_for_cervical_cancer), map.get(getString(R.string.cecap_individual_counseling_for_cervical_cancer)));
        }
        if (map.containsKey(getString(R.string.cecap_individual_counseling_for_breast_cancer))) {
            actionList.put(getString(R.string.cecap_individual_counseling_for_breast_cancer), map.get(getString(R.string.cecap_individual_counseling_for_breast_cancer)));
        }
        if (map.containsKey(getString(R.string.cecap_individual_counseling_for_prostate_cancer))) {
            actionList.put(getString(R.string.cecap_individual_counseling_for_prostate_cancer), map.get(getString(R.string.cecap_individual_counseling_for_prostate_cancer)));
        }

        if (map.containsKey(getString(R.string.cecap_clinical_breast_examination))) {
            actionList.put(getString(R.string.cecap_clinical_breast_examination), map.get(getString(R.string.cecap_clinical_breast_examination)));
        }

        if (map.containsKey(getString(R.string.cecap_vaginal_speculum_examination))) {
            actionList.put(getString(R.string.cecap_vaginal_speculum_examination), map.get(getString(R.string.cecap_vaginal_speculum_examination)));
        }

        if (map.containsKey(getString(R.string.cecap_screening_method))) {
            actionList.put(getString(R.string.cecap_screening_method), map.get(getString(R.string.cecap_screening_method)));
        }

        if (map.containsKey(getString(R.string.cecap_hpv_dna_sample_collection))) {
            actionList.put(getString(R.string.cecap_hpv_dna_sample_collection), map.get(getString(R.string.cecap_hpv_dna_sample_collection)));
        }

        if (map.containsKey(getString(R.string.cecap_via_approach))) {
            actionList.put(getString(R.string.cecap_via_approach), map.get(getString(R.string.cecap_via_approach)));
        }

        if (map.containsKey(getString(R.string.cecap_pap_sample_collection))) {
            actionList.put(getString(R.string.cecap_pap_sample_collection), map.get(getString(R.string.cecap_pap_sample_collection)));
        }
        //====================End of Necessary evil ====================================


        for (Map.Entry<String, BaseCecapVisitAction> entry : map.entrySet()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                actionList.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        displayProgressBar(false);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void displayToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Boolean getEditMode() {
        return isEditMode;
    }

    @Override
    public void onMemberDetailsReloaded(MemberObject memberObject) {
        this.memberObject = memberObject;
        presenter.initialize();
        redrawHeader(memberObject);
    }

    @Override
    protected void onCreation() {
        Timber.v("Empty onCreation");
    }

    @Override
    protected void onResumption() {
        Timber.v("Empty onResumption");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close) {
            displayExitDialog(() -> close());
        } else if (v.getId() == R.id.customFontTextViewSubmit) {
            submitVisit();
        }
    }

    @Override
    public BaseCecapVisitContract.Presenter presenter() {
        return presenter;
    }

    @Override
    public Form getFormConfig() {
        return null;
    }

    @Override
    public void startForm(BaseCecapVisitAction cecapVisitAction) {
        current_action = cecapVisitAction.getTitle();

        if (StringUtils.isNotBlank(cecapVisitAction.getJsonPayload())) {
            try {
                JSONObject jsonObject = new JSONObject(cecapVisitAction.getJsonPayload());
                startFormActivity(jsonObject);
            } catch (Exception e) {
                Timber.e(e);
                String locationId = CecapLibrary.getInstance().context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
                presenter().startForm(cecapVisitAction.getFormName(), memberObject.getBaseEntityId(), locationId);
            }
        } else {
            String locationId = CecapLibrary.getInstance().context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
            presenter().startForm(cecapVisitAction.getFormName(), memberObject.getBaseEntityId(), locationId);
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, JsonFormActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        if (getFormConfig() != null) {
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getFormConfig());
        }

        startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void startFragment(BaseCecapVisitAction pmtctHomeVisitAction) {
        current_action = pmtctHomeVisitAction.getTitle();

        if (pmtctHomeVisitAction.getDestinationFragment() != null)
            pmtctHomeVisitAction.getDestinationFragment().show(getSupportFragmentManager(), current_action);

    }

    @Override
    public void redrawHeader(MemberObject memberObject) {
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", memberObject.getFullName(), String.valueOf(memberObject.getAge()), getString(R.string.cecap_visit)));
    }

    @Override
    public void redrawVisitUI() {
        boolean valid = actionList.size() > 0;
        for (Map.Entry<String, BaseCecapVisitAction> entry : actionList.entrySet()) {
            BaseCecapVisitAction action = entry.getValue();
            if (
                    (!action.isOptional() && (action.getActionStatus() == BaseCecapVisitAction.Status.PENDING && action.isValid()))
                            || !action.isEnabled()
            ) {
                valid = false;
                break;
            }
        }

        int res_color = valid ? R.color.white : R.color.light_grey;
        tvSubmit.setTextColor(getResources().getColor(res_color));
        tvSubmit.setOnClickListener(valid ? this : null); // update listener to null

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void displayProgressBar(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }


    @Override
    public Map<String, BaseCecapVisitAction> getPmtctHomeVisitActions() {
        return actionList;
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void submittedAndClose() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        close();
    }

    @Override
    public BaseCecapVisitContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void submitVisit() {
        getPresenter().submitVisit();
    }

    @Override
    public void onDialogOptionUpdated(String jsonString) {
        BaseCecapVisitAction pmtctHomeVisitAction = actionList.get(current_action);
        if (pmtctHomeVisitAction != null) {
            pmtctHomeVisitAction.setJsonPayload(jsonString);
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            redrawVisitUI();
        }
    }

    @Override
    public Context getMyContext() {
        return this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GET_JSON) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                    BaseCecapVisitAction cecapVisitAction = actionList.get(current_action);
                    if (cecapVisitAction != null) {
                        cecapVisitAction.setJsonPayload(jsonString);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {

                BaseCecapVisitAction pmtctHomeVisitAction = actionList.get(current_action);
                if (pmtctHomeVisitAction != null)
                    pmtctHomeVisitAction.evaluateStatus();
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        // update the adapter after every payload
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            redrawVisitUI();
        }
    }

    @Override
    public void onBackPressed() {
        displayExitDialog(BaseCecapVisitActivity.this::finish);
    }

    protected void displayExitDialog(final Runnable onConfirm) {
        AlertDialog dialog = new AlertDialog.Builder(this, com.vijay.jsonwizard.R.style.AppThemeAlertDialog).setTitle(confirmCloseTitle)
                .setMessage(confirmCloseMessage).setNegativeButton(com.vijay.jsonwizard.R.string.yes, (dialog1, which) -> {
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                }).setPositiveButton(com.vijay.jsonwizard.R.string.no, (dialog2, which) -> Timber.d("No button on dialog in %s", JsonFormActivity.class.getCanonicalName())).create();

        dialog.show();
    }

}