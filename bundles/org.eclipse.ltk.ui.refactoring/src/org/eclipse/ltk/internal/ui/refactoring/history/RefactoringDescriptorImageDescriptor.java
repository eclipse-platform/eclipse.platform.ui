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

import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;

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
			CachedImageDataProvider dp= createCachedImageDataProvider(RefactoringPluginImages.DESC_OVR_WORKSPACE);
			x-= dp.getWidth();
			drawImage(dp, x, size.y - dp.getHeight());
		}
	}

	@Override
	protected void drawCompositeImage(final int width, final int height) {
		drawImage(createCachedImageDataProvider(fImage), 0, 0);
		drawBottomRight();
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null || !RefactoringDescriptorImageDescriptor.class.equals(object.getClass()))
			return false;
		final RefactoringDescriptorImageDescriptor other= (RefactoringDescriptorImageDescriptor) object;
		return (fImage.equals(other.fImage) && fFlags == other.fFlags && fSize.equals(other.fSize));
	}

	@Override
	protected Point getSize() {
		return fSize;
	}

	@Override
	public int hashCode() {
		return fImage.hashCode() | fFlags | fSize.hashCode();
	}
}
