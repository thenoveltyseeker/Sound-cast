package com.talview.soundcast.view.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.talview.soundcast.R;
import com.talview.soundcast.presenter.SongCollectionPresenter;
import com.talview.soundcast.presenter.SongCollectionPresenterImpl;
import com.talview.soundcast.service.model.SongDetails;
import com.talview.soundcast.view.SongCollectionView;
import com.talview.soundcast.view.adapter.SongsAdapter;
import com.talview.soundcast.view.adapter.SongsAdapterCallback;
import com.talview.soundcast.view.musicplayer.SongsLibrary;
import com.talview.soundcast.view.ui.activities.NowPlayingActivity;
import com.talview.soundcast.view.utils.Constants;
import com.talview.soundcast.view.utils.Utility;

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

	private ProgressBar mPbLoadingView;
	private RecyclerView mRvSongsCollection;

	public SongCollectionFragment() {
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mContext = context;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPresenter = new SongCollectionPresenterImpl(this);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_song_collection, container, false);
		mPbLoadingView = rootView.findViewById(R.id.progress_bar);
		setupRecyclerview(rootView);

		/*Request to get songs data*/
		if (Utility.isOnline(mContext)) {
			mPresenter.getSongsList();
		} else {
			Utility.showError(mContext, "No Network connection");
		}
		return rootView;
	}

	private void setupRecyclerview(View rootView) {
		mRvSongsCollection = rootView.findViewById(R.id.songs_collection_rv);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
		mRvSongsCollection.setLayoutManager(linearLayoutManager);

		RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext, linearLayoutManager.getOrientation());
		mRvSongsCollection.addItemDecoration(itemDecoration);
		mRvSongsCollection.setHasFixedSize(true);

		mSongAdapter = new SongsAdapter(mSongList, this);
		mRvSongsCollection.setAdapter(mSongAdapter);
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
		Utility.showError(mContext, "Something bad happened");
	}

	@Override
	public void showProgress() {
		mRvSongsCollection.setVisibility(View.GONE);
		mPbLoadingView.setVisibility(View.VISIBLE);
	}

	@Override
	public void hideProgress() {
		mRvSongsCollection.setVisibility(View.VISIBLE);
		mPbLoadingView.setVisibility(View.GONE);
	}

	@Override
	public void onSongSelected(SongDetails nowPlaying) {
		//Start Now Playing Activity
		Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
		//FixMe - Change to parcelable.
		intent.putExtra(Constants.BundleKeys.SELECTED_SONG, nowPlaying);
		startActivity(intent);
		//Let's have some transition animation
		((AppCompatActivity) mContext).overridePendingTransition(R.anim.slide_from_right, R.anim.slide_out_left);
	}

	@Override
	public void onDestroy() {
		mPresenter.onDestroy();
		mContext = null;
		super.onDestroy();
	}
}
