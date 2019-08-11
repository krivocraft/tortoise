package ru.krivocraft.kbmp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import ru.krivocraft.kbmp.constants.Constants;

import static android.content.Context.NOTIFICATION_SERVICE;

class NotificationBuilder {

    static final int NOTIFY_ID = 124;
    private MediaPlaybackService context;
    private NotificationCompat.Action playAction;
    private NotificationCompat.Action pauseAction;
    private NotificationCompat.Action nextAction;
    private NotificationCompat.Action previousAction;
    private NotificationCompat.Action stopAction;


    NotificationBuilder(MediaPlaybackService context) {
        this.context = context;

        playAction = new NotificationCompat.Action(R.drawable.ic_play, "play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_PLAY));

        pauseAction = new NotificationCompat.Action(R.drawable.ic_pause, "pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_PAUSE));

        nextAction = new NotificationCompat.Action(R.drawable.ic_next, "next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        previousAction = new NotificationCompat.Action(R.drawable.ic_previous, "previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        stopAction = new NotificationCompat.Action(R.drawable.ic_close, "stop",
                PendingIntent.getBroadcast(context.getApplicationContext(), 228, new Intent(Constants.Actions.ACTION_REQUEST_STOP), PendingIntent.FLAG_CANCEL_CURRENT));
    }

    Notification getNotification(MediaSessionCompat mediaSession) {
        if (mediaSession.getController().getMetadata() != null) {
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, PlayerActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

            android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle mediaStyle = new android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle();
            mediaStyle.setMediaSession(mediaSession.getSessionToken());
            mediaStyle.setShowCancelButton(true);
            mediaStyle.setShowActionsInCompactView(2);


            NotificationManager service = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            String CHANNEL_ID = "channel_01";

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .addAction(stopAction)
                    .addAction(previousAction)
                    .setContentTitle(mediaSession.getController().getMetadata().getDescription().getTitle())
                    .setContentText(mediaSession.getController().getMetadata().getDescription().getSubtitle())
                    .setContentIntent(contentIntent)
                    .setSound(null)
                    .setStyle(mediaStyle)
                    .setColorized(true)
                    .setOngoing(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            Bitmap image = Utils.loadArt(mediaSession.getController().getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));

            if (image != null) {
                notificationBuilder.setLargeIcon(image);
            } else {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_track_image_default));
            }

            if (mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                notificationBuilder.addAction(pauseAction).setSmallIcon(R.drawable.ic_play);
            } else {
                notificationBuilder.addAction(playAction).setSmallIcon(R.drawable.ic_pause);
            }

            notificationBuilder.addAction(nextAction);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Tortoise", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setImportance(NotificationManager.IMPORTANCE_LOW);
                notificationBuilder.setChannelId(CHANNEL_ID);
                if (service != null) {
                    service.createNotificationChannel(channel);
                }
            }

            return notificationBuilder.build();
        }
        return null;
    }

}