/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.util.Arrays;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * An OverlayIcon consists of a main icon and several adornments.
 */
public abstract class OverlayIcon extends CompositeImageDescriptor {
	// the base image
	private Image base;
	// the overlay images
	private ImageDescriptor[] overlays;
	// the size
	private Point size;

	/**
	 * OverlayIcon constructor.
	 * 
	 * @param base the base image
	 * @param overlays the overlay images
	 * @param size the size
	 */
	public OverlayIcon(Image base, ImageDescriptor[] overlays, Point size) {
		this.base = base;
		this.overlays = overlays;
		this.size = size;
	}
	/**
	 * Superclasses override to draw the overlays.
	 */
	protected abstract void drawOverlays(ImageDescriptor[] overlays);

	public boolean equals(Object o) {
		if (! (o instanceof OverlayIcon)) return false;
		OverlayIcon other = (OverlayIcon) o;
		return base.equals(other.base) && Arrays.equals(overlays, other.overlays);
	}

	public int hashCode() {
		int code = base.hashCode();
		for (int i = 0; i < overlays.length; i++) {
			code ^= overlays[i].hashCode();
		}
		return code;
	}


	protected void drawCompositeImage(int width, int height) {
		drawImage(base.getImageData(), 0, 0);
		drawOverlays(overlays);
	}

	protected Point getSize() {
		return size;
	}
}
