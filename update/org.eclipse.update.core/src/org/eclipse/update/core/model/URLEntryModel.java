package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * An object which represents an annotated URL entry.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @since 2.0
 */

public class URLEntryModel extends ModelObject {
	
	private String annotation;
	private String localizedAnnotation;
	private String urlString;
	private URL url;
	
	/**
	 * Creates a uninitialized information entry model object.
	 * 
	 * @since 2.0
	 */
	public URLEntryModel() {
		super();
	}
		
	/**
	 * Returns annotation.
	 *
	 * @return text string, or <code>null</code>
	 * @since 2.0
	 */
	public String getAnnotation() {
		if (localizedAnnotation != null)
			return localizedAnnotation;
		else
			return annotation;
	}
		
	/**
	 * Returns annotation.
	 *
	 * @return text string, or <code>null</code>
	 * @since 2.0
	 */
	public String getAnnotationNonLocalized() {
		return annotation;
	}

	/**
	 * Returns URL string containing additional information.
	 *
	 * @return url, <code>null</code>
	 * @since 2.0
	 */
	public String getURLString() {
		return urlString;
	}
	
	/**
	 * Returns the resolved URL for the entry.
	 * 
	 * @return url, or <code>null</code>
	 * @since 2.0
	 */
	public URL getURL() {
		return url;
	}
	
	/**
	 * Sets the annotation.
	 * This object must not be read-only.
	 *
	 * @param annotation string. Can be <code>null</code>.
	 * @since 2.0
	 */	
	public void setAnnotation(String annotation) {
		assertIsWriteable();
		this.annotation = annotation;
		this.localizedAnnotation = null;
	}
	
	/**
	 * Sets the URL containing additional information.
	 * This object must not be read-only.
	 *
	 * @param url url for additional information. Can be <code>null</code>.
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
		localizedAnnotation = resolveNLString(bundle, annotation);
		url = resolveURL(base, bundle, urlString);
	}
}
