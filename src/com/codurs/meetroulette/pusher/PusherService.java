package com.codurs.meetroulette.pusher;

import static com.codurs.meetroulette.core.Constants.Http.APP_ID;
import static com.codurs.meetroulette.core.Constants.Http.HEADER_APP_ID;
import static com.codurs.meetroulette.core.Constants.Http.HEADER_REST_API_KEY;
import static com.codurs.meetroulette.core.Constants.Http.PUSHER_API_KEY;
import static com.codurs.meetroulette.core.Constants.Http.REST_API_KEY;
import static com.codurs.meetroulette.core.Constants.Http.URL_PUSHER_AUTH;

import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.codurs.meetroulette.core.BootstrapApplication;
import com.codurs.meetroulette.R;
import com.codurs.meetroulette.core.UserAgentProvider;
import com.codurs.meetroulette.util.Ln;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.channel.User;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

public class PusherService extends Service
{
    public static final String TAG = "PushService";

    @Inject
    UserAgentProvider userAgentProvider;

    private Pusher pusher;

    private PresenceChannel channel;

    private ConnectionState targetState = ConnectionState.CONNECTED;

    private final String defaultChannelName = "presence-planner-channel";

    private String channelName;

    private int failedConnectionAttempts = 0;

    private static int MAX_RETRIES = 10;

    private Timer connectionTimer;

    public static final String EXTRA_USERNAME = "com.codurs.meetroulette.pusher.extra.username";

    public static final String EXTRA_CHANNEL_NAME = "com.codurs.meetroulette.pusher.extra.channelName";

    public static final String EXTRA_EVENT_NAMES = "com.codurs.meetroulette.pusher.extra.eventNames";

    public static final String ACTION_USER_SUBSCRIBED = "com.codurs.meetroulette.pusher.action.USER_SUBSCRIBED";

    public static final String ACTION_USER_UNSUBSCRIBED = "com.codurs.meetroulette.pusher.action.USER_UNSUBSCRIBED";

    public static final String ACTION_EVENT_RECEIVED = "com.codurs.meetroulette.pusher.action.EVENT_RECEIVED";

    public static final String EXTRA_EVENT_NAME = "com.codurs.meetroulette.pusher.extra.eventName";

    public static final String EXTRA_EVENT_DATA = "com.codurs.meetroulette.pusher.extra.eventData";

    private PushReceiver mPushReceiver = new PushReceiver();

    private SubscriptionTask mSubscriptionTask = new SubscriptionTask();

    private EventSubscriptionTask mEventSubscriptionTask = new EventSubscriptionTask();

    private EventTriggerTask mEventTriggerTask = new EventTriggerTask();

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public void sendMessageToChannelMembers(String eventName, Object message)
    {
        if (mEventTriggerTask.getStatus() != Status.RUNNING)
        {
            mEventTriggerTask.execute(eventName, new Gson().toJson(message));
        }
    }

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder
    {
        public PusherService getService()
        {
            // Return this instance of PusherService so clients can call public methods
            return PusherService.this;
        }
    }

    Handler mEventHandler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            Intent intent = new Intent(ACTION_EVENT_RECEIVED);
            intent.putExtra(EXTRA_EVENT_NAME, msg.getData().getString(EXTRA_EVENT_NAME));
            intent.putExtra(EXTRA_EVENT_DATA, msg.getData().getString(EXTRA_EVENT_DATA));
            sendBroadcast(intent);
            return true;
        }
    });

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        BootstrapApplication.getInstance().inject(this);
        connectionTimer = new Timer();
        PusherOptions options = new PusherOptions().setEncrypted(true);
        HttpAuthorizer authorizer = new HttpAuthorizer(URL_PUSHER_AUTH);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(HEADER_REST_API_KEY, REST_API_KEY);
        headers.put(HEADER_APP_ID, APP_ID);
        authorizer.setHeaders(headers);
        HashMap<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("userName", BootstrapApplication.getInstance().getUser());
        authorizer.setQueryStringParameters(queryParams);
        options.setAuthorizer(authorizer);
        pusher = new Pusher(PUSHER_API_KEY, options);
    }

    @Override
    public void onDestroy()
    {
        Ln.d("Destroying PusherService");
        pusher.unsubscribe(channelName);
        pusher.disconnect();
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        if (intent != null)
        {
            this.channelName = intent.getExtras().getString(EXTRA_CHANNEL_NAME, defaultChannelName);
            achieveTargetConnectionState();
        }
    }

    private void subscribeToChannel()
    {
        if (mSubscriptionTask.getStatus() != Status.RUNNING)
        {
            mSubscriptionTask.execute(channelName);
        }
    }

    public void subscribeToEvents(String... events)
    {
        if (mEventSubscriptionTask.getStatus() != Status.RUNNING)
        {
            mEventSubscriptionTask.execute(events);
        }
    }

    public PresenceChannel getChannel()
    {
        return channel;
    }

    class PushReceiver implements ConnectionEventListener, ChannelEventListener, PresenceChannelEventListener,
            SubscriptionEventListener
    {
        @Override
        public void onConnectionStateChange(ConnectionStateChange change)
        {
            String message = String.format("Connection state changed from [%s] to [%s]", change.getPreviousState(),
                    change.getCurrentState());

            if (change.getCurrentState() == ConnectionState.CONNECTED && channel == null)
            {
                subscribeToChannel();
            }
            generateNotification(getApplicationContext(), TAG, message);
            achieveTargetConnectionState();
        }

        @Override
        public void onError(String message, String code, Exception e)
        {
            String msg = String.format("Connection error: [%s] [%s] [%s]", message, code, e);
            generateNotification(getApplicationContext(), TAG, msg);
        }

        @Override
        public void onEvent(String channelName, String eventName, String data)
        {
            String msg = String.format("Event received: [%s] [%s] [%s]", channelName, eventName, data);
            generateNotification(getApplicationContext(), TAG, msg);

            Message m2 = Message.obtain();
            Bundle b2 = new Bundle();
            b2.putString(EXTRA_EVENT_NAME, eventName);
            b2.putString(EXTRA_EVENT_DATA, data);
            m2.setData(b2);
            mEventHandler.sendMessage(m2);
        }

        @Override
        public void onSubscriptionSucceeded(String channelName)
        {
            String msg = String.format("Subscription succeeded for [%s]", channelName);
            generateNotification(getApplicationContext(), TAG, msg);
        }

        @Override
        public void onUsersInformationReceived(String channelName, Set<User> users)
        {
            for (User user : users)
            {
                userSubscribed(channelName, user);
            }
        }

        @Override
        public void userSubscribed(String channelName, User user)
        {
            String message = String.format("%s has joined channel [%s]: %s", user.getId(), channelName, user.getInfo());
            generateNotification(getApplicationContext(), "PusherService", message);

            if (user.equals(((PresenceChannel) channel).getMe()))
            {
                generateNotification(getApplicationContext(), "I am connected", user.getId());
            }
            else
            {
                Intent intent = new Intent(ACTION_USER_SUBSCRIBED);
                intent.putExtra(EXTRA_USERNAME, user.getId());
                sendBroadcast(intent);
            }
        }

        @Override
        public void userUnsubscribed(String channelName, User user)
        {
            String message = String.format("A user has left channel [%s]: %s %s", channelName, user.getId(), user.getInfo());
            generateNotification(getApplicationContext(), TAG, message);
            Intent intent = new Intent(ACTION_USER_UNSUBSCRIBED);
            intent.putExtra(EXTRA_USERNAME, user.getId());
            sendBroadcast(intent);
        }

        @Override
        public void onAuthenticationFailure(String message, Exception e)
        {
            generateNotification(getApplicationContext(), TAG, message);
        }
    }

    class SubscriptionTask extends AsyncTask<String, Void, Boolean>
    {
        String channelName;

        @Override
        protected Boolean doInBackground(String... params)
        {
            channelName = params[0];
            if (pusher.getConnection().getState() == ConnectionState.CONNECTED)
            {
                try
                {
                    if (channelName.startsWith("presence-") && channel == null)
                    {
                        channel = pusher.subscribePresence(channelName, mPushReceiver);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            return channel != null;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            mSubscriptionTask = new SubscriptionTask();
        }
    }

    class EventSubscriptionTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... params)
        {
            if (channel != null)
            {
                for (String eventName : params)
                {
                    channel.bind(eventName, mPushReceiver);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            mEventSubscriptionTask = new EventSubscriptionTask();
        }

    }

    class EventTriggerTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... params)
        {
            if (pusher != null && pusher.getConnection().getState() == ConnectionState.CONNECTED && channel != null)
            {
                try
                {
                    channel.trigger(params[0], params[1]);
                }
                catch (IllegalStateException e)
                {
                    Ln.e(e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            mEventTriggerTask = new EventTriggerTask();
        }

    }

    public class PusherConnectionTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            pusher.connect(mPushReceiver, ConnectionState.ALL);
            return true;
        }
    }

    final Handler handler = new Handler();

    private void achieveTargetConnectionState()
    {
        ConnectionState currentState = pusher.getConnection().getState();
        if (currentState == targetState)
        {
            // do nothing, we're there.
            failedConnectionAttempts = 0;
        }
        else if (targetState == ConnectionState.CONNECTED && failedConnectionAttempts == MAX_RETRIES)
        {
            targetState = ConnectionState.DISCONNECTED;
            String message = "failed to connect after " + failedConnectionAttempts
                    + " attempts. Reconnection attempts stopped.";
            generateNotification(getApplicationContext(), TAG, message);
        }
        else if (currentState == ConnectionState.DISCONNECTED && targetState == ConnectionState.CONNECTED
                && failedConnectionAttempts != MAX_RETRIES)
        {
            generateNotification(getApplicationContext(), "Connection attempt", "Connecting in " + failedConnectionAttempts
                    + " seconds");

            TimerTask doAsynchronousTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    handler.post(new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                PusherConnectionTask performBackgroundTask = new PusherConnectionTask();
                                performBackgroundTask.execute();
                            }
                            catch (Exception e)
                            {
                            }
                        }
                    });
                }
            };
            connectionTimer.schedule(doAsynchronousTask, 0, ++failedConnectionAttempts * 1000);
        }
        else if (currentState == ConnectionState.CONNECTED && targetState == ConnectionState.DISCONNECTED)
        {
            pusher.disconnect();
        }
    }

    private void generateNotification(Context context, String title, String message)
    {
        Ln.d(title + " - " + message);
        long when = System.currentTimeMillis();

        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context).setContentTitle(title)
                .setContentText(message).setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setWhen(when)
                .setSmallIcon(R.drawable.ic_launcher);

        int notifyID = 1;

        Notification notification = mNotifyBuilder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notifyID, notification);
    }
}