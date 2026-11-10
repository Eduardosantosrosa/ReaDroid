package com.nst.iptvsmarterstvbox.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/* JADX INFO: loaded from: classes2.dex */
public class BootStartupActivity extends BroadcastReceiver {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public SharedPreferences f25428a;

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auto_start", 0);
        this.f25428a = sharedPreferences;
        if (sharedPreferences.getBoolean("auto_start", true) && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent intent2 = new Intent(context, (Class<?>) SplashActivity.class);
            intent2.addFlags(268435456);
            context.startActivity(intent2);
        }
    }
}
