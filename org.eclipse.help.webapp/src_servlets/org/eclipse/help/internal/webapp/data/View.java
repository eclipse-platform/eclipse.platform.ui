package org.eclipse.help.internal.webapp.data;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
public class View {
	private String name;
	private String url;
	private String imageURL;

	public View(String name, String url, String imageURL) {
		this.name = name;
		this.url = url;
		this.imageURL = imageURL;
	}
	
	public String getName() {
		return name;
	}
	
	public String getURL() {
		return url;
	}
	
	/**
	 * Returns the enabled gray image
	 * @return String
	 */
	public String getImage() {
		int i = imageURL.lastIndexOf('/');
		return imageURL.substring(0, i) + "/e_"+ imageURL.substring(i+1);
	}
	
	/**
	 * Returns the image when selected
	 * @return String
	 */
	public String getOnImage() {
		return imageURL;
	}
}
