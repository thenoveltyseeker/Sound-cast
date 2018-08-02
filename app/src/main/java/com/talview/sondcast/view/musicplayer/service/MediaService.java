package com.talview.sondcast.view.musicplayer.service;

import android.media.session.MediaSession;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.talview.sondcast.view.musicplayer.SongsLibrary;
import com.talview.sondcast.view.musicplayer.managers.MusicPlaybackManager;
import com.talview.sondcast.view.musicplayer.managers.PlaybackNotificationManager;

import java.util.List;

public class MediaService extends MediaBrowserServiceCompat {
	private MediaSessionCompat mMediaSession;
	private MusicPlaybackManager mPlaybackManager;

	public final MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {
		@Override
		public void onPlayFromMediaId(String mediaId, Bundle extras) {
			MediaMetadataCompat metadata = SongsLibrary.getMetadata(mediaId);
			mMediaSession.setActive(true);
			mMediaSession.setMetadata(metadata);
			mPlaybackManager.playSong(metadata);
		}

		@Override
		public void onPlay() {
			if (mPlaybackManager.getCurrentMediaId() != null) {
				mPlaybackManager.onPlay();
			}
		}

		@Override
		public void onPause() {
			mPlaybackManager.pause();
		}

		@Override
		public void onStop() {
			stopSelf();
		}

		@Override
		public void onSkipToNext() {
			onPlayFromMediaId(SongsLibrary.getNextSong(mPlaybackManager.getCurrentMediaId()), null);
		}

		@Override
		public void onSkipToPrevious() {
			onPlayFromMediaId(SongsLibrary.getPreviousSong(mPlaybackManager.getCurrentMediaId()), null);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		// Start a new MediaSession
		mMediaSession = new MediaSessionCompat(this, "MediaService");
		mMediaSession.setCallback(mMediaSessionCallback);
		mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
				MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
		setSessionToken(mMediaSession.getSessionToken());

		PlaybackNotificationManager notificationManager = new PlaybackNotificationManager(this);
		mPlaybackManager = new MusicPlaybackManager(this, new MusicPlaybackManager.Callback() {
			@Override
			public void onPlaybackStatusChanged(PlaybackStateCompat state) {
				mMediaSession.setPlaybackState(state);
				notificationManager.updateNotification(mPlaybackManager.getCurrentMediaMetadata(), state, getSessionToken());
			}

			@Override
			public void onSongCompleted() {
				mMediaSessionCallback.onSkipToNext();
			}
		});
	}

	@Override
	public void onDestroy() {
		mPlaybackManager.stop();
		mMediaSession.release();
		mPlaybackManager = null;
		super.onDestroy();
	}

	@Override
	public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
		return new BrowserRoot(SongsLibrary.getRoot(), null);
	}

	@Override
	public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
		//Not necessary to do anything, since ours is a streaming only player.
	}
}
