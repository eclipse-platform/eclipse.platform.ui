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
package org.eclipse.ui.internal.decorators;

import java.util.Arrays;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.*;

/**
 * An DecoratorOverlayIcon consists of a main icon and several adornments.
 */
class DecoratorOverlayIcon extends CompositeImageDescriptor {
	// the base image
	private Image base;
	// the overlay images
	private ImageDescriptor[] overlays;
	// the size
	private Point size;

	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM_RIGHT = 3;

	/**
	 * OverlayIcon constructor.
	 * 
	 * @param base the base image
	 * @param overlays the overlay images
	 * @param locations the location of each image
	 * @param size the size
	 */
	public DecoratorOverlayIcon(
		Image base,
		ImageDescriptor[] overlays,
		Point size) {
		this.base = base;
		this.overlays = overlays;
		this.size = size;
	}
	/**
	 * Draw the overlays for the reciever.
	 */
	protected void drawOverlays(ImageDescriptor[] overlays) {
		Point size = getSize();	
			
		for (int i = 0; i < overlays.length; i++) {
			ImageDescriptor overlay = overlays[i];
			if (overlay == null)
				continue;
			ImageData overlayData = overlay.getImageData();
			//Use the missing descriptor if it is not there.
			if(overlayData == null)
				overlayData = ImageDescriptor.getMissingImageDescriptor().getImageData(); 
			switch (i) {
				case TOP_LEFT :
					drawImage(overlayData, 0, 0);
					break;
				case TOP_RIGHT :
					drawImage(overlayData, size.x - overlayData.width, 0);
					break;
				case BOTTOM_LEFT :
					drawImage(overlayData, 0, size.y - overlayData.height);
					break;
				case BOTTOM_RIGHT :
					drawImage(
						overlayData,
						size.x - overlayData.width,
						size.y - overlayData.height);
					break;
			}
		}
	}

	public boolean equals(Object o) {
		if (!(o instanceof DecoratorOverlayIcon))
			return false;
		DecoratorOverlayIcon other = (DecoratorOverlayIcon) o;
		return base.equals(other.base)
			&& Arrays.equals(overlays, other.overlays);
	}

	public int hashCode() {
		int code = base.hashCode();
		for (int i = 0; i < overlays.length; i++) {
			if(overlays[i] != null)	
				code ^= overlays[i].hashCode();
		}
		return code;
	}

	protected void drawCompositeImage(int width, int height) {
		ImageDescriptor underlay = overlays[DecoratorRegistryReader.UNDERLAY];
		if(underlay != null)
			drawImage(underlay.getImageData(),0,0);
		drawImage(base.getImageData(), 0, 0);
		drawOverlays(overlays);
	}

	protected Point getSize() {
		return size;
	}
}
