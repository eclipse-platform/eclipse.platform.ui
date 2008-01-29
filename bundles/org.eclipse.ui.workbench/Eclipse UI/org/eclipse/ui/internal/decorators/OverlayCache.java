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
package org.eclipse.ui.internal.decorators;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;

/**
 * The OverlayCache is a helper class used by the DecoratorManger to manage the
 * life cycle of overlaid images.
 */
class OverlayCache {

	// Use a resource manager to hold onto any images we have to create ourselves
	private LocalResourceManager resourceManager;

	/**
	 * 
	 */
	public OverlayCache() {
		super();
		//As we are not in the UI Thread lookup the Display
		resourceManager = new LocalResourceManager(JFaceResources
				.getResources(PlatformUI.getWorkbench().getDisplay()));
	}

	/**
	 * Returns and caches an image corresponding to the specified icon.
	 * 
	 * @param icon
	 *            the icon
	 * @param context - the context to look up the {@link ResourceManager} with
	 * @return the image
	 */
	private Image getImageFor(DecorationOverlayIcon icon, IDecorationContext context) {
		
		return findManager(context).createImage(icon);
	}

	/**
	 * Find the {@link ResourceManager} to use to find the decorator.
	 * @param context
	 * @return {@link ResourceManager}
	 */
	private ResourceManager findManager(IDecorationContext context) {
		
		if(context == null)
			return resourceManager;
		
		Object manager = context.getProperty(DecorationContext.RESOURCE_MANAGER_KEY);
		if(manager == null)
			return resourceManager;
		
		if(manager instanceof ResourceManager)
			return (ResourceManager) manager;
		return resourceManager;
	}

	/**
	 * Disposes of all images in the cache.
	 */
	void disposeAll() {
		resourceManager.dispose();
	}

	/**
	 * Apply the descriptors for the receiver to the supplied image.
	 * 
	 * @param source
	 * @param descriptors
	 * @param context The context to find the manager from.
	 * @return Image
	 */

	Image applyDescriptors(Image source, ImageDescriptor[] descriptors, IDecorationContext context) {
		Rectangle bounds = source.getBounds();
		Point size = new Point(bounds.width, bounds.height);
		DecorationOverlayIcon icon = new DecorationOverlayIcon(source, descriptors, size);
		return getImageFor(icon,context);
	}

}
