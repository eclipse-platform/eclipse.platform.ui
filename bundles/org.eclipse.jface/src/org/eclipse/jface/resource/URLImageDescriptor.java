/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
/**
 * An ImageDescriptor that gets its information from a URL.
 * This class is not public API.  Use ImageDescriptor#createFromURL
 * to create a descriptor that uses a URL.
 */
class URLImageDescriptor extends ImageDescriptor {
	private URL url;
/**
 * Creates a new URLImageDescriptor.
 * @param url The URL to load the image from.  Must be non-null.
 */
URLImageDescriptor(URL url) {
	this.url = url;
}
/* (non-Javadoc)
 * Method declared on Object.
 */
public boolean equals(Object o) {
	if (!(o instanceof URLImageDescriptor)) {
		return false;
	}
	return ((URLImageDescriptor)o).url.equals(this.url);
}
/* (non-Javadoc)
 * Method declared on ImageDesciptor.
 * Returns null if the image data cannot be read.
 */
public ImageData getImageData() {
	InputStream in = getStream();
	if (in != null) {
		try {
			return new ImageData(in);
		}
		catch (SWTException e) {
			if (e.code == SWT.ERROR_INVALID_IMAGE)
				return null;
			else
				throw e;
		}
		finally {
			try {
				in.close();
			}
			catch (IOException e) {
				return null;
			}
		}
	} else {
		return null;
	}
}
/**
 * Returns a stream on the image contents.  Returns
 * null if a stream could not be opened.
 */
protected InputStream getStream() {
	try {
		return url.openStream();
	} catch (IOException e) {
		return null;
	}
}
/* (non-Javadoc)
 * Method declared on Object.
 */
public int hashCode() {
	return url.hashCode();
}
/* (non-Javadoc)
 * Method declared on Object.
 */
/**
 * The <code>URLImageDescriptor</code> implementation of this <code>Object</code> method 
 * returns a string representation of this object which is suitable only for debugging.
 */
public String toString() {
	return "URLImageDescriptor(" + url + ")"; //$NON-NLS-1$ //$NON-NLS-2$
}
}
