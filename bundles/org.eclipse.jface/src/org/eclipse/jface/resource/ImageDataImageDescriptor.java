/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;

/**
 * @since 3.1
 */
class ImageDataImageDescriptor extends ImageDescriptor {

	private ImageDataProvider dataProvider;

    /**
     * Original image being described, or null if this image is described
     * completely using its ImageData
     */
    private Image originalImage = null;

    /**
     * Creates an image descriptor, given an image and the device it was created on.
     *
     * @param originalImage
     */
    ImageDataImageDescriptor(Image originalImage) {
		this(originalImage::getImageData);
        this.originalImage = originalImage;
    }

	/**
	 * Creates an image descriptor, given some image data.
	 *
	 * @param data describing the image
	 * @deprecated use {@link #ImageDataImageDescriptor(ImageDataProvider)}
	 */
	@Deprecated
	ImageDataImageDescriptor(ImageData data) {
		this(zoom -> zoom == 100 ? data : null);
	}

    /**
	 * Creates an image descriptor, given an image data provider.
	 *
	 * @param provider describing the image
	 */
	ImageDataImageDescriptor(ImageDataProvider provider) {
		dataProvider = provider;
	}

	@Override
	public Object createResource(Device device) throws DeviceResourceException {

        // If this descriptor is based on an existing image, then we can return the original image
        // if this is the same device.
        if (originalImage != null) {
            // If we're allocating on the same device as the original image, return the original.
            if (originalImage.getDevice() == device) {
                return originalImage;
            }
        }

        return super.createResource(device);
    }

    @Override
	public void destroyResource(Object previouslyCreatedObject) {
        if (previouslyCreatedObject == originalImage) {
            return;
        }

        super.destroyResource(previouslyCreatedObject);
    }

    @Override
	public ImageData getImageData(int zoom) {
        return dataProvider.getImageData(zoom);
    }

    @Override
	public int hashCode() {
    	 if (originalImage != null) {
             return System.identityHashCode(originalImage);
         }
         return dataProvider.getImageData(100).hashCode();
    }

    @Override
	public boolean equals(Object obj) {
        if (!(obj instanceof ImageDataImageDescriptor)) {
			return false;
		}

        ImageDataImageDescriptor imgWrap = (ImageDataImageDescriptor) obj;

        //Intentionally using == instead of equals() as Image.hashCode() changes
        //when the image is disposed and so leaks may occur with equals()

        if (originalImage != null) {
            return imgWrap.originalImage == originalImage;
        }

        return (imgWrap.originalImage == null && dataProvider.equals(imgWrap.dataProvider));
    }

}
