package org.smartregister.chw.cecap.activity;

import android.content.Intent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;

public class BaseCecapRegisterActivityTest {
    @Mock
    public Intent data;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private BaseCecapRegisterActivity baseTestRegisterActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void assertNotNull() {
        Assert.assertNotNull(baseTestRegisterActivity);
    }

    @Test
    public void testFormConfig() {
        Assert.assertNull(baseTestRegisterActivity.getFormConfig());
    }

    @Test
    public void checkIdentifier() {
        Assert.assertNotNull(baseTestRegisterActivity.getViewIdentifiers());
    }

    @Test(expected = Exception.class)
    public void onActivityResult() throws Exception {
        Whitebox.invokeMethod(baseTestRegisterActivity, "onActivityResult", 2244, -1, data);
        Mockito.verify(baseTestRegisterActivity.presenter()).saveForm(null);
    }

}
