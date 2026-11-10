package org.readroid.extremerebuild.ui;

import static org.junit.Assert.*;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {
    @Test public void navigatesToSettingsAndHighlightsFocusedDpadControl() {
        try (ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                View content=activity.findViewById(android.R.id.content);
                TextView settings=findText(content,"Configurações");
                assertNotNull("Settings navigation control is visible",settings);
                assertTrue("Navigation control can receive remote focus",settings.isFocusable());
                assertTrue("Navigation control accepts focus",settings.requestFocus());
                assertTrue(settings.hasFocus());
                assertEquals(Color.rgb(118,215,165),settings.getBackgroundTintList().getDefaultColor());
                settings.performClick();
                assertNotNull("Settings screen is rendered after activation",findText(content,"Gerenciar fontes"));
            });
        }
    }

    private static TextView findText(View node,String value) {
        if(node instanceof TextView&&value.contentEquals(((TextView)node).getText()))return (TextView)node;
        if(node instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)node;for(int i=0;i<group.getChildCount();i++){TextView found=findText(group.getChildAt(i),value);if(found!=null)return found;}}
        return null;
    }
}
