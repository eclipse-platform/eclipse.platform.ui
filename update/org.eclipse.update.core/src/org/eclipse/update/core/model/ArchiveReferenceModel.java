package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * An object which represents a site archive reference entry.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @since 2.0
 */

public class ArchiveReferenceModel extends ModelObject {
	
	private String path;
	private String urlString;
	private URL url;
	
	/**
	 * Creates a uninitialized model object.
	 * 
	 * @since 2.0
	 */
	public ArchiveReferenceModel() {
		super();
	}
		
	/**
	 * Returns path.
	 *
	 * @return text string, or <code>null</code>
	 * @since 2.0
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns URL string for the archive.
	 *
	 * @return url string, or <code>null</code>
	 * @since 2.0
	 */
	public String getURLString() {
		return urlString;
	}
	
	/**
	 * Returns the resolved URL for the archive.
	 * 
	 * @return url, or <code>null</code>
	 * @since 2.0
	 */
	public URL getURL() {
		return url;
	}
	
	/**
	 * Sets the path.
	 * This object must not be read-only.
	 *
	 * @param annotation string. Can be <code>null</code>.
	 * @since 2.0
	 */	
	public void setPath(String path) {
		assertIsWriteable();
		this.path = path;
	}
	
	/**
	 * Sets the URL for the archive.
	 * This object must not be read-only.
	 *
	 * @param urlString url string for additional information. Can be <code>null</code>.
	 * @since 2.0
	 */	
	public void setURLString(String urlString) {
		assertIsWriteable();
		this.urlString = urlString;
		this.url = null;
	}
	
	/**
	 * @since 2.0
	 */
	public void resolve(URL base, ResourceBundle bundle) throws MalformedURLException {
		// resolve local elements
		url = resolveURL(base, bundle, urlString);
	}
}
