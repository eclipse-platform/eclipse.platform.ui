/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
/**
 * Registry to hold mappings from project natures to images
 */

public class ProjectImageRegistry {
	private Map map = new HashMap(10);
/**
 * Returns the image for the given nature id or
 * <code>null</code> if no image is registered for the given id
 */
public ImageDescriptor getNatureImage(String natureId) {
	return (ImageDescriptor)map.get(natureId);
}
/**
 * Reads from the plugin registry.
 */
public void load() {
	ProjectImageRegistryReader reader = new ProjectImageRegistryReader();
	reader.readProjectNatureImages(Platform.getPluginRegistry(), this);
}
/**
 * Sets the image for the given nature id
 */
public void setNatureImage(String natureId, ImageDescriptor image) {
	map.put(natureId, image);
}
}
