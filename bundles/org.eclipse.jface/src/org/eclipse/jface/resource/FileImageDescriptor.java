/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christoph Läubrich - Bug 567898 - [JFace][HiDPI] ImageDescriptor support alternative naming scheme for high dpi
 *     Daniel Kruegler - #375, #376, #378, #396, #398, #401,
 *                       #679: Ensure that a fresh ImageFileNameProvider instance is created to preserve Image#equals invariant.
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
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
class FileImageDescriptor extends ImageDescriptor implements IAdaptable {

	private class ImageProvider implements ImageFileNameProvider {

		@Override
		public String getImagePath(int zoom) {
			final boolean logIOException = zoom == 100;
			if (zoom == 100) {
				return getFilePath(name, logIOException);
			}
			String xName = getxName(name, zoom);
			if (xName != null) {
				String xResult = getFilePath(xName, logIOException);
				if (xResult != null) {
					return xResult;
				}
			}
			String xPath = getxPath(name, zoom);
			if (xPath != null) {
				String xResult = getFilePath(xPath, logIOException);
				if (xResult != null) {
					return xResult;
				}
			}
			return null;
		}

	}

	private static final Pattern XPATH_PATTERN = Pattern.compile("(\\d+)x(\\d+)"); //$NON-NLS-1$

	/**
	 * The class whose resource directory contain the file, or <code>null</code>
	 * if none.
	 */
	private final Class<?> location;

	/**
	 * The name of the file.
	 */
	private final String name;

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
		super(true);
		this.location = clazz;
		this.name = filename;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FileImageDescriptor)) {
			return false;
		}
		FileImageDescriptor other = (FileImageDescriptor) o;
		return Objects.equals(location, other.location) && Objects.equals(name, other.name);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The FileImageDescriptor implementation of this method is not used by
	 * {@link ImageDescriptor#createImage(boolean, Device)} as of version
	 * 3.4 so that the SWT OS optimized loading can be used.
	 */
	@Override
	public ImageData getImageData(int zoom) {
		InputStream in = getStream(zoom);
		if (in != null) {
			try (BufferedInputStream stream = new BufferedInputStream(in)) {
				return new ImageData(stream, zoom);
			} catch (SWTException e) {
				if (e.code != SWT.ERROR_INVALID_IMAGE) {
					throw e;
					// fall through otherwise
				}
			} catch (IOException ioe) {
				// fall through
			}
		}
		return null;
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
		if (zoom == 100) {
			return getStream(name);
		}

		InputStream xstream = getStream(getxName(name, zoom));
		if (xstream != null) {
			return xstream;
		}

		InputStream xpath = getStream(getxPath(name, zoom));
		if (xpath != null) {
			return xpath;
		}

		return null;
	}

	/**
	 * try to obtain a stream for a given name, if the name does not match a valid
	 * resource null is returned
	 *
	 * @param fileName the filename to check
	 * @return an {@link InputStream} to read from, or <code>null</code> if fileName
	 *         does not denotes an existing resource
	 */
	private InputStream getStream(String fileName) {
		if (fileName != null) {
			if (location != null) {
				return location.getResourceAsStream(fileName);
			}
			try {
				return new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		return null;
	}

	static String getxPath(String name, int zoom) {
		Matcher matcher = XPATH_PATTERN.matcher(name);
		if (matcher.find()) {
			try {
				int currentWidth = Integer.parseInt(matcher.group(1));
				int desiredWidth = Math.round((zoom / 100f) * currentWidth);
				int currentHeight = Integer.parseInt(matcher.group(2));
				int desiredHeight = Math.round((zoom / 100f) * currentHeight);
				String lead = name.substring(0, matcher.start(1));
				String tail = name.substring(matcher.end(2));
				return lead + desiredWidth + "x" + desiredHeight + tail; //$NON-NLS-1$
			} catch (RuntimeException e) {
				// should never happen but if then we can't use the alternative name...
			}
		}
		return null;
	}

	static String getxName(String name, int zoom) {
		int dot = name.lastIndexOf('.');
		if (dot != -1 && (zoom == 150 || zoom == 200)) {
			String lead = name.substring(0, dot);
			String tail = name.substring(dot);
			if (InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_2x_PNG_FOR_GIF && ".gif".equalsIgnoreCase(tail)) { //$NON-NLS-1$
				tail = ".png"; //$NON-NLS-1$
			}
			String x = zoom == 150 ? "@1.5x" : "@2x"; //$NON-NLS-1$ //$NON-NLS-2$
			return lead + x + tail;
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
				// We really want a fresh ImageFileNameProvider instance to make
				// sure the code that uses created images can use equals(),
				// see Image#equals
				return new Image(device, new ImageProvider());
			} catch (SWTException | IllegalArgumentException exception) {
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
			return IPath.fromOSString(name).toOSString();

		URL resource = location.getResource(name);

		if (resource == null)
			return null;
		try {
			if (!InternalPolicy.OSGI_AVAILABLE) {// Stand-alone case
				return IPath.fromOSString(resource.getFile()).toOSString();
			}
			return IPath.fromOSString(FileLocator.toFileURL(resource).getPath()).toOSString();
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

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == URL.class) {
			if (location != null && name != null) {
				return adapter.cast(location.getResource(name));
			}
		}
		if (adapter == ImageFileNameProvider.class) {
			return adapter.cast(new ImageProvider());
		}
		return null;
	}

}
