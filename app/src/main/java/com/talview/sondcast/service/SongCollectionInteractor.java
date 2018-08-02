package com.talview.sondcast.service;

import com.talview.sondcast.service.model.SongDetails;

import java.util.List;

public interface SongCollectionInteractor {

	void getSongsList(ResponseListener responseListener);

	interface ResponseListener {

		void onSuccess(List<SongDetails> response);

		void onError();
	}
}
