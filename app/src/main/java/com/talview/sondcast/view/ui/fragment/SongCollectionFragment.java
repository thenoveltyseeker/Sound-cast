package com.talview.sondcast.view.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.talview.sondcast.R;
import com.talview.sondcast.presenter.SongCollectionPresenter;
import com.talview.sondcast.presenter.SongCollectionPresenterImpl;
import com.talview.sondcast.service.model.SongDetails;
import com.talview.sondcast.view.SongCollectionView;
import com.talview.sondcast.view.adapter.SongsAdapter;
import com.talview.sondcast.view.adapter.SongsAdapterCallback;
import com.talview.sondcast.view.musicplayer.SongsLibrary;
import com.talview.sondcast.view.ui.activities.NowPlayingActivity;
import com.talview.sondcast.view.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Song collection, whole list of available songs.
 *
 * @author Rolbin.
 */
public class SongCollectionFragment extends Fragment implements SongCollectionView, SongsAdapterCallback {
	private List<SongDetails> mSongList = new ArrayList<>();
	private SongsAdapter mSongAdapter;
	private SongCollectionPresenter mPresenter;
	private Context mContext;

	public SongCollectionFragment() {
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mContext = context;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.song_collection_layout, container, false);
		mPresenter = new SongCollectionPresenterImpl(this);
		/*Request to get songs data*/
		mPresenter.getSongsList();
		setupRecyclerview(rootView);
		return rootView;
	}

	private void setupRecyclerview(View rootView) {
		RecyclerView recyclerView = rootView.findViewById(R.id.songs_collection_rv);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
		RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext, RecyclerView.HORIZONTAL);
		recyclerView.addItemDecoration(itemDecoration);
		mSongAdapter = new SongsAdapter(mSongList, this);
		recyclerView.setAdapter(mSongAdapter);
	}

	@Override
	public void showSongCollection(List<SongDetails> songCollection) {
		/*incase if the list is added with the songs*/
		mSongList.clear();
		mSongList.addAll(songCollection);
		/*Create metadata for background playing*/
		SongsLibrary.createSongMetaData(songCollection);
		mSongAdapter.notifyDataSetChanged();
	}

	@Override
	public void showError() {

	}

	@Override
	public void showProgress() {

	}

	@Override
	public void hideProgress() {

	}

	@Override
	public void onSongSelected(SongDetails nowPlaying) {
		//Start Now Playing Activity
		Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
		//FixMe - Change to parcelable.
		intent.putExtra(Constants.Keys.SELECTED_SONG, nowPlaying);
		//Todo Add a transition
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		mPresenter.onDestroy();
		mContext = null;
		super.onDestroy();
	}
}
