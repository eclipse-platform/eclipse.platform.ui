package org.eclipse.update.internal.security;

import java.net.URL;


/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
/**
 * Manages a handle to a keystore
 */
public class KeystoreHandle {
	
	private URL location;
	private String type;
	
	public KeystoreHandle(URL url, String type){
		this.location = url;
		this.type = type;
	}

	/**
	 * Gets the location.
	 * @return Returns a URL
	 */
	public URL getLocation() {
		return location;
	}

	/**
	 * Sets the location.
	 * @param location The location to set
	 */
	public void setLocation(URL location) {
		this.location = location;
	}

	/**
	 * Gets the type.
	 * @return Returns a String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
