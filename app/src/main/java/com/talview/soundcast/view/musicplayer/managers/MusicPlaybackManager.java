package com.talview.soundcast.view.musicplayer.managers;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import com.talview.soundcast.view.musicplayer.SongsLibrary;

import java.io.IOException;

import static android.media.MediaPlayer.OnCompletionListener;

/**
 * Helps in playback of songs and related actions
 *
 * @author Rolbin
 */
public class MusicPlaybackManager implements AudioManager.OnAudioFocusChangeListener,
		MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

	private final Context mContext;
	private final Callback mPlaybackManagerCallback;
	private final AudioManager mAudioManager;
	private volatile MediaMetadataCompat mCurrentMediaMetadata;
	private MediaPlayer mMediaPlayer;
	private int mPlaybackState;
	private boolean isFocusGained;

	public MusicPlaybackManager(Context context, Callback callback) {
		this.mContext = context;
		this.mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		this.mPlaybackManagerCallback = callback;
	}

	private boolean isPlaying() {
		return isFocusGained || (mMediaPlayer != null && mMediaPlayer.isPlaying());
	}

	public MediaMetadataCompat getCurrentMediaMetadata() {
		return this.mCurrentMediaMetadata;
	}

	public String getCurrentMediaId() {
		return mCurrentMediaMetadata == null ? null : mCurrentMediaMetadata.getDescription().getMediaId();
	}

	private int getCurrentStreamPosition() {
		return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
	}

	public void playSong(MediaMetadataCompat metadata) {
		String mediaId = metadata.getDescription().getMediaId();
		boolean isSongChanged = (mCurrentMediaMetadata == null || !getCurrentMediaId().equals(mediaId));

		mCurrentMediaMetadata = metadata;

		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setWakeMode(mContext.getApplicationContext(),
					PowerManager.PARTIAL_WAKE_LOCK);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnErrorListener(this);
			prepareMediaPlayer(mediaId);
		} else if (isSongChanged) {
			mMediaPlayer.reset();
			/*since reset is called, we need to set source and all again*/
			prepareMediaPlayer(mediaId);
		} else {
			/*media player is pause, let's continue.*/
			mMediaPlayer.start();
			mPlaybackState = PlaybackState.STATE_PLAYING;
			updatePlaybackState();
		}
	}

	private void prepareMediaPlayer(String mediaId) {
		try {
			mMediaPlayer.setDataSource(mContext, Uri.parse(SongsLibrary.getMusicStreamUrl(mediaId)));
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.prepareAsync();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void pause() {
		if (isPlaying()) {
			mMediaPlayer.pause();
			mAudioManager.abandonAudioFocus(this);
		}
		mPlaybackState = PlaybackState.STATE_PAUSED;
		updatePlaybackState();
	}

	public void stop() {
		mPlaybackState = PlaybackState.STATE_STOPPED;
		updatePlaybackState();
		// Give up Audio focus
		mAudioManager.abandonAudioFocus(this);
		// Relax all resources
		releaseMediaPlayer();
	}


	private boolean requestAudioFocus() {
		int result = mAudioManager.requestAudioFocus(
				this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
	}

	/**
	 * Called by AudioManager on audio focus changes.
	 * Implementation of {@link AudioManager.OnAudioFocusChangeListener}
	 */
	@Override
	public void onAudioFocusChange(int focusChange) {
		boolean gotFullFocus = false;
		boolean canDuck = false;
		if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
			gotFullFocus = true;

		} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
				focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
				focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
			// We have lost focus. If we can duck (low playback volume), we can keep playing.
			// Otherwise, we need to pause the playback.
			canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
		}

		if (gotFullFocus || canDuck) {
			if (mMediaPlayer != null) {
				if (isFocusGained) {
					isFocusGained = false;
					mMediaPlayer.start();
					mPlaybackState = PlaybackState.STATE_PLAYING;
					updatePlaybackState();
				}
				float volume = canDuck ? 0.2f : 1.0f;
				mMediaPlayer.setVolume(volume, volume);
			}
		} else if (mPlaybackState == PlaybackState.STATE_PLAYING) {
			mMediaPlayer.pause();
			mPlaybackState = PlaybackState.STATE_PAUSED;
			updatePlaybackState();
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if (requestAudioFocus()) {
			isFocusGained = false;
			mMediaPlayer.start();
			mPlaybackState = PlaybackState.STATE_PLAYING;
			updatePlaybackState();
		} else {
			isFocusGained = true;
		}
	}

	/**
	 * Called when media player is done playing current song.
	 *
	 * @see OnCompletionListener
	 */
	@Override
	public void onCompletion(MediaPlayer player) {
		if (mPlaybackManagerCallback == null) {
			releaseMediaPlayer();
			return;
		}
		mPlaybackManagerCallback.onSongCompleted();
	}

	/**
	 * Releases resources used by the service for playback.
	 */
	private void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private long getAvailableActions() {
		long actions = PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
				PlaybackState.ACTION_PLAY_FROM_SEARCH |
				PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS;
		if (isPlaying()) {
			actions |= PlaybackState.ACTION_PAUSE;
		}
		return actions;
	}

	private void updatePlaybackState() {
		if (mPlaybackManagerCallback == null) {
			return;
		}
		PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
				.setActions(getAvailableActions());
		stateBuilder.setState(mPlaybackState, getCurrentStreamPosition(), 1.0f, SystemClock.elapsedRealtime());
		mPlaybackManagerCallback.onPlaybackStatusChanged(stateBuilder.build());
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Toast.makeText(mContext, "Media player error! Resetting.",
				Toast.LENGTH_SHORT).show();

		releaseMediaPlayer();
		//Let's try playing next song
		mPlaybackManagerCallback.onSongCompleted();
		return true; // true indicates we handled the error
	}

	public void onPlay() {
		if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
			requestAudioFocus();
		}
		mPlaybackState = PlaybackState.STATE_PLAYING;
		updatePlaybackState();
	}

	public interface Callback {

		void onPlaybackStatusChanged(PlaybackStateCompat state);

		void onSongCompleted();

	}

}
