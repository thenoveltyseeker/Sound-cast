package com.talview.soundcast.view.ui.activities;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.talview.soundcast.R;
import com.talview.soundcast.service.model.SongDetails;
import com.talview.soundcast.view.musicplayer.SongsLibrary;
import com.talview.soundcast.view.musicplayer.service.MediaService;
import com.talview.soundcast.view.utils.Constants;

import org.jetbrains.annotations.Nullable;

import be.rijckaert.tim.animatedvector.FloatingMusicActionButton;

public class NowPlayingActivity extends AppCompatActivity implements View.OnClickListener {
	private MediaMetadataCompat mCurrentMediaMetadata;
	private PlaybackStateCompat mCurrentPlaybackState;
	private MediaControllerCompat.Callback mMediaControllerCallback;
	private MediaBrowserCompat mMediaBrowser;
	private SongDetails nowPlayingSongDetails;

	private FloatingMusicActionButton mPlayPauseBtn;
	private TextView mMusicTitle;
	private TextView mSongNumber;
	private ImageView mAlbumArtImage;
	private ProgressBar mProgressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_now_playing);

		if (getIntent().getSerializableExtra(Constants.BundleKeys.SELECTED_SONG) != null) {
			nowPlayingSongDetails = (SongDetails) getIntent().getSerializableExtra(Constants.BundleKeys.SELECTED_SONG);
		}

		initViews();
		mPlayPauseBtn.setEnabled(false);
		mProgressBar.setVisibility(View.VISIBLE);
		mMediaControllerCallback = new MediaControllerCompat.Callback() {
			@Override
			public void onMetadataChanged(MediaMetadataCompat metadata) {
				updateMetadata(metadata);
			}

			@Override
			public void onPlaybackStateChanged(PlaybackStateCompat state) {
				updatePlaybackState(state);
			}

			@Override
			public void onSessionDestroyed() {
				updatePlaybackState(null);
			}
		};
	}

	private void initViews() {
		// Playback controls configuration:
		mPlayPauseBtn = findViewById(R.id.play_pause);
		mProgressBar = findViewById(R.id.progress_bar);
		ImageButton previousTrackBtn = findViewById(R.id.prev);
		ImageButton nextTrackBtn = findViewById(R.id.next);
		ImageView backButton = findViewById(R.id.back_button);

		mPlayPauseBtn.setOnClickListener(this);
		previousTrackBtn.setOnClickListener(this);
		nextTrackBtn.setOnClickListener(this);
		backButton.setOnClickListener(this);

		mMusicTitle = findViewById(R.id.title);
		mSongNumber = findViewById(R.id.song_id);
		mAlbumArtImage = findViewById(R.id.album_art);
		DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
		int height = displayMetrics.heightPixels;
		mAlbumArtImage.setMinimumHeight(height / 2);
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
				if (nowPlayingSongDetails != null && getTransportControls() != null) {
					getTransportControls().playFromMediaId(nowPlayingSongDetails.getMediaId(), null);
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
		if (getMediaControllerCompat() != null) {
			getMediaControllerCompat().unregisterCallback(mMediaControllerCallback);
		}
		if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
			mMediaBrowser.disconnect();
			nowPlayingSongDetails = null;
		}
	}

	private void updatePlaybackState(@Nullable PlaybackStateCompat state) {
		mCurrentPlaybackState = state;
		if (isNotInPlayableState(state)) {
			mPlayPauseBtn.changeMode(FloatingMusicActionButton.Mode.PLAY_TO_PAUSE);
		} else {
			mPlayPauseBtn.changeMode(FloatingMusicActionButton.Mode.PAUSE_TO_PLAY);
		}
		mProgressBar.setVisibility(View.GONE);
		mPlayPauseBtn.setEnabled(true);
	}

	private void updateMetadata(MediaMetadataCompat metadata) {
		/*
		 * Music title and album image should have ALWAYS updated here, but since the
		 * streaming takes a bit time, the album title and image gets updated before
		 * the song starts playing and also the notification is yet to be updated.
		 * so, let's do that in updatePlaybackState().
		 */
		mCurrentMediaMetadata = metadata;
		if (mCurrentMediaMetadata != null) {
			mMusicTitle.setText(getMediaTitle());
			mSongNumber.setText(String.format(getString(R.string.song), getMediaId()));
			mAlbumArtImage.setImageBitmap(getIconBitmap());
		}
	}

	private boolean isNotInPlayableState(PlaybackStateCompat state) {
		return state == null || state.getState() == PlaybackState.STATE_PAUSED ||
				state.getState() == PlaybackState.STATE_STOPPED || state.getState() == PlaybackState.STATE_NONE;
	}

	private MediaControllerCompat.TransportControls getTransportControls() {
		if (getMediaControllerCompat() != null) {
			return getMediaControllerCompat().getTransportControls();
		}
		return null;
	}

	private MediaControllerCompat getMediaControllerCompat() {
		return MediaControllerCompat.getMediaController(NowPlayingActivity.this);
	}

	private MediaDescriptionCompat getMediaDescriptionCompat() {
		return mCurrentMediaMetadata.getDescription();
	}

	private String getMediaId() {
		return getMediaDescriptionCompat().getMediaId();
	}

	private CharSequence getMediaTitle() {
		return getMediaDescriptionCompat().getTitle();
	}

	private Bitmap getIconBitmap() {
		return getMediaDescriptionCompat().getIconBitmap();
	}


	@Override
	public void onClick(View v) {
		if (v.getId() != R.id.back_button && getTransportControls() == null) {
			return;
		}
		switch (v.getId()) {
			case R.id.play_pause:
				if (isNotInPlayableState(mCurrentPlaybackState)) {
					if (mCurrentMediaMetadata == null && nowPlayingSongDetails != null) {
						mCurrentMediaMetadata = SongsLibrary.getMetadata(nowPlayingSongDetails.getMediaId());
						updateMetadata(mCurrentMediaMetadata);
					}
					getTransportControls().playFromMediaId(getMediaId(), null);
				} else {
					getTransportControls().pause();
				}
				break;
			case R.id.prev:
				mPlayPauseBtn.setEnabled(false);
				mProgressBar.setVisibility(View.VISIBLE);
				getTransportControls().skipToPrevious();
				break;
			case R.id.next:
				mPlayPauseBtn.setEnabled(false);
				mProgressBar.setVisibility(View.VISIBLE);
				getTransportControls().skipToNext();
				break;
			case R.id.back_button:
				onBackPressed();
				break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mCurrentPlaybackState = null;
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_out_right);
	}
}
