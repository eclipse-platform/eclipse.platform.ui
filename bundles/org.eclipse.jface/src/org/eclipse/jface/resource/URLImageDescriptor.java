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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 483465
 *     Christoph Läubrich - Bug 567898 - [JFace][HiDPI] ImageDescriptor support alternative naming scheme for high dpi
 *     Daniel Kruegler - #376, #396, #398, #399, #401,
 *                       #679: Ensure that fresh ImageFileNameProvider/ImageDataProvider instances are created to preserve Image#equals invariant.
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

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
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.ImageFileNameProvider;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.internal.DPIUtil.ElementAtZoom;
import org.eclipse.swt.internal.NativeImageLoader;

/**
 * An ImageDescriptor that gets its information from a URL. This class is not
 * public API. Use ImageDescriptor#createFromURL to create a descriptor that
 * uses a URL.
 */
class URLImageDescriptor extends ImageDescriptor implements IAdaptable {

	private static class URLImageFileNameProvider implements ImageFileNameProvider {

		private final String url;

		public URLImageFileNameProvider(String url) {
			this.url = url;
		}

		@Override
		public String getImagePath(int zoom) {
			URL tempURL = getURL(url);
			if (tempURL != null) {
				final boolean logIOException = zoom == 100;
				if (zoom == 100) {
					return getFilePath(tempURL, logIOException);
				}
				SourceAtZoom<URL> xUrl = getxURL(tempURL, zoom);
				if (xUrl != null) {
					String xResult = getFilePath(xUrl.source(), logIOException);
					if (xResult != null) {
						return xResult;
					}
				}
				SourceAtZoom<String> xpath = FileImageDescriptor.getxPath(url, zoom);
				if (xpath != null) {
					URL xPathUrl = getURL(xpath.source());
					if (xPathUrl != null) {
						return getFilePath(xPathUrl, logIOException);
					}
				}
			}
			return null;
		}

	}

	/**
	 * Represents an element, such as InputStream, Path or URL, at a specific zoom
	 * level.
	 *
	 * @param <T> type of the element to be presented, e.g., {@link InputStream}
	 */
	record SourceAtZoom<T>(T source, int zoom) {
		public SourceAtZoom {
			if (source == null) {
				SWT.error(SWT.ERROR_NULL_ARGUMENT);
			}
			if (zoom <= 0) {
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}
		}
	}

	private static class URLImageDataProvider implements ImageDataProvider {

		private final String url;

		public URLImageDataProvider(String url) {
			this.url = url;
		}

		@Override
		public ImageData getImageData(int zoom) {
			return URLImageDescriptor.getImageData(url, zoom);
		}

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
		if (!(o instanceof URLImageDescriptor)) {
			return false;
		}
		return ((URLImageDescriptor) o).url.equals(this.url);
	}

	@Deprecated
	@Override
	public ImageData getImageData() {
		// TODO What to do with this? targetZoom and fileZoom is unknown.
		// the first can be extracted from the url but the latter?
		return getImageData(getURL(url), 100, 100);
	}

	@Override
	public ImageData getImageData(int zoom) {
		return getImageData(url, zoom);
	}

	private static ImageData getImageData(String url, int zoom) {
		URL tempURL = getURL(url);
		if (tempURL != null) {
			if (tempURL.toString().endsWith(".svg")) { //$NON-NLS-1$
				return getImageData(tempURL, 100, zoom);
			}
			if (zoom == 100) {
				return getImageData(tempURL, 100, zoom);
			}
			SourceAtZoom<URL> xUrl = getxURL(tempURL, zoom);
			if (xUrl != null) {
				ImageData xdata = getImageData(xUrl.source(), xUrl.zoom(), zoom);
				if (xdata != null) {
					return xdata;
				}
			}
			SourceAtZoom<String> xpath = FileImageDescriptor.getxPath(url, zoom);
			if (xpath != null) {
				URL xPathUrl = getURL(xpath.source());
				if (xPathUrl != null) {
					return getImageData(xPathUrl, xpath.zoom(), zoom);
				}
			}
		}
		return null;
	}

	@SuppressWarnings("restriction")
	private static ImageData getImageData(URL url, int fileZoom, int targetZoom) {
		ImageData result = null;
		try (InputStream in = getStream(url)) {
			if (in != null) {
				result = NativeImageLoader
						.load(new ElementAtZoom<>(in, fileZoom), new ImageLoader(), targetZoom).get(0)
						.element();
			}
		} catch (SWTException e) {
			if (e.code != SWT.ERROR_INVALID_IMAGE) {
				throw e;
				// fall through otherwise
			}
		} catch (IOException e) {
			Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, e.getLocalizedMessage(), e));
		}
		return result;
	}

	/**
	 * Returns a stream on the image contents. Returns null if a stream could
	 * not be opened.
	 *
	 * @return the stream for loading the data
	 */
	protected InputStream getStream() {
		return getStream(getURL(url));
	}

	private static InputStream getStream(URL url) {
		if (url == null) {
			return null;
		}

		try {
			if (InternalPolicy.OSGI_AVAILABLE) {
				URL platformURL = FileLocator.find(url);
				if (platformURL != null) {
					url = platformURL;
				}
			}
			return new BufferedInputStream(url.openStream());
		} catch (IOException e) {
			if (InternalPolicy.DEBUG_LOG_URL_IMAGE_DESCRIPTOR_MISSING_2x) {
				String path = url.getPath();
				if (path.endsWith("@2x.png") || path.endsWith("@1.5x.png")) { //$NON-NLS-1$ //$NON-NLS-2$
					String message = "High-resolution image missing: " + url; //$NON-NLS-1$
					Policy.getLog().log(new Status(IStatus.WARNING, Policy.JFACE, message, e));
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

	private static SourceAtZoom<URL> getxURL(URL url, int zoom) {
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
				if (x == "@1.5x") { //$NON-NLS-1$
					return new SourceAtZoom<>(new URL(url.getProtocol(), url.getHost(), url.getPort(), file), 150);
				}
				return new SourceAtZoom<>(new URL(url.getProtocol(), url.getHost(), url.getPort(), file), 200);
			} catch (MalformedURLException e) {
				Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, e.getLocalizedMessage(), e));
			}
		}
		return null;

	}

	/**
	 * Returns the filename for the ImageData.
	 *
	 * @return {@link String} or <code>null</code> if the file cannot be found
	 */
	private static String getFilePath(URL url, boolean logIOException) {
		try {
			if (!InternalPolicy.OSGI_AVAILABLE) {
				if (FILE_PROTOCOL.equalsIgnoreCase(url.getProtocol()))
					return IPath.fromOSString(url.getFile()).toOSString();
				return null;
			}

			URL platformURL = FileLocator.find(url);
			if (platformURL != null) {
				url = platformURL;
			}
			URL locatedURL = FileLocator.toFileURL(url);
			if (FILE_PROTOCOL.equalsIgnoreCase(locatedURL.getProtocol())) {
				String filePath = IPath.fromOSString(locatedURL.getPath()).toOSString();
				if (Files.exists(Path.of(filePath))) {
					return filePath;
				}
			}
			return null;
		} catch (IOException e) {
			if (logIOException) {
				Policy.logException(e);
			} else if (InternalPolicy.DEBUG_LOG_URL_IMAGE_DESCRIPTOR_MISSING_2x) {
				String path = url.getPath();
				if (path.endsWith("@2x.png") || path.endsWith("@1.5x.png")) { //$NON-NLS-1$ //$NON-NLS-2$
					String message = "High-resolution image missing: " + url; //$NON-NLS-1$
					Policy.getLog().log(new Status(IStatus.WARNING, Policy.JFACE, message, e));
				}
			}
			return null;
		}
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
						return new Image(device, new URLImageFileNameProvider(url));
					} catch (SWTException | IllegalArgumentException exception) {
						// If we fail fall back to the slower input stream method.
					}
				}

				Image image = null;
				try {
					// We really want a fresh ImageDataProvider instance to make
					// sure the code that uses created images can use equals(),
					// see Image#equals
					image = new Image(device, new URLImageDataProvider(url));
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
			Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, e.getLocalizedMessage(), e));
		}
		return result;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == URL.class) {
			return adapter.cast(getURL(url));
		}
		if (adapter == ImageFileNameProvider.class) {
			return adapter.cast(new URLImageFileNameProvider(url));
		}
		if (adapter == ImageDataProvider.class) {
			return adapter.cast(new URLImageDataProvider(url));
		}
		return null;
	}

}
