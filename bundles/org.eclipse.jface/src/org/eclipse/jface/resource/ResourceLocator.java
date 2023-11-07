/*******************************************************************************
 * Copyright (c) 2000-2019 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 520080
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * This class contains a collection of helper methods for finding JFace
 * resources in bundles.
 *
 * @since 3.17
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class ResourceLocator {

	/**
	 * Returns an <code>{@link Optional}&lt;{@link URL}&gt;</code> containing the
	 * URL for the given path in the given bundle. Returns {@link Optional#empty()}
	 * if the URL could not be computed or created.
	 *
	 * @see FileLocator#find(URL)
	 * @see ImageDescriptor#createFromURL(URL)
	 *
	 * @param bundleSymbolicName the {@link Bundle} symbolic name
	 * @param filePath     the path of the resource file in the given bundle,
	 *                     relative to the root of the bundle
	 * @return an <code>{@link Optional}&lt;{@link URL}&gt;</code> or
	 *         {@link Optional#empty()}.
	 */
	public static Optional<URL> locate(String bundleSymbolicName, String filePath) {
		// Don't resolve the URL here, but create a URL using the
		// "platform:/plugin" protocol, which also supports fragments.
		// Caveat: The resulting URL may contain $nl$ etc., which is not
		// directly supported by PlatformURLConnection and needs to go through
		// FileLocator#find(URL), see bug 250432.
		IPath uriPath = IPath.fromOSString("/plugin").append(bundleSymbolicName).append(filePath); //$NON-NLS-1$
		URL url;
		try {
			URI uri = new URI("platform", null, uriPath.toString(), null); //$NON-NLS-1$
			url = uri.toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			return Optional.empty();
		}

		// look for the resource
		URL fullPathString = FileLocator.find(url);
		if (fullPathString == null) {
			// If not found, reinterpret imageFilePath as full URL.
			// This is unspecified, but apparently widely-used, see bug 395126.
			try {
				url = new URL(filePath);
			} catch (MalformedURLException e) {
				return Optional.empty();
			}
		}
		return Optional.ofNullable(url);
	}

	/**
	 * Returns an <code>{@link Optional}&lt;{@link URL}&gt;</code> containing the
	 * URL for the given path in the given bundle. Returns {@link Optional#empty()}
	 * if the URL could not be computed or created.
	 *
	 * @see FileLocator#find(URL)
	 * @see ImageDescriptor#createFromURL(URL)
	 *
	 * @param classFromBundle A class defined by a bundle class loader.
	 * @param filePath        the path of the resource file in the given bundle,
	 *                        relative to the root of the bundle
	 * @return an <code>{@link Optional}&lt;{@link URL}&gt;</code> or
	 *         {@link Optional#empty()}.
	 */
	public static Optional<URL> locate(Class<?> classFromBundle, String filePath) {
		return Optional.ofNullable(FileLocator.find(FrameworkUtil.getBundle(classFromBundle), IPath.fromOSString(filePath)));
	}

	/**
	 * Returns a new <code>{@link Optional}&lt;{@link ImageDescriptor}&gt;</code>
	 * for an image file located within the specified bundle or
	 * {@link Optional#empty()}.
	 * <p>
	 * This is a convenience method that simply locates the image file in within the
	 * bundle. The path is relative to the root of the bundle, and takes into
	 * account files coming from bundle fragments. The path may include $arg$
	 * elements. However, the path must not have a leading "." or path separator.
	 * Clients should use a path like "icons/mysample.png" rather than
	 * "./icons/mysample.png" or "/icons/mysample.png".
	 * </p>
	 *
	 * @see ImageDescriptor#createFromURL(URL)
	 *
	 * @param bundleSymbolicName the {@link Bundle} symbolic name
	 * @param imageFilePath      the path of the image file in the given bundle,
	 *                           relative to the root of the bundle
	 * @return <code>{@link Optional}&lt;{@link ImageDescriptor}&gt;</code> or
	 *         {@link Optional#empty()}
	 */
	public static Optional<ImageDescriptor> imageDescriptorFromBundle(String bundleSymbolicName, String imageFilePath) {
		Optional<URL> locate = locate(bundleSymbolicName, imageFilePath);
		return locate.map(ImageDescriptor::createFromURL);
	}

	/**
	 * Returns a new <code>{@link Optional}&lt;{@link ImageDescriptor}&gt;</code>
	 * for an image file located within the specified bundle or
	 * {@link Optional#empty()}.
	 * <p>
	 * This is a convenience method that simply locates the image file in within the
	 * bundle. The path is relative to the root of the bundle, and takes into
	 * account files coming from bundle fragments. The path may include $arg$
	 * elements. However, the path must not have a leading "." or path separator.
	 * Clients should use a path like "icons/mysample.png" rather than
	 * "./icons/mysample.png" or "/icons/mysample.png".
	 * </p>
	 *
	 * @see ImageDescriptor#createFromURL(URL)
	 *
	 * @param classFromBundle A class defined by a bundle class loader.
	 * @param imageFilePath   the path of the image file in the given bundle,
	 *                        relative to the root of the bundle
	 * @return <code>{@link Optional}&lt;{@link ImageDescriptor}&gt;</code> or
	 *         {@link Optional#empty()}
	 */
	public static Optional<ImageDescriptor> imageDescriptorFromBundle(Class<?> classFromBundle, String imageFilePath) {
		Optional<URL> locate = locate(classFromBundle, imageFilePath);
		return locate.map(ImageDescriptor::createFromURL);
	}

}
