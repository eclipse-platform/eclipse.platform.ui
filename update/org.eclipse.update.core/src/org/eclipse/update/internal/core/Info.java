package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;
import org.eclipse.update.core.IInfo;

/**
 * Default implementation of IInfo
 */

public class Info implements IInfo{

	private String text;
	private URL url;

	/**
	 * Constructor for Info
	 */
	public Info() {
		super();
	}
	
	/**
	 * Constructor for Info
	 */
	public Info(URL url) {
		super();
		this.url = url;
	}
	
	/**
	 * Constructor for Info
	 */
	public Info(String text) {
		super();
		this.text = text;
	}
	
	/**
	 * Constructor for Info
	 */
	public Info(String text,URL url) {
		super();
		this.text = text;
		this.url = url;
	}
	
	

	/**
	 * @see IInfo#getText()
	 */
	public String getText() {
		return text;
	}

	/**
	 * @see IInfo#getURL()
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Sets the text
	 * @param text The text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Sets the url
	 * @param url The url to set
	 */
	public void setURL(URL url) {
		this.url = url;
	}

}

