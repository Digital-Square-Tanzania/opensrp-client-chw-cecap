package org.smartregister.chw.cecap.listener;


import android.view.View;

import org.smartregister.chw.cecap.R;
import org.smartregister.chw.cecap.fragment.BaseCecapCallDialogFragment;
import org.smartregister.chw.cecap.util.CecapUtil;

import timber.log.Timber;

public class BaseCecapCallWidgetDialogListener implements View.OnClickListener {

    private BaseCecapCallDialogFragment callDialogFragment;

    public BaseCecapCallWidgetDialogListener(BaseCecapCallDialogFragment dialogFragment) {
        callDialogFragment = dialogFragment;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.cecap_call_close) {
            callDialogFragment.dismiss();
        } else if (i == R.id.cecap_call_head_phone) {
            try {
                String phoneNumber = (String) v.getTag();
                CecapUtil.launchDialer(callDialogFragment.getActivity(), callDialogFragment, phoneNumber);
                callDialogFragment.dismiss();
            } catch (Exception e) {
                Timber.e(e);
            }
        } else if (i == R.id.call_cecap_client_phone) {
            try {
                String phoneNumber = (String) v.getTag();
                CecapUtil.launchDialer(callDialogFragment.getActivity(), callDialogFragment, phoneNumber);
                callDialogFragment.dismiss();
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }
}
