package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.*;
public interface IURLNamePair {
/**
 * Returns a displayable label (name) for this URL-Name pair.
 * Returns the empty string if no label for this URL-Name pair
 * is specified in its  manifest file.
 * <p> Note that any translation specified in the manifest
 * file is automatically applied. 
 * </p>
 *
 * @see #getResourceString 
 *
 * @return a displayable string label for this URL-Name pair,
 *    possibly the empty string
 */
public String getLabel();
/**
 * Returns the url for this URL-Name pair.
 *
 * @return the url for this URL-Name pair.
 */
public URL getURL();
/**
 * Returns the url string for this URL-Name pair.
 *
 * @return the url string for this URL-Name pair.
 */
public String getURLString();
}
