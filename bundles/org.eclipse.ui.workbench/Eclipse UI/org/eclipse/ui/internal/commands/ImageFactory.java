/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

final class ImageFactory {

	private static ImageRegistry imageRegistry = new ImageRegistry();
	private static Map map = new HashMap();
	
	static {
		put("category_collapsed", "icons/full/commands/category_collapsed.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("category_expanded", "icons/full/commands/category_expanded.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("change", "icons/full/commands/change.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("exclamation", "icons/full/commands/exclamation.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("gesture_configuration_active", "icons/full/commands/gesture_configuration_active.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("gesture_configuration_inactive", "icons/full/commands/gesture_configuration_inactive.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("key_configuration_active", "icons/full/commands/key_configuration_active.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("key_configuration_inactive", "icons/full/commands/key_configuration_inactive.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("minus", "icons/full/commands/minus.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("plus", "icons/full/commands/plus.gif"); //$NON-NLS-1$//$NON-NLS-2$
	}

	private static ImageDescriptor create(String path) {
		try {
			URL url = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().getInstallURL();
			url = new URL(url, path);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException eMalformedURL) {
			return null;
		}
	}

	private static void put(String key, String value) {
		map.put(key, create(value));	
	}

	static Image getImage(String key) {
		Image image = (Image) imageRegistry.get(key);

		if (image == null) {
			ImageDescriptor imageDescriptor = getImageDescriptor(key);

			if (imageDescriptor != null) {
				image = imageDescriptor.createImage(false);

				if (image == null)
					System.err.println(ImageFactory.class + ": error creating image for " + key); //$NON-NLS-1$

				imageRegistry.put(key, image);
			}
		}

		return image;
	}

	static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor imageDescriptor = (ImageDescriptor) map.get(key);

		if (imageDescriptor == null)
			System.err.println(ImageFactory.class + ": no image descriptor for " + key); //$NON-NLS-1$

		return imageDescriptor;
	}
}
