package com.rohitbalan.modal;

public class Category {
	private String name;

	public Category(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public Category() {
	}

	private String url;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return name + ": " + url;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Category))
			return false;
		return name.equals(((Category) obj).getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
