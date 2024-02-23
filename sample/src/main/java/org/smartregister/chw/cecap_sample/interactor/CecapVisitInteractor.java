package org.smartregister.chw.cecap_sample.interactor;

import org.smartregister.chw.cecap.domain.MemberObject;
import org.smartregister.chw.cecap.interactor.BaseCecapVisitInteractor;
import org.smartregister.chw.cecap_sample.activity.EntryActivity;

public class CecapVisitInteractor extends BaseCecapVisitInteractor {
    @Override
    public MemberObject getMemberClient(String memberID) {
        return EntryActivity.getSampleMember();
    }
}
