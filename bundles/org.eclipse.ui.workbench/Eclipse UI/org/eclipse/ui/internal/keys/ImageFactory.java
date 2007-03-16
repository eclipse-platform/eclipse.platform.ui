/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.ImageSupport;

final class ImageFactory {

	private static ImageRegistry imageRegistry = new ImageRegistry();
	private static Map map = new HashMap();

	static {
		put("blank", "$nl$/icons/full/obj16/blank.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("change", "$nl$/icons/full/obj16/change_obj.gif"); //$NON-NLS-1$//$NON-NLS-2$
		
		/*
		 * TODO Remove these images from the registry if they are no longer
		 * needed.
		 */
		put("minus", "$nl$/icons/full/obj16/delete_obj.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("plus", "$nl$/icons/full/obj16/add_obj.gif"); //$NON-NLS-1$//$NON-NLS-2$
	}

	static Image getImage(String key) {
		Image image = imageRegistry.get(key);

		if (image == null) {
			ImageDescriptor imageDescriptor = getImageDescriptor(key);

			if (imageDescriptor != null) {
				image = imageDescriptor.createImage(false);

				if (image == null) {
					WorkbenchPlugin.log(ImageFactory.class +": error creating image for " + key); //$NON-NLS-1$
				}

				imageRegistry.put(key, image);
			}
		}

		return image;
	}

	static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor imageDescriptor = (ImageDescriptor) map.get(key);

		if (imageDescriptor == null) {
			WorkbenchPlugin.log(ImageFactory.class +": no image descriptor for " + key); //$NON-NLS-1$
		}

		return imageDescriptor;
	}

	private static void put(String key, String value) {
		map.put(key, ImageSupport.getImageDescriptor(value));
	}
}
