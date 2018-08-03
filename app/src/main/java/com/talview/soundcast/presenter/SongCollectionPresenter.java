package com.talview.soundcast.presenter;

import com.talview.soundcast.BasePresenter;

/**
 * Presenter class for Songs collection view
 *
 * @author Rolbin
 */
public interface SongCollectionPresenter extends BasePresenter {

	/**
	 * Get the list of songs to stream.
	 */
	void getSongsList();

}
