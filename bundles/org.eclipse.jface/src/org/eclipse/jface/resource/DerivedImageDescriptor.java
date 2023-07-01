/*******************************************************************************
 * Copyright (c) 2005, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * An image descriptor which creates images based on another ImageDescriptor, but with
 * additional SWT flags. Note that this is only intended for compatibility.
 *
 * @since 3.1
 */
final class DerivedImageDescriptor extends ImageDescriptor {

	private final ImageDescriptor original;
	private final int flags;

	/**
	 * Create a new image descriptor
	 * @param original the original one
	 * @param swtFlags flags to be used when image is created {@link Image#Image(Device, Image, int)}
	 * @see SWT#IMAGE_COPY
	 * @see SWT#IMAGE_DISABLE
	 * @see SWT#IMAGE_GRAY
	 */
	public DerivedImageDescriptor(ImageDescriptor original, int swtFlags) {
		super(original.shouldBeCached());
		this.original = original;
		flags = swtFlags;
	}

	@Override
	public Object createResource(Device device) throws DeviceResourceException {
		try {
			return internalCreateImage(device);
		} catch (SWTException e) {
			throw new DeviceResourceException(this, e);
		}
	}

	@Override
	public Image createImage(Device device) {
		return internalCreateImage(device);
	}

	@Override
	public int hashCode() {
		return original.hashCode() + flags;
	}

	@Override
	public boolean equals(Object arg0) {
		return arg0 instanceof DerivedImageDescriptor desc && desc.original.equals(original) && flags == desc.flags;
	}

	/**
	 * Creates a new Image on the given device. Note that we defined a new
	 * method rather than overloading createImage since this needs to be
	 * called by getImageData(), and we want to be absolutely certain not
	 * to cause infinite recursion if the base class gets refactored.
	 *
	 * @param device device to create the image on
	 * @return a newly allocated Image. Must be disposed by calling image.dispose().
	 */
	private final Image internalCreateImage(Device device) {
		Image originalImage = original.createImage(device);
		Image result = new Image(device, originalImage, flags);
		original.destroyResource(originalImage);
		return result;
	}

	@Override
	public ImageData getImageData(int zoom) {
		Image image = internalCreateImage(Display.getCurrent());
		ImageData result = image.getImageData(zoom);
		image.dispose();
		return result;
	}
}
