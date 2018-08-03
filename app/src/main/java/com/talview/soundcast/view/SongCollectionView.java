package com.talview.soundcast.view;

import com.talview.soundcast.BaseView;
import com.talview.soundcast.service.model.SongDetails;

import java.util.List;

/**
 * Song collection view
 *
 * @author Rolbin
 */
public interface SongCollectionView extends BaseView {

	/**
	 * Show songs in UI
	 *
	 * @param songCollection list of song
	 */
	void showSongCollection(List<SongDetails> songCollection);

	/**
	 * Show error.
	 */
	void showError();

}
