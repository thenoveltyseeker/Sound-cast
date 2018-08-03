package com.talview.soundcast;

import com.talview.soundcast.service.model.SongDetails;
import com.talview.soundcast.view.musicplayer.SongsLibrary;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;

public class SongsLibraryTest {

	@Test
	public void getRoot_test() {
		Assert.assertThat(SongsLibrary.getRoot()
				.equals(""), is(true));
	}

	@Test
	public void getNextSong_test() {
		createMetadata();
		Assert.assertThat(SongsLibrary.getNextSong("2")
				.equals("3"), is(true));
	}
	@Test
	public void getPrevSong_test() {
		createMetadata();
		Assert.assertThat(SongsLibrary.getNextSong("2")
				.equals("1"), is(true));
	}

	//Todo need to mock bundle
	private void createMetadata() {
		List<SongDetails> songList = new ArrayList<>();
		SongDetails songDetails1 = new SongDetails();
		songDetails1.setMediaId("1");
		songDetails1.setMediaUrl("url1");

		SongDetails songDetails2 = new SongDetails();
		songDetails2.setMediaId("2");
		songDetails2.setMediaUrl("url2");

		SongDetails songDetails3 = new SongDetails();
		songDetails3.setMediaId("3");
		songDetails3.setMediaUrl("url3");

		songList.add(songDetails1);
		songList.add(songDetails2);
		songList.add(songDetails3);

		SongsLibrary.createSongMetaData(songList);
	}

}
