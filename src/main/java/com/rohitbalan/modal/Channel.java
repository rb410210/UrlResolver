package com.rohitbalan.modal;

public class Channel extends Category {
	private String thumbnail;
	private String video;
	private boolean routedVideo;

	public boolean isRoutedVideo() {
		return routedVideo;
	}

	public void setRoutedVideo(boolean routedVideo) {
		this.routedVideo = routedVideo;
	}

	@Override
	public String toString() {
		return this.getName() + ": " + this.getUrl() + " - " + this.getThumbnail() + " - " + this.getVideo();
	}

	public Channel(String name, String url, String thumbnail) {
		super(name, url);
		this.thumbnail = thumbnail;
	}

	public Channel(String name, String thumbnail) {
		super(name, null);
		this.thumbnail = thumbnail;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

}
