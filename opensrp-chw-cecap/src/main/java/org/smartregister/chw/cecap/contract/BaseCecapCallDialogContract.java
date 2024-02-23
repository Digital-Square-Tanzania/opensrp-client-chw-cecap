package org.smartregister.chw.cecap.contract;

import android.content.Context;

public interface BaseCecapCallDialogContract {

    interface View {
        void setPendingCallRequest(Dialer dialer);
        Context getCurrentContext();
    }

    interface Dialer {
        void callMe();
    }
}
