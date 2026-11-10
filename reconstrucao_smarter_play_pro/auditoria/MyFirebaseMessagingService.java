package com.nst.iptvsmarterstvbox.WHMCSClientapp.notifications;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import c.f.c.u.u;
import c.h.a.e.f.a;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.nst.iptvsmarterstvbox.WHMCSClientapp.activities.MyTicketActivity;
import org.achartengine.ChartFactory;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    public static final String f24927h = MyFirebaseMessagingService.class.getSimpleName();

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    public a f24928i;

    @Override // com.google.firebase.messaging.FirebaseMessagingService
    public void p(u uVar) {
        String str = f24927h;
        Log.e(str, "From: " + uVar.J());
        if (uVar.K() != null) {
            Log.e(str, "Notification Body: " + uVar.K().a());
            v(uVar.K().a());
        }
        if (uVar.I().size() > 0) {
            Log.e(str, "Data Payload: " + uVar.I().toString());
            try {
                u(new JSONObject(uVar.I().toString()));
            } catch (Exception e2) {
                Log.e(f24927h, "Exception: " + e2.getMessage());
            }
        }
    }

    public final void u(JSONObject jSONObject) {
        String str;
        StringBuilder sb;
        String message;
        String str2 = f24927h;
        Log.e(str2, "push json: " + jSONObject.toString());
        try {
            JSONObject jSONObject2 = jSONObject.getJSONObject("data");
            String string = jSONObject2.getString(ChartFactory.TITLE);
            String string2 = jSONObject2.getString("message");
            boolean z = jSONObject2.getBoolean("is_background");
            String string3 = jSONObject2.getString("image");
            String string4 = jSONObject2.getString("timestamp");
            JSONObject jSONObject3 = jSONObject2.getJSONObject("payload");
            Log.e(str2, "title: " + string);
            Log.e(str2, "message: " + string2);
            Log.e(str2, "isBackground: " + z);
            Log.e(str2, "payload: " + jSONObject3.toString());
            Log.e(str2, "imageUrl: " + string3);
            Log.e(str2, "timestamp: " + string4);
            Intent intent = new Intent(getApplicationContext(), (Class<?>) MyTicketActivity.class);
            intent.setAction("notification_action");
            intent.putExtra("ticketid", "4");
            if (TextUtils.isEmpty(string3)) {
                w(getApplicationContext(), string, string2, string4, intent);
            } else {
                x(getApplicationContext(), string, string2, string4, intent, string3);
            }
        } catch (JSONException e2) {
            str = f24927h;
            sb = new StringBuilder();
            sb.append("Json Exception: ");
            message = e2.getMessage();
            sb.append(message);
            Log.e(str, sb.toString());
        } catch (Exception e3) {
            str = f24927h;
            sb = new StringBuilder();
            sb.append("Exception: ");
            message = e3.getMessage();
            sb.append(message);
            Log.e(str, sb.toString());
        }
    }

    public final void v(String str) {
        if (a.b(getApplicationContext())) {
            return;
        }
        Intent intent = new Intent("pushNotification");
        intent.putExtra("message", str);
        a.r.a.a.b(this).c(intent);
        new a(getApplicationContext()).c();
    }

    public final void w(Context context, String str, String str2, String str3, Intent intent) {
        this.f24928i = new a(context);
        intent.setFlags(268468224);
        this.f24928i.e(str, str2, str3, intent);
    }

    public final void x(Context context, String str, String str2, String str3, Intent intent, String str4) {
        this.f24928i = new a(context);
        intent.setFlags(268468224);
        this.f24928i.f(str, str2, str3, intent, str4);
    }
}
