/*******************************************************************************
 * Copyright (c) 2004, 2020 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *     Tasktop Technologies - initial API and implementation
 *     SAP SE - port to platform.ui
 *******************************************************************************/
package org.eclipse.jface.notifications.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class CommonImages {

	private static final URL baseUrl;

	static {
		Bundle bundle = null;
		if (Platform.isRunning()) {
			bundle = Platform.getBundle("org.eclipse.jface.notifications");
		}
		if (bundle != null) {
			baseUrl = bundle.getEntry("/icons/"); //$NON-NLS-1$
		} else {
			URL iconsUrl = null;
			try {
				// lookup location of CommonImages class on disk
				iconsUrl = new URL(CommonImages.class.getResource("CommonImages.class"), "../../../../../../icons/"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (MalformedURLException e) {
				// ignore
			}
			baseUrl = iconsUrl;
		}
	}

	private static ImageRegistry imageRegistry;

	private static final String T_EVIEW = "eview16"; //$NON-NLS-1$

	public static final ImageDescriptor NOTIFICATION_CLOSE = create(T_EVIEW, "notification-close.png"); //$NON-NLS-1$
	public static final ImageDescriptor NOTIFICATION_CLOSE_HOVER = create(T_EVIEW, "notification-close-active.png"); //$NON-NLS-1$

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/**
	 * Lazily initializes image map.
	 *
	 * @param imageDescriptor
	 * @return Image
	 */
	public static Image getImage(ImageDescriptor imageDescriptor) {
		ImageRegistry imageRegistry = getImageRegistry();
		Image image = imageRegistry.get(Integer.toString(imageDescriptor.hashCode()));
		if (image == null) {
			image = imageDescriptor.createImage(true);
			imageRegistry.put(Integer.toString(imageDescriptor.hashCode()), image);
		}
		return image;
	}

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}

		return imageRegistry;
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (baseUrl == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(baseUrl, buffer.toString());
	}
}
