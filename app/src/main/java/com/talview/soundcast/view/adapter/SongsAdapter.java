package com.talview.soundcast.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.talview.soundcast.R;
import com.talview.soundcast.service.model.SongDetails;

import java.util.List;

/**
 * Adapter class for showing songs overview
 *
 * @author Rolbin
 */
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {
	private List<SongDetails> mSongList;
	private SongsAdapterCallback mCallbackListener;


	public SongsAdapter(List<SongDetails> songList, SongsAdapterCallback callbackListener) {
		this.mSongList = songList;
		this.mCallbackListener = callbackListener;
	}

	@NonNull
	@Override
	public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_song_row, parent, false);
		return new SongViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final SongViewHolder holder, int position) {
		SongDetails playableSongDetails = mSongList.get(position);

		holder.songTitle.setText(playableSongDetails.getMediaTitle());
		holder.songId.setText(String.format("Song - %s", playableSongDetails.getMediaId()));
		Picasso.get().load(playableSongDetails.getThumbnail()).into(holder.songThumbnail);

		holder.itemView.setOnClickListener(v -> mCallbackListener.onSongSelected(playableSongDetails));
	}

	@Override
	public int getItemCount() {
		return mSongList.size();
	}

	static class SongViewHolder extends RecyclerView.ViewHolder {
		private TextView songTitle;
		private TextView songId;
		private ImageView songThumbnail;

		SongViewHolder(View itemView) {
			super(itemView);
			songTitle = itemView.findViewById(R.id.title);
			songId = itemView.findViewById(R.id.song_id);
			songThumbnail = itemView.findViewById(R.id.thumbnail);
		}
	}

}
