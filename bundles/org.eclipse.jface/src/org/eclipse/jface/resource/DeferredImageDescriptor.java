/*******************************************************************************
 * Copyright (c) 2020, 2022, Alex Blewitt and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Blewitt - initial API and implementation
 *     Daniel Kruegler - #396, #401
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.net.URL;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * Creates an ImageDescriptor on demand given a URL supplier.
 *
 * When the cost of searching for an image outweighs the cost of creating the
 * image, it can be more better to defer the lookup until the image is required.
 * This also helps situations with large numbers of images that may be defined
 * but not used in a session.
 *
 * Using the {@link ImageDescriptor#createFromURLSupplier(Supplier)} will
 * trigger this class, which defers calculating the URL until the image is
 * required for the first time.
 *
 * @since 3.21
 */
final class DeferredImageDescriptor extends ImageDescriptor implements IAdaptable {

	/**
	 * The supplier of the class.
	 */
	private final Supplier<URL> supplier;

	private final boolean useMissingImage;

	/**
	 * URL referring to the actual image, computed lazily when required.
	 */
	private URL url;

	/**
	 * Create a new DeferredImageDescriptor with the given URL supplier.
	 *
	 * When {@link ImageDescriptor#getImageData(int)} is called, the supplier will
	 * be asked to give the URL. If <code>null</code> is returned, then an Image
	 * from {@link ImageDescriptor#getMissingImageDescriptor()} will be returned.
	 * @param useMissingImage return a missing image if the URL is null, or null if
	 *                        not
	 * @param supplier        the supplier of the URL
	 */
	DeferredImageDescriptor(boolean useMissingImage, Supplier<URL> supplier) {
		super(false);
		this.supplier = Objects.requireNonNull(supplier);
		this.useMissingImage = useMissingImage;
	}

	@Override
	public ImageData getImageData(int zoom) {
		URL url = getURL();
		if (url == null) {
			return useMissingImage ? ImageDescriptor.getMissingImageDescriptor().getImageData(zoom) : null;
		}
		return ImageDescriptor.createFromURL(url).getImageData(zoom);
	}

	@Override
	public Image createImage(boolean returnMissingImageOnError, Device device) {
		URL url = getURL();
		if (url == null) {
			return returnMissingImageOnError ? ImageDescriptor.getMissingImageDescriptor().createImage() : null;
		}
		return ImageDescriptor.createFromURL(url).createImage(returnMissingImageOnError, device);
	}

	private final URL getURL() {
		if (url == null) {
			url = supplier.get();
		}
		return url;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == URL.class) {
			return adapter.cast(getURL());
		}
		return null;
	}

}
