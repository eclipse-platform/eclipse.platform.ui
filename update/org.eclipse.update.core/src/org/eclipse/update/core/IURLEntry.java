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
public interface IURLEntry {
	
	/** 
	 * Returns the short description of the URLEntry object,
	 * or the label of the associated URL.
	 * 
	 * The text is intended to be translated.
	 * 
	 * @return the short description
	 * @since 2.0 
	 */

	String getAnnotation();
	
	/**
	 * Returns a URL containing more information
	 * 
	 * @return the URL pointing to the longer description
	 * @since 2.0 
	 */

	URL getURL();
}

