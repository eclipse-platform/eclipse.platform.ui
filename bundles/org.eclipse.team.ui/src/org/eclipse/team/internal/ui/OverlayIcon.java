package org.eclipse.team.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * An OverlayIcon consists of a main icon and several adornments.
 */
public class OverlayIcon extends CompositeImageDescriptor {
	// Constants for default size
	static final int DEFAULT_WIDTH = 22;
	static final int DEFAULT_HEIGHT = 16;
	
	// The size of this icon
	private Point size = null;
		
	// The base image
	private ImageData base;
	// All overlay images
	private ImageDescriptor overlays[][];

	/**
	 * OverlayIcon constructor
	 * 
	 * @param base  the base image
	 * @param overlays  the overlay images
	 * @param size  the size of the icon
	 */
	public OverlayIcon(ImageData base, ImageDescriptor[][] overlays, Point size) {
		this.base = base;
		this.overlays = overlays;
		this.size = size;
	}
	/**
	 * Draws the overlays in the bottom left
	 * 
	 * @param overlays  the overlay images
	 */
	protected void drawBottomLeft(ImageDescriptor[] overlays) {
		if (overlays == null) return;
		int length = overlays.length;
		int x = 0;
		for (int i= 0; i < 3; i++) {
			if (i < length && overlays[i] != null) {
				ImageData id = overlays[i].getImageData();
				drawImage(id, x, getSize().y - id.height);
				//x += id.width;
			}
		}
	}
	/**
	 * Draws the overlays in the bottom right
	 * 
	 * @param overlays  the overlay images
	 */
	protected void drawBottomRight(ImageDescriptor[] overlays) {
		if (overlays == null) return;
		int length = overlays.length;
		int x = getSize().x;
		for (int i= 2; i >= 0; i--) {
			if (i < length && overlays[i] != null) {
				ImageData id = overlays[i].getImageData();
				//x -= id.width;
				drawImage(id, x, getSize().y - id.height);
			}
		}
	}
	/*
	 * @see CompositeImage#fill
	 */
	protected void drawCompositeImage(int width, int height) {
		ImageData bg = base;
		if (bg == null) {
			bg = DEFAULT_IMAGE_DATA;
		}
		drawImage(bg, 0, 0);
		
		if (overlays != null) {
			if (overlays.length > 0) {
				drawTopRight(overlays[0]);
			}
				
			if (overlays.length > 1) {
				drawBottomRight(overlays[1]);
			}
				
			if (overlays.length > 2) {
				drawBottomLeft(overlays[2]);
			}
				
			if (overlays.length > 3) {
				drawTopLeft(overlays[3]);
			}
		}	
	}
	/**
	 * Draws the overlays in the top left
	 * 
	 * @param overlays  the overlay images
	 */
	protected void drawTopLeft(ImageDescriptor[] overlays) {
		if (overlays == null) return;
		int length = overlays.length;
		int x = 0;
		for (int i= 0; i < 3; i++) {
			if (i < length && overlays[i] != null) {
				ImageData id = overlays[i].getImageData();
				drawImage(id, x, 0);
				//x += id.width;
			}
		}
	}
	/**
	 * Draws the overlays in the top right
	 * 
	 * @param overlays  the overlay images
	 */
	protected void drawTopRight(ImageDescriptor[] overlays) {
		if (overlays == null) return;
		int length = overlays.length;
		//int x = getSize().x;
		int x = 0;
		for (int i = 8; i >= 0; i--) {
			if (i < length && overlays[i] != null) {
				ImageData id = overlays[i].getImageData();
				//x -= id.width;
				drawImage(id, x, 0);
			}
		}
	}
	/*
	 * @see CompositeImage#getSize
	 */
	protected Point getSize() {
		return size;
	}
}
