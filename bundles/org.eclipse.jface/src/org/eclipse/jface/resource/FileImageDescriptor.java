/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.InternalPolicy;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageFileNameProvider;

/**
 * An image descriptor that loads its image information from a file.
 */
class FileImageDescriptor extends ImageDescriptor {

	private class ImageProvider implements ImageFileNameProvider {
		@Override
		public String getImagePath(int zoom) {
			String xName = getxName(name, zoom);
			if (xName != null) {
				return getFilePath(xName, zoom == 100);
			}
			return null;
		}
	}

	/**
	 * The class whose resource directory contain the file, or <code>null</code>
	 * if none.
	 */
	private Class<?> location;

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
	FileImageDescriptor(Class<?> clazz, String filename) {
		this.location = clazz;
		this.name = filename;
	}

	@Override
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * The FileImageDescriptor implementation of this method is not used by
	 * {@link ImageDescriptor#createImage(boolean, Device)} as of version
	 * 3.4 so that the SWT OS optimised loading can be used.
	 */
	@Override
	public ImageData getImageData(int zoom) {
		InputStream in = getStream(zoom);
		ImageData result = null;
		if (in != null) {
			try {
				result = new ImageData(in);
			} catch (SWTException e) {
				if (e.code != SWT.ERROR_INVALID_IMAGE) {
					throw e;
					// fall through otherwise
				}
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					// System.err.println(getClass().getName()+".getImageData():
					// "+
					// "Exception while closing InputStream : "+e);
				}
			}
		}
		return result;
	}

	/**
	 * Returns a stream on the image contents. Returns null if a stream could
	 * not be opened.
	 *
	 * @param zoom the zoom factor
	 * @return the buffered stream on the file or <code>null</code> if the
	 *         file cannot be found
	 */
	private InputStream getStream(int zoom) {
		String xName = getxName(name, zoom);
		if (xName == null) {
			return null;
		}
		InputStream is = null;

		if (location != null) {
			is = location.getResourceAsStream(xName);

		} else {
			try {
				is = new FileInputStream(xName);
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		if (is == null) {
			return null;
		}
		return new BufferedInputStream(is);
	}

	static String getxName(String name, int zoom) {
		// see also URLImageDescriptor#getxURL(URL, int)
		if (zoom == 100) {
			return name;
		}
		int dot = name.lastIndexOf('.');
		if (dot != -1 && (zoom == 150 || zoom == 200)) {
			String lead = name.substring(0, dot);
			String tail = name.substring(dot);
			if (InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_2x_PNG_FOR_GIF && ".gif".equalsIgnoreCase(tail)) { //$NON-NLS-1$
				tail = ".png"; //$NON-NLS-1$
			}
			String x = zoom == 150 ? "@1.5x" : "@2x"; //$NON-NLS-1$ //$NON-NLS-2$
			String file = lead + x + tail;
			return file;
		}
		return null;
	}

	@Override
	public int hashCode() {
		int code = name.hashCode();
		if (location != null) {
			code += location.hashCode();
		}
		return code;
	}

	/**
	 * The <code>FileImageDescriptor</code> implementation of this
	 * <code>Object</code> method returns a string representation of this
	 * object which is suitable only for debugging.
	 */
	@Override
	public String toString() {
		return "FileImageDescriptor(location=" + location + ", name=" + name + ")";//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
	}

	@Override
	public Image createImage(boolean returnMissingImageOnError, Device device) {
		if (InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_2x) {
			try {
				return new Image(device, new ImageProvider());
			} catch (SWTException exception) {
				// If we fail, fall back to the old 1x implementation.
			} catch (IllegalArgumentException exception) {
				// If we fail, fall back to the old 1x implementation.
			}
		}

		String path = getFilePath(name, true);
		if (path == null)
			return createDefaultImage(returnMissingImageOnError, device);
		try {
			return new Image(device, path);
		} catch (SWTException exception) {
			//if we fail try the default way using a stream
		}
		return super.createImage(returnMissingImageOnError, device);
	}

	/**
	 * Return default image if returnMissingImageOnError is true.
	 *
	 * @param device
	 * @return Image or <code>null</code>
	 */
	private Image createDefaultImage(boolean returnMissingImageOnError,
			Device device) {
		try {
			if (returnMissingImageOnError)
				return new Image(device, DEFAULT_IMAGE_DATA);
		} catch (SWTException nextException) {
			return null;
		}
		return null;
	}

	/**
	 * Returns the filename for the ImageData.
	 *
	 * @param name the file name
	 * @return {@link String} or <code>null</code> if the file cannot be found
	 */
	String getFilePath(String name, boolean logIOException) {

		if (location == null)
			return new Path(name).toOSString();

		URL resource = location.getResource(name);

		if (resource == null)
			return null;
		try {
			if (!InternalPolicy.OSGI_AVAILABLE) {// Stand-alone case

				return new Path(resource.getFile()).toOSString();
			}
			return new Path(FileLocator.toFileURL(resource).getPath()).toOSString();
		} catch (IOException e) {
			if (logIOException) {
				Policy.logException(e);
			} else if (InternalPolicy.DEBUG_LOG_URL_IMAGE_DESCRIPTOR_MISSING_2x) {
				if (name.endsWith("@2x.png") || name.endsWith("@1.5x.png")) { //$NON-NLS-1$ //$NON-NLS-2$
					String message = "High-resolution image missing: " + location + ' ' + name; //$NON-NLS-1$
					Policy.getLog().log(new Status(IStatus.WARNING, Policy.JFACE, message, e));
				}
			}
			return null;
		}
	}
}
