/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

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
		return fImage.getImageData(zoom);
	}

	@Override
	public int hashCode() {
		return fImage.hashCode();
	}
}