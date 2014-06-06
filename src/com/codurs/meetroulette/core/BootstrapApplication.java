package com.codurs.meetroulette.core;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.FROYO;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.github.kevinsawicki.http.HttpRequest;
import com.codurs.meetroulette.core.Constants.Http;
import com.codurs.meetroulette.pusher.BootReceiver.PushAlarm;
import com.codurs.meetroulette.pusher.PusherService;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.okhttp.OkHttpClient;

import dagger.ObjectGraph;

/**
 * moowe application
 */
public class BootstrapApplication extends Application {
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";

    private final String channel = "presence-planner-channel";
    private boolean isSubscribed = false;
    private static BootstrapApplication instance;
    private SharedPreferences mPreferences;
    ObjectGraph objectGraph;

    /**
     * Create main application
     */
    public BootstrapApplication() {
        // Disable http.keepAlive on Froyo and below
        if (SDK_INT <= FROYO)
            HttpRequest.keepAlive(false);
        HttpRequest.setConnectionFactory(new OkConnectionFactory());
    }

    /**
     * Create main application
     *
     * @param context
     */
    public BootstrapApplication(final Context context) {
        this();
        attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        // Perform Injection
        objectGraph = ObjectGraph.create(getRootModule());
        objectGraph.inject(this);
        objectGraph.injectStatics();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getInstance())
                .denyCacheImageMultipleSizesInMemory().memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .discCacheSize(50 * 1024 * 1024).discCacheFileCount(100).writeDebugLogs()
                .defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);

		/*
         * only really matters the first time you launch the app after you install.. otherwise, it
		 * will start on boot
		 */
        // startPusherService();

        if (!isSubscribed) {
            subscribe();
        }
    }

    private Object getRootModule() {
        return new RootModule();
    }

    /**
     * Create main application
     *
     * @param instrumentation
     */
    public BootstrapApplication(final Instrumentation instrumentation) {
        this();
        attachBaseContext(instrumentation.getTargetContext());
    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }

    public String getUser() {
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(Constants.Auth.MOOWE_ACCOUNT_TYPE);
        if (accounts.length != 0) {
            return accounts[0].name;
        }
        return null;
    }

    public static BootstrapApplication getInstance() {
        return instance;
    }

    @SuppressWarnings("unused")
    private void startPusherService() {
        AlarmManager am = (AlarmManager) BootstrapApplication.getInstance().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(BootstrapApplication.getInstance(), PushAlarm.class);
        // wake up
        PendingIntent pendingIntent = PendingIntent.getBroadcast(BootstrapApplication.getInstance(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // stays alive
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (5 * 60 * 1000), pendingIntent);
    }

    private void subscribe() {
        Intent pusherService = new Intent(getInstance(), PusherService.class);
        pusherService.putExtra(PusherService.EXTRA_CHANNEL_NAME, channel);
        startService(pusherService);
    }

    private void unsubscribe() {
        try {
            BootstrapApplication.getInstance().stopService(
                    new Intent(BootstrapApplication.getInstance(), PusherService.class));
        } catch (Exception ex) {
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        unsubscribe();
    }

    /** Was using while service was running in a remote process, not anymore. */
	/*
	 * private void killService()
	 * {
	 * ActivityManager manager = (ActivityManager) getSherlockActivity()
	 * .getSystemService(SherlockActivity.ACTIVITY_SERVICE);
	 * List<RunningAppProcessInfo> services = manager.getRunningAppProcesses();
	 * for (RunningAppProcessInfo service : services)
	 * {
	 * if (service.processName.equals("com.codurs.meetroulette.pusher:pusher"))
	 * {
	 * int pid = service.pid;
	 * android.os.Process.killProcess(pid);
	 * }
	 * }
	 * }
	 */

    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     * Used by Volley
     *
     * @param headers (Response Headers)
     */
    public final void checkAndSaveSessionCookie(Map<String, String> headers) {
        if (headers.containsKey(SET_COOKIE_KEY) && headers.get(SET_COOKIE_KEY).startsWith(Http.SESSION_TOKEN)) {
            String cookie = headers.get(SET_COOKIE_KEY);
            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                Editor prefEditor = mPreferences.edit();
                prefEditor.putString(Http.SESSION_TOKEN, cookie);
                prefEditor.commit();
            }
        }
    }

    /**
     * Adds session cookie to headers if exists.
     * Used by Volley
     *
     * @param headers
     */
    public final void addSessionCookie(Map<String, String> headers) {
        String sessionToken = mPreferences.getString(Http.SESSION_TOKEN, "");
        if (sessionToken.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(Http.SESSION_TOKEN);
            builder.append("=");
            builder.append(sessionToken);
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, builder.toString());
        }
    }

    public class OkConnectionFactory implements HttpRequest.ConnectionFactory {
        private final OkHttpClient client;

        public OkConnectionFactory() {
            this(new OkHttpClient());
        }

        public OkConnectionFactory(OkHttpClient client) {
            if (client == null) {
                throw new NullPointerException("Client must not be null.");
            }
            this.client = client;
        }

        public HttpURLConnection create(URL url) throws IOException {
            return client.open(url);
        }

        public HttpURLConnection create(URL url, Proxy proxy) throws IOException {
            throw new UnsupportedOperationException(
                    "Per-connection proxy is not supported. Use OkHttpClient's setProxy instead.");
        }
    }

}