/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
	private boolean fLeft= true;

	public DiffImage(Image base, ImageDescriptor overlay, int w) {
		fBaseImage= base;
		fOverlayImage= overlay;
		fWidth= w;
	}

	public DiffImage(Image base, ImageDescriptor overlay, int w, boolean onLeft) {
		fBaseImage= base;
		fOverlayImage= overlay;
		fWidth= w;
		fLeft= onLeft;
	}

	protected Point getSize() {
		return new Point(fWidth, HEIGHT);
	}

	protected void drawCompositeImage(int width, int height) {
		if (fLeft) {
			if (fBaseImage != null) {
				ImageData base= fBaseImage.getImageData();
				if (base == null)
					base= DEFAULT_IMAGE_DATA;
				drawImage(base, fWidth - base.width, 0);
			}
	
			if (fOverlayImage != null) {
				ImageData overlay= fOverlayImage.getImageData();
				if (overlay == null)
					overlay= DEFAULT_IMAGE_DATA;
				drawImage(overlay, 0, (HEIGHT - overlay.height) / 2);
			}
		} else {
			if (fBaseImage != null) {
				ImageData base= fBaseImage.getImageData();
				if (base == null)
					base= DEFAULT_IMAGE_DATA;
				drawImage(base, 0, 0);
			}
	
			if (fOverlayImage != null) {
				ImageData overlay= fOverlayImage.getImageData();
				if (overlay == null)
					overlay= DEFAULT_IMAGE_DATA;
				drawImage(overlay, fWidth - overlay.width, (HEIGHT - overlay.height) / 2);
			}
		}
	}
}
