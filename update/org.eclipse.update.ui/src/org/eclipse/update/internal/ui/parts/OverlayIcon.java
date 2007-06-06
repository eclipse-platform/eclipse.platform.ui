/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.parts;

import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;

/**
 * An OverlayIcon consists of a main icon and several adornments.
 */
public class OverlayIcon extends CompositeImageDescriptor {
	
	static final int DEFAULT_WIDTH= 16;
	static final int DEFAULT_HEIGHT= 16;
	
	private Point fSize= null;
		
	private ImageDescriptor fBase;
	private ImageDescriptor fOverlays[][];

	public OverlayIcon(ImageDescriptor base, ImageDescriptor[][] overlays) {
		fBase= base;
		if (fBase == null)
			fBase= ImageDescriptor.getMissingImageDescriptor();
		fOverlays= overlays;
		fSize= new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	public OverlayIcon(ImageDescriptor base, ImageDescriptor[][] overlays, Point size) {
		fBase= base;
		if (fBase == null)
			fBase= ImageDescriptor.getMissingImageDescriptor();
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
	protected void drawCompositeImage(int width, int height) {
		ImageData bg= fBase.getImageData();
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

	protected Point getSize() {
		return fSize;
	}
}
