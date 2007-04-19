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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * The OverlayCache is a helper class used by the DecoratorManger to manage the
 * lifecycle of overlaid images.
 */
class OverlayCache {

	/**
	 * The CacheEntry
	 * 
	 * @since 3.3
	 * 
	 */
	class CacheEntry extends ImageDescriptor {
		Image base;
		ImageDescriptor[] descriptors;
		Point size;

		/**
		 * Create a new instance of the receiver.
		 * 
		 * @param source
		 * @param descriptorArray
		 * @param bounds
		 */
		public CacheEntry(Image source, ImageDescriptor[] descriptorArray,
				Point bounds) {
			base = source;
			descriptors = descriptorArray;
			size = bounds;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.resource.ImageDescriptor#createImage(boolean, org.eclipse.swt.graphics.Device)
		 */
		public Image createImage(boolean returnMissingImageOnError,
				Device device) {
			Image[] images = new Image[descriptors.length];
			for (int i = 0; i < images.length; i++) {
				if(descriptors[i] != null){
					images[i] = resourceManager.createImage(descriptors[i]);
				}
			}
			return DecorationImageBuilder.compositeImage(base, images);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
		 */
		public ImageData getImageData() {
			return createImage().getImageData();
		}
		
		 /* (non-Javadoc)
	     * @see java.lang.Object#equals(java.lang.Object)
	     */
	    public boolean equals(Object o) {
	        if (!(o instanceof CacheEntry)) {
				return false;
			}
	        CacheEntry other = (CacheEntry) o;
	        return base.equals(other.base)
	                && Arrays.equals(descriptors, other.descriptors);
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Object#hashCode()
	     */
	    public int hashCode() {
	        int code = base.hashCode();
	        for (int i = 0; i < descriptors.length; i++) {
	            if (descriptors[i] != null) {
					code ^= descriptors[i].hashCode();
				}
	        }
	        return code;
	    }

	}

	private Set keys = new HashSet(); // Hold onto the cache entries we
										// created
	// Use a resource manager to hold onto any images we have to create
	private LocalResourceManager resourceManager;

	/**
	 * 
	 */
	public OverlayCache() {
		super();
		resourceManager = new LocalResourceManager(JFaceResources
				.getResources());
	}

	/**
	 * Returns and caches an image corresponding to the specified icon.
	 * 
	 * @param icon
	 *            the icon
	 * @return the image
	 */
	private Image getImageFor(CacheEntry icon) {
		keys.add(icon);// Cache the keys so there is a reference somewhere
		return resourceManager.createImage(icon);
	}

	/**
	 * Disposes of all images in the cache.
	 */
	void disposeAll() {
		keys.clear();
		resourceManager.dispose();
	}

	/**
	 * Apply the descriptors for the receiver to the supplied image.
	 * 
	 * @param source
	 * @param descriptors
	 * @return Image
	 */

	Image applyDescriptors(Image source, ImageDescriptor[] descriptors) {
		Rectangle bounds = source.getBounds();
		Point size = new Point(bounds.width, bounds.height);
		CacheEntry icon = new CacheEntry(source, descriptors, size);
		return getImageFor(icon);
	}

}
