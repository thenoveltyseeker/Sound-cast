package com.talview.soundcast.view.musicplayer.managers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.PlaybackState;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.talview.soundcast.R;
import com.talview.soundcast.view.musicplayer.SongsLibrary;
import com.talview.soundcast.view.musicplayer.service.MediaService;
import com.talview.soundcast.view.ui.activities.NowPlayingActivity;
import com.talview.soundcast.view.utils.Constants;

/**
 * For keeping track of the notification and will update it based on
 * MediaSession. This is required so that the music service
 * don't get killed during playback.
 *
 * @author Rolbin
 */
public class PlaybackNotificationManager extends BroadcastReceiver {
	private static final int NOTIFICATION_ID = 9 /* Cause it's my lucky number */;
	private static final int REQUEST_CODE = 100;

	private static final String ACTION_PLAY = "soundcast.play";
	private static final String ACTION_PAUSE = "soundcast.pause";
	private static final String ACTION_NEXT = "soundcast.next";
	private static final String ACTION_PREV = "soundcast.prev";

	private final NotificationCompat.Action mActionPlay;
	private final NotificationCompat.Action mActionPause;
	private final NotificationCompat.Action mActionNext;
	private final NotificationCompat.Action mActionPrev;

	private final MediaService mMediaService;
	private final NotificationManager mNotificationManager;
	private boolean isServiceStarted;

	public PlaybackNotificationManager(MediaService service) {
		mMediaService = service;
		String packageName = mMediaService.getPackageName();

		PendingIntent playIntent = PendingIntent.getBroadcast(mMediaService, REQUEST_CODE,
				new Intent(ACTION_PLAY).setPackage(packageName), PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent pauseIntent = PendingIntent.getBroadcast(mMediaService, REQUEST_CODE,
				new Intent(ACTION_PAUSE).setPackage(packageName), PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent nextIntent = PendingIntent.getBroadcast(mMediaService, REQUEST_CODE,
				new Intent(ACTION_NEXT).setPackage(packageName), PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent prevIntent = PendingIntent.getBroadcast(mMediaService, REQUEST_CODE,
				new Intent(ACTION_PREV).setPackage(packageName), PendingIntent.FLAG_CANCEL_CURRENT);

		mActionPlay = new NotificationCompat.Action(R.drawable.ic_play_arrow_white_24dp,
				mMediaService.getString(R.string.label_play), playIntent);
		mActionPause = new NotificationCompat.Action(R.drawable.ic_pause_white_24dp,
				mMediaService.getString(R.string.label_pause), pauseIntent);
		mActionNext = new NotificationCompat.Action(R.drawable.ic_skip_next_white_24dp,
				mMediaService.getString(R.string.label_next), nextIntent);
		mActionPrev = new NotificationCompat.Action(R.drawable.ic_skip_previous_white_24dp,
				mMediaService.getString(R.string.label_previous), prevIntent);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_NEXT);
		intentFilter.addAction(ACTION_PAUSE);
		intentFilter.addAction(ACTION_PLAY);
		intentFilter.addAction(ACTION_PREV);
		mMediaService.registerReceiver(this, intentFilter);

		mNotificationManager = (NotificationManager) mMediaService
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// Cancel all notifications to handle the case where the Service was killed and
		// restarted by the system.
		if (mNotificationManager != null) {
			mNotificationManager.cancelAll();
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (action == null) {
			return;
		}
		switch (action) {
			case ACTION_PLAY:
				mMediaService.mMediaSessionCallback.onPlay();
				break;
			case ACTION_PAUSE:
				mMediaService.mMediaSessionCallback.onPause();
				break;
			case ACTION_NEXT:
				mMediaService.mMediaSessionCallback.onSkipToNext();
				break;
			case ACTION_PREV:
				mMediaService.mMediaSessionCallback.onSkipToPrevious();
				break;
			default:
				break;
		}
	}

	public void updateNotification(MediaMetadataCompat metadata, PlaybackStateCompat playbackState, MediaSessionCompat.Token token) {
		if (playbackState == null || playbackState.getState() == PlaybackState.STATE_STOPPED ||
				playbackState.getState() == PlaybackState.STATE_NONE) {
			mMediaService.stopForeground(true);
			try {
				mMediaService.unregisterReceiver(this);
			} catch (IllegalArgumentException ex) {
				// ignore receiver not registered
			}
			mMediaService.stopSelf();
			return;
		}

		if (metadata == null) {
			return;
		}
		boolean isPlaying = playbackState.getState() == PlaybackState.STATE_PLAYING;

		NotificationCompat.Builder builder;
		MediaDescriptionCompat description = metadata.getDescription();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			builder = new NotificationCompat.Builder(mMediaService, createChannel());
		} else {
			//FixMe - have to use normal notification for older devices.
			builder = new NotificationCompat.Builder(mMediaService);
		}
		builder.setStyle(new MediaStyle()
				.setMediaSession(token)
				.setShowActionsInCompactView(0, 1, 2))
				.setColor(mMediaService.getApplication().getResources().getColor(R.color.notification_bg))
				.setSmallIcon(R.drawable.ic_notification)
				.setVisibility(Notification.VISIBILITY_PRIVATE)
				.setContentIntent(showNowPlaying())
				.setContentTitle(description.getTitle())
				.setContentText(description.getSubtitle())
				.setLargeIcon(SongsLibrary.getAlbumRes(description.getMediaId()))
				.setOngoing(isPlaying)
				.setWhen(isPlaying ? System.currentTimeMillis() - playbackState.getPosition() : 0)
				.setShowWhen(isPlaying)
				.setUsesChronometer(isPlaying);

		// If skip to next action is enabled
		if ((playbackState.getActions() & PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0) {
			builder.addAction(mActionPrev);
		}
		builder.addAction(isPlaying ? mActionPause : mActionPlay);
		// If skip to prev action is enabled
		if ((playbackState.getActions() & PlaybackState.ACTION_SKIP_TO_NEXT) != 0) {
			builder.addAction(mActionNext);
		}

		Notification notification = builder.build();
		if (isPlaying && !isServiceStarted) {
			mMediaService.startService(new Intent(mMediaService.getApplicationContext(), MediaService.class));
			mMediaService.startForeground(NOTIFICATION_ID, notification);
			isServiceStarted = true;
		} else {
			if (!isPlaying) {
				mMediaService.stopForeground(false);
				isServiceStarted = false;
			}
			mNotificationManager.notify(NOTIFICATION_ID, notification);
		}
	}

	/**
	 * Only for O devices and above
	 *
	 * @return channel id
	 */
	@RequiresApi(api = Build.VERSION_CODES.O)
	private String createChannel() {
		String channelId = Constants.CHANNEL_ID;
		String channelName = Constants.CHANNEL_NAME;
		NotificationChannel channel = new NotificationChannel(channelId,
				channelName, NotificationManager.IMPORTANCE_LOW);
		channel.setLightColor(ContextCompat.getColor(mMediaService.getApplicationContext(), R.color.notification_light));
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		NotificationManager notificationManager = (NotificationManager) mMediaService.getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notificationManager != null) {
			notificationManager.createNotificationChannel(channel);
		}
		return channelId;
	}

	/**
	 * Show now playing activity
	 *
	 * @return pending intent
	 */
	private PendingIntent showNowPlaying() {
		Intent nowPlaying = new Intent(mMediaService, NowPlayingActivity.class);
		nowPlaying.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return PendingIntent.getActivity(mMediaService, REQUEST_CODE, nowPlaying,
				PendingIntent.FLAG_CANCEL_CURRENT);
	}
}