package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.*;
import java.io.Serializable;
public class URLNamePair extends URLNamePairModel implements IURLNamePair, Serializable {
	private NLResourceHelper fNLHelper = null;
/**
 * URLNamePair constructor comment.
 */
public URLNamePair() {
	super();
}
/**
 * Returns the URL of this plug-in's install directory. 
 * This is the ..../plugins/plugin-dir directory where plug-in
 * files are stored.
 *
 * @return the URL of this plug-in's install directory
 */
public java.net.URL getInstallURL() {

	try {
		return new URL(_getInstallURL());
	} catch (MalformedURLException e) {
		return null;
	}
}
/**
 * Returns a displayable label (name) for this URL.
 * Returns the empty string if no label for this URLt
 * is specified in its  manifest file.
 * <p> Note that any translation specified in the manifest
 * file is automatically applied. 
 * </p>
 *
 * @see #getResourceString 
 *
 * @return a displayable string label for this URL,
 *    possibly the empty string
 */
public String getLabel() {
	String s = _getName();
	if (fNLHelper == null)
		fNLHelper = new NLResourceHelper( DEFAULT_BUNDLE_NAME,getInstallURL());
	return s==null ? "" : fNLHelper.getResourceString(s);
}
/**
 * Returns the url for this URL-Name pair.
 *
 * @return the url for this URL-Name pair.
 */
public java.net.URL getURL() {
	try {
		return new URL(_getURL());
	} catch (MalformedURLException e) {
		return null;
	}
}
/**
 * Returns the url string for this URL.
 *
 * @return the url string for this URL
 */
public String getURLString() {
	return _getURL();
}
}
