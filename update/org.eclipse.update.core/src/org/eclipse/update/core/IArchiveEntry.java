package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.URL;

/**
 * Interface for information objects that can have a short description as a text
 * and a long one in a URL.
 */
public interface IArchiveEntry {
	
	/** 
	 *	 
	 * @return the short description
	 * @since 2.0 
	 */

	String getPath();
	
	/**
	 * Returns a URL containing more information
	 * 
	 * @return the URL pointing to the longer description
	 * @since 2.0 
	 */

	URL getURL();
}

