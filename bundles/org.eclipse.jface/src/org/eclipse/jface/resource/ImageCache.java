/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A weakly referenced cache of image descriptors to arrays of image instances
 * (representing normal, gray and disabled images). This is used to hold images
 * in memory while their descriptors are defined. When the image descriptors
 * become weakly referred to, the corresponding array of images will be
 * disposed.
 * 
 * This class may be instantiated; it is not intended to be subclassed.
 * 
 * NOTE: This API is experimental and is subject to change, including removal.
 * 
 * @since 3.1
 */
public final class ImageCache {

	/**
	 * Image cache value to store the image descriptor and its associated image.
	 * This is primarily used to ensure that the user of this class will be
	 * returned not only the image, but also the image descriptor needed to keep
	 * the image alive.
	 */
	public class ImageCacheValue {

		final Image image;

		final ImageDescriptor imageDescriptor;

		/**
		 * Image cache value constructor.
		 * 
		 * @param imageDescriptor
		 *            The image descriptor.
		 * @param image
		 *            The image.
		 */
		ImageCacheValue(final ImageDescriptor imageDescriptor, final Image image) {
			this.image = image;
			this.imageDescriptor = imageDescriptor;
		}

		/**
		 * Get the image.
		 * 
		 * @return the image.
		 */
		public Image getImage() {
			return image;
		}

		/**
		 * Get the image descriptor.
		 * 
		 * @return the image descriptor.
		 */
		public ImageDescriptor getImageDescriptor() {
			return imageDescriptor;
		}

	}

	/**
	 * A thread for cleaning up the reference queue as the garbage collector
	 * fills it. It takes a map and a reference queue. When an item appears in
	 * the reference queue, it uses it as a key to remove values from the map.
	 * If the value is an array of images, then the images in that array are
	 * disposed. To shutdown the thread, call <code>stopCleaning()</code>.
	 * 
	 * @since 3.1
	 */
	private static class ReferenceCleanerThread extends Thread {

		/**
		 * The number of reference cleaner threads created.
		 */
		private static int threads = 0;

		/**
		 * A marker indicating that the reference cleaner thread should exit.
		 * This is enqueued when the thread is told to stop. Any referenced
		 * enqueued after the thread is told to stop will not be cleaned up.
		 */
		private final WeakReference endMarker;

		/**
		 * The map from which to remove values. This value will not be
		 * <code>null</code>.
		 */
		private final Map map;

		/**
		 * The reference queue to check; will not be <code>null</code>.
		 */
		private final ReferenceQueue referenceQueue;

		/**
		 * Constructs a new instance of <code>ReferenceCleanerThread</code>.
		 * 
		 * @param referenceQueue
		 *            The reference queue to check for garbage. This value must
		 *            not be <code>null</code>.
		 * @param map
		 *            The map to check for values; must not be <code>null</code>.
		 *            It is expected that the keys are <code>Reference</code>
		 *            instances. The values are expected to be
		 *            <code>Image[]</code> objects, but it is okay if they are
		 *            not.
		 */
		private ReferenceCleanerThread(final ReferenceQueue referenceQueue,
				final Map map) {
			super("Reference Cleaner - " + ++threads); //$NON-NLS-1$

			if (referenceQueue == null) {
				throw new NullPointerException(
						"The reference queue should not be null.");} //$NON-NLS-1$

			if (map == null) {
				throw new NullPointerException("The map should not be null.");} //$NON-NLS-1$

			this.endMarker = new WeakReference(referenceQueue, referenceQueue);
			this.referenceQueue = referenceQueue;
			this.map = map;
		}

		/**
		 * Waits for new garbage. When new garbage arrives, it removes it,
		 * clears it, and disposes of any corresponding images.
		 */
		public final void run() {
			while (true) {
				// Get the next reference to dispose.
				Reference reference = null;
				try {
					reference = referenceQueue.remove();
				} catch (final InterruptedException e) {
					// Reference will be null.
				}

				// Check to see if we've been told to stop.
				if (reference == endMarker) {
					break;
				}

				// Remove the images and dispose them.
				final Object value = map.remove(reference);
				if (value instanceof Image[]) {
					Display display = Display.getCurrent();
					if (display == null) {
						display = Display.getDefault();
					}
					display.syncExec(new Runnable() {

						public void run() {
							final Image[] images = (Image[]) value;
							for (int i = 0; i < images.length; i++) {
								final Image image = images[i];
								if ((image != null) && (!image.isDisposed())) {
									image.dispose();
								}
							}
						}
					});
				}

				// Clear the reference.
				if (reference != null) {
					reference.clear();
				}
			}
		}

		/**
		 * Tells this thread to stop trying to clean up. This is usually run
		 * when the cache is shutting down.
		 */
		private final void stopCleaning() {
			endMarker.enqueue();
		}
	}

	/**
	 * The index of the disabled image in the array of images.
	 */
	private static final int DISABLED = 0;

	/**
	 * The index of the gray image in the array of images.
	 */
	private static final int GRAY = 1;

	/**
	 * The index of the normal image in the array of images.
	 */
	private static final int NORMAL = 2;

	/**
	 * The total number of types of images.
	 */
	private static final int TYPES_OF_IMAGES = 3;

	/**
	 * The thread responsible for cleaning out images that are no longer needed.
	 */
	private final ReferenceCleanerThread imageCleaner;

	/**
	 * Map of weak references to array of images.
	 */
	private final Map imageMap = new HashMap();

	/**
	 * A queue of references waiting to be garbage collected. This value is
	 * never <code>null</code>. This is the queue for <code>imageMap</code>.
	 */
	private final ReferenceQueue imageReferenceQueue = new ReferenceQueue();

	/**
	 * The image to display when no image is available. This value is
	 * <code>null</code> until it is first used.
	 */
	private Image missingImage = null;

	/**
	 * A weak hash map of image descriptors to weak references.
	 */
	private final WeakHashMap referenceMap = new WeakHashMap();

	/**
	 * Constructs a new instance of <code>ImageCache</code>, and starts a
	 * thread to monitor the reference queue.
	 */
	public ImageCache() {
		imageCleaner = new ReferenceCleanerThread(imageReferenceQueue, imageMap);
		imageCleaner.start();
	}

	/**
	 * Add a weak reference to the image map and the reference map.
	 * 
	 * @param descriptor
	 *            The image descriptor.
	 * @param images
	 *            The array of images.
	 */
	private void addWeakReference(final ImageDescriptor descriptor,
			final Image[] images) {
		final WeakReference weakReferenceValue = new WeakReference(descriptor,
				imageReferenceQueue);
		imageMap.put(weakReferenceValue, images);
		referenceMap.put(descriptor, weakReferenceValue);
	}

	/**
	 * Cleans up all images in the cache. This disposes of all of the images,
	 * and drops references to them.
	 */
	public final void dispose() {
		// Clean up the missing image.
		if ((missingImage != null) && (!missingImage.isDisposed())) {
			missingImage.dispose();
			missingImage = null;
		}

		// Stop the image cleaner thread
		imageCleaner.stopCleaning();

		// Clear all of the weak references and dispose of all of the images.
		final Iterator imageItr = imageMap.entrySet().iterator();
		while (imageItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) imageItr.next();

			final WeakReference reference = (WeakReference) entry.getKey();
			reference.clear();

			final Image[] images = (Image[]) entry.getValue();
			for (int i = 0; i < images.length; i++) {
				final Image image = images[i];
				if ((image != null) && (!image.isDisposed())) {
					image.dispose();
				}
			}
		}
		imageMap.clear();

		final Iterator referenceItr = referenceMap.entrySet().iterator();
		while (referenceItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) referenceItr.next();

			ImageDescriptor descriptor = (ImageDescriptor) entry.getKey();
			descriptor = null;

			final WeakReference reference = (WeakReference) entry.getValue();
			if (reference != null) {
				reference.clear();
			}

		}
		referenceMap.clear();
	}

	/**
	 * Returns an image cache value, composed of the disabled image and its
	 * associated image descriptor. This caches the result so that future
	 * attempts to get the image for the same descriptor will only access the
	 * cache. Note that when the last reference to the returned image descriptor
	 * is dropped, the image will be cleaned up. This clean up makes no time
	 * guarantees about how long this will take.
	 * 
	 * @param descriptor
	 *            The image descriptor for which a disabled image should be
	 *            created; may be <code>null</code>.
	 * @return The disabled image, either newly created or from the cache. This
	 *         value is <code>null</code> if the descriptor parameter passed
	 *         in is <code>null</code>.
	 */
	public final ImageCacheValue getDisabledImage(
			final ImageDescriptor descriptor) {
		return getDisabledImage(descriptor, getDisplay());
	}

	/**
	 * Returns an image cache value, composed of the disabled image and its
	 * associated image descriptor. This caches the result so that future
	 * attempts to get the image for the same descriptor will only access the
	 * cache. Not that when the last reference to the returned image descriptor
	 * is dropped, the image will be cleaned up. This clean up makes no time
	 * guarantees about how long this will take.
	 * 
	 * @param descriptor
	 *            The image descriptor for which a disabled image should be
	 *            created; may be <code>null</code>.
	 * @param device
	 *            The device on which to create the image.
	 * @return The disabled image, either newly created or from the cache. This
	 *         value is <code>null</code> if the descriptor parameter passed
	 *         in is <code>null</code>.
	 */
	public final ImageCacheValue getDisabledImage(
			final ImageDescriptor descriptor, final Device device) {
		if (descriptor == null) {
			return null;
		}

		// Retrieve the images if present
		final Object value = imageMap.get(referenceMap.get(descriptor));
		if (value instanceof Image[]) {
			final Image[] images = (Image[]) value;
			final Image disabledImage = images[DISABLED];
			if (disabledImage != null) {
				return new ImageCacheValue(
						getImageDescriptorReference(descriptor), disabledImage);
			}
		}

		// Try to create a disabled image from the regular image.
		final Image image = getImage(descriptor).getImage();
		if (image != null) {
			final Image disabledImage = new Image(device, image,
					SWT.IMAGE_DISABLE);
			final Image[] images;
			if (value instanceof Image[]) {
				images = (Image[]) value;
			} else {
				images = new Image[TYPES_OF_IMAGES];
				addWeakReference(descriptor, images);
			}
			images[DISABLED] = disabledImage;
			return new ImageCacheValue(getImageDescriptorReference(descriptor),
					disabledImage);
		}

		// All attempts have failed.
		return null;
	}

	/**
	 * Get the display.
	 * 
	 * @return the current display, or default display if the current display is
	 *         null.
	 */
	private final Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/**
	 * Returns an image cache value, composed of the gray image and its
	 * associated image descriptor. This caches the result so that future
	 * attempts to get the image for the same descriptor will only access the
	 * cache. Note that when the last reference to the returned image descriptor
	 * is dropped, the image will be cleaned up. This clean up makes no time
	 * guarantees about how long this will take.
	 * 
	 * @param descriptor
	 *            The image descriptor for which a greyed image should be
	 *            created; may be <code>null</code>.
	 * @return The greyed image, either newly created or from the cache. This
	 *         value is <code>null</code> if the descriptor parameter passed
	 *         in is <code>null</code>.
	 */
	public final ImageCacheValue getGrayImage(final ImageDescriptor descriptor) {
		return getGrayImage(descriptor, getDisplay());
	}

	/**
	 * Returns an image cache value, composed of the gray image and its
	 * associated image descriptor. This caches the result so that future
	 * attempts to get the image for the same descriptor will only access the
	 * cache. Note that when the last reference to the returned image descriptor
	 * is dropped, the image will be cleaned up. This clean up makes no time
	 * guarantees about how long this will take.
	 * 
	 * @param descriptor
	 *            The image descriptor for which a greyed image should be
	 *            created; may be <code>null</code>.
	 * @param device
	 *            The device on which to create the image.
	 * @return The greyed image, either newly created or from the cache. This
	 *         value is <code>null</code> if the descriptor parameter passed
	 *         in is <code>null</code>.
	 */
	public final ImageCacheValue getGrayImage(final ImageDescriptor descriptor,
			final Device device) {
		if (descriptor == null) {
			return null;
		}

		// Retrieve the images if present
		final Object value = imageMap.get(referenceMap.get(descriptor));
		if (value instanceof Image[]) {
			final Image[] images = (Image[]) value;
			if (GRAY < images.length) {
				final Image grayImage = images[GRAY];
				if (grayImage != null) {
					return new ImageCacheValue(
							getImageDescriptorReference(descriptor), grayImage);
				}
			}
		}

		// Try to create a grey image from the regular image.
		final Image image = getImage(descriptor).getImage();
		if (image != null) {
			final Image grayImage = new Image(device, image, SWT.IMAGE_GRAY);
			final Image[] images;
			if (value instanceof Image[]) {
				images = (Image[]) value;
			} else {
				images = new Image[TYPES_OF_IMAGES];
				addWeakReference(descriptor, images);
			}
			images[GRAY] = grayImage;
			return new ImageCacheValue(getImageDescriptorReference(descriptor),
					grayImage);
		}

		// All attempts have failed.
		return null;
	}

	/**
	 * Returns an image cache value, composed of the regular image (i.e.,
	 * enabled) and its associated image descriptor. This caches the result so
	 * that future attempts to get the image for the same descriptor will only
	 * access the cache. Note that when the last reference to the returned image
	 * descriptor is dropped, the image will be cleaned up. This clean up makes
	 * no time guarantees about how long this will take.
	 * 
	 * @param descriptor
	 *            The image descriptor for which an image should be created; may
	 *            be <code>null</code>.
	 * @return The image, either newly created or from the cache. This value is
	 *         <code>null</code> if the descriptor parameter passed in is
	 *         <code>null</code>.
	 */
	public final ImageCacheValue getImage(final ImageDescriptor descriptor) {
		return getImage(descriptor, true);
	}

	/**
	 * Returns an image cache value, composed of the regular image (i.e.,
	 * enabled) and its associated image descriptor. This caches the result so
	 * that future attempts to get the image for the same descriptor will only
	 * access the cache. Note that when the last reference to the returned image
	 * descriptor is dropped, the image will be cleaned up. This clean up makes
	 * no time guarantees about how long this will take.
	 * 
	 * @param descriptor
	 *            The image descriptor for which an image should be created; may
	 *            be <code>null</code>.
	 * @param returnMissingImageOnError
	 *            Flag that determines if a default image is returned on error.
	 * @return The image, either newly created or from the cache. This value is
	 *         <code>null</code> if the descriptor parameter passed in is
	 *         <code>null</code>.
	 */
	public final ImageCacheValue getImage(final ImageDescriptor descriptor,
			final boolean returnMissingImageOnError) {
		return getImage(descriptor, returnMissingImageOnError, getDisplay());
	}

	/**
	 * Returns an image cache value, composed of the regular image (i.e.,
	 * enabled) and its associated image descriptor. This caches the result so
	 * that future attempts to get the image for the same descriptor will only
	 * access the cache. Note that when the last reference to the returned image
	 * descriptor is dropped, the image will be cleaned up. This clean up makes
	 * no time guarantees about how long this will take.
	 * 
	 * @param descriptor
	 *            The image descriptor for which an image should be created; may
	 *            be <code>null</code>.
	 * @param returnMissingImageOnError
	 *            Flag that determines if a default image is returned on error.
	 * @param device
	 *            The device on which to create the image.
	 * @return The image, either newly created or from the cache. This value is
	 *         <code>null</code> if the descriptor parameter passed in is
	 *         <code>null</code>.
	 */
	public final ImageCacheValue getImage(final ImageDescriptor descriptor,
			final boolean returnMissingImageOnError, final Device device) {
		if (descriptor == null) {
			return null;
		}

		// Retrieve the images if present
		final Object value = imageMap.get(referenceMap.get(descriptor));
		if (value instanceof Image[]) {
			final Image[] images = (Image[]) value;
			if (NORMAL < images.length) {
				final Image normalImage = images[NORMAL];
				if (normalImage != null) {
					return new ImageCacheValue(
							getImageDescriptorReference(descriptor),
							normalImage);
				}
			}
		}

		// Use the descriptor to create the image
		final Image image = descriptor.createImage(returnMissingImageOnError,
				device);
		final Image[] images;
		// The images array already exists
		if (value instanceof Image[]) {
			images = (Image[]) value;
		} else { // The images array does not exist
			images = new Image[TYPES_OF_IMAGES];
			addWeakReference(descriptor, images);
		}
		images[NORMAL] = image;
		return new ImageCacheValue(getImageDescriptorReference(descriptor),
				image);
	}

	/**
	 * Returns an image cache value, composed of the regular image (i.e.,
	 * enabled) and its associated image descriptor. This caches the result so
	 * that future attempts to get the image for the same descriptor will only
	 * access the cache. Note that when the last reference to the returned image
	 * descriptor is dropped, the image will be cleaned up. This clean up makes
	 * no time guarantees about how long this will take.
	 * 
	 * @param descriptor
	 *            The image descriptor for which an image should be created; may
	 *            be <code>null</code>.
	 * @param device
	 *            The device on which to create the image.
	 * @return The image, either newly created or from the cache. This value is
	 *         <code>null</code> if the descriptor parameter passed in is
	 *         <code>null</code>.
	 */
	public final ImageCacheValue getImage(final ImageDescriptor descriptor,
			final Device device) {
		return getImage(descriptor, true, device);
	}

	/**
	 * Get the image descriptor used as a weak reference in the cache.
	 * 
	 * @param imageDescriptor
	 *            The image descriptor.
	 * @return the image descriptor or null if the image descriptor is not in
	 *         the cache.
	 */
	public ImageDescriptor getImageDescriptorReference(
			final ImageDescriptor imageDescriptor) {
		Iterator i = referenceMap.keySet().iterator();
		ImageDescriptor currentDescriptor = null;
		while (i.hasNext()) {
			currentDescriptor = (ImageDescriptor) i.next();
			if (currentDescriptor.equals(imageDescriptor)) {
				return currentDescriptor;
			}
		}

		// The descriptor is not in cache
		return null;

	}

	/**
	 * Returns the image to display when no image can be found, or none is
	 * specified. This image is only disposed when the cache is disposed.
	 * 
	 * @return The image to display for missing images. This value will never be
	 *         <code>null</code>.
	 */
	public final Image getMissingImage() {
		if (missingImage == null) {
			missingImage = ImageDescriptor.getMissingImageDescriptor()
					.createImage();
		}

		return missingImage;
	}
}