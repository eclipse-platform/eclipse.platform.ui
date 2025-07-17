/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 483465
 *     Christoph Läubrich - Bug 567898 - [JFace][HiDPI] ImageDescriptor support alternative naming scheme for high dpi
 *     Daniel Kruegler - #376, #396, #398, #399, #401,
 *                       #679: Ensure that fresh ImageFileNameProvider/ImageDataProvider instances are created to preserve Image#equals invariant.
 *******************************************************************************/
package org.eclipse.jface.resource;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.InternalPolicy;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.ImageFileNameProvider;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.internal.DPIUtil.ElementAtZoom;
import org.eclipse.swt.internal.NativeImageLoader;
import org.eclipse.swt.internal.image.FileFormat;

/**
 * An ImageDescriptor that gets its information from a URL. This class is not
 * public API. Use ImageDescriptor#createFromURL to create a descriptor that
 * uses a URL.
 */
class URLImageDescriptor extends ImageDescriptor implements IAdaptable {

	private ImageFileNameProvider createURLImageFileNameProvider() {
		return zoom -> {
			URL tempURL = getURL(url);
			if (tempURL != null) {
				final boolean logIOException = zoom == 100;
				if (zoom == 100) {
					// Do not take this path if the image file can be scaled up dynamically.
					// The calling image will do that itself!
					return getFilePath(tempURL, logIOException);
				}
				return getZoomedImageSource(tempURL, url, zoom, u -> getFilePath(u, logIOException));
			}
			return null;
		};
	}

	private ImageDataProvider createURLImageDataProvider() {
		return zoom -> getImageData(zoom);
	}

	private static long cumulativeTime;

	/**
	 * Constant for the file protocol for optimized loading
	 */
	private static final String FILE_PROTOCOL = "file";  //$NON-NLS-1$

	private final String url;

	/**
	 * Creates a new URLImageDescriptor.
	 *
	 * @param url The URL to load the image from. Must be non-null.
	 */
	URLImageDescriptor(URL url) {
		super(true);
		this.url = url.toExternalForm();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof URLImageDescriptor other)) {
			return false;
		}
		return other.url.equals(this.url);
	}

	@Override
	public ImageData getImageData(int zoom) {
		URL tempURL = getURL(url);
		if (tempURL != null) {
			if (zoom == 100 || canLoadAtZoom(tempURL, zoom)) {
				return getImageData(tempURL, 100, zoom);
			}
			return getZoomedImageSource(tempURL, url, zoom, u -> getImageData(u, zoom, zoom));
		}
		return null;
	}

	private static <R> R getZoomedImageSource(URL url, String urlString, int zoom, Function<URL, R> getImage) {
		URL xUrl = getxURL(url, zoom);
		if (xUrl != null) {
			R xdata = getImage.apply(xUrl);
			if (xdata != null) {
				return xdata;
			}
		}
		String xpath = getxPath(urlString, zoom);
		if (xpath != null) {
			URL xPathUrl = getURL(xpath);
			if (xPathUrl != null) {
				return getImage.apply(xPathUrl);
			}
		}
		return null;
	}

	private static ImageData getImageData(URL url, int fileZoom, int targetZoom) {
		try (InputStream in = getStream(url)) {
			if (in != null) {
				return loadImageFromStream(new BufferedInputStream(in), fileZoom, targetZoom);
			}
		} catch (SWTException e) {
			if (e.code != SWT.ERROR_INVALID_IMAGE) {
				throw e;
				// fall through otherwise
			}
		} catch (IOException e) {
			Policy.logException(e);
		}
		return null;
	}

	@SuppressWarnings("restriction")
	private static ImageData loadImageFromStream(InputStream stream, int fileZoom, int targetZoom) {
		return NativeImageLoader.load(new ElementAtZoom<>(stream, fileZoom), new ImageLoader(), targetZoom).get(0)
				.element();
	}

	@SuppressWarnings("restriction")
	private static boolean canLoadAtZoom(URL url, int zoom) {
		try (InputStream in = getStream(url)) {
			if (in != null) {
				return FileFormat.canLoadAtZoom(new ElementAtZoom<>(in, 100), zoom);
			}
		} catch (IOException e) {
			Policy.logException(e);
		}
		return false;
	}

	private static InputStream getStream(URL url) {
		try {
			if (InternalPolicy.OSGI_AVAILABLE) {
				url = resolvePathVariables(url);
			}
			return url.openStream();
		} catch (IOException e) {
			if (InternalPolicy.DEBUG_LOG_URL_IMAGE_DESCRIPTOR_MISSING_2x) {
				String path = url.getPath();
				if (path.endsWith("@2x.png") || path.endsWith("@1.5x.png")) { //$NON-NLS-1$ //$NON-NLS-2$
					Policy.getLog().log(Status.warning("High-resolution image missing: " + url, e)); //$NON-NLS-1$
				}
			}
			return null;
		}
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}

	/**
	 * The <code>URLImageDescriptor</code> implementation of this
	 * <code>Object</code> method returns a string representation of this
	 * object which is suitable only for debugging.
	 */
	@Override
	public String toString() {
		return "URLImageDescriptor(" + url + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static URL getxURL(URL url, int zoom) {
		String path = url.getPath();
		int dot = path.lastIndexOf('.');
		if (dot != -1 && (zoom == 150 || zoom == 200)) {
			String lead = path.substring(0, dot);
			String tail = path.substring(dot);
			if (InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_2x_PNG_FOR_GIF && ".gif".equalsIgnoreCase(tail)) { //$NON-NLS-1$
				tail = ".png"; //$NON-NLS-1$
			}
			String x = zoom == 150 ? "@1.5x" : "@2x"; //$NON-NLS-1$ //$NON-NLS-2$
			try {
				String file = lead + x + tail;
				if (url.getQuery() != null) {
					file += '?' + url.getQuery();
				}
				return new URL(url.getProtocol(), url.getHost(), url.getPort(), file);
			} catch (MalformedURLException e) {
				Policy.logException(e);
			}
		}
		return null;

	}

	private static final Pattern XPATH_PATTERN = Pattern.compile("(\\d+)x(\\d+)"); //$NON-NLS-1$

	private static String getxPath(String name, int zoom) {
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

	/**
	 * Returns the filename for the ImageData.
	 *
	 * @return {@link String} or <code>null</code> if the file cannot be found
	 */
	private static String getFilePath(URL url, boolean logException) {
		try {
			if (!InternalPolicy.OSGI_AVAILABLE) {
				return getFilePath(url);
			}
			url = resolvePathVariables(url);
			URL locatedURL = FileLocator.toFileURL(url);
			return getFilePath(locatedURL);
		} catch (IOException e) {
			if (logException) {
				Policy.logException(e);
			} else if (InternalPolicy.DEBUG_LOG_URL_IMAGE_DESCRIPTOR_MISSING_2x) {
				String path = url.getPath();
				if (path.endsWith("@2x.png") || path.endsWith("@1.5x.png")) { //$NON-NLS-1$ //$NON-NLS-2$
					Policy.getLog().log(Status.warning("High-resolution image missing: " + url, e)); //$NON-NLS-1$
				}
			}
			return null;
		}
	}

	private static String getFilePath(URL url) {
		if (FILE_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
			File file = IPath.fromOSString(url.getPath()).toFile();
			if (file.exists()) {
				return file.getPath();
			}
		}
		return null;
	}

	private static URL resolvePathVariables(URL url) {
		URL platformURL = FileLocator.find(url); // Resolve variables within URL's path
		if (platformURL != null) {
			url = platformURL;
		}
		return url;
	}

	@Override
	public Image createImage(boolean returnMissingImageOnError, Device device) {
		long start = 0;
		if (InternalPolicy.DEBUG_TRACE_URL_IMAGE_DESCRIPTOR) {
			start = System.nanoTime();
		}
		try {
			if (InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_2x) {
				if (!InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_DIRECTLY) {
					try {
						// We really want a fresh ImageFileNameProvider instance to make
						// sure the code that uses created images can use equals(),
						// see Image#equals
						return new Image(device, createURLImageFileNameProvider());
					} catch (SWTException | IllegalArgumentException exception) {
						// If we fail fall back to the slower input stream method.
					}
				}

				Image image = null;
				try {
					// We really want a fresh ImageDataProvider instance to make
					// sure the code that uses created images can use equals(),
					// see Image#equals
					image = new Image(device, createURLImageDataProvider());
				} catch (SWTException e) {
					if (e.code != SWT.ERROR_INVALID_IMAGE) {
						throw e;
					}
				} catch (IllegalArgumentException e) {
					// fall through
				}
				if (image == null && returnMissingImageOnError) {
					try {
						image = new Image(device, DEFAULT_IMAGE_DATA);
					} catch (SWTException nextException) {
						return null;
					}
				}
				return image;
			}
			if (InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_DIRECTLY) {
				return super.createImage(returnMissingImageOnError, device);
			}

			// Try to see if we can optimize using SWTs file based image support.
			URL pathURL = getURL(url);
			if (pathURL != null) {
				String path = getFilePath(pathURL, true);
				if (path != null) {
					try {
						return new Image(device, path);
					} catch (SWTException exception) {
						// If we fail fall back to the slower input stream
						// method.
					}
				}
			}
			return super.createImage(returnMissingImageOnError, device);
		} finally {
			if (InternalPolicy.DEBUG_TRACE_URL_IMAGE_DESCRIPTOR) {
				long time = System.nanoTime() - start;
				cumulativeTime += time;
				System.out.println("Accumulated time (ms) to load URLImageDescriptor images: " + cumulativeTime / 1000000); //$NON-NLS-1$
			}
		}
	}

	private static URL getURL(String urlString) {
		URL result = null;
		try {
			result = new URL(urlString);
		} catch (MalformedURLException e) {
			Policy.logException(e);
		}
		return result;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == URL.class) {
			return adapter.cast(getURL(url));
		}
		if (adapter == ImageFileNameProvider.class) {
			return adapter.cast(createURLImageFileNameProvider());
		}
		if (adapter == ImageDataProvider.class) {
			return adapter.cast(createURLImageDataProvider());
		}
		return null;
	}

}
