package com.talview.soundcast.service.model;

import java.io.Serializable;

/**
 * UI model class
 *
 * @author Rolbin
 */
//TODO change to parcelable
public class SongDetails implements Serializable {
	private String mediaId;
	private String mediaTitle;
	private String mediaUrl;
	private String thumbnail;

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public String getMediaTitle() {
		return mediaTitle;
	}

	public void setMediaTitle(String mediaTitle) {
		this.mediaTitle = mediaTitle;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

}
