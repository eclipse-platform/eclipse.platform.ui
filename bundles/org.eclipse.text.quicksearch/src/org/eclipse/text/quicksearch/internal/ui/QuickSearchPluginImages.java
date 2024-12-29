/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jozef Tomek - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

/**
 * The images provided by the quicksearch plugin.
 */
public class QuickSearchPluginImages {

	/**
	 * The image registry containing <code>Image</code>s and the <code>ImageDescriptor</code>s.
	 */
	private static ImageRegistry imageRegistry;

	/**
	 * Returns the ImageRegistry.
	 *
	 * @return the ImageRegistry
	 */
	static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
		}
		return imageRegistry;
	}

	/**
	 * Returns the <code>Image</code> identified by the given key, or
	 * <code>null</code> if it does not exist.
	 *
	 * @param key the image's key
	 * @return the <code>Image</code> identified by the given key, or
	 *         <code>null</code> if it does not exist
	 */
	static Image getImage(String key) {
		return getImageRegistry().get(key);
	}

	/**
	 * Returns the <code>ImageDescriptor</code> identified by the given key, or
	 * <code>null</code> if it does not exist.
	 *
	 * @param key the image's key
	 * @return the <code>ImageDescriptor</code> identified by the given key, or
	 *         <code>null</code> if it does not exist
	 */
	static ImageDescriptor getImageDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}

	static void dispose() {
		if (imageRegistry != null) {
			imageRegistry.dispose();
			imageRegistry = null;
		}
	}

}
