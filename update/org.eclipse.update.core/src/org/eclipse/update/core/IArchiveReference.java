package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.URL;

/**
 * Site archive interface.
 * Site archive is a representation of a packaged archive (file) located
 * on an update site. It allows a "symbolic" path used to identify
 * a plug-in or non-plug-in feature entry to be explicitly mapped
 * to a specific URL. 
 * <p>
 * Clients may implement this interface. However, in most cases clients should 
 * directly instantiate or subclass the provided implementation of this 
 * interface.
 * </p>
 * @see org.eclipse.update.core.ArchiveReference
 * @since 2.0
 */
public interface IArchiveReference {

	/** 
	 * 
	 * @return the archive "symbolic" path, or <code>null</code>
	 * @since 2.0 
	 */
	String getPath();

	/**
	 * Retrieve the site archive URL 
	 * 
	 * @return the archive URL, or <code>null</code>
	 * @since 2.0 
	 */
	URL getURL();
}