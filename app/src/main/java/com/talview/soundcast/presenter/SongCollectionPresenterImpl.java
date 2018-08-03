package com.talview.soundcast.presenter;

import com.talview.soundcast.service.SongCollectionInteractor;
import com.talview.soundcast.service.SongCollectionInteractorImpl;
import com.talview.soundcast.service.model.SongDetails;
import com.talview.soundcast.view.SongCollectionView;

import java.util.List;

/**
 * Implementation class of {@link SongCollectionPresenter}
 *
 * @author Rolbin
 */
public class SongCollectionPresenterImpl implements SongCollectionPresenter {
	private SongCollectionView mSongCollectionView;
	private SongCollectionInteractor mSongCollectionInteractor;

	public SongCollectionPresenterImpl(SongCollectionView view) {
		this.mSongCollectionView = view;
		this.mSongCollectionInteractor = new SongCollectionInteractorImpl();
	}

	@Override
	public void getSongsList() {
		mSongCollectionView.showProgress();
		mSongCollectionInteractor.getSongsList(new SongCollectionInteractor.ResponseListener() {
			@Override
			public void onSuccess(List<SongDetails> songCollection) {
				mSongCollectionView.showSongCollection(songCollection);
				mSongCollectionView.hideProgress();
			}

			@Override
			public void onError() {
				mSongCollectionView.hideProgress();
				mSongCollectionView.showError();
			}
		});
	}

	@Override
	public void onDestroy() {
		mSongCollectionView = null;
		mSongCollectionInteractor = null;
	}
}
