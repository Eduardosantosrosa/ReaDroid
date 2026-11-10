package org.readroid.extremerebuild.ui;

import static org.junit.Assert.*;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {
    @Test public void settingsNavigationSupportsRealDpadFocusAndActivation() {
        try (ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            AtomicReference<TextView> settingsRef=new AtomicReference<>();
            AtomicReference<TextView> sourcesRef=new AtomicReference<>();

            scenario.onActivity(activity -> {
                View content=activity.findViewById(android.R.id.content);
                TextView settings=findText(content,"Configurações");
                TextView sources=findText(content,"Fontes");

                assertNotNull("Settings control exists",settings);
                assertNotNull("Sources control exists",sources);
                assertTrue("Settings is attached",settings.isAttachedToWindow());
                assertTrue("Settings is visible",settings.isShown());
                assertTrue("Settings is focusable",settings.isFocusable());
                assertTrue("Settings accepts touch-mode focus",settings.isFocusableInTouchMode());
                assertTrue("Settings receives actual focus",settings.requestFocusFromTouch());
                assertTrue("Settings reports focused state",settings.hasFocus());

                settingsRef.set(settings);
                sourcesRef.set(sources);
            });

            InstrumentationRegistry.getInstrumentation()
                .sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);

            scenario.onActivity(activity -> {
                assertTrue("DPAD_LEFT moves focus to Sources",
                    sourcesRef.get().hasFocus());
            });

            InstrumentationRegistry.getInstrumentation()
                .sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);

            scenario.onActivity(activity -> {
                TextView settings=settingsRef.get();

                assertTrue("DPAD_RIGHT returns focus to Settings",
                    settings.hasFocus());
            });

            InstrumentationRegistry.getInstrumentation()
                .sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);

            scenario.onActivity(activity -> {
                View content=activity.findViewById(android.R.id.content);
                assertNotNull("Settings screen opens",
                    findText(content,"Gerenciar fontes"));
            });
        }
    }

    private static TextView findText(View node,String value) {
        if(node instanceof TextView &&
           value.contentEquals(((TextView)node).getText())) {
            return (TextView)node;
        }

        if(node instanceof android.view.ViewGroup) {
            android.view.ViewGroup group=(android.view.ViewGroup)node;

            for(int i=0;i<group.getChildCount();i++) {
                TextView found=findText(group.getChildAt(i),value);
                if(found!=null)return found;
            }
        }

        return null;
    }
}
