package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
