package com.talview.sondcast.presenter;

import com.talview.sondcast.service.SongCollectionInteractor;
import com.talview.sondcast.service.SongCollectionInteractorImpl;
import com.talview.sondcast.service.model.SongDetails;
import com.talview.sondcast.view.SongCollectionView;

import java.util.List;

public class SongCollectionPresenterImpl implements SongCollectionPresenter {

	private SongCollectionView mSongCollectionView;
	private SongCollectionInteractor mSongCollectionInteractor;

	public SongCollectionPresenterImpl(SongCollectionView view) {
		this.mSongCollectionView = view;
		this.mSongCollectionInteractor = new SongCollectionInteractorImpl();
	}

	@Override
	public void getSongsList() {
		mSongCollectionInteractor.getSongsList(new SongCollectionInteractor.ResponseListener() {
			@Override
			public void onSuccess(List<SongDetails> songCollection) {
				mSongCollectionView.showSongCollection(songCollection);
			}

			@Override
			public void onError() {

			}
		});
	}

	@Override
	public void onDestroy() {
		mSongCollectionView = null;
		mSongCollectionInteractor = null;
	}
}
