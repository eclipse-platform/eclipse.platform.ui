package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

/**
 * Interface for information objects that can have a short description as a text
 * and a long one in a URL.
 */
public interface IInfo {
	
	/** 
	 * Returns the short description of the Info object,
	 * or the label of the associated URL.
	 * 
	 * The text is intended to be translated.
	 * 
	 * @return the short description
	 */
	String getText();
	
	/**
	 * Returns a URL containing more information
	 * 
	 * @return the URL pointing to the longer description
	 */
	URL getURL();
}

