package org.eclipse.update.internal.ui.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.*;

/**
 * An OverlayIcon consists of a main icon and several adornments.
 */
public class ImageOverlayIcon extends AbstractOverlayIcon {
	private Image base;

	public ImageOverlayIcon(Image base, ImageDescriptor[][] overlays) {
		this(base, overlays, null);
	}
	
	public ImageOverlayIcon(Image base, ImageDescriptor[][] overlays, Point size) {
		super(overlays, size);
		this.base = base;
	}
	
	protected ImageData getBaseImageData() {
		return base.getImageData();
	}
}
