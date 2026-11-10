package org.readroid.deviceinspector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.content.pm.ServiceInfo;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Foreground MediaProjection capture plus authenticated loopback HTTP bridge for Termux. */
public final class InspectorBridgeService extends Service {
    static final String EXTRA_RESULT_CODE = "projection_result_code";
    static final String EXTRA_RESULT_DATA = "projection_result_data";
    static final String EXTRA_PAIRING_CODE = "pairing_code";
    private static final String CHANNEL_ID = "readroid_inspector_capture";
    private static final int NOTIFICATION_ID = 7291;
    private static volatile boolean running;

    private String pairingCode;
    private MediaProjection projection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private HandlerThread captureThread;
    private Handler captureHandler;
    private ServerSocket server;
    private volatile boolean stopping;
    private final Object frameLock = new Object();
    private Bitmap latestFrame;
    private long frameSequence;
    private final ExecutorService clients = Executors.newCachedThreadPool();

    static boolean isRunning() { return running; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) { stopSelf(); return START_NOT_STICKY; }
        pairingCode = intent.getStringExtra(EXTRA_PAIRING_CODE);
        startForeground(NOTIFICATION_ID, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        try {
            int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
            Intent resultData = intent.getParcelableExtra(EXTRA_RESULT_DATA);
            MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            projection = manager.getMediaProjection(resultCode, resultData);
            if (projection == null) throw new IllegalStateException("MediaProjection não autorizada");
            setupCapture();
            startHttpBridge();
            running = true;
        } catch (Exception error) {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private Notification notification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                    "Inspeção de tela ReaDroid", NotificationManager.IMPORTANCE_LOW));
        }
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, open,
                Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        return builder.setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle("ReaDroid Device Inspector")
                .setContentText("Captura de tela autorizada ativa · 127.0.0.1:8765")
                .setContentIntent(pending).setOngoing(true).build();
    }

    private void setupCapture() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(metrics);
        captureThread = new HandlerThread("ReadroidScreenCapture");
        captureThread.start(); captureHandler = new Handler(captureThread.getLooper());
        imageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels,
                PixelFormat.RGBA_8888, 3);
        imageReader.setOnImageAvailableListener(this::consumeFrame, captureHandler);
        virtualDisplay = projection.createVirtualDisplay("ReadroidDeviceInspector",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, captureHandler);
        projection.registerCallback(new MediaProjection.Callback() {
            @Override public void onStop() { stopSelf(); }
        }, captureHandler);
    }

    private void consumeFrame(ImageReader reader) {
        Image image = null;
        Bitmap padded = null;
        try {
            image = reader.acquireLatestImage();
            if (image == null) return;
            Image.Plane plane = image.getPlanes()[0];
            int pixelStride = plane.getPixelStride();
            int rowPadding = plane.getRowStride() - pixelStride * image.getWidth();
            int paddedWidth = image.getWidth() + rowPadding / pixelStride;
            padded = Bitmap.createBitmap(paddedWidth, image.getHeight(), Bitmap.Config.ARGB_8888);
            padded.copyPixelsFromBuffer(plane.getBuffer());
            Bitmap frame = Bitmap.createBitmap(padded, 0, 0, image.getWidth(), image.getHeight());
            synchronized (frameLock) {
                if (latestFrame != null) latestFrame.recycle();
                latestFrame = frame;
                frameSequence++;
                frameLock.notifyAll();
            }
        } catch (Exception ignored) {
        } finally {
            if (padded != null) padded.recycle();
            if (image != null) image.close();
        }
    }

    private byte[] capturePng() throws Exception {
        Bitmap frame;
        synchronized (frameLock) {
            long deadline = System.currentTimeMillis() + 4000;
            while (latestFrame == null && System.currentTimeMillis() < deadline) {
                frameLock.wait(Math.max(1, deadline - System.currentTimeMillis()));
            }
            if (latestFrame == null) throw new IllegalStateException("MediaProjection ainda não forneceu quadro");
            frame = latestFrame.copy(Bitmap.Config.ARGB_8888, false);
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        frame.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        frame.recycle();
        return bytes.toByteArray();
    }

    private void startHttpBridge() throws Exception {
        server = new ServerSocket();
        server.setReuseAddress(true);
        server.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8765), 12);
        Thread acceptor = new Thread(() -> {
            while (!stopping) {
                try {
                    Socket socket = server.accept();
                    clients.execute(() -> handle(socket));
                } catch (Exception error) {
                    if (!stopping) stopSelf();
                }
            }
        }, "ReadroidInspectorLoopback");
        acceptor.setDaemon(true); acceptor.start();
    }

    private void handle(Socket socket) {
        try (Socket client = socket) {
            client.setSoTimeout(6000);
            BufferedInputStream in = new BufferedInputStream(client.getInputStream());
            OutputStream out = client.getOutputStream();
            String request = readLine(in);
            if (request == null) return;
            String[] requestParts = request.split(" ", 3);
            if (requestParts.length < 2) { respond(out, 400, "application/json", jsonError("invalid request")); return; }
            Map<String, String> headers = new HashMap<>();
            String header;
            while ((header = readLine(in)) != null && !header.isEmpty()) {
                int colon = header.indexOf(':');
                if (colon > 0) headers.put(header.substring(0, colon).trim().toLowerCase(Locale.ROOT),
                        header.substring(colon + 1).trim());
            }
            int length = 0;
            try { length = Integer.parseInt(headers.getOrDefault("content-length", "0")); }
            catch (NumberFormatException ignored) { }
            if (length > 1024 * 1024) { respond(out, 413, "application/json", jsonError("payload too large")); return; }
            byte[] body = readBytes(in, length);
            URI uri = URI.create(requestParts[1]);
            if ("GET".equals(requestParts[0]) && "/health".equals(uri.getPath())) {
                respond(out, 200, "application/json", new JSONObject()
                        .put("ok", true).put("service", "readroid-device-inspector").put("port", 8765).toString());
                return;
            }
            if (!constantTimeEquals(pairingCode, headers.get("x-inspector-code"))) {
                respond(out, 401, "application/json", jsonError("pairing code rejected")); return;
            }
            if ("GET".equals(requestParts[0])) {
                switch (uri.getPath()) {
                    case "/status":
                        respond(out, 200, "application/json", new JSONObject()
                                .put("accessibility_enabled", DeviceAccessibilityService.isConnected())
                                .put("projection_active", projection != null && running)
                                .put("loopback", "127.0.0.1:8765").toString());
                        return;
                    case "/snapshot":
                        respond(out, 200, "application/json", DeviceAccessibilityService.snapshot().toString());
                        return;
                    case "/capture":
                        respond(out, 200, "image/png", capturePng());
                        return;
                    case "/events":
                        long since = parseLong(query(uri.getRawQuery()).get("since"), 0);
                        respond(out, 200, "application/json", new JSONObject()
                                .put("events", DeviceAccessibilityService.eventsSince(since)).toString());
                        return;
                    default:
                        respond(out, 404, "application/json", jsonError("unknown endpoint"));
                }
            } else if ("POST".equals(requestParts[0]) && "/action".equals(uri.getPath())) {
                JSONObject action = new JSONObject(new String(body, StandardCharsets.UTF_8));
                respond(out, 200, "application/json", perform(action).toString());
            } else {
                respond(out, 404, "application/json", jsonError("unknown endpoint"));
            }
        } catch (SecurityException error) {
            try { respond(socket.getOutputStream(), 403, "application/json", jsonError(error.getMessage())); } catch (Exception ignored) { }
        } catch (Exception error) {
            try { respond(socket.getOutputStream(), 500, "application/json", jsonError(error.getMessage())); } catch (Exception ignored) { }
        }
    }

    private JSONObject perform(JSONObject request) throws Exception {
        String type = request.optString("type", "");
        boolean ok;
        switch (type) {
            case "back": ok = DeviceAccessibilityService.back(); break;
            case "home": ok = DeviceAccessibilityService.home(); break;
            case "click": ok = DeviceAccessibilityService.click(request.getInt("index")); break;
            case "tap": ok = DeviceAccessibilityService.tap((float) request.getDouble("x"), (float) request.getDouble("y")); break;
            case "launch":
                String pkg = request.getString("package");
                android.content.Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
                if (launch == null) throw new IllegalArgumentException("No launch activity for package: " + pkg);
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launch); ok = true; break;
            default: throw new IllegalArgumentException("Unsupported action");
        }
        return new JSONObject().put("ok", ok).put("type", type);
    }

    private Map<String, String> query(String raw) throws Exception {
        Map<String, String> result = new HashMap<>();
        if (raw == null) return result;
        for (String pair : raw.split("&")) {
            String[] p = pair.split("=", 2);
            result.put(URLDecoder.decode(p[0], "UTF-8"), p.length == 2 ? URLDecoder.decode(p[1], "UTF-8") : "");
        }
        return result;
    }

    private byte[] jsonError(String message) throws Exception {
        return new JSONObject().put("ok", false).put("error", message == null ? "request failed" : message).toString()
                .getBytes(StandardCharsets.UTF_8);
    }

    private void respond(OutputStream out, int status, String type, String body) throws Exception {
        respond(out, status, type, body.getBytes(StandardCharsets.UTF_8));
    }

    private void respond(OutputStream out, int status, String type, byte[] body) throws Exception {
        String label = status == 200 ? "OK" : status == 401 ? "Unauthorized" : status == 403 ? "Forbidden"
                : status == 404 ? "Not Found" : status == 413 ? "Payload Too Large" : status == 400 ? "Bad Request" : "Error";
        String header = "HTTP/1.1 " + status + " " + label + "\r\nContent-Type: " + type
                + "\r\nContent-Length: " + body.length + "\r\nConnection: close\r\nCache-Control: no-store\r\n\r\n";
        out.write(header.getBytes(StandardCharsets.US_ASCII)); out.write(body); out.flush();
    }

    private String readLine(InputStream in) throws Exception {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\n') break;
            if (c != '\r') line.write(c);
            if (line.size() > 8192) throw new IllegalArgumentException("header line too long");
        }
        return c == -1 && line.size() == 0 ? null : line.toString("US-ASCII");
    }

    private byte[] readBytes(InputStream in, int count) throws Exception {
        byte[] result = new byte[count];
        int offset = 0;
        while (offset < count) {
            int read = in.read(result, offset, count - offset);
            if (read < 0) throw new IllegalArgumentException("incomplete request body");
            offset += read;
        }
        return result;
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) return false;
        return java.security.MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                actual.getBytes(StandardCharsets.US_ASCII));
    }

    private long parseLong(String value, long fallback) {
        try { return Long.parseLong(value); } catch (Exception ignored) { return fallback; }
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public void onDestroy() {
        stopping = true; running = false;
        if (server != null) try { server.close(); } catch (Exception ignored) { }
        clients.shutdownNow();
        if (projection != null) projection.stop();
        if (virtualDisplay != null) virtualDisplay.release();
        if (imageReader != null) imageReader.close();
        if (captureThread != null) captureThread.quitSafely();
        synchronized (frameLock) { if (latestFrame != null) { latestFrame.recycle(); latestFrame = null; } }
        stopForeground(true);
        super.onDestroy();
    }
}
