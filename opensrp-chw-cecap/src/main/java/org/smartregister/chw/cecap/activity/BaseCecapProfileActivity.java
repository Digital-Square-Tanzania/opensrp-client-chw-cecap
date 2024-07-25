package org.smartregister.chw.cecap.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.cecap.R;
import org.smartregister.chw.cecap.contract.CecapProfileContract;
import org.smartregister.chw.cecap.custom_views.BaseCecapFloatingMenu;
import org.smartregister.chw.cecap.dao.CecapDao;
import org.smartregister.chw.cecap.domain.MemberObject;
import org.smartregister.chw.cecap.interactor.BaseCecapProfileInteractor;
import org.smartregister.chw.cecap.presenter.BaseCecapProfilePresenter;
import org.smartregister.chw.cecap.util.CecapUtil;
import org.smartregister.chw.cecap.util.Constants;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.activity.BaseProfileActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


public class BaseCecapProfileActivity extends BaseProfileActivity implements CecapProfileContract.View, CecapProfileContract.InteractorCallBack {
    protected MemberObject memberObject;

    protected CecapProfileContract.Presenter profilePresenter;

    protected CircleImageView imageView;

    protected TextView textViewName;

    protected TextView textViewGender;

    protected TextView textViewLocation;

    protected TextView textViewUniqueID;

    protected TextView textViewRecordCecap;

    protected View viewSeparator1;

    protected View viewSeparator2;

    protected RelativeLayout rlLastVisit;

    protected RelativeLayout visitStatus;

    protected ImageView imageViewCross;

    protected TextView textViewUndo;
    protected TextView textViewVisitDone;
    protected RelativeLayout visitDone;
    protected LinearLayout recordVisits;
    protected TextView textViewVisitDoneEdit;
    protected BaseCecapFloatingMenu baseCecapFloatingMenu;
    protected RelativeLayout rlTestResults;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
    private ProgressBar progressBar;

    public static void startProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, BaseCecapProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_cecap_profile);
        Toolbar toolbar = findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        String baseEntityId = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
        }

        toolbar.setNavigationOnClickListener(v -> BaseCecapProfileActivity.this.finish());
        appBarLayout = this.findViewById(R.id.collapsing_toolbar_appbarlayout);
        if (Build.VERSION.SDK_INT >= 21) {
            appBarLayout.setOutlineProvider(null);
        }

        textViewName = findViewById(R.id.textview_name);
        textViewGender = findViewById(R.id.textview_gender);
        textViewLocation = findViewById(R.id.textview_address);
        textViewUniqueID = findViewById(R.id.textview_id);
        viewSeparator1 = findViewById(R.id.separator_1);
        viewSeparator2 = findViewById(R.id.separator_2);
        imageViewCross = findViewById(R.id.tick_image);
        rlLastVisit = findViewById(R.id.rlLastVisit);
        rlTestResults = findViewById(R.id.rlTestResults);
        textViewVisitDone = findViewById(R.id.textview_visit_done);
        visitStatus = findViewById(R.id.record_visit_not_done_bar);
        visitDone = findViewById(R.id.visit_done_bar);
        recordVisits = findViewById(R.id.record_visits);
        progressBar = findViewById(R.id.progress_bar);
        textViewVisitDoneEdit = findViewById(R.id.textview_edit);
        textViewRecordCecap = findViewById(R.id.textview_record_cecap);
        textViewUndo = findViewById(R.id.textview_undo);
        imageView = findViewById(R.id.imageview_profile);
        textViewVisitDoneEdit.setOnClickListener(this);
        rlLastVisit.setOnClickListener(this);
        rlTestResults.setOnClickListener(this);
        textViewRecordCecap.setOnClickListener(this);
        textViewUndo.setOnClickListener(this);

        imageRenderHelper = new ImageRenderHelper(this);
        memberObject = getMemberObject(baseEntityId);
        initializePresenter();
        profilePresenter.fillProfileData(memberObject);
        setupViews();
    }

    protected MemberObject getMemberObject(String baseEntityId) {
        return CecapDao.getMember(baseEntityId);
    }

    @Override
    protected void setupViews() {
        initializeFloatingMenu();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.title_layout) {
            onBackPressed();
        } else if (id == R.id.rlLastVisit) {
            this.openMedicalHistory();
        } else if (id == R.id.textview_record_cecap) {
            this.recordCecap(memberObject);
        } else if (id == R.id.rlTestResults) {
            this.openTestResults();
        }
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        profilePresenter = new BaseCecapProfilePresenter(this, new BaseCecapProfileInteractor(), memberObject);
        fetchProfileData();
        profilePresenter.refreshProfileBottom();
    }

    public void initializeFloatingMenu() {
        if (StringUtils.isNotBlank(memberObject.getPhoneNumber())) {
            baseCecapFloatingMenu = new BaseCecapFloatingMenu(this, memberObject);
            baseCecapFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            addContentView(baseCecapFloatingMenu, linearLayoutParams);
        }
    }

    @Override
    public void hideView() {
        textViewRecordCecap.setVisibility(View.GONE);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void setProfileViewWithData() {
        textViewName.setText(String.format("%s %s %s, %d", memberObject.getFirstName(), memberObject.getMiddleName(), memberObject.getLastName(), memberObject.getAge()));
        textViewGender.setText(CecapUtil.getGenderTranslated(this, memberObject.getGender()));
        textViewLocation.setText(memberObject.getAddress());
        textViewUniqueID.setText(memberObject.getUniqueId());
    }

    @Override
    public void setOverDueColor() {
        textViewRecordCecap.setBackground(getResources().getDrawable(R.drawable.record_btn_selector_overdue));
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    protected void fetchProfileData() {
        //fetch profile data
    }

    @Override
    public void showProgressBar(boolean status) {
        progressBar.setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        showProgressBar(false);
        rlLastVisit.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
        textViewRecordCecap.setText(hasHistory ? R.string.record_cecap : R.string.health_services_provided);
        UpdateRecordCecapButtonText(hasHistory);
    }

    public void UpdateRecordCecapButtonText(boolean hasHistory) {
        if (!hasHistory) {
            textViewRecordCecap.setVisibility(View.VISIBLE);
            textViewRecordCecap.setText(getString(R.string.health_services_provided));
        } else if (hasPendingVia(memberObject.getBaseEntityId())) {
//            textViewRecordCecap.setVisibility(View.VISIBLE);
//            textViewRecordCecap.setText(getString(R.string.cecap_via_approach));
            textViewRecordCecap.setVisibility(View.VISIBLE);
            textViewRecordCecap.setText(getString(R.string.record_cecap));
        } else if (CecapDao.hasTestResults(memberObject.getBaseEntityId())) {
            textViewRecordCecap.setVisibility(View.GONE);
        } else {
            textViewRecordCecap.setVisibility(View.VISIBLE);
            textViewRecordCecap.setText(getString(R.string.record_cecap));
        }
    }

    public boolean hasPendingVia(String baseEntityID) {

        String screenTestPerformed = CecapDao.getScreenTestPerformed(baseEntityID);
        boolean hasPendingVia = false;

        if (screenTestPerformed != null && screenTestPerformed.contains("hpv_dna")) {
            String hpvFindings = CecapDao.getHpvFindings(baseEntityID);
            if (hpvFindings != null && hpvFindings.equals("positive")) {
                hasPendingVia = true;
            }
        }
        return hasPendingVia;
    }

    @Override
    public void openMedicalHistory() {
        //implement
    }


    public void openTestResults() {
        //implement
    }

    @Override
    public void recordCecap(MemberObject memberObject) {
        //implement
    }

    @Nullable
    private String formatTime(Date dateTime) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
            return formatter.format(dateTime);
        } catch (Exception e) {
            Timber.d(e);
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            profilePresenter.saveForm(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
        }
    }
}
