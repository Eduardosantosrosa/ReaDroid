package org.readroid.deviceinspector;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayDeque;

/** Reads the active accessibility tree and performs explicit click/back/tap requests. */
public final class DeviceAccessibilityService extends AccessibilityService {
    private static volatile DeviceAccessibilityService instance;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ArrayDeque<JSONObject> events = new ArrayDeque<>();
    private long eventSequence = 0;
    private volatile String targetPackage = "";

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        record("SERVICE_CONNECTED", null, null, null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        String packageName = event.getPackageName() == null ? "" : event.getPackageName().toString();
        if (!packageName.equals(targetPackage) && !"com.android.systemui".equals(packageName)) return;
        String className = event.getClassName() == null ? "" : event.getClassName().toString();
        JSONArray labels = new JSONArray();
        for (CharSequence text : event.getText()) {
            if (text != null && text.length() > 0) labels.put(text.toString());
        }
        record(AccessibilityEvent.eventTypeToString(event.getEventType()), packageName, className,
                labels, event.getContentChangeTypes());
    }

    @Override public void onInterrupt() { record("SERVICE_INTERRUPTED", null, null, null, null); }

    @Override public void onDestroy() {
        if (instance == this) instance = null;
        super.onDestroy();
    }

    private synchronized void record(String type, String packageName, String className, JSONArray labels, Integer changes) {
        try {
            JSONObject event = new JSONObject();
            event.put("sequence", ++eventSequence);
            event.put("time_ms", System.currentTimeMillis());
            event.put("type", type);
            if (packageName != null) event.put("package", packageName);
            if (className != null) event.put("class", className);
            if (labels != null) event.put("text", labels);
            if (changes != null) event.put("content_change_types", changes);
            events.addLast(event);
            while (events.size() > 1000) events.removeFirst();
        } catch (Exception ignored) { }
    }

    static boolean isConnected() { return instance != null; }

    static void setTargetPackage(String packageName) {
        DeviceAccessibilityService service = instance;
        if (service != null) service.targetPackage = packageName == null ? "" : packageName;
    }

    static boolean isEnabled(Context context) {
        String enabled = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (TextUtils.isEmpty(enabled)) return false;
        ComponentName component = new ComponentName(context, DeviceAccessibilityService.class);
        return enabled.contains(component.flattenToString());
    }

    static JSONObject snapshot() throws Exception {
        DeviceAccessibilityService service = instance;
        if (service == null) throw new IllegalStateException("AccessibilityService não está habilitado");
        final JSONObject[] result = new JSONObject[1];
        final Exception[] failure = new Exception[1];
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        service.main.post(() -> {
            try {
                result[0] = service.makeSnapshot();
            } catch (Exception e) { failure[0] = e; }
            finally { latch.countDown(); }
        });
        if (!latch.await(3, java.util.concurrent.TimeUnit.SECONDS)) throw new IllegalStateException("Timeout lendo árvore de acessibilidade");
        if (failure[0] != null) throw failure[0];
        return result[0];
    }

    private JSONObject makeSnapshot() throws Exception {
        JSONObject result = new JSONObject();
        result.put("captured_at_ms", System.currentTimeMillis());
        JSONArray nodes = new JSONArray();
        AccessibilityNodeInfo root = getRootInActiveWindow();
        String packageName = "";
        if (root != null) {
            CharSequence name = root.getPackageName();
            packageName = name == null ? "" : name.toString();
            flatten(root, nodes, 0, new int[]{0});
            root.recycle();
        }
        result.put("package", packageName);
        result.put("nodes", nodes);
        result.put("node_count", nodes.length());
        JSONArray windows = new JSONArray();
        for (AccessibilityWindowInfo window : getWindows()) {
            Rect bounds = new Rect(); window.getBoundsInScreen(bounds);
            JSONObject row = new JSONObject();
            row.put("type", window.getType()); row.put("active", window.isActive());
            row.put("focused", window.isFocused()); row.put("bounds", rect(bounds));
            windows.put(row); window.recycle();
        }
        result.put("windows", windows);
        return result;
    }

    private void flatten(AccessibilityNodeInfo node, JSONArray output, int depth, int[] index) throws Exception {
        if (node == null || depth > 40 || output.length() >= 1500) return;
        JSONObject row = new JSONObject();
        Rect bounds = new Rect(); node.getBoundsInScreen(bounds);
        CharSequence text = node.getText(), desc = node.getContentDescription();
        CharSequence cls = node.getClassName(), pkg = node.getPackageName(), viewId = node.getViewIdResourceName();
        row.put("index", index[0]++);
        row.put("depth", depth);
        row.put("class", cls == null ? "" : cls.toString());
        row.put("text", text == null ? "" : text.toString());
        row.put("content_description", desc == null ? "" : desc.toString());
        row.put("view_id", viewId == null ? "" : viewId.toString());
        row.put("package", pkg == null ? "" : pkg.toString());
        row.put("bounds", rect(bounds));
        row.put("clickable", node.isClickable()); row.put("enabled", node.isEnabled());
        row.put("scrollable", node.isScrollable()); row.put("editable", node.isEditable());
        row.put("password", node.isPassword());
        output.put(row);
        int count = node.getChildCount();
        for (int i = 0; i < count && output.length() < 1500; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) { flatten(child, output, depth + 1, index); child.recycle(); }
        }
    }

    private JSONObject rect(Rect r) throws Exception {
        JSONObject b = new JSONObject(); b.put("left", r.left); b.put("top", r.top);
        b.put("right", r.right); b.put("bottom", r.bottom); return b;
    }

    static JSONArray eventsSince(long sequence) throws Exception {
        DeviceAccessibilityService service = instance;
        if (service == null) throw new IllegalStateException("AccessibilityService não está habilitado");
        JSONArray result = new JSONArray();
        synchronized (service) {
            for (JSONObject event : service.events) if (event.optLong("sequence") > sequence) result.put(event);
        }
        return result;
    }

    static boolean click(int index) throws Exception {
        DeviceAccessibilityService service = require();
        final boolean[] done = new boolean[1];
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        service.main.post(() -> {
            try {
                AccessibilityNodeInfo root = service.getRootInActiveWindow();
                if (root != null) { done[0] = service.clickAt(root, index, new int[]{0}); root.recycle(); }
            } finally { latch.countDown(); }
        });
        if (!latch.await(3, java.util.concurrent.TimeUnit.SECONDS)) throw new IllegalStateException("Timeout no clique acessível");
        return done[0];
    }

    private boolean clickAt(AccessibilityNodeInfo node, int wanted, int[] current) {
        if (current[0]++ == wanted) return node.isEnabled() && node.isClickable()
                && node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                boolean result = clickAt(child, wanted, current); child.recycle();
                if (result) return true;
            }
        }
        return false;
    }

    static boolean tap(float x, float y) throws Exception {
        DeviceAccessibilityService service = require();
        final boolean[] ok = new boolean[1];
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        service.main.post(() -> {
            Path path = new Path(); path.moveTo(x, y);
            GestureDescription gesture = new GestureDescription.Builder()
                    .addStroke(new GestureDescription.StrokeDescription(path, 0, 80)).build();
            ok[0] = service.dispatchGesture(gesture, new GestureResultCallback() {
                @Override public void onCompleted(GestureDescription g) { latch.countDown(); }
                @Override public void onCancelled(GestureDescription g) { latch.countDown(); }
            }, service.main);
            if (!ok[0]) latch.countDown();
        });
        if (!latch.await(3, java.util.concurrent.TimeUnit.SECONDS)) throw new IllegalStateException("Timeout no gesto");
        return ok[0];
    }

    static boolean back() { return require().performGlobalAction(GLOBAL_ACTION_BACK); }
    static boolean home() { return require().performGlobalAction(GLOBAL_ACTION_HOME); }

    private static DeviceAccessibilityService require() {
        DeviceAccessibilityService service = instance;
        if (service == null) throw new IllegalStateException("AccessibilityService não está habilitado");
        return service;
    }
}
