package org.eclipse.help.servlet.data;

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
	
	public String getImageURL() {
		return imageURL;
	}
}
