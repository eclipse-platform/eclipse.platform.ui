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
package org.eclipse.ui.internal.misc;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An OverlayIcon consists of a main icon and several adornments.
 */
public class OverlayIcon extends CompositeImageDescriptor {
	
	static final int DEFAULT_WIDTH= 22;
	static final int DEFAULT_HEIGHT= 16;
	
	private Point fSize= null;
		
	private ImageDescriptor fBase;
	private ImageDescriptor fOverlays[][];

	public OverlayIcon(ImageDescriptor base, ImageDescriptor[][] overlays, Point size) {
		fBase= base;
		fOverlays= overlays;
		fSize= size;
	}
	protected void drawBottomLeft(ImageDescriptor[] overlays) {
		if (overlays == null)
			return;
		int length= overlays.length;
		int x= 0;
		for (int i= 0; i < 3; i++) {
			if (i < length && overlays[i] != null) {
				ImageData id= overlays[i].getImageData();
				drawImage(id, x, getSize().y-id.height);
				x+= id.width;
			}
		}
	}
	protected void drawBottomRight(ImageDescriptor[] overlays) {
		if (overlays == null)
			return;
		int length= overlays.length;
		int x= getSize().x;
		for (int i= 2; i >= 0; i--) {
			if (i < length && overlays[i] != null) {
				ImageData id= overlays[i].getImageData();
				x-= id.width;
				drawImage(id, x, getSize().y-id.height);
			}
		}
	}
	/**
	 * @see CompositeImage#fill
	 */
	protected void drawCompositeImage(int width, int height) {
		ImageData bg;
		if (fBase == null || (bg= fBase.getImageData()) == null)
			bg= DEFAULT_IMAGE_DATA;
		drawImage(bg, 0, 0);
		
		if (fOverlays != null) {
			if (fOverlays.length > 0)
				drawTopRight( fOverlays[0]);
				
			if (fOverlays.length > 1)
				drawBottomRight(fOverlays[1]);
				
			if (fOverlays.length > 2)
				drawBottomLeft(fOverlays[2]);
				
			if (fOverlays.length > 3)
				drawTopLeft(fOverlays[3]);
		}	
	}
	protected void drawTopLeft(ImageDescriptor[] overlays) {
		if (overlays == null)
			return;
		int length= overlays.length;
		int x= 0;
		for (int i= 0; i < 3; i++) {
			if (i < length && overlays[i] != null) {
				ImageData id= overlays[i].getImageData();
				drawImage(id, x, 0);
				x+= id.width;
			}
		}
	}
	protected void drawTopRight(ImageDescriptor[] overlays) {
		if (overlays == null)
			return;
		int length= overlays.length;
		int x= getSize().x;
		for (int i= 2; i >= 0; i--) {
			if (i < length && overlays[i] != null) {
				ImageData id= overlays[i].getImageData();
				x-= id.width;
				drawImage(id, x, 0);
			}
		}
	}
	/**
	 * @see CompositeImage#getSize
	 */
	protected Point getSize() {
		return fSize;
	}
}
