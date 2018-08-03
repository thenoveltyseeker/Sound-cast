package com.talview.soundcast.service.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response class
 *
 * @author Rolbin
 */
public class SongCollection {

	@SerializedName("songs")
	private List<Song> songs;

	public List<Song> getSongs() {
		return songs;
	}

	public void setSongs(List<Song> songs) {
		this.songs = songs;
	}
}
