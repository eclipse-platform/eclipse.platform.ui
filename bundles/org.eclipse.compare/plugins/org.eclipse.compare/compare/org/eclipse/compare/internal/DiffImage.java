/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000, 2001
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.graphics.*;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Combines an image with an overlay.
 */
public class DiffImage extends CompositeImageDescriptor {

	static final int HEIGHT= 16;

	private Image fBaseImage;
	private ImageDescriptor fOverlayImage;
	private int fWidth;

	public DiffImage(Image base, ImageDescriptor overlay, int w) {
		fBaseImage= base;
		fOverlayImage= overlay;
		fWidth= w;
	}

	protected Point getSize() {
		return new Point(fWidth, HEIGHT);
	}

	protected void drawCompositeImage(int width, int height) {

		if (fBaseImage != null) {
			ImageData base= fBaseImage.getImageData();
			if (base == null)
				base= DEFAULT_IMAGE_DATA;
			//try {
				drawImage(base, fWidth - base.width, 0);
			//} catch (ArrayIndexOutOfBoundsException ex) {
				// workaround for PR 1GCQKWP 
			//}
		}

		if (fOverlayImage != null) {
			ImageData dir= fOverlayImage.getImageData();
			if (dir == null)
				dir= DEFAULT_IMAGE_DATA;
			drawImage(dir, 0, (HEIGHT - dir.height) / 2);
		}
	}
}
