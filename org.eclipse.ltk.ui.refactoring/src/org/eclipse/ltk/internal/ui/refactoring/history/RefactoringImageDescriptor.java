/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Image descriptor which encapsulates an image.
 *
 * @since 3.2
 */
public final class RefactoringImageDescriptor extends ImageDescriptor {

	/** The image */
	private Image fImage;

	/**
	 * Creates a new refactoring image descriptor.
	 *
	 * @param image
	 *            the image to describe
	 */
	public RefactoringImageDescriptor(final Image image) {
		fImage= image;
	}

	@Override
	public boolean equals(final Object object) {
		return (object != null) && getClass().equals(object.getClass()) && fImage.equals(((RefactoringImageDescriptor) object).fImage);
	}

	@Override
	public ImageData getImageData(int zoom) {
		// workaround for the missing API Image#getImageData(int zoom) (bug 496409)
		if (zoom == 100) {
			return fImage.getImageData();
		}
		ImageData zoomedImageData = fImage.getImageDataAtCurrentZoom();
		Rectangle bounds = fImage.getBounds();
		//TODO: Could have off-by-one problems at fractional zoom levels:
		if (bounds.width == scaleDown(zoomedImageData.width, zoom)
				&& bounds.height == scaleDown(zoomedImageData.height, zoom)) {
			return zoomedImageData;
		}
		return null;
	}

	private static int scaleDown(int value, int zoom) {
		// @see SWT's internal DPIUtil#autoScaleDown(int)
		float scaleFactor = zoom / 100f;
		return Math.round(value / scaleFactor);
	}

	@Override
	public int hashCode() {
		return fImage.hashCode();
	}
}