package org.eclipse.jface.resource;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.graphics.Image;
import java.util.*;

/**
 * An image registry maintains a mapping between symbolic image names 
 * and SWT image objects or special image descriptor objects which
 * defer the creation of SWT image objects until they are needed.
 * <p>
 * An image registry owns all of the image objects registered
 * with it, and automatically disposes of them when the SWT Display
 * that creates the images is disposed. Because of this, clients do not 
 * need to (indeed, must not attempt to) dispose of these images themselves.
 * </p>
 * <p>
 * Clients may instantiate this class (it was not designed to be subclassed).
 * </p>
 * <p>
 *
 * <p>
 * Unlike the FontRegistry, it is an error to replace images. As a result
 * there are no events that fire when values are changed in the registry
 * </p>
 */
public class ImageRegistry {
	
	/**
	 * Table of known images keyed by symbolic image name
	 * (key type: <code>String</code>, 
	 *  value type: <code>org.eclipse.swt.graphics.Image</code>
	 *  or <code>ImageDescriptor</code>).
	 */
	private Map table = new HashMap(10);
/**
 * Creates an empty image registry.
 * <p>
 * There must be an SWT Display created in the current 
 * thread before calling this method.
 * </p>
 */
public ImageRegistry() {
	Display display = Display.getCurrent();
	Assert.isNotNull(display);
	hookDisplayDispose(display);
}
/**
 * Creates an empty image registry.
 */
public ImageRegistry(Display display) {
	Assert.isNotNull(display);
	hookDisplayDispose(display);
}
/**
 * Returns the image associated with the given key in this registry, 
 * or <code>null</code> if none.
 *
 * @param key the key
 * @return the image, or <code>null</code> if none
 */
public Image get(String key) {
	Object entry = table.get(key);
	if (entry == null) {
		return null;
	}
	if (entry instanceof Image) {
		return (Image)entry;
	}
	Image image = ((ImageDescriptor)entry).createImage();
	table.put(key, image);
	return image;
}
/**
 * Shut downs this resource registry and disposes of all registered images.
 */
private void handleDisplayDispose() {

	for (Iterator e = table.values().iterator(); e.hasNext();) {
		Object next = e.next();
		if (next instanceof Image) {
			((Image)next).dispose();
		}
	}
	table = null;
}
/**
 * Hook a dispose listener on the SWT display.
 *
 * @param display the Display
 */
private void hookDisplayDispose(Display display) {
	display.disposeExec(new Runnable() {
		public void run() {
			handleDisplayDispose();
		}	
	});
}
/**
 * Adds (or replaces) an image descriptor to this registry. The first time
 * this new entry is retrieved, the image descriptor's image will be computed 
 * (via </code>ImageDescriptor.createImage</code>) and remembered. 
 * This method replaces an existing image descriptor associated with the 
 * given key, but fails if there is a real image associated with it.
 *
 * @param key the key
 * @param descriptor the ImageDescriptor
 * @exception IllegalArgumentException if the key already exists
 */
public void put(String key, ImageDescriptor descriptor) {
	Object entry = table.get(key);
	if (entry == null || entry instanceof ImageDescriptor) {
		//replace with the new descriptor
		table.put(key, descriptor);
		return;
	}
	throw new IllegalArgumentException("ImageRegistry key already in use: " + key);//$NON-NLS-1$
}
/**
 * Adds an image to this registry.  This method
 * fails if there is already an image with the given key.
 * <p>
 * Note that an image registry owns all of the image objects registered
 * with it, and automatically disposes of them the SWT Display is disposed. 
 * Because of this, clients must not register an image object
 * that is managed by another object.
 * </p>
 *
 * @param key the key
 * @param image the image
 * @exception IllegalArgumentException if the key already exists
 */
public void put(String key, Image image) {
	Object entry = table.get(key);
	if (entry == null || entry instanceof ImageDescriptor) {
		//replace with the new descriptor
		table.put(key, image);
		return;
	}
	throw new IllegalArgumentException("ImageRegistry key already in use: " + key);//$NON-NLS-1$
}
}
