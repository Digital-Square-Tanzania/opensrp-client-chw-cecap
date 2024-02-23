package org.smartregister.chw.cecap.actionhelper;

import android.content.Context;

import org.smartregister.chw.cecap.model.BaseCecapVisitAction;
import org.smartregister.chw.cecap.domain.VisitDetail;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Designed to set default methods for the CECAP Action Helper
 * This object must remain inert to the Home Visit action. Its designed primarily for extension by simple visit actions
 */
public abstract class CecapVisitActionHelper implements BaseCecapVisitAction.CecapVisitActionHelper {
    protected Context context;

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.context = context;
    }

    /**
     * set preprocessed status to be inert
     *
     * @return null
     */
    @Override
    public String getPreProcessed() {
        return null;
    }

    /**
     * set schedule status to be inert
     *
     * @return null
     */
    @Override
    public BaseCecapVisitAction.ScheduleStatus getPreProcessedStatus() {
        return BaseCecapVisitAction.ScheduleStatus.DUE;
    }

    /**
     * set schedule status to be inert
     *
     * @return null
     */
    @Override
    public String getPreProcessedSubTitle() {
        return null;
    }

    /**
     * prevent post processing
     *
     * @return null
     */
    @Override
    public String postProcess(String jsonPayload) {
        return null;
    }

    /**
     * Do nothing on payload received
     *
     * @param baseCecapVisitAction
     */
    @Override
    public void onPayloadReceived(BaseCecapVisitAction baseCecapVisitAction) {
        Timber.v("onPayloadReceived");
    }

    public Context getContext() {
        return context;
    }
}