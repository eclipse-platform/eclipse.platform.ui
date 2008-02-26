/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.internal.JFaceActivator;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;

/**
 * An image descriptor that loads its image information from a file.
 */
class FileImageDescriptor extends ImageDescriptor {

	/**
	 * The class whose resource directory contain the file, or <code>null</code>
	 * if none.
	 */
	private Class location;

	/**
	 * The name of the file.
	 */
	private String name;

	/**
	 * Creates a new file image descriptor. The file has the given file name and
	 * is located in the given class's resource directory. If the given class is
	 * <code>null</code>, the file name must be absolute.
	 * <p>
	 * Note that the file is not accessed until its <code>getImageDate</code>
	 * method is called.
	 * </p>
	 * 
	 * @param clazz
	 *            class for resource directory, or <code>null</code>
	 * @param filename
	 *            the name of the file
	 */
	FileImageDescriptor(Class clazz, String filename) {
		this.location = clazz;
		this.name = filename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof FileImageDescriptor)) {
			return false;
		}
		FileImageDescriptor other = (FileImageDescriptor) o;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
	 */
	public ImageData getImageData() {

		if (JFaceActivator.getBundleContext() == null) {// Stand-alone case

			if (location == null)
				return new ImageData(name);

			return new ImageData(location.getResource(name).getFile());
		}

		// OSGi is present so we need to use it to look up a class
		try {
			String path = getFilePath();
			if (path != null)
				return new ImageData(path);
		} catch (SWTException e) {
			if (e.code != SWT.ERROR_INVALID_IMAGE) {
				throw e;
				// fall through otherwise
			}
		}
		return null;

	}

	/**
	 * Returns the filename for the ImageData.
	 * 
	 * @return {@link String} or <code>null</code> if the file cannot be found
	 */
	private String getFilePath() {

		if (location == null)
			return name;
		try {
			return FileLocator.toFileURL(location.getResource(name)).getPath();
		} catch (IOException e) {
			Policy.logException(e);
			return null;
		}
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int code = name.hashCode();
		if (location != null) {
			code += location.hashCode();
		}
		return code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		/**
		 * The <code>FileImageDescriptor</code> implementation of this
		 * <code>Object</code> method returns a string representation of this
		 * object which is suitable only for debugging.
		 */
		return "FileImageDescriptor(location=" + location + ", name=" + name + ")";//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
	}
}
