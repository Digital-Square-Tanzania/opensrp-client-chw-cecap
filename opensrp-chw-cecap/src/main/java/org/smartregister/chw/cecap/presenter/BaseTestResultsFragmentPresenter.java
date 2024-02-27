package org.smartregister.chw.cecap.presenter;

import static org.apache.commons.lang3.StringUtils.trim;

import org.smartregister.chw.cecap.contract.TestResultsFragmentContract;
import org.smartregister.chw.cecap.util.Constants;
import org.smartregister.chw.cecap.util.DBConstants;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.View;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.TreeSet;

public class BaseTestResultsFragmentPresenter implements TestResultsFragmentContract.Presenter {
    protected WeakReference<TestResultsFragmentContract.View> viewReference;
    protected TestResultsFragmentContract.Model model;

    protected RegisterConfiguration config;
    protected Set<View> visibleColumns = new TreeSet<>();
    protected String viewConfigurationIdentifier;


    public BaseTestResultsFragmentPresenter(TestResultsFragmentContract.View view, TestResultsFragmentContract.Model model, String viewConfigurationIdentifier) {
        this.viewReference = new WeakReference<>(view);
        this.model = model;
        this.viewConfigurationIdentifier = viewConfigurationIdentifier;
        this.config = model.defaultRegisterConfiguration();
    }

    @Override
    public String getMainCondition() {
        return " " + Constants.TABLES.CECAP_FOLLOW_UP + "." + DBConstants.KEY.HPV_DNA_SAMPLE_ID + " IS NOT NULL OR "+ DBConstants.KEY.PAP_SMEAR_SAMPLE_ID + " IS NOT NULL";
    }

    @Override
    public String getDefaultSortQuery() {
        return "";
    }

    @Override
    public String getMainTable() {
        return Constants.TABLES.CECAP_FOLLOW_UP;
    }

    @Override
    public String getDueFilterCondition() {
        return null;
    }


    @Override
    public void processViewConfigurations() {
        //implement
    }

    @Override
    public void initializeQueries(String mainCondition) {
        String tableName = getMainTable();
        mainCondition = trim(getMainCondition()).equals("") ? mainCondition : getMainCondition();
        String countSelect = model.countSelect(tableName, mainCondition);
        String mainSelect = model.mainSelect(tableName, mainCondition);

        if (getView() != null) {

            getView().initializeQueryParams(tableName, countSelect, mainSelect);
            getView().initializeAdapter(visibleColumns);

            getView().countExecute();
            getView().filterandSortInInitializeQueries();
        }
    }


    protected TestResultsFragmentContract.View getView() {
        if (viewReference != null)
            return viewReference.get();
        else
            return null;
    }

    @Override
    public void startSync() {
        //implement
    }

    @Override
    public void searchGlobally(String s) {
        //implement
    }
}
