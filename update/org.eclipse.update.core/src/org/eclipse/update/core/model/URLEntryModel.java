package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Annotated URL model object.
 * <p>
 * This class may be instantiated or subclassed by clients. However, in most 
 * cases clients should instead instantiate or subclass the provided 
 * concrete implementation of this model.
 * </p>
 * @see org.eclipse.update.core.URLEntry
 * @since 2.0
 */

public class URLEntryModel extends ModelObject {
	
	private String annotation;
	private String localizedAnnotation;
	private String urlString;
	private URL url;
	
	/**
	 * Creates a uninitialized annotated URL model object.
	 * 
	 * @since 2.0
	 */
	public URLEntryModel() {
		super();
	}
		
	/**
	 * Returns the url annotation. If the model object has been resolved, 
	 * the annotation is localized.
	 * 
	 * @return url annotation, or <code>null</code>.
	 * @since 2.0
	 */
	public String getAnnotation() {
		if (localizedAnnotation != null)
			return localizedAnnotation;
		else
			return annotation;
	}
		
	/**
	 * returns the non-localized url annotation.
	 * 
	 * @return non-localized url annotation, or <code>null</code>.
	 * @since 2.0
	 */
	public String getAnnotationNonLocalized() {
		return annotation;
	}

	/**
	 * Returns the unresolved url string.
	 *
	 * @return url string, or <code>null</code>
	 * @since 2.0
	 */
	public String getURLString() {
		return urlString;
	}
	
	/**
	 * Returns the resolved URL.
	 * 
	 * @return url, or <code>null</code>
	 * @since 2.0
	 */
	public URL getURL() {
		return url;
	}
	
	/**
	 * Sets the annotation.
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param annotation annotation
	 * @since 2.0
	 */	
	public void setAnnotation(String annotation) {
		assertIsWriteable();
		this.annotation = annotation;
		this.localizedAnnotation = null;
	}
	
	/**
	 * Sets the url string
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param url url string
	 * @since 2.0
	 */	
	public void setURLString(String urlString) {
		assertIsWriteable();
		this.urlString = urlString;
		this.url = null;
	}
	
	/**
	 * Resolve the model object.
	 * Any URL strings in the model are resolved relative to the 
	 * base URL argument. Any translatable strings in the model that are
	 * specified as translation keys are localized using the supplied 
	 * resource bundle.
	 * 
	 * @param base URL
	 * @param bundle resource bundle
	 * @exception MalformedURLException
	 * @since 2.0
	 */
	public void resolve(URL base, ResourceBundle bundle) throws MalformedURLException {
		// resolve local elements
		localizedAnnotation = resolveNLString(bundle, annotation);
		url = resolveURL(base, bundle, urlString);
	}
}
