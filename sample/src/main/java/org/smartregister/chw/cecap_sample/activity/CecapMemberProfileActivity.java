package org.smartregister.chw.cecap_sample.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.cecap.activity.BaseCecapProfileActivity;
import org.smartregister.chw.cecap.domain.MemberObject;
import org.smartregister.chw.cecap.util.Constants;


public class CecapMemberProfileActivity extends BaseCecapProfileActivity {

    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, CecapMemberProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void recordCecap(MemberObject memberObject) {
        CecapVisitActivity.startMe(this, memberObject.getBaseEntityId(), false);
    }

    @Override
    protected MemberObject getMemberObject(String baseEntityId) {
        return EntryActivity.getSampleMember();
    }
}