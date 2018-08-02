package com.talview.sondcast.view.musicplayer;

import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.support.v4.media.MediaMetadataCompat;

import com.squareup.picasso.Picasso;
import com.talview.sondcast.service.model.SongDetails;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class SongsLibrary {

	private static final TreeMap<String, MediaMetadataCompat> sMusicTracksMetadata = new TreeMap<>();
	private static final HashMap<String, Bitmap> sAlbumCovers = new HashMap<>();
	private static final HashMap<String, String> sMusicStramUrl = new HashMap<>();

	public static void createSongMetaData(List<SongDetails> songsList) {
		for (SongDetails song : songsList) {
			buildMetadata(song);
		}
	}

	public static String getRoot() {
		return "";
	}

	public static String getMusicStreamUrl(String mediaId) {
		return sMusicStramUrl.containsKey(mediaId) ? sMusicStramUrl.get(mediaId) : "";
	}

	public static Bitmap getAlbumRes(String mediaId) {
		return sAlbumCovers.containsKey(mediaId) ? sAlbumCovers.get(mediaId) : null;
	}

	public static String getPreviousSong(String currentMediaId) {
		String prevMediaId = sMusicTracksMetadata.lowerKey(currentMediaId);
		if (prevMediaId == null) {
			prevMediaId = sMusicTracksMetadata.lastKey();
		}
		return prevMediaId;
	}

	public static String getNextSong(String currentMediaId) {
		String nextMediaId = sMusicTracksMetadata.higherKey(currentMediaId);
		if (nextMediaId == null) {
			nextMediaId = sMusicTracksMetadata.firstKey();
		}
		return nextMediaId;
	}

	public static MediaMetadataCompat getNextSongMetadata(String currentMediaId) {
		String nextSongMediaId = getNextSong(currentMediaId);
		MediaMetadataCompat metadata = sMusicTracksMetadata.get(nextSongMediaId);
		return metadata;
	}

	public static MediaMetadataCompat getMetadata(String mediaId) {
		MediaMetadataCompat metadataWithoutBitmap = sMusicTracksMetadata.get(mediaId);
		Bitmap albumArt = getAlbumRes(mediaId);

		// Since MediaMetadata is immutable, we need to create a copy to set the album art
		// We don't set it initially on all items so that they don't take unnecessary memory
		MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
		for (String key : new String[]{MediaMetadata.METADATA_KEY_MEDIA_ID, MediaMetadata.METADATA_KEY_TITLE}) {
			builder.putString(key, metadataWithoutBitmap.getString(key));
		}
		builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt);
		return builder.build();
	}

	private static void buildMetadata(SongDetails song) {
		String mediaId = song.getMediaId();
		String title = song.getMediaTitle();
		String mediaUrl = song.getMediaUrl();
		String albumArtUrl = song.getThumbnail();
		sMusicTracksMetadata.put(mediaId,
				new MediaMetadataCompat.Builder()
						.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId)
						.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, albumArtUrl)
						.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, albumArtUrl)
						.putString(MediaMetadata.METADATA_KEY_TITLE, title)
						.build());

		Runnable runnable = () -> {
			try {
				sAlbumCovers.put(mediaId, Picasso.get().load(albumArtUrl).get());
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
		sMusicStramUrl.put(mediaId, mediaUrl);
	}
}