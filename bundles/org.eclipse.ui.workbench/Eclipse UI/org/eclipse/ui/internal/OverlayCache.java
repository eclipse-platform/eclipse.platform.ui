package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.*;

/**
 * The OverlayCache is a helper class used by the DecoratorManger
 * to manage the lifecycle of overlaid images.
 */
class OverlayCache {
	private Map cache = new HashMap(); /*from OverlayIcon to Image*/

	/**
	 * Returns and caches an image corresponding to the specified icon.
	 * @param icon the icon
	 * @return the image
	 */
	Image getImageFor(DecoratorOverlayIcon icon) {
		Image image = (Image) cache.get(icon);
		if (image == null) {
			image = icon.createImage();
			cache.put(icon, image);
		}
		return image;
	}

	/**
	 * Disposes of all images in the cache.
	 */
	void disposeAll() {
		for (Iterator it = cache.values().iterator(); it.hasNext();) {
			Image image = (Image) it.next();
			image.dispose();
		}
		cache.clear();
	}

	/**
	 * Apply the descriptors for the receiver to the supplied
	 * image.
	 * @param source
	 * @param descriptors
	 * @return Image
	 */

	Image applyDescriptors(Image source, ImageDescriptor[] descriptors) {
		Rectangle bounds = source.getBounds();
		Point size = new Point(bounds.width, bounds.height);
		DecoratorOverlayIcon icon =
			new DecoratorOverlayIcon(source, descriptors, size);
		return getImageFor(icon);
	}

	/**
	 * Find the overlays for element using the supplied decorators.
	 * @return boolean - true if anything was applied.
	 * @param element
	 * @param decorators
	 * @param descriptors
	 */
	boolean findDescriptors(
		Object element,
		LightweightDecoratorDefinition[] decorators,
		ImageDescriptor[] descriptors) {
		boolean anythingApplied = false;
		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].getEnablement().isEnabledFor(element)) {
				ImageDescriptor overlay = decorators[i].getOverlay(element);
				if (overlay != null) {
					int quadrant = decorators[i].getQuadrant();
					//Only allow one per quadrant
					if (descriptors[quadrant] == null) {
						descriptors[quadrant] = overlay;
						anythingApplied = true;
					}
				}
			}
		}
		return anythingApplied;
	}

}
