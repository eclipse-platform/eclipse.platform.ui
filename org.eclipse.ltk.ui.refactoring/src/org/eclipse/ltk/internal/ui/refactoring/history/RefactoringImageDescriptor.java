package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.core.runtime.Assert;

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
		Assert.isNotNull(image);
		fImage= image;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(final Object object) {
		return (object != null) && getClass().equals(object.getClass()) && fImage.equals(((RefactoringImageDescriptor) object).fImage);
	}

	/**
	 * {@inheritDoc}
	 */
	public ImageData getImageData() {
		return fImage.getImageData();
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return fImage.hashCode();
	}
}