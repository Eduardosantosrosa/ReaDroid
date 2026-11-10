package com.nst.iptvsmarterstvbox.view.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import c.h.a.i.q.m;
import com.amazonaws.services.s3.internal.Constants;
import com.nst.iptvsmarterstvbox.R;
import com.nst.iptvsmarterstvbox.WHMCSClientapp.activities.FreeTrailActivity;
import com.nst.iptvsmarterstvbox.model.callback.ActivationCallBack;
import com.nst.iptvsmarterstvbox.model.callback.LoginCallback;
import com.nst.iptvsmarterstvbox.vpn.activities.ProfileActivity;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import me.zhanghai.android.materialprogressbar.BuildConfig;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
public class LoginActivity extends a.b.k.c implements c.h.a.k.f.f, c.h.a.f.c<String>, c.h.a.k.f.a {

    /* JADX INFO: renamed from: d, reason: collision with root package name */
    public static InputFilter f25780d = new a();
    public SharedPreferences A;
    public SharedPreferences B;
    public SharedPreferences C;
    public SharedPreferences D;
    public SharedPreferences.Editor E;
    public SharedPreferences.Editor F;
    public SharedPreferences.Editor G;
    public SharedPreferences.Editor H;
    public SharedPreferences.Editor I;
    public SharedPreferences.Editor J;
    public SharedPreferences K;
    public SharedPreferences L;
    public SharedPreferences.Editor M;
    public c.h.a.i.q.g N;
    public c.h.a.i.q.a O;
    public String X;
    public SharedPreferences Y;
    public SharedPreferences.Editor Z;

    @BindView
    public LinearLayout activityLogin;

    @BindView
    public Button btn_free_trail;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    public EditText f25781e;

    @BindView
    public ImageView eyepass;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    public EditText f25782f;
    public SharedPreferences f0;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    public EditText f25783g;
    public SharedPreferences.Editor g0;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    public EditText f25784h;
    public SharedPreferences h0;

    @BindView
    public ImageView iv_connect_vpn;

    @BindView
    public ImageView iv_list_users;

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    public c.h.a.j.c f25787k;

    @BindView
    public LinearLayout linearLayout;

    @BindView
    public TextView link_transform;

    @BindView
    public TextView loginTV;

    /* JADX INFO: renamed from: m, reason: collision with root package name */
    public String f25789m;

    /* JADX INFO: renamed from: n, reason: collision with root package name */
    public String f25790n;
    public String n0;

    /* JADX INFO: renamed from: o, reason: collision with root package name */
    public String f25791o;
    public String o0;
    public ProgressDialog p;
    public String p0;

    @BindView
    public LinearLayout password_full;
    public SharedPreferences q;
    public String q0;
    public SharedPreferences.Editor r;

    @BindView
    public Button rl_bt_refresh;

    @BindView
    public RelativeLayout rl_bt_submit;

    @BindView
    public RelativeLayout rl_connect_vpn;

    @BindView
    public RelativeLayout rl_email;

    @BindView
    public RelativeLayout rl_list_users;

    @BindView
    public RelativeLayout rl_name;

    @BindView
    public RelativeLayout rl_password;

    @BindView
    public RelativeLayout rl_server_url;
    public SharedPreferences s;
    public int s0;
    public SharedPreferences t;
    public String t0;

    @BindView
    public TextView tv_add_user;

    @BindView
    public TextView tv_list_users;

    @BindView
    public TextView tv_vpn_con;
    public SharedPreferences u;
    public c.h.a.j.a u0;
    public SharedPreferences.Editor v;
    public c.h.a.k.d.a.a v0;
    public SharedPreferences w;
    public SharedPreferences.Editor x;
    public c.h.a.i.q.f y;

    @BindView
    public ImageView yourLogioTV;
    public SharedPreferences z;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    public int f25785i = -1;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    public boolean f25786j = false;

    /* JADX INFO: renamed from: l, reason: collision with root package name */
    public Context f25788l = this;
    public String P = BuildConfig.FLAVOR;
    public String Q = BuildConfig.FLAVOR;
    public String R = BuildConfig.FLAVOR;
    public String S = BuildConfig.FLAVOR;
    public long T = -1;
    public String U = BuildConfig.FLAVOR;
    public String V = BuildConfig.FLAVOR;
    public long W = -1;
    public String i0 = BuildConfig.FLAVOR;
    public ArrayList<String> j0 = new ArrayList<>();
    public String k0 = P0();
    public long l0 = 0;
    public SimpleDateFormat m0 = new SimpleDateFormat("dd/MM/yyyy");
    public String r0 = Build.MODEL;

    public static class a implements InputFilter {
        @Override // android.text.InputFilter
        public CharSequence filter(CharSequence charSequence, int i2, int i3, Spanned spanned, int i4, int i5) {
            while (i2 < i3) {
                if (Character.getType(charSequence.charAt(i2)) == 19) {
                    return BuildConfig.FLAVOR;
                }
                i2++;
            }
            return null;
        }
    }

    public class b implements View.OnClickListener {
        public b() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            c.h.a.h.n.a.f17037a = true;
            m.e0("login", LoginActivity.this.f25788l);
            Intent intent = new Intent(LoginActivity.this.f25788l, (Class<?>) ProfileActivity.class);
            intent.putExtra("typeid", "login");
            LoginActivity.this.startActivity(intent);
        }
    }

    public class c implements View.OnClickListener {
        public c() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            LoginActivity.this.Y0();
        }
    }

    public class d implements View.OnClickListener {
        public d() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            LoginActivity.this.W0();
        }
    }

    public class e implements View.OnClickListener {
        public e() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            LoginActivity.this.startActivity(new Intent(LoginActivity.this, (Class<?>) FreeTrailActivity.class));
        }
    }

    public class f implements View.OnClickListener {
        public f() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            c.h.a.h.n.a.f17044h = c.h.a.h.n.a.f17044h.booleanValue() ? Boolean.FALSE : Boolean.TRUE;
            LoginActivity.this.R0();
        }
    }

    public class g implements View.OnClickListener {
        public g() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            LoginActivity loginActivity = LoginActivity.this;
            int i2 = loginActivity.f25785i;
            if (i2 != -1) {
                loginActivity.f25785i = i2 - 1;
                loginActivity.f25782f.setInputType(129);
                LoginActivity.this.eyepass.setImageResource(R.drawable.hidepassword);
            } else {
                loginActivity.f25782f.setInputType(145);
                LoginActivity.this.eyepass.setImageResource(R.drawable.showpassword);
                LoginActivity.this.f25785i++;
            }
        }
    }

    public class h implements Runnable {
        public h() {
        }

        @Override // java.lang.Runnable
        public void run() {
            LoginActivity.this.f25786j = false;
        }
    }

    public class i extends Dialog implements View.OnClickListener {

        /* JADX INFO: renamed from: b, reason: collision with root package name */
        public Activity f25799b;

        /* JADX INFO: renamed from: c, reason: collision with root package name */
        public TextView f25800c;

        /* JADX INFO: renamed from: d, reason: collision with root package name */
        public TextView f25801d;

        /* JADX INFO: renamed from: e, reason: collision with root package name */
        public LinearLayout f25802e;

        /* JADX INFO: renamed from: f, reason: collision with root package name */
        public LinearLayout f25803f;

        public class a implements View.OnFocusChangeListener {

            /* JADX INFO: renamed from: b, reason: collision with root package name */
            public View f25805b;

            public a(View view) {
                this.f25805b = view;
            }

            /* JADX WARN: Code duplicated, block: B:24:0x0050 A[PHI: r1
              0x0050: PHI (r1v3 int) = (r1v0 int), (r1v4 int) binds: [B:23:0x004e, B:9:0x001d] A[DONT_GENERATE, DONT_INLINE]] */
            @Override // android.view.View.OnFocusChangeListener
            @SuppressLint({"ResourceType"})
            public void onFocusChange(View view, boolean z) {
                int i2;
                LinearLayout linearLayout;
                if (z) {
                    View view2 = this.f25805b;
                    i2 = R.drawable.blue_btn_effect;
                    if (view2 == null || view2.getTag() == null || !this.f25805b.getTag().equals("1")) {
                        View view3 = this.f25805b;
                        if (view3 == null || view3.getTag() == null || !this.f25805b.getTag().equals("2")) {
                            return;
                        }
                        linearLayout = i.this.f25803f;
                    } else {
                        linearLayout = i.this.f25802e;
                    }
                } else {
                    View view4 = this.f25805b;
                    i2 = R.drawable.black_button_dark;
                    if (view4 == null || view4.getTag() == null || !this.f25805b.getTag().equals("1")) {
                        View view5 = this.f25805b;
                        if (view5 == null || view5.getTag() == null || !this.f25805b.getTag().equals("2")) {
                            return;
                        }
                        linearLayout = i.this.f25803f;
                    } else {
                        linearLayout = i.this.f25802e;
                    }
                }
                linearLayout.setBackgroundResource(i2);
            }
        }

        public i(Activity activity) {
            super(activity);
            this.f25799b = activity;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            int id = view.getId();
            try {
                if (id == R.id.btn_close) {
                    dismiss();
                } else {
                    if (id != R.id.btn_try_again) {
                        return;
                    }
                    dismiss();
                    LoginActivity.this.Y0();
                }
            } catch (Exception unused) {
            }
        }

        @Override // android.app.Dialog
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            setContentView(LoginActivity.this.v0.A().equals(c.h.a.h.n.a.s0) ? R.layout.custom_internet_not_working_layout_tv : R.layout.custom_internet_not_working_layout);
            this.f25800c = (TextView) findViewById(R.id.btn_try_again);
            this.f25801d = (TextView) findViewById(R.id.btn_close);
            this.f25802e = (LinearLayout) findViewById(R.id.ll_yes_button_main_layout);
            this.f25803f = (LinearLayout) findViewById(R.id.ll_no_button_main_layout);
            this.f25800c.setOnClickListener(this);
            this.f25801d.setOnClickListener(this);
            TextView textView = this.f25800c;
            textView.setOnFocusChangeListener(new a(textView));
            TextView textView2 = this.f25801d;
            textView2.setOnFocusChangeListener(new a(textView2));
        }
    }

    @SuppressLint({"StaticFieldLeak"})
    public class j extends AsyncTask<Void, Boolean, Boolean> {
        public j() {
        }

        @Override // android.os.AsyncTask
        /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
        public Boolean doInBackground(Void... voidArr) {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://www.google.com").openConnection();
                httpURLConnection.setRequestMethod("HEAD");
                httpURLConnection.setReadTimeout(1500);
                httpURLConnection.setConnectTimeout(1500);
                return Boolean.valueOf(httpURLConnection.getResponseCode() == 200);
            } catch (Exception e2) {
                Log.e("LOG_TAG", e2.toString());
                return Boolean.FALSE;
            }
        }

        @Override // android.os.AsyncTask
        /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
        public void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            if (!bool.booleanValue()) {
                LoginActivity.this.b();
                LoginActivity loginActivity = LoginActivity.this;
                i iVar = loginActivity.new i((Activity) loginActivity.f25788l);
                iVar.setCancelable(false);
                iVar.show();
                return;
            }
            if (!c.h.a.h.n.a.f17044h.booleanValue()) {
                LoginActivity.this.A0();
                return;
            }
            LoginActivity loginActivity2 = LoginActivity.this;
            loginActivity2.u0 = new c.h.a.j.a(loginActivity2, loginActivity2.f25788l);
            LoginActivity.this.u0.a(LoginActivity.this.f25789m);
        }
    }

    public class k implements View.OnFocusChangeListener {

        /* JADX INFO: renamed from: b, reason: collision with root package name */
        public final View f25808b;

        public k(View view) {
            this.f25808b = view;
        }

        public final void a(boolean z) {
            if (z) {
                ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.f25808b, "alpha", z ? 0.6f : 0.5f);
                objectAnimatorOfFloat.setDuration(150L);
                objectAnimatorOfFloat.start();
            }
        }

        public final void b(float f2) {
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.f25808b, "scaleX", f2);
            objectAnimatorOfFloat.setDuration(150L);
            objectAnimatorOfFloat.start();
        }

        public final void c(float f2) {
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.f25808b, "scaleY", f2);
            objectAnimatorOfFloat.setDuration(150L);
            objectAnimatorOfFloat.start();
        }

        @Override // android.view.View.OnFocusChangeListener
        @SuppressLint({"ResourceType"})
        public void onFocusChange(View view, boolean z) {
            float f2;
            LoginActivity loginActivity;
            TextView textView;
            EditText editText;
            int length;
            if (!z) {
                if (z) {
                    return;
                }
                f2 = z ? 1.09f : 1.0f;
                b(f2);
                c(f2);
                a(z);
                View view2 = this.f25808b;
                if (view2 == null || view2.getTag() == null || !this.f25808b.getTag().equals("rl_list_users")) {
                    View view3 = this.f25808b;
                    if (view3 == null || view3.getTag() == null || !this.f25808b.getTag().equals("rl_connect_vpn")) {
                        View view4 = this.f25808b;
                        if (view4 == null || view4.getTag() == null || !this.f25808b.getTag().equals("rl_bt_submit")) {
                            return;
                        }
                        LoginActivity.this.rl_bt_submit.setBackgroundResource(R.drawable.box_unfocused);
                        loginActivity = LoginActivity.this;
                        textView = loginActivity.tv_add_user;
                    } else {
                        LoginActivity.this.rl_connect_vpn.setBackgroundResource(R.drawable.login_btn_unfocused);
                        LoginActivity.this.iv_connect_vpn.setImageResource(R.drawable.login_icon1_unfocused);
                        loginActivity = LoginActivity.this;
                        textView = loginActivity.tv_vpn_con;
                    }
                } else {
                    LoginActivity.this.rl_list_users.setBackgroundResource(R.drawable.login_btn_unfocused);
                    LoginActivity.this.iv_list_users.setImageResource(R.drawable.login_icon2_unfocused);
                    loginActivity = LoginActivity.this;
                    textView = loginActivity.tv_list_users;
                }
                textView.setTextColor(loginActivity.f25788l.getResources().getColor(R.color.black));
                return;
            }
            f2 = z ? 1.15f : 1.0f;
            try {
                Log.e("id is", BuildConfig.FLAVOR + this.f25808b.getTag());
                if (this.f25808b.getTag().equals("1")) {
                    editText = LoginActivity.this.f25781e;
                    length = editText.length();
                } else if (this.f25808b.getTag().equals("2")) {
                    editText = LoginActivity.this.f25782f;
                    length = editText.length();
                } else {
                    if (!this.f25808b.getTag().equals("3")) {
                        if (this.f25808b.getTag().equals("rl_list_users")) {
                            LoginActivity.this.rl_list_users.setBackgroundResource(R.drawable.login_btn_focused);
                            LoginActivity.this.iv_list_users.setImageResource(R.drawable.login_icon2_focused);
                            LoginActivity loginActivity2 = LoginActivity.this;
                            loginActivity2.tv_list_users.setTextColor(loginActivity2.f25788l.getResources().getColor(R.color.white));
                            b(f2);
                        } else {
                            if (!this.f25808b.getTag().equals("rl_connect_vpn")) {
                                if (this.f25808b.getTag().equals("rl_bt_submit")) {
                                    LoginActivity.this.rl_bt_submit.setBackgroundResource(R.drawable.box_focused);
                                    LoginActivity loginActivity3 = LoginActivity.this;
                                    loginActivity3.tv_add_user.setTextColor(loginActivity3.f25788l.getResources().getColor(R.color.white));
                                    return;
                                }
                                return;
                            }
                            LoginActivity.this.rl_connect_vpn.setBackgroundResource(R.drawable.login_btn_focused);
                            LoginActivity.this.iv_connect_vpn.setImageResource(R.drawable.login_icon1_focused);
                            LoginActivity loginActivity4 = LoginActivity.this;
                            loginActivity4.tv_vpn_con.setTextColor(loginActivity4.f25788l.getResources().getColor(R.color.white));
                            b(f2);
                        }
                        c(f2);
                        return;
                    }
                    editText = LoginActivity.this.f25783g;
                    length = editText.length();
                }
                editText.setSelection(length);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public static String L0(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] charArray = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        boolean z = true;
        for (char c2 : charArray) {
            if (z && Character.isLetter(c2)) {
                sb.append(Character.toUpperCase(c2));
                z = false;
            } else {
                if (Character.isWhitespace(c2)) {
                    z = true;
                }
                sb.append(c2);
            }
        }
        return sb.toString();
    }

    public static String Q0() {
        String str = Build.MANUFACTURER;
        String str2 = Build.MODEL;
        if (str2.startsWith(str)) {
            return L0(str2);
        }
        return L0(str) + " " + str2;
    }

    public static String T0(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes());
            byte[] bArrDigest = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b2 : bArrDigest) {
                String hexString = Integer.toHexString(b2 & 255);
                while (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }
                sb.append(hexString);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
            return BuildConfig.FLAVOR;
        }
    }

    public void A0() {
        this.n0 = T0(c.h.a.f.f.c(this) + "*" + c.h.a.f.f.d(this) + "-" + this.f25789m + "-" + c.h.a.f.b.f16735b + "-" + this.q0 + "-unknown-" + Q0() + "-" + this.p0);
        ArrayList arrayList = new ArrayList();
        c.h.a.f.g.f16757a = arrayList;
        arrayList.add(c.h.a.f.g.a("m", "gu"));
        c.h.a.f.g.f16757a.add(c.h.a.f.g.a("k", c.h.a.f.f.c(this)));
        c.h.a.f.g.f16757a.add(c.h.a.f.g.a("sc", this.n0));
        c.h.a.f.g.f16757a.add(c.h.a.f.g.a("u", this.f25789m));
        c.h.a.f.g.f16757a.add(c.h.a.f.g.a("pw", "no_password"));
        c.h.a.f.g.f16757a.add(c.h.a.f.g.a("r", c.h.a.f.b.f16735b));
        c.h.a.f.g.f16757a.add(c.h.a.f.g.a("av", this.q0));
        c.h.a.f.g.f16757a.add(c.h.a.f.g.a("dt", "unknown"));
        c.h.a.f.g.f16757a.add(c.h.a.f.g.a("d", Q0()));
        c.h.a.f.g.f16757a.add(c.h.a.f.g.a("do", this.p0));
        c.h.a.f.g.f16758b.b(this);
    }

    /* JADX WARN: Code restructure failed: missing block: B:53:0x037c, code lost:
    
        if (c.h.a.h.n.a.f17038b.booleanValue() != false) goto L68;
     */
    /* JADX WARN: Code restructure failed: missing block: B:67:0x03e5, code lost:
    
        if (c.h.a.h.n.a.f17038b.booleanValue() != false) goto L68;
     */
    /* JADX WARN: Code restructure failed: missing block: B:68:0x03e7, code lost:
    
        r0 = getResources().getString(com.nst.iptvsmarterstvbox.R.string.invalid_detail);
     */
    /* JADX WARN: Code restructure failed: missing block: B:70:0x03f8, code lost:
    
        c.h.a.h.n.e.l0(r18.f25788l, "Your Account is invalid or has expired !");
     */
    /* JADX WARN: Code restructure failed: missing block: B:71:0x03fd, code lost:
    
        return;
     */
    @Override // c.h.a.k.f.f
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void D(com.nst.iptvsmarterstvbox.model.callback.LoginCallback r19, java.lang.String r20, java.util.ArrayList<java.lang.String> r21) {
        /*
            Method dump skipped, instruction units count: 1022
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nst.iptvsmarterstvbox.view.activity.LoginActivity.D(com.nst.iptvsmarterstvbox.model.callback.LoginCallback, java.lang.String, java.util.ArrayList):void");
    }

    @Override // c.h.a.k.f.a
    public void F(ActivationCallBack activationCallBack, String str) {
        A0();
    }

    @Override // c.h.a.k.f.f
    public void I(String str) {
        ProgressDialog progressDialog = this.p;
        if (progressDialog != null) {
            progressDialog.dismiss();
            Toast.makeText(this, this.f25788l.getResources().getString(R.string.invalid_server_url), 0).show();
        }
    }

    public void K0() {
        try {
            this.q0 = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e2) {
            e2.printStackTrace();
        }
    }

    public final void M0() {
        Window window = getWindow();
        int i2 = Build.VERSION.SDK_INT;
        if (i2 >= 19) {
            window.clearFlags(67108864);
        }
        if (i2 >= 21) {
            window.addFlags(Integer.MIN_VALUE);
        }
        if (i2 >= 21) {
            window.setStatusBarColor(a.i.i.b.d(this, R.color.colorPrimaryDark));
        }
    }

    public boolean N0() {
        EditText editText;
        Resources resources;
        int i2;
        EditText editText2;
        Resources resources2;
        int i3;
        if (c.h.a.h.n.a.f17040d.booleanValue() && this.f25784h.getText().toString().trim().length() == 0) {
            this.f25784h.requestFocus();
            editText = this.f25784h;
            resources = getResources();
            i2 = R.string.enter_any_name;
        } else {
            if (this.f25781e.getText().toString().trim().length() == 0) {
                this.f25781e.requestFocus();
                if (c.h.a.h.n.a.f17044h.booleanValue()) {
                    editText2 = this.f25781e;
                    resources2 = getResources();
                    i3 = R.string.enter_act_code;
                } else {
                    editText2 = this.f25781e;
                    resources2 = getResources();
                    i3 = R.string.enter_username_error;
                }
                editText2.setError(resources2.getString(i3));
                return false;
            }
            if (!c.h.a.h.n.a.f17044h.booleanValue() && this.f25782f.getText().toString().trim().length() == 0) {
                this.f25782f.requestFocus();
                editText = this.f25782f;
                resources = getResources();
                i2 = R.string.enter_password_error;
            } else {
                if (!c.h.a.h.n.a.M.booleanValue() || this.f25783g.getText().toString().trim().length() != 0) {
                    return true;
                }
                this.f25783g.requestFocus();
                editText = this.f25783g;
                resources = getResources();
                i2 = R.string.enter_server_url_error;
            }
        }
        editText.setError(resources.getString(i2));
        return false;
    }

    @Override // c.h.a.k.f.f
    public void O(ArrayList<String> arrayList, String str) {
        if (arrayList == null || arrayList.size() <= 0) {
            b();
            if (c.h.a.h.n.a.f17038b.booleanValue()) {
                Toast.makeText(this, getResources().getString(R.string.invalid_detail), 0).show();
                return;
            } else {
                c.h.a.h.n.e.l0(this.f25788l, "Your Account is invalid or has expired !");
                return;
            }
        }
        try {
            this.x.putString(c.h.a.h.n.a.t, arrayList.get(0).trim());
            this.x.apply();
            arrayList.remove(0);
            this.f25787k.h(this.f25789m, this.f25790n, arrayList);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    @SuppressLint({"ResourceType"})
    public void O0() {
        Button button;
        int i2;
        V0();
        this.rl_connect_vpn.setOnClickListener(new b());
        this.rl_bt_submit.setOnClickListener(new c());
        this.rl_list_users.setOnClickListener(new d());
        if (c.h.a.h.n.a.f17043g.booleanValue()) {
            button = this.btn_free_trail;
            i2 = 0;
        } else {
            button = this.btn_free_trail;
            i2 = 8;
        }
        button.setVisibility(i2);
        this.btn_free_trail.setOnClickListener(new e());
        this.link_transform.setOnClickListener(new f());
    }

    public final String P0() {
        return c.h.a.h.n.e.Q(Calendar.getInstance().getTime().toString());
    }

    public final void R0() {
        RelativeLayout relativeLayout;
        RelativeLayout relativeLayout2;
        ProgressDialog progressDialog;
        String string;
        try {
            this.f25788l = this;
            this.N = new c.h.a.i.q.g(this.f25788l);
            this.y = new c.h.a.i.q.f(this.f25788l);
            if (c.h.a.h.n.a.f17045i.booleanValue()) {
                this.link_transform.setVisibility(0);
            }
            if (c.h.a.h.n.a.f17040d.booleanValue()) {
                this.link_transform.setNextFocusLeftId(R.id.rl_list_users);
            } else {
                this.link_transform.setNextFocusLeftId(R.id.rl_connect_vpn);
            }
            if (c.h.a.h.n.a.f17044h.booleanValue()) {
                this.f25781e.setHint((CharSequence) null);
                this.f25781e.setHint(BuildConfig.FLAVOR);
                this.link_transform.setText(getResources().getString(R.string.with_act_code));
                this.loginTV.setText(getResources().getString(R.string.enter_act_code));
                this.rl_server_url.setVisibility(8);
                this.password_full.setVisibility(8);
                this.f25781e.setVisibility(8);
                this.f25781e.setVisibility(0);
                this.f25781e.setHint(getResources().getString(R.string.act_code));
                this.tv_add_user.setText(getResources().getString(R.string.code_act));
                if (c.h.a.h.n.a.f17040d.booleanValue()) {
                    this.rl_name.setVisibility(0);
                } else {
                    this.rl_name.setVisibility(8);
                }
                if ((c.h.a.h.n.a.f17039c.booleanValue() && c.h.a.h.n.a.f17040d.booleanValue()) || c.h.a.h.n.a.f17041e.booleanValue()) {
                    relativeLayout2 = this.rl_list_users;
                    relativeLayout2.setVisibility(0);
                } else {
                    relativeLayout = this.rl_list_users;
                    relativeLayout.setVisibility(8);
                }
            } else {
                if (c.h.a.h.n.a.f17040d.booleanValue()) {
                    this.f25781e.setHint((CharSequence) null);
                    this.f25781e.setHint(BuildConfig.FLAVOR);
                    this.link_transform.setText(getResources().getString(R.string.with_user_pass));
                    this.tv_add_user.setText(getResources().getString(R.string.add_user));
                    this.f25781e.setVisibility(8);
                    this.f25781e.setVisibility(0);
                    this.f25781e.setHint(getResources().getString(R.string.username));
                    this.password_full.setVisibility(0);
                    this.rl_name.setVisibility(0);
                    if (c.h.a.h.n.a.M.booleanValue()) {
                        this.rl_server_url.setVisibility(0);
                    } else {
                        this.rl_server_url.setVisibility(8);
                    }
                    if ((c.h.a.h.n.a.f17039c.booleanValue() && c.h.a.h.n.a.f17040d.booleanValue()) || c.h.a.h.n.a.f17041e.booleanValue()) {
                        relativeLayout2 = this.rl_list_users;
                        relativeLayout2.setVisibility(0);
                    } else {
                        relativeLayout = this.rl_list_users;
                    }
                } else {
                    this.f25781e.setHint((CharSequence) null);
                    this.f25781e.setHint(BuildConfig.FLAVOR);
                    this.link_transform.setText(getResources().getString(R.string.with_user_pass));
                    this.loginTV.setText(getResources().getString(R.string.credential_detail));
                    this.f25781e.setVisibility(8);
                    this.f25781e.setVisibility(0);
                    this.password_full.setVisibility(0);
                    this.f25781e.setHint(getResources().getString(R.string.username));
                    this.tv_add_user.setText(getResources().getString(R.string.submit));
                    this.rl_server_url.setVisibility(8);
                    this.rl_name.setVisibility(8);
                    relativeLayout = this.rl_list_users;
                }
                relativeLayout.setVisibility(8);
            }
            this.f25784h.setError(null);
            this.f25781e.setError(null);
            this.f25782f.setError(null);
            this.O = new c.h.a.i.q.a(this.f25788l);
            if (this.f25788l != null) {
                this.p = new ProgressDialog(this.f25788l);
                String str = this.t0;
                if (str != null && str.equalsIgnoreCase("login_perform")) {
                    progressDialog = this.p;
                    string = "Auto Login";
                } else if (c.h.a.h.n.a.f17044h.booleanValue()) {
                    this.p.setMessage(BuildConfig.FLAVOR);
                    progressDialog = this.p;
                    string = getResources().getString(R.string.please_wait_act);
                } else {
                    this.p.setMessage(BuildConfig.FLAVOR);
                    progressDialog = this.p;
                    string = getResources().getString(R.string.please_wait);
                }
                progressDialog.setMessage(string);
                this.p.setCanceledOnTouchOutside(false);
                this.p.setCancelable(false);
                this.p.setProgressStyle(0);
            }
            this.f25789m = this.f25781e.getText().toString();
            this.f25790n = this.f25782f.getText().toString();
            this.f25787k = new c.h.a.j.c(this, this.f25788l);
            this.q = getSharedPreferences("sharedPreference", 0);
            this.t = getSharedPreferences("loginPrefs", 0);
            this.u = getSharedPreferences("selected_language", 0);
            SharedPreferences sharedPreferences = getSharedPreferences("loginPrefsserverurl", 0);
            this.w = sharedPreferences;
            this.x = sharedPreferences.edit();
            SharedPreferences sharedPreferences2 = getSharedPreferences("upgradeDatePref", 0);
            this.K = sharedPreferences2;
            this.J = sharedPreferences2.edit();
            this.v = this.s.edit();
            this.r = this.q.edit();
            SharedPreferences sharedPreferences3 = getSharedPreferences("multiDNS", 0);
            this.Y = sharedPreferences3;
            this.Z = sharedPreferences3.edit();
            SharedPreferences sharedPreferences4 = getSharedPreferences("multiDNSValid", 0);
            this.f0 = sharedPreferences4;
            this.g0 = sharedPreferences4.edit();
            this.h0 = getSharedPreferences("serverUrlDNS", 0);
            if (c.h.a.h.n.a.f17040d.booleanValue()) {
                this.f25781e.setText(BuildConfig.FLAVOR);
                this.f25782f.setText(BuildConfig.FLAVOR);
            }
            S0();
        } catch (Resources.NotFoundException e2) {
            e2.printStackTrace();
        }
    }

    public void S0() {
        EditText editText;
        try {
            if (c.h.a.h.n.a.f17040d.booleanValue()) {
                this.f25784h.requestFocus();
                editText = this.f25784h;
            } else {
                this.f25781e.requestFocus();
                editText = this.f25781e;
            }
            editText.requestFocusFromTouch();
            b();
        } catch (Exception unused) {
        }
    }

    @Override // c.h.a.f.c
    /* JADX INFO: renamed from: U0, reason: merged with bridge method [inline-methods] */
    public void d0(String str, int i2, boolean z) {
        String strTrim;
        if (!z) {
            b();
            Toast.makeText(this, this.f25788l.getResources().getString(R.string.could_not_connect), 0).show();
            return;
        }
        if (i2 == 1) {
            try {
                JSONObject jSONObject = new JSONObject(str);
                c.h.a.f.b.f16734a = jSONObject;
                if (!jSONObject.getString("status").equalsIgnoreCase("true")) {
                    b();
                    Toast.makeText(this, this.f25788l.getResources().getString(R.string.status_suspend), 0).show();
                    return;
                }
                this.U = c.h.a.f.b.f16734a.getString("su");
                this.V = c.h.a.f.b.f16734a.getString("ndd");
                this.W = System.currentTimeMillis();
                try {
                    if (c.h.a.h.n.a.f17044h.booleanValue()) {
                        this.f25789m = m.A(this.f25788l);
                        strTrim = m.B(this.f25788l);
                    } else {
                        this.f25789m = this.f25781e.getText().toString().trim();
                        strTrim = this.f25782f.getText().toString().trim();
                    }
                    this.f25790n = strTrim;
                    c.h.a.f.f.e(this, c.h.a.f.b.f16734a.optString("su"));
                    this.o0 = T0(c.h.a.f.b.f16734a.optString("su") + "*" + c.h.a.f.f.d(this) + "*" + c.h.a.f.b.f16735b);
                    if (!c.h.a.f.b.f16734a.getString("sc").equalsIgnoreCase(this.o0)) {
                        b();
                        Toast.makeText(this, this.f25788l.getResources().getString(R.string.could_not_connect), 0).show();
                        return;
                    }
                    this.x.putString(c.h.a.h.n.a.t, c.h.a.f.f.a(this));
                    this.x.apply();
                    this.r.putString(c.h.a.h.n.a.t, c.h.a.f.f.a(this));
                    this.r.putString("username", this.f25789m);
                    this.r.apply();
                    if (c.h.a.h.n.a.f17040d.booleanValue()) {
                        X0(this.U.toLowerCase());
                    } else {
                        this.f25787k.g(this.f25789m, this.f25790n);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            } catch (Exception e3) {
                Log.e("Login check", e3.getMessage());
            }
        }
    }

    @SuppressLint({"ResourceType"})
    public final void V0() {
        EditText editText;
        this.f25784h = new EditText(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        this.f25784h.setPaddingRelative(35, 0, 35, 0);
        this.f25784h.setLayoutParams(layoutParams);
        this.f25784h.setHint(getResources().getString(R.string.your_name));
        this.f25784h.setHintTextColor(getResources().getColor(R.color.white));
        this.f25784h.setHintTextColor(-1);
        if (c.h.a.h.n.a.f17040d.booleanValue()) {
            this.f25784h.setNextFocusLeftId(R.id.rl_list_users);
        } else {
            this.f25784h.setNextFocusLeftId(R.id.rl_connect_vpn);
        }
        this.f25784h.setTextSize(22.0f);
        this.f25784h.setId(101);
        this.f25784h.setBackground(getResources().getDrawable(R.drawable.selector_login_fields));
        this.f25784h.setFocusable(true);
        this.f25784h.setTypeface(Typeface.SANS_SERIF);
        this.f25784h.setInputType(161);
        this.rl_name.addView(this.f25784h);
        this.f25781e = new EditText(this);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -1);
        this.f25781e.setPaddingRelative(35, 0, 35, 0);
        this.f25781e.setLayoutParams(layoutParams2);
        if (c.h.a.h.n.a.f17045i.booleanValue()) {
            this.linearLayout.setGravity(16);
        }
        this.f25781e.setHint(getResources().getString(R.string.username));
        this.f25781e.setHintTextColor(getResources().getColor(R.color.white));
        this.f25781e.setHintTextColor(-1);
        this.f25781e.setTextSize(22.0f);
        this.f25781e.setId(102);
        if (c.h.a.h.n.a.f17040d.booleanValue()) {
            this.f25781e.setNextFocusLeftId(R.id.rl_list_users);
        } else {
            this.f25781e.setNextFocusLeftId(R.id.rl_connect_vpn);
        }
        this.f25781e.setFocusable(true);
        this.f25781e.setBackground(getResources().getDrawable(R.drawable.selector_login_fields));
        this.f25781e.setTypeface(Typeface.SANS_SERIF);
        this.f25781e.setInputType(161);
        this.rl_email.addView(this.f25781e);
        this.f25782f = new EditText(this);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -1);
        this.f25782f.setPaddingRelative(35, 0, 35, 0);
        this.f25782f.setLayoutParams(layoutParams3);
        this.f25782f.setHint(getResources().getString(R.string.password));
        this.f25782f.setHintTextColor(getResources().getColor(R.color.white));
        this.f25782f.setHintTextColor(-1);
        this.f25782f.setTextSize(22.0f);
        this.f25782f.setId(103);
        if (c.h.a.h.n.a.f17040d.booleanValue()) {
            this.f25782f.setNextFocusLeftId(R.id.rl_list_users);
        } else {
            this.f25782f.setNextFocusLeftId(R.id.rl_connect_vpn);
        }
        this.f25782f.setBackground(getResources().getDrawable(R.drawable.selector_login_fields));
        this.f25782f.setFocusable(true);
        this.f25782f.setTypeface(Typeface.SANS_SERIF);
        this.f25782f.setInputType(129);
        this.rl_password.addView(this.f25782f);
        this.eyepass.setId(110);
        this.eyepass.setFocusable(true);
        this.f25782f.setNextFocusDownId(104);
        this.f25782f.setNextFocusUpId(102);
        this.eyepass.setNextFocusDownId(104);
        this.eyepass.setNextFocusUpId(102);
        this.eyepass.setBackground(getResources().getDrawable(R.drawable.selector_login_fields));
        this.eyepass.setOnClickListener(new g());
        if (c.h.a.h.n.a.M.booleanValue()) {
            this.f25783g = new EditText(this);
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -1);
            this.f25783g.setPaddingRelative(35, 0, 35, 0);
            this.f25783g.setLayoutParams(layoutParams4);
            this.f25783g.setHint(getResources().getString(R.string.serverurl));
            this.f25783g.setHintTextColor(getResources().getColor(R.color.white));
            this.f25783g.setHintTextColor(-1);
            this.f25783g.setTextSize(22.0f);
            this.f25783g.setId(104);
            this.f25783g.setBackground(getResources().getDrawable(R.drawable.selector_login_fields));
            this.f25783g.setFocusable(true);
            this.f25783g.setTypeface(Typeface.SANS_SERIF);
            this.f25783g.setInputType(161);
            this.rl_server_url.addView(this.f25783g);
        }
        if (c.h.a.h.n.a.f17040d.booleanValue()) {
            this.f25784h.requestFocus();
            editText = this.f25784h;
        } else {
            this.f25781e.requestFocus();
            editText = this.f25781e;
        }
        editText.requestFocusFromTouch();
    }

    public final void W0() {
        m.N("api", this.f25788l);
        startActivity(new Intent(this, (Class<?>) MultiUserActivity.class));
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
        finish();
    }

    @Override // c.h.a.k.f.f
    public void X(String str) {
        b();
        if (!str.equals(BuildConfig.FLAVOR)) {
            c.h.a.h.n.e.l0(this.f25788l, str);
        } else if (c.h.a.h.n.a.f17038b.booleanValue()) {
            Toast.makeText(this, getResources().getString(R.string.invalid_detail), 0).show();
        } else {
            c.h.a.h.n.e.l0(this.f25788l, "Your Account is invalid or has expired !");
        }
    }

    public final void X0(String str) {
        if (str != null && !str.equals(BuildConfig.FLAVOR) && !str.isEmpty()) {
            this.j0 = new ArrayList<>(Arrays.asList(str.split(",")));
        }
        ArrayList<String> arrayList = this.j0;
        if (arrayList == null || arrayList.size() < 1) {
            ArrayList<String> arrayList2 = this.j0;
            if (arrayList2 == null || arrayList2.size() != 0) {
                return;
            }
            b();
            Toast.makeText(this, this.f25788l.getResources().getString(R.string.please_check_portal), 0).show();
            return;
        }
        try {
            this.x.putString(c.h.a.h.n.a.t, this.j0.get(0).trim());
            this.x.commit();
            this.j0.remove(0);
            this.f25787k.h(this.f25789m, this.f25790n, this.j0);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public final void Y0() {
        c.h.a.j.c cVar;
        String str;
        this.f25789m = this.f25781e.getText().toString().trim();
        this.f25790n = this.f25782f.getText().toString().trim();
        this.X = this.f25784h.getText().toString().trim();
        this.x = this.w.edit();
        if (c.h.a.h.n.a.f17044h.booleanValue() || !N0()) {
            if (c.h.a.h.n.a.f17044h.booleanValue() && N0()) {
                m.J(this.f25788l, this.f25789m);
                a();
                this.X = this.f25784h.getText().toString().trim();
                new j().execute(new Void[0]);
                return;
            }
            return;
        }
        a();
        try {
            if (c.h.a.h.n.a.f17038b.booleanValue()) {
                this.x.putString(c.h.a.h.n.a.t, "http://qqtv.nl");
                this.x.apply();
                this.r.putString(c.h.a.h.n.a.t, "http://qqtv.nl");
                this.r.putString("username", this.f25789m);
                this.r.apply();
                if (c.h.a.h.n.a.f17040d.booleanValue()) {
                    X0("http://qqtv.nl");
                } else {
                    cVar = this.f25787k;
                    str = this.f25789m;
                    cVar.g(str, this.f25790n);
                }
            } else if (c.h.a.h.n.a.M.booleanValue()) {
                String lowerCase = this.f25783g.getText().toString().trim().toLowerCase();
                this.f25791o = lowerCase;
                this.x.putString(c.h.a.h.n.a.t, lowerCase);
                this.x.apply();
                this.r.putString(c.h.a.h.n.a.t, this.f25791o);
                this.r.apply();
                cVar = this.f25787k;
                str = this.f25789m;
                cVar.g(str, this.f25790n);
            } else {
                new j().execute(new Void[0]);
            }
        } catch (Exception unused) {
        }
        this.v.putString("username", this.f25789m);
        this.v.putString("password", this.f25790n);
        this.v.putString("activationCode", BuildConfig.FLAVOR);
        this.v.putString("loginWith", "loginWithDetails");
        this.v.apply();
        this.x.apply();
    }

    @Override // c.h.a.k.f.b
    public void a() {
        ProgressDialog progressDialog = this.p;
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    @Override // c.h.a.k.f.b
    public void b() {
        try {
            ProgressDialog progressDialog = this.p;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception unused) {
        }
    }

    @Override // c.h.a.k.f.b
    public void c(String str) {
        if (this.f25788l == null || str.isEmpty()) {
            return;
        }
        c.h.a.h.n.e.l0(this.f25788l, str);
    }

    @Override // c.h.a.k.f.a
    public void h0(String str) {
        b();
        if (str != null) {
            c.h.a.h.n.e.l0(this.f25788l, str);
        } else {
            c.h.a.h.n.e.l0(this.f25788l, "Your Activation code is not invalid");
        }
    }

    @Override // c.h.a.f.c
    public void n(int i2) {
        if (this.f25788l != null) {
            b();
            Toast.makeText(this, this.f25788l.getResources().getString(R.string.could_not_connect), 0).show();
        }
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        if (c.h.a.h.n.a.f17041e.booleanValue()) {
            super.onBackPressed();
        } else {
            if (this.f25786j) {
                super.onBackPressed();
                return;
            }
            this.f25786j = true;
            try {
                Toast.makeText(this, getResources().getString(R.string.press_back_to_exit), 0).show();
            } catch (Exception unused) {
            }
            new Handler().postDelayed(new h(), 2000L);
        }
    }

    @Override // a.l.d.e, androidx.activity.ComponentActivity, a.i.h.g, android.app.Activity
    @SuppressLint({"RtlHardcoded"})
    public void onCreate(Bundle bundle) {
        EditText editText;
        int i2;
        EditText editText2;
        this.f25788l = this;
        c.h.a.f.g.f16758b = new c.h.a.f.g(this);
        super.onCreate(bundle);
        c.h.a.k.d.a.a aVar = new c.h.a.k.d.a.a(this.f25788l);
        this.v0 = aVar;
        setContentView(aVar.A().equals(c.h.a.h.n.a.s0) ? R.layout.login_new_tv : R.layout.login_new);
        ButterKnife.a(this);
        if (!c.h.a.h.n.a.M.booleanValue()) {
            K0();
            y0();
            Q0();
            z0();
        }
        this.s = getSharedPreferences("sharedprefremberme", 0);
        O0();
        R0();
        M0();
        String string = this.f25788l.getSharedPreferences("selected_language", 0).getString("selected_language", "English");
        this.i0 = string;
        if (string.equalsIgnoreCase("Arabic")) {
            editText = this.f25782f;
            i2 = 21;
        } else {
            editText = this.f25782f;
            i2 = 19;
        }
        editText.setGravity(i2);
        (c.h.a.h.n.a.f17040d.booleanValue() ? this.f25784h : this.f25781e).requestFocus();
        RelativeLayout relativeLayout = this.rl_connect_vpn;
        relativeLayout.setOnFocusChangeListener(new k(relativeLayout));
        RelativeLayout relativeLayout2 = this.rl_list_users;
        relativeLayout2.setOnFocusChangeListener(new k(relativeLayout2));
        RelativeLayout relativeLayout3 = this.rl_bt_submit;
        relativeLayout3.setOnFocusChangeListener(new k(relativeLayout3));
        if (c.h.a.h.n.a.f17040d.booleanValue()) {
            this.rl_bt_submit.setNextFocusLeftId(R.id.rl_list_users);
            if (c.h.a.h.n.a.M.booleanValue() && (editText2 = this.f25783g) != null) {
                editText2.setNextFocusLeftId(R.id.rl_list_users);
            }
        } else {
            this.rl_bt_submit.setNextFocusLeftId(R.id.rl_connect_vpn);
        }
        if (c.h.a.h.n.a.f17040d.booleanValue()) {
            this.rl_connect_vpn.setNextFocusRightId(R.id.rl_list_users);
        } else {
            this.rl_connect_vpn.setNextFocusRightId(R.id.rl_bt_submit);
        }
        this.rl_list_users.setNextFocusRightId(R.id.rl_bt_submit);
        c.h.a.k.h.b.a(this.f25782f);
        this.f25781e.setFilters(new InputFilter[]{f25780d});
        String action = getIntent().getAction();
        this.t0 = action;
        if (action == null || !action.equalsIgnoreCase("login_perform")) {
            return;
        }
        this.f25781e.setText(m.A(this.f25788l));
        this.f25782f.setText(m.B(this.f25788l));
        if (c.h.a.h.n.a.f17040d.booleanValue()) {
            this.f25784h.setText("Free Trial");
        }
        this.rl_bt_submit.performClick();
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i2, KeyEvent keyEvent) {
        if (i2 != 19) {
            return super.onKeyUp(i2, keyEvent);
        }
        return true;
    }

    @Override // a.l.d.e, android.app.Activity
    public void onResume() {
        super.onResume();
        c.h.a.h.n.e.g(this.f25788l);
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
    }

    @Override // c.h.a.k.f.f
    public void p(LoginCallback loginCallback, String str) {
        int i2;
        String string;
        Context context;
        String string2;
        int i3;
        if (this.f25788l != null) {
            if (loginCallback == null || loginCallback.b() == null) {
                b();
                c(getResources().getString(R.string.invalid_server_response));
                return;
            }
            if (loginCallback.b().c().intValue() == 1) {
                String strI = loginCallback.b().i();
                if (strI.equals("Active")) {
                    String strJ = loginCallback.b().j();
                    String strH = loginCallback.b().h();
                    String strB = loginCallback.a().b();
                    String strF = loginCallback.a().f();
                    String strE = loginCallback.b().e();
                    String strF2 = loginCallback.b().f();
                    String strA = loginCallback.b().a();
                    String strD = loginCallback.b().d();
                    String strG = loginCallback.b().g();
                    List<String> listB = loginCallback.b().b();
                    String strD2 = loginCallback.a().d();
                    String strA2 = loginCallback.a().a();
                    String strC = loginCallback.a().c();
                    String strE2 = loginCallback.a().e();
                    if (listB.size() != 0) {
                        listB.get(0);
                    }
                    this.f25788l.getSharedPreferences("loginPrefsserverurl", 0).getString(c.h.a.h.n.a.t, BuildConfig.FLAVOR).toLowerCase();
                    SharedPreferences.Editor editorEdit = getSharedPreferences("loginPrefs", 0).edit();
                    editorEdit.putString("username", strJ);
                    editorEdit.putString("password", strH);
                    editorEdit.putString("serverPort", strB);
                    editorEdit.putString("serverUrl", strF);
                    editorEdit.putString("expDate", strE);
                    editorEdit.putString("isTrial", strF2);
                    editorEdit.putString("activeCons", strA);
                    editorEdit.putString("createdAt", strD);
                    editorEdit.putString("maxConnections", strG);
                    editorEdit.putString(c.h.a.h.n.a.t, strF + ":" + strB);
                    editorEdit.putString("serverProtocol", strD2);
                    editorEdit.putString("serverPortHttps", strA2);
                    editorEdit.putString("serverPortRtmp", strC);
                    editorEdit.putString("serverTimeZone", strE2);
                    editorEdit.apply();
                    this.z = this.f25788l.getSharedPreferences("allowedFormat", 0);
                    this.A = this.f25788l.getSharedPreferences("timeFormat", 0);
                    this.B = this.f25788l.getSharedPreferences("epgchannelupdate", 0);
                    this.C = this.f25788l.getSharedPreferences("automation_channels", 0);
                    this.D = this.f25788l.getSharedPreferences("automation_epg", 0);
                    this.G = this.z.edit();
                    this.E = this.A.edit();
                    this.F = this.B.edit();
                    this.H = this.C.edit();
                    this.I = this.D.edit();
                    SharedPreferences sharedPreferences = this.f25788l.getSharedPreferences("auto_start", 0);
                    this.L = sharedPreferences;
                    SharedPreferences.Editor editorEdit2 = sharedPreferences.edit();
                    this.M = editorEdit2;
                    if (editorEdit2 != null) {
                        editorEdit2.putBoolean("full_epg", true);
                        this.M.apply();
                    }
                    if (this.C.getString("automation_channels", BuildConfig.FLAVOR).equals(BuildConfig.FLAVOR)) {
                        this.H.putString("automation_channels", "checked");
                        this.H.apply();
                    }
                    if (this.D.getString("automation_epg", BuildConfig.FLAVOR).equals(BuildConfig.FLAVOR)) {
                        this.I.putString("automation_epg", "checked");
                        this.I.apply();
                    }
                    c.h.a.h.n.a.K = Boolean.FALSE;
                    if (this.z.getString("allowedFormat", BuildConfig.FLAVOR).equals(BuildConfig.FLAVOR)) {
                        this.G.putString("allowedFormat", "ts");
                        this.G.apply();
                    }
                    if (this.A.getString("timeFormat", c.h.a.h.n.a.m0).equals(BuildConfig.FLAVOR)) {
                        this.E.putString("timeFormat", c.h.a.h.n.a.m0);
                        this.E.apply();
                    }
                    if (this.B.getString("epgchannelupdate", BuildConfig.FLAVOR).equals(BuildConfig.FLAVOR)) {
                        this.F.putString("epgchannelupdate", "all");
                        this.F.apply();
                    }
                    this.N = new c.h.a.i.q.g(this.f25788l);
                    boolean zBooleanValue = c.h.a.h.n.a.M.booleanValue();
                    c.h.a.i.q.g gVar = this.N;
                    if (zBooleanValue) {
                        try {
                            if (gVar.l(this.X, strJ, strH, strF, "api", strF)) {
                                b();
                                context = this.f25788l;
                                string2 = getString(R.string.already_exist_with_name) + this.X + getString(R.string.username_with_cllon) + strJ + getString(R.string.and_portal) + c.h.a.h.n.a.H;
                                i3 = 0;
                            } else {
                                this.N.d(this.X, strJ, strH, c.h.a.h.n.a.H, strF);
                                context = this.f25788l;
                                string2 = getResources().getString(R.string.user_added);
                                i3 = 0;
                            }
                            Toast.makeText(context, string2, i3).show();
                        } catch (WindowManager.BadTokenException unused) {
                        }
                        SharedPreferences.Editor editorEdit3 = this.f25788l.getSharedPreferences("loginprefsmultiuser", 0).edit();
                        editorEdit3.putString("name", this.X);
                        editorEdit3.putString("username", strJ);
                        editorEdit3.putString("password", strH);
                        editorEdit3.putString(c.h.a.h.n.a.t, strF);
                        editorEdit3.apply();
                    } else if (!gVar.l(BuildConfig.FLAVOR, strJ, strH, strF, "api", strF)) {
                        this.N.d(BuildConfig.FLAVOR, strJ, strH, c.h.a.h.n.a.H, strF);
                    }
                    try {
                        if (Build.VERSION.SDK_INT != 25) {
                            Toast.makeText(this.f25788l, getResources().getString(R.string.logged_in), 0).show();
                        }
                    } catch (WindowManager.BadTokenException unused2) {
                    }
                    if (this.f25788l != null) {
                        m.f0(this.N.q(this.X, strJ, strH, strF, "api", strF), this.f25788l);
                    }
                    b();
                    startActivity(new Intent(this, (Class<?>) NewDashboardActivity.class));
                    finish();
                    return;
                }
                b();
                string = getResources().getString(R.string.invalid_status) + strI;
                i2 = 0;
            } else {
                i2 = 0;
                if (!str.equals("validateLogin")) {
                    return;
                }
                b();
                string = getResources().getString(R.string.invalid_details);
            }
            Toast.makeText(this, string, i2).show();
        }
    }

    @Override // c.h.a.k.f.f
    public void t(ArrayList<String> arrayList, String str) {
        if (arrayList == null || arrayList.size() <= 0) {
            b();
            if (c.h.a.h.n.a.f17038b.booleanValue()) {
                Toast.makeText(this, getResources().getString(R.string.invalid_detail), 0).show();
                return;
            } else {
                c.h.a.h.n.e.l0(this.f25788l, "Your Account is invalid or has expired !");
                return;
            }
        }
        try {
            this.x.putString(c.h.a.h.n.a.t, arrayList.get(0).trim());
            this.x.apply();
            arrayList.remove(0);
            this.f25787k.h(this.f25789m, this.f25790n, arrayList);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public void y0() {
        this.p0 = Build.VERSION.RELEASE + " " + Build.VERSION_CODES.class.getFields()[Build.VERSION.SDK_INT].getName();
    }

    public void z0() {
        int iNextInt = new Random().nextInt(8378600) + Constants.MAXIMUM_UPLOAD_PARTS;
        this.s0 = iNextInt;
        c.h.a.f.b.f16735b = String.valueOf(iNextInt);
    }
}
