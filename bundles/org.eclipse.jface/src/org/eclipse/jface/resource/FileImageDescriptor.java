/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;

/**
 * An image descriptor that loads its image information
 * from a file.
 */
class FileImageDescriptor extends ImageDescriptor {

	/**
	 * The class whose resource directory contain the file, 
	 * or <code>null</code> if none.
	 */
	private Class location;

	/**
	 * The name of the file.
	 */
	private String name;
/**
 * Creates a new file image descriptor.
 * The file has the given file name and is located
 * in the given class's resource directory. If the given
 * class is <code>null</code>, the file name must be absolute.
 * <p>
 * Note that the file is not accessed until its
 * <code>getImageDate</code> method is called.
 * </p>
 *
 * @param clazz class for resource directory, or
 *   <code>null</code>
 * @param filename the name of the file
 */
FileImageDescriptor(Class clazz, String filename) {
	this.location = clazz;
	this.name = filename;
}
/* (non-Javadoc)
 * Method declared on Object.
 */
public boolean equals(Object o) {
	if (!(o instanceof FileImageDescriptor)) {
		return false;
	}
	FileImageDescriptor other = (FileImageDescriptor)o;
	if (location != null) {
		if (!location.equals(other.location)) {
			return false;
		}
	} else {
		if (other.location != null) {
			return false;
		}
	}
	return name.equals(other.name);
}
/* (non-Javadoc)
 * Method declared on ImageDesciptor.
 * Returns null if the image data cannot be read.
 */
public ImageData getImageData() {
	InputStream in = getStream();
	ImageData result = null;
	if (in != null) {
		try {
			result= new ImageData(in);
		} catch (SWTException e) {
			if (e.code != SWT.ERROR_INVALID_IMAGE)
					throw e;
				// fall through otherwise
		} finally {
			try {
				in.close();
			}catch (IOException e) {
				//System.err.println(getClass().getName()+".getImageData(): "+
				//  "Exception while closing InputStream : "+e);
			}
		}
	}
	return result;
}
/**
 * Returns a stream on the image contents.  Returns
 * null if a stream could not be opened.
 * 
 * @return the buffered stream on the file or <code>null</code>
 * if the file cannot be found
 */
private InputStream getStream() {
	InputStream is = null;

	if (location != null) {
		is = location.getResourceAsStream(name);
		
	} else {
		try {
			is = new FileInputStream(name);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	if(is == null)
		return null;
	else
		return new BufferedInputStream(is);
}
/* (non-Javadoc)
 * Method declared on Object.
 */
public int hashCode() {
	int code = name.hashCode();
	if (location != null) {
		code += location.hashCode();
	}
	return code;
}
/* (non-Javadoc)
 * Method declared on Object.
 */
/**
 * The <code>FileImageDescriptor</code> implementation of this <code>Object</code> method 
 * returns a string representation of this object which is suitable only for debugging.
 */
public String toString() {
	return "FileImageDescriptor(location=" + location + ", name=" + name + ")";//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
}
}
