package xyz.trankvila.menteiaalirilo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
class ExampleInstrumentedTest {
    @Test
    void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("xyz.trankvila.menteiaalirilo", appContext.getPackageName());
    }
}
