/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.presentation.sidewinder;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.IPresentablePart;

public class PartItem extends Canvas implements PaintListener {

	private String text;
	private Image image;
	private boolean selected;
	private boolean focus = false;
	private IPresentablePart part;
	
	private static final int VERT_SPACING = 5;
	private static final int HORIZ_SPACING = 3;
	
	int[] SIMPLE_TOP_LEFT_CORNER = new int[] {0,4, 4,0};
	int[] SIMPLE_TOP_RIGHT_CORNER = new int[] {0,0, 0,0};
	
	//static final int[] SIMPLE_TOP_LEFT_CORNER = new int[] {0,2, 1,1, 2,0};
	//static final int[] SIMPLE_TOP_RIGHT_CORNER = new int[] {-2,0, -1,1, 0,2};
	
	static final int[] SIMPLE_BOTTOM_LEFT_CORNER = new int[] {0,-2, 1,-1, 2,0};
	static final int[] SIMPLE_BOTTOM_RIGHT_CORNER = new int[] {-2,0, -1,-1, 0,-2};
	private boolean showImage;
	private boolean showText;
	
	public PartItem(Composite parent, IPresentablePart part) {
		super(parent, SWT.NONE);
		addPaintListener(this);
		this.part = part;
	}

	public void paintControl(PaintEvent e) {
		Rectangle titleRect = getClientArea();
		int x = titleRect.x + VERT_SPACING;
		int y = titleRect.y + HORIZ_SPACING;
		GC gc = e.gc;
		setBackground(getParent().getBackground());
		fill(gc, titleRect.x, titleRect.y, titleRect.width - 1, titleRect.height);
		
		Image image = getImage();
		if (image != null && showImage) {
			Rectangle imageBounds = image.getBounds();
				int imageX = x;
				int imageHeight = imageBounds.height;
				int imageY = (titleRect.height - imageHeight) / 2;
				int imageWidth = imageBounds.width * imageHeight / imageBounds.height;
				gc.drawImage(image, 
					         imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height,
					         imageX, imageY, imageWidth, imageHeight);
				x += imageWidth + VERT_SPACING;		
		}
		
		int textWidth = titleRect.width - 1;
		if (textWidth > 0 && text != null && showText) {
			Font gcFont = gc.getFont();
			gc.setFont(getFont());
			Point extent = gc.textExtent(text, SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC);	
			int textY = titleRect.y + (titleRect.height - extent.y) / 2;
			
			if(selected)					
				gc.setForeground(e.display.getSystemColor(SWT.COLOR_WHITE));
			else 
				gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
			gc.setFont(JFaceResources.getDefaultFont());
			gc.drawText(text, x, textY, SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC);	
		}
		
	}	
	
	public Point computeSize(int wHint, int hHint) {
		int width = VERT_SPACING; int height = HORIZ_SPACING;
		GC gc = new GC(this);
		if(image != null && showImage) {
			Rectangle imageBounds = image.getBounds();			
			height = imageBounds.height + HORIZ_SPACING;
			width += imageBounds.width + VERT_SPACING;
		}
		
		if(text != null && showText) {
			Point extent = gc.textExtent(text, SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC);
			width += extent.x + VERT_SPACING;
			height = Math.max(height, extent.y) + HORIZ_SPACING;
		}
		
		if (wHint != SWT.DEFAULT) width = wHint;
		if (hHint != SWT.DEFAULT) height = hHint;
		gc.dispose();
		return new Point(width, height);
	}
	
	private void fill(GC gc, int x, int y, int width, int height) {
		int[] left = SIMPLE_TOP_LEFT_CORNER;
		int[] right = SIMPLE_TOP_RIGHT_CORNER;
		int[] shape = new int[left.length + right.length + 4];
		int index = 0;
		shape[index++] = x;
		shape[index++] = y + height + 1;
		for (int i = 0; i < left.length / 2; i++) {
			shape[index++] = x + left[2 * i];
			shape[index++] = y + left[2 * i + 1];
		}
		for (int i = 0; i < right.length / 2; i++) {
			shape[index++] = x + width + right[2 * i];
			shape[index++] = y + right[2 * i + 1];
		}
		shape[index++] = x + width;
		shape[index++] = y + height + 1;
		
		// Fill in background
		Region clipping = new Region();
		gc.getClipping(clipping);
		Region region = new Region();
		region.add(shape);
		region.intersect(clipping);
		gc.setClipping(region);
		
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		
		Color fg = null;
		if(part.isDirty())
			fg = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
		else if(!selected)
			fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		//else if(focus)
			//fg = getDisplay().getSystemColor(SWT.COLOR_BLUE);
		else 
			fg = getDisplay().getSystemColor(SWT.COLOR_BLUE);
		
		gc.setForeground(fg);
		
		gc.fillGradientRectangle(x, y, x + width, y + height, true);
		//gc.fillRectangle(x, y, x + width, y + height);
		
		
		region.dispose();
		clipping.dispose();
		
		gc.setClipping((Rectangle)null);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		gc.drawPolyline(shape);
		
		
		// Fill in parent background for non-rectangular shape
		Region r = new Region();
		r.add(new Rectangle(x, y, width + 1, height + 1));
		r.subtract(shape);
		gc.setBackground(getParent().getBackground());
		//fillRegion(gc, r);
		r.dispose();
	}
	
	static void fillRegion(GC gc, Region region) {
		// NOTE: region passed in to this function will be modified
		Region clipping = new Region();
		gc.getClipping(clipping);
		region.intersect(clipping);
		gc.setClipping(region);
		gc.fillRectangle(region.getBounds());
		gc.setClipping(clipping);
		clipping.dispose();
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return computeSize(wHint, hHint);
	}
	
	public void setText(String text) {
		this.text = text;
		redraw();
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setImage(Image image) {
		this.image = image;
	}
	
	public Image getImage() {
		return this.image;	
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
		redraw();
	}
	
	public boolean getSelected() {
		return this.selected;
	}
	
	public boolean isFocus() {
		return focus;
	}
	
	public void setFocus(boolean focus) {
		this.focus = focus;
	}

	public void setShowImage(boolean showImage) {
		this.showImage = showImage;
	}

	public void setShowText(boolean showText) {
		this.showText = showText;
	}
}
