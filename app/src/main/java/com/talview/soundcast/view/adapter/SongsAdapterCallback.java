package com.talview.soundcast.view.adapter;

import com.talview.soundcast.service.model.SongDetails;

/**
 * Song selection callback
 *
 * @author Rolbin
 */
public interface SongsAdapterCallback {

	/**
	 * song selected
	 *
	 * @param nowPlaying details of the song, which is gonna be played.
	 */
	void onSongSelected(SongDetails nowPlaying);

}
