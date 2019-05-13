/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.ide.registry;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Registry to hold mappings from project natures to images
 */

public class ProjectImageRegistry {
	private Map<String, ImageDescriptor> map = new HashMap<>(10);

	/**
	 * Returns the image for the given nature id or
	 * <code>null</code> if no image is registered for the given id
	 */
	public ImageDescriptor getNatureImage(String natureId) {
		return map.get(natureId);
	}

	/**
	 * Reads from the plugin registry.
	 */
	public void load() {
		ProjectImageRegistryReader reader = new ProjectImageRegistryReader();
		reader.readProjectNatureImages(Platform.getExtensionRegistry(), this);
	}

	/**
	 * Sets the image for the given nature id
	 */
	public void setNatureImage(String natureId, ImageDescriptor image) {
		map.put(natureId, image);
	}
}
