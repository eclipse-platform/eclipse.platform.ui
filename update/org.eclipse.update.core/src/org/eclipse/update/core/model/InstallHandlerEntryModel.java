package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Install handler entry model object.
 * An object which represents the definition of a custom install handler
 * <p>
 * This class may be instantiated or subclassed by clients. However, in most 
 * cases clients should instead instantiate or subclass the provided 
 * concrete implementation of this model.
 * </p>
 * @see org.eclipse.update.core.InstallHandlerEntry
 * @since 2.0
 */
public class InstallHandlerEntryModel extends ModelObject {

	private String urlString;
	private URL url;
	private String library;
	private String name;

	/**
	 * Creates a uninitialized install handler entry model object.
	 * 
	 * @since 2.0
	 */
	public InstallHandlerEntryModel() {
		super();
	}

	/**
	 * Returns the URL string used for browser-triggered installation handling.
	 *
	 * @return url string or <code>null</code>
	 * @since 2.0
	 */
	public String getURLString() {
		return urlString;
	}

	/**
	 * Returns the resolved URL used for browser-triggered installation handling.
	 * 
	 * @return url, or <code>null</code>
	 * @since 2.0
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Returns the name of the custom installer library.
	 *
	 * @return library path, or <code>null</code>
	 * @since 2.0
	 */
	public String getLibrary() {
		return library;
	}

	/**
	 * Returns the name of the custom installer.
	 *
	 * @return handler name, or <code>null</code>
	 * @since 2.0
	 */
	public String getHandlerName() {
		return name;
	}

	/**
	 * Sets URL string used for browser-triggered installation handling.
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param urlString trigget page URL string, may be <code>null</code>.
	 * @since 2.0
	 */
	public void setURLString(String urlString) {
		assertIsWriteable();
		this.urlString = urlString;
		this.url = null;
	}

	/**
	 * Sets the custom install handler library name.
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param library name, may be <code>null</code>.
	 * @since 2.0
	 */
	public void setLibrary(String library) {
		assertIsWriteable();
		this.library = library;
	}

	/**
	 * Sets the name of the custom install handler.
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param name name of the install handler, may be <code>null</code>.
	 * @since 2.0
	 */
	public void setHandlerName(String name) {
		assertIsWriteable();
		this.name = name;
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
	public void resolve(URL base, ResourceBundle bundle)
		throws MalformedURLException {
		// resolve local elements
		url = resolveURL(base, bundle, urlString);
	}
}