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

import static org.eclipse.jface.resource.FileImageDescriptor.getImageSource;

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

/**
 * An ImageDescriptor that gets its information from a URL. This class is not
 * public API. Use ImageDescriptor#createFromURL to create a descriptor that
 * uses a URL.
 */
class URLImageDescriptor extends ImageDescriptor implements IAdaptable {

	private static ImageFileNameProvider createURLImageFileNameProvider(String url) {
		return zoom -> {
			final boolean logIOException = zoom == 100;
			return getImageSource(url, URLImageDescriptor::getURL, URLImageDescriptor::getxURL,
					u -> getFilePath(u, logIOException), zoom);
		};
	}

	private static ImageDataProvider createURLImageDataProvider(String url) {
		return zoom -> getImageData(url, zoom);
	}

	private static long cumulativeTime;

	/**
	 * Constant for the file protocol for optimized loading
	 */
	private static final String FILE_PROTOCOL = "file"; //$NON-NLS-1$

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

	@Deprecated
	@Override
	public ImageData getImageData() {
		return getImageData(getURL(url));
	}

	@Override
	public ImageData getImageData(int zoom) {
		return getImageData(url, zoom);
	}

	private static ImageData getImageData(String url, int zoom) {
		return getImageSource(url, URLImageDescriptor::getURL, URLImageDescriptor::getxURL,
				URLImageDescriptor::getImageData, zoom);
	}

	private static ImageData getImageData(URL url) {
		try (InputStream in = getStream(url)) {
			if (in != null) {
				return new ImageData(in);
			}
		} catch (SWTException e) {
			if (e.code != SWT.ERROR_INVALID_IMAGE) {
				throw e;
				// fall through otherwise
			}
		} catch (IOException e) {
			Policy.getLog().log(Status.error(e.getLocalizedMessage(), e));
		}
		return null;
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
		String name = url.getPath();
		String file = FileImageDescriptor.getxName(name, zoom);
		if (file != null) {
			try {
				if (url.getQuery() != null) {
					file += '?' + url.getQuery();
				}
				return new URL(url.getProtocol(), url.getHost(), url.getPort(), file);
			} catch (MalformedURLException e) {
				Policy.getLog().log(Status.error(e.getLocalizedMessage(), e));
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
					Policy.getLog().log(Status.warning("High-resolution image missing: " + url, e)); //$NON-NLS-1$
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
						return new Image(device, createURLImageFileNameProvider(url));
					} catch (SWTException | IllegalArgumentException exception) {
						// If we fail fall back to the slower input stream method.
					}
				}

				Image image = null;
				try {
					// We really want a fresh ImageDataProvider instance to make
					// sure the code that uses created images can use equals(),
					// see Image#equals
					image = new Image(device, createURLImageDataProvider(url));
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
		try {
			return new URL(urlString);
		} catch (MalformedURLException e) {
			Policy.getLog().log(Status.error(e.getLocalizedMessage(), e));
			return null;
		}
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == URL.class) {
			return adapter.cast(getURL(url));
		}
		if (adapter == ImageFileNameProvider.class) {
			return adapter.cast(createURLImageFileNameProvider(url));
		}
		if (adapter == ImageDataProvider.class) {
			return adapter.cast(createURLImageDataProvider(url));
		}
		return null;
	}

}
