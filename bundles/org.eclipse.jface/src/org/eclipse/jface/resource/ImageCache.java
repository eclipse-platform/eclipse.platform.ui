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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A weakly referenced cache of image descriptors to image instances. This is
 * used to hold images in memory, while they are defined. When the image
 * descriptor becomes weakly referred to, the corresponding image will be
 * disposed.
 * 
 * This class may be instantiated; it is not intended to be subclassed.
 * 
 * @since 3.1
 */
public final class ImageCache {

    /**
     * This class spoofs a few method calls by passing them through to the
     * underlying weakly referred object (if available). This allows the weak
     * reference to be used as a key in a <code>HashMap</code>.
     * 
     * @since 3.1
     */
    private static final class HashableWeakReference extends WeakReference {

        /**
         * Constructs a new instance of <code>HashableWeakReference</code>.
         * 
         * @param referent
         *            The object to refer to; may be <code>null</code>.
         * @param referenceQueue
         *            The reference queue to use; should not be
         *            <code>null</code>.
         */
        private HashableWeakReference(final Object referent,
                final ReferenceQueue referenceQueue) {
            super(referent, referenceQueue);
        }

        /**
         * @see Object#equals(java.lang.Object)
         */
        public final boolean equals(Object object) {
            final Object referent = get();
            if (referent == null) {
                return super.equals(object);
            }

            if (object instanceof HashableWeakReference) {
                object = ((HashableWeakReference) object).get();
            }

            return referent.equals(object);
        }

        /**
         * @see Object#hashCode()
         */
        public final int hashCode() {
            final Object referent = get();
            if (referent == null) {
                return super.hashCode();
            }

            return referent.hashCode();
        }
    }

    /**
     * A thread for cleaner up the reference queues as the garbage collector
     * fills them. It takes a map and a reference queue. When an item appears in
     * the reference queue, it uses it as a key to remove values from the map.
     * If the value is an image, then it is disposed. To shutdown the thread,
     * call <code>stopCleaning()</code>.
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
         *            <code>Image</code> objects, but it is okay if they are
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
         * Waits for new garbage. When new garbage arriving, it removes it,
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

                // Remove the image and dispose it.
                final Object value = map.remove(reference);
                if (value instanceof Image) {
                    Display.getCurrent().syncExec(new Runnable() {

                        public void run() {
                            final Image image = (Image) value;
                            if (!image.isDisposed()) {
                                image.dispose();
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
     * The thread responsible for cleaning out disabled images that are no
     * longer needed.
     */
    private final ReferenceCleanerThread disabledCleaner;

    /**
     * A map of image descriptors to the corresponding disabled images. The
     * image descriptors are actually weak references to image descriptors. As
     * the weak references become suitable for collection, the corresponding
     * images (i.e., native resources) will be disposed. This value may be
     * empty, but it is never <code>null</code>.
     */
    private final Map disabledMap = new HashMap();

    /**
     * A queue of references waiting to be garbage collected. This value is
     * never <code>null</code>. This is the queue for
     * <code>disabledMap</code>.
     */
    private final ReferenceQueue disabledReferenceQueue = new ReferenceQueue();

    /**
     * The thread responsible for cleaning out greyed images that are no longer
     * needed.
     */
    private final ReferenceCleanerThread greyCleaner;

    /**
     * A map of image descriptors to the corresponding greyed images. The image
     * descriptors are actually weak references to image descriptors. As the
     * weak references become suitable for collection, the corresponding images
     * (i.e., native resources) will be disposed. This value may be empty, but
     * it is never <code>null</code>.
     */
    private final Map greyMap = new HashMap();

    /**
     * A queue of references waiting to be garbage collected. This value is
     * never <code>null</code>. This is the queue for <code>greyMap</code>.
     */
    private final ReferenceQueue greyReferenceQueue = new ReferenceQueue();

    /**
     * The thread responsible for cleaning out images that are no longer needed.
     */
    private final ReferenceCleanerThread imageCleaner;

    /**
     * A map of image descriptors to the corresponding loaded images. The image
     * descriptors are actually weak references to image descriptors. As the
     * weak references become suitable for collection, the corresponding images
     * (i.e., native resources) will be disposed. This value may be empty, but
     * it is never <code>null</code>.
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
     * Constructs a new instance of <code>ImageCache</code>, and starts a
     * couple of threads to monitor the reference queues.
     */
    public ImageCache() {
        greyCleaner = new ReferenceCleanerThread(greyReferenceQueue, greyMap);
        imageCleaner = new ReferenceCleanerThread(imageReferenceQueue, imageMap);
        disabledCleaner = new ReferenceCleanerThread(disabledReferenceQueue,
                disabledMap);

        greyCleaner.start();
        imageCleaner.start();
        disabledCleaner.start();

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

        // Stop the image cleaner thread, clear all of the weak references and
        // dispose of all of the images.
        imageCleaner.stopCleaning();
        final Iterator imageItr = imageMap.entrySet().iterator();
        while (imageItr.hasNext()) {
            final Map.Entry entry = (Map.Entry) imageItr.next();

            final WeakReference reference = (WeakReference) entry.getKey();
            reference.clear();

            final Image image = (Image) entry.getValue();
            if ((image != null) && (!image.isDisposed())) {
                image.dispose();
            }
        }
        imageMap.clear();

        // Stop the greyed image cleaner thread, clear all of the weak
        // references and dispose of all of the greyed images.
        greyCleaner.stopCleaning();
        final Iterator greyItr = greyMap.entrySet().iterator();
        while (greyItr.hasNext()) {
            final Map.Entry entry = (Map.Entry) greyItr.next();

            final WeakReference reference = (WeakReference) entry.getKey();
            reference.clear();

            final Image image = (Image) entry.getValue();
            if ((image != null) && (!image.isDisposed())) {
                image.dispose();
            }
        }
        greyMap.clear();

        // Stop the disabled image cleaner thread, clear all of the weak
        // references and dispose of all of the disabled images.
        disabledCleaner.stopCleaning();
        final Iterator disabledItr = disabledMap.entrySet().iterator();
        while (disabledItr.hasNext()) {
            final Map.Entry entry = (Map.Entry) disabledItr.next();

            final WeakReference reference = (WeakReference) entry.getKey();
            reference.clear();

            final Image image = (Image) entry.getValue();
            if ((image != null) && (!image.isDisposed())) {
                image.dispose();
            }
        }
        disabledMap.clear();
    }

    /**
     * Returns the disabled image for the given image descriptor. This caches
     * the result so that future attempts to get the disabled image for the same
     * descriptor will only access the cache. When the last reference to the
     * image descriptor is dropped, the image will be cleaned up. This clean up
     * makes no time guarantees about how long this will take.
     * 
     * @param descriptor
     *            The image descriptor for which a disabled image should be
     *            created; may be <code>null</code>.
     * @return The disabled image, either newly created or from the cache. This
     *         value is <code>null</code> if the descriptor parameter passed
     *         in is <code>null</code>.
     */
    public final Image getDisabledImage(final ImageDescriptor descriptor) {
        return getDisabledImage(descriptor, Display.getCurrent());
    }

    /**
     * Returns the disabled image for the given image descriptor. This caches
     * the result so that future attempts to get the disabled image for the same
     * descriptor will only access the cache. When the last reference to the
     * image descriptor is dropped, the image will be cleaned up. This clean up
     * makes no time guarantees about how long this will take.
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
    public final Image getDisabledImage(final ImageDescriptor descriptor,
            final Device device) {
        if (descriptor == null) {
            return null;
        }

        // Try to load a cached image.
        final HashableWeakReference key = new HashableWeakReference(descriptor,
                imageReferenceQueue);
        final Object value = disabledMap.get(key);
        if (value instanceof Image) {
            key.clear();
            return (Image) value;
        }

        // Try to create a disabled image from the regular image.
        final Image image = getImage(descriptor);
        if (image != null) {
            final Image disabledImage = new Image(device, image,
                    SWT.IMAGE_DISABLE);
            disabledMap.put(key, disabledImage);
            return disabledImage;
        }

        // All attempts have failed.
        return null;
    }

    /**
     * Returns the greyed image for the given image descriptor. This caches the
     * result so that future attempts to get the greyed image for the same
     * descriptor will only access the cache. When the last reference to the
     * image descriptor is dropped, the image will be cleaned up. This clean up
     * makes no time guarantees about how long this will take.
     * 
     * @param descriptor
     *            The image descriptor for which a greyed image should be
     *            created; may be <code>null</code>.
     * @return The greyed image, either newly created or from the cache. This
     *         value is <code>null</code> if the descriptor parameter passed
     *         in is <code>null</code>.
     */
    public final Image getGrayImage(final ImageDescriptor descriptor) {
        return getGrayImage(descriptor, Display.getCurrent());
    }

    /**
     * Returns the greyed image for the given image descriptor. This caches the
     * result so that future attempts to get the greyed image for the same
     * descriptor will only access the cache. When the last reference to the
     * image descriptor is dropped, the image will be cleaned up. This clean up
     * makes no time guarantees about how long this will take.
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
    public final Image getGrayImage(final ImageDescriptor descriptor,
            final Device device) {
        if (descriptor == null) {
            return null;
        }

        // Try to load a cached image.
        final HashableWeakReference key = new HashableWeakReference(descriptor,
                imageReferenceQueue);
        final Object value = greyMap.get(key);
        if (value instanceof Image) {
            key.clear();
            return (Image) value;
        }

        // Try to create a grey image from the regular image.
        final Image image = getImage(descriptor);
        if (image != null) {
            final Image greyImage = new Image(device, image, SWT.IMAGE_GRAY);
            greyMap.put(key, greyImage);
            return greyImage;
        }

        // All attempts have failed.
        return null;
    }

    /**
     * Returns the regular image (i.e., enabled) for the given image descriptor.
     * This caches the result so that future attempts to get the image for the
     * same descriptor will only access the cache. When the last reference to
     * the image descriptor is dropped, the image will be cleaned up. This clean
     * up makes no time guarantees about how long this will take.
     * 
     * @param descriptor
     *            The image descriptor for which an image should be created; may
     *            be <code>null</code>.
     * @return The image, either newly created or from the cache. This value is
     *         <code>null</code> if the descriptor parameter passed in is
     *         <code>null</code>.
     */
    public final Image getImage(final ImageDescriptor descriptor) {
        return getImage(descriptor, true);
    }

    /**
     * Returns the regular image (i.e., enabled) for the given image descriptor.
     * This caches the result so that future attempts to get the image for the
     * same descriptor will only access the cache. When the last reference to
     * the image descriptor is dropped, the image will be cleaned up. This clean
     * up makes no time guarantees about how long this will take.
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
    public final Image getImage(final ImageDescriptor descriptor,
            final boolean returnMissingImageOnError) {
        return getImage(descriptor, returnMissingImageOnError, Display
                .getCurrent());
    }

    /**
     * Returns the regular image (i.e., enabled) for the given image descriptor.
     * This caches the result so that future attempts to get the image for the
     * same descriptor will only access the cache. When the last reference to
     * the image descriptor is dropped, the image will be cleaned up. This clean
     * up makes no time guarantees about how long this will take.
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
    public final Image getImage(final ImageDescriptor descriptor,
            final boolean returnMissingImageOnError, final Device device) {
        if (descriptor == null) {
            return null;
        }

        // Try to load the cached value.
        final HashableWeakReference key = new HashableWeakReference(descriptor,
                imageReferenceQueue);
        final Object value = imageMap.get(key);
        if (value instanceof Image) {
            key.clear();
            return (Image) value;
        }

        // Use the descriptor to create the image.
        final Image image = descriptor.createImage(returnMissingImageOnError,
                device);
        imageMap.put(key, image);
        return image;
    }

    /**
     * Returns the regular image (i.e., enabled) for the given image descriptor.
     * This caches the result so that future attempts to get the image for the
     * same descriptor will only access the cache. When the last reference to
     * the image descriptor is dropped, the image will be cleaned up. This clean
     * up makes no time guarantees about how long this will take.
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
    public final Image getImage(final ImageDescriptor descriptor,
            final Device device) {
        return getImage(descriptor, true, device);
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
            missingImage = getImage(ImageDescriptor.getMissingImageDescriptor());
        }

        return missingImage;
    }
}