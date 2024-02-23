package org.smartregister.chw.cecap.custom_views;

import android.app.Activity;
import android.content.Context;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;
import android.widget.LinearLayout;

import org.smartregister.chw.cecap.R;
import org.smartregister.chw.cecap.domain.MemberObject;
import org.smartregister.chw.cecap.fragment.BaseCecapCallDialogFragment;

public class BaseCecapFloatingMenu extends LinearLayout implements View.OnClickListener {
    private MemberObject MEMBER_OBJECT;

    public BaseCecapFloatingMenu(Context context, MemberObject MEMBER_OBJECT) {
        super(context);
        initUi();
        this.MEMBER_OBJECT = MEMBER_OBJECT;
    }

    protected void initUi() {
        inflate(getContext(), R.layout.view_cecap_floating_menu, this);
        FloatingActionButton fab = findViewById(R.id.cecap_fab);
        if (fab != null)
            fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.cecap_fab) {
            Activity activity = (Activity) getContext();
            BaseCecapCallDialogFragment.launchDialog(activity, MEMBER_OBJECT);
        }  else if (view.getId() == R.id.cecap_refer_to_facility_layout) {
            Activity activity = (Activity) getContext();
            BaseCecapCallDialogFragment.launchDialog(activity, MEMBER_OBJECT);
        }
    }
}