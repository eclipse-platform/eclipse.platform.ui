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
	 * Get the image for the decorators and the element.
	 * Note that enablement checking is done here to prevent
	 * the need for excess filtering be callers of this method.
	 * @param Image. The source Image to decorate.
	 * @param Object. The object being decorated.
	 * @param LightweightDecoratorDefinition[]. The decorators to apply.
	 */
	Image getImageFor(
		Image source,
		Object element,
		LightweightDecoratorDefinition[] decorators) {

		//Do not bother if there is no work to do,
		if (decorators.length == 0)
			return source;

		ImageDescriptor[] descriptors = new ImageDescriptor[4];

		if (decorateWith(element, decorators, descriptors)) {

			Rectangle bounds = source.getBounds();
			Point size = new Point(bounds.width, bounds.height);
			DecoratorOverlayIcon icon =
				new DecoratorOverlayIcon(source, descriptors, size);
			return getImageFor(icon);
		} else
			return source;
	}

	/**
	 * Get the image for the decorators and the element and the 
	 * adapted decorators and element.
	 * Note that enablement checking is done here to prevent
	 * the need for excess filtering be callers of this method.
	 * @param Image. The source Image to decorate.
	 * @param Object. The object being decorated.
	 * @param LightweightDecoratorDefinition[]. The decorators to apply.
	 * @param Object. The adapted value of the object.
	 * @param LightweightDecoratorDefinition[]. The decorators for the adapted
	 * 			object.
	 */
	Image getImageFor(
		Image source,
		Object element,
		LightweightDecoratorDefinition[] decorators,
		Object adapted,
		LightweightDecoratorDefinition[] adaptedDecorators) {

		//Do not bother if there is no work to do,
		if (decorators.length == 0 && adaptedDecorators.length == 0)
			return source;

		ImageDescriptor[] descriptors = new ImageDescriptor[4];

		boolean decorated = decorateWith(element, decorators, descriptors);
		decorated =
			decorated || decorateWith(adapted, adaptedDecorators, descriptors);

		//Don't do the math if you don't need to
		if (!decorated)
			return source;

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
	private boolean decorateWith(
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
