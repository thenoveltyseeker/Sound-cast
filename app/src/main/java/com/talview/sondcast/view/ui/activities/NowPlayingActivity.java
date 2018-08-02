package com.talview.sondcast.view.ui.activities;

import android.content.ComponentName;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.talview.sondcast.R;
import com.talview.sondcast.service.model.SongDetails;
import com.talview.sondcast.view.musicplayer.SongsLibrary;
import com.talview.sondcast.view.musicplayer.service.MediaService;
import com.talview.sondcast.view.utils.Constants;

public class NowPlayingActivity extends AppCompatActivity implements View.OnClickListener {
	private MediaMetadataCompat mCurrentMediaMetadata;
	private PlaybackStateCompat mCurrentPlaybackState;
	private MediaControllerCompat.Callback mMediaControllerCallback;
	private MediaBrowserCompat mMediaBrowser;
	private SongDetails nowPlayingSongDetails;

	private boolean isSongDetailsUpdated;

	private ImageButton mPlayPauseBtn;
	private TextView mMusicTitle;
	private ImageView mAlbumArtImage;
	private ViewGroup mPlaybackControls;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.now_playing_layout);

		if (getIntent().getSerializableExtra(Constants.Keys.SELECTED_SONG) != null) {
			nowPlayingSongDetails = (SongDetails) getIntent().getSerializableExtra(Constants.Keys.SELECTED_SONG);
		}
		//Always keep this true, untill the updation fails.
		isSongDetailsUpdated = true;

		// Playback controls configuration:
		mPlaybackControls = findViewById(R.id.playback_controls);
		mPlayPauseBtn = findViewById(R.id.play_pause);
		ImageButton mPreviousTrackBtn = findViewById(R.id.prev);
		ImageButton mNextTrackBtn = findViewById(R.id.next);

		mPlayPauseBtn.setEnabled(true);
		mPlayPauseBtn.setOnClickListener(this);
		mPreviousTrackBtn.setOnClickListener(this);
		mNextTrackBtn.setOnClickListener(this);

		mMusicTitle = findViewById(R.id.title);
		mAlbumArtImage = findViewById(R.id.album_art);

		mMediaControllerCallback = new MediaControllerCompat.Callback() {
			@Override
			public void onMetadataChanged(MediaMetadataCompat metadata) {
				updateMetadata(metadata);
			}

			@Override
			public void onPlaybackStateChanged(PlaybackStateCompat state) {
				/* When a song is completed, update the metadata */
				updatePlaybackState(state);
			}

			@Override
			public void onSessionDestroyed() {
				updatePlaybackState(null);
			}
		};
	}

	@Override
	public void onStart() {
		super.onStart();
		MediaBrowserCompat.ConnectionCallback mConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {
			@Override
			public void onConnected() {
				try {
					MediaControllerCompat mediaController = new MediaControllerCompat
							(NowPlayingActivity.this, mMediaBrowser.getSessionToken());
					if (mediaController.isSessionReady()) {
						updatePlaybackState(mediaController.getPlaybackState());
						updateMetadata(mediaController.getMetadata());
						mediaController.registerCallback(mMediaControllerCallback);
						MediaControllerCompat.setMediaController(NowPlayingActivity.this, mediaController);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				if (nowPlayingSongDetails != null) {
					MediaControllerCompat.getMediaController(NowPlayingActivity.this)
							.getTransportControls().playFromMediaId(nowPlayingSongDetails.getMediaId(), null);
				}
			}
		};

		ComponentName componentName = new ComponentName(this, MediaService.class);
		mMediaBrowser = new MediaBrowserCompat(this, componentName, mConnectionCallback, null);
		mMediaBrowser.connect();

	}

	@Override
	public void onStop() {
		super.onStop();
		if (MediaControllerCompat.getMediaController(NowPlayingActivity.this) != null) {
			MediaControllerCompat.getMediaController(NowPlayingActivity.this).unregisterCallback(mMediaControllerCallback);
		}
		if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
			mMediaBrowser.disconnect();
			nowPlayingSongDetails = null;
		}
	}

	private void updatePlaybackState(PlaybackStateCompat state) {
		mCurrentPlaybackState = state;
		if (state == null || state.getState() == PlaybackState.STATE_PAUSED ||
				state.getState() == PlaybackState.STATE_STOPPED) {
			mPlayPauseBtn.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_36dp));
		} else {
			mPlayPauseBtn.setImageDrawable(getDrawable(R.drawable.ic_pause_black_36dp));
		}
		mPlaybackControls.setVisibility(state == null ? View.GONE : View.VISIBLE);

		//FixME - gets called unnecessarily when the song is paused and played.
		if (mCurrentMediaMetadata == null) {
			//updation failed.
			isSongDetailsUpdated = false;
		} else {
			mMusicTitle.setText(mCurrentMediaMetadata.getDescription().getTitle());
			mAlbumArtImage.setImageBitmap(mCurrentMediaMetadata.getDescription().getIconBitmap());
			//To be double sure :)
			isSongDetailsUpdated = true;
		}
	}

	private void updateMetadata(MediaMetadataCompat metadata) {
		/*
		 * Music title and album image should have ALWAYS updated here, but since the
		 * streaming takes a bit time, the album title and image gets updated before
		 * the song starts playing and also the notification update is will yet to be called.
		 * so, let's do that in updatePlaybackState().
		 */
		mCurrentMediaMetadata = metadata;
		if (!isSongDetailsUpdated && mCurrentMediaMetadata != null) {
			isSongDetailsUpdated = true;
			mMusicTitle.setText(mCurrentMediaMetadata.getDescription().getTitle());
			mAlbumArtImage.setImageBitmap(mCurrentMediaMetadata.getDescription().getIconBitmap());
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.play_pause) {
			int state = mCurrentPlaybackState == null ? PlaybackState.STATE_NONE : mCurrentPlaybackState.getState();

			if (state == PlaybackState.STATE_PAUSED || state == PlaybackState.STATE_STOPPED || state == PlaybackState.STATE_NONE) {
				if (mCurrentMediaMetadata == null && nowPlayingSongDetails != null) {
					mCurrentMediaMetadata = SongsLibrary.getMetadata(nowPlayingSongDetails.getMediaId());
					updateMetadata(mCurrentMediaMetadata);
				}
				MediaControllerCompat.getMediaController(NowPlayingActivity.this).getTransportControls().playFromMediaId(
						mCurrentMediaMetadata.getDescription().getMediaId(), null);
			} else {
				MediaControllerCompat.getMediaController(NowPlayingActivity.this).getTransportControls().pause();
			}
		} else if (v.getId() == R.id.prev) {
			MediaControllerCompat.getMediaController(NowPlayingActivity.this).getTransportControls().skipToPrevious();
		} else if (v.getId() == R.id.next) {
			MediaControllerCompat.getMediaController(NowPlayingActivity.this).getTransportControls().skipToNext();
		}
	}
}
