package com.talview.soundcast.service;

import com.talview.soundcast.service.model.SongDetails;

import java.util.List;

/**
 * Interactor to get song data
 *
 * @author Rolbin
 */
public interface SongCollectionInteractor {

	/**
	 * Get song collection
	 *
	 * @param responseListener response listener for callback.
	 */
	void getSongsList(ResponseListener responseListener);

	/**
	 * Response listener callback interface
	 */
	interface ResponseListener {

		/**
		 * Success callback
		 *
		 * @param response list of songs
		 */
		void onSuccess(List<SongDetails> response);

		/**
		 * Error callback
		 */
		//Todo should add error message param
		void onError();
	}
}
