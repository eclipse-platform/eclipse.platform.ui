/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

/**
 * Image descriptor for decorated refactoring descriptors.
 *
 * @since 3.2
 */
public final class RefactoringDescriptorImageDescriptor extends CompositeImageDescriptor {

	/** The workspace flag */
	public static final int WORKSPACE= 1 << 1;

	/** The image flags */
	private final int fFlags;

	/** The base image */
	private final ImageDescriptor fImage;

	/** The image size */
	private final Point fSize;

	/**
	 * Creates a new refactoring descriptor image descriptor.
	 *
	 * @param image
	 *            the base image
	 * @param flags
	 *            image flags
	 * @param size
	 *            the size of the image
	 */
	public RefactoringDescriptorImageDescriptor(final ImageDescriptor image, final int flags, final Point size) {
		fImage= image;
		fFlags= flags;
		fSize= size;
	}

	/**
	 * Draws the bottom right image decorations.
	 */
	private void drawBottomRight() {
		final Point size= getSize();
		int x= size.x;

		if ((fFlags & WORKSPACE) != 0) {
			ImageData data= getImageData(RefactoringPluginImages.DESC_OVR_WORKSPACE);
			x-= data.width;
			drawImage(data, x, size.y - data.height);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void drawCompositeImage(final int width, final int height) {
		drawImage(getImageData(fImage), 0, 0);
		drawBottomRight();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(final Object object) {
		if (object == null || !RefactoringDescriptorImageDescriptor.class.equals(object.getClass()))
			return false;
		final RefactoringDescriptorImageDescriptor other= (RefactoringDescriptorImageDescriptor) object;
		return (fImage.equals(other.fImage) && fFlags == other.fFlags && fSize.equals(other.fSize));
	}

	/**
	 * Returns the image data for the specified descriptor.
	 *
	 * @param descriptor
	 *            the image descriptor
	 * @return the image data
	 */
	private ImageData getImageData(final ImageDescriptor descriptor) {
		ImageData data= descriptor.getImageData();
		if (data == null) {
			data= DEFAULT_IMAGE_DATA;
			RefactoringUIPlugin.logErrorMessage("Image data not available: " + descriptor.toString()); //$NON-NLS-1$
		}
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Point getSize() {
		return fSize;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return fImage.hashCode() | fFlags | fSize.hashCode();
	}
}
