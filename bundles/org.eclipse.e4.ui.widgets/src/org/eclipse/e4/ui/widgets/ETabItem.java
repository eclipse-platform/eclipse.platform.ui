/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class ETabItem extends CTabItem {

/**
 * @param parent
 * @param style
 */
public ETabItem(ETabFolder parent, int style) {
	this(parent, style, parent.getItemCount());
}

/**
 * @param parent
 * @param style
 * @param index
 */
public ETabItem(ETabFolder parent, int style, int index) {
	super(parent, style, index);
}

public ETabFolder getETabParent() {
	return (ETabFolder) parent;
}

boolean useEllipses() {
	return false;
}

void drawSelected(GC gc) {
	
	if(! getETabParent().webbyStyle || this.parent.onBottom || parent.single) {
		super.drawSelected(gc);
		return;
	}
	
	int topOffset = getETabParent().getSelectedTabTopOffset();
	
	Point size = parent.getSize();
	int rightEdge = Math.min (x + width, parent.getRightItemEdge());
	
	//	 Draw selection border across all tabs
	int xx = parent.borderLeft;
	int yy = parent.borderTop + height + 1;
	int ww = size.x - parent.borderLeft - parent.borderRight;
	int hh = parent.highlight_header - 1;
	int[] shape = new int[] {xx,yy, xx+ww,yy, xx+ww,yy+hh, xx,yy+hh};
	if (parent.selectionGradientColors != null && !parent.selectionGradientVertical) {
		parent.drawBackground(gc, shape, true);
	} else {
		gc.setBackground(parent.selectionBackground);
		gc.fillRectangle(xx, yy, ww, hh);
	}
	

	// if selected tab scrolled out of view or partially out of view
	// just draw bottom line
	if (!showing){
		int x1 = Math.max(0, parent.borderLeft - 1);
		int y1 = y + height;
		int x2 = size.x - parent.borderRight;
		gc.setForeground(getETabParent().tabKeylineColor);
		gc.drawLine(x1, y1, x2, y1);
		return;
	}
		
	// draw selected tab background and outline
	shape = null;
	
	int[] left = ETabFolder.E_TOP_LEFT_CORNER;
	int[] right = ETabFolder.E_TOP_RIGHT_CORNER;

	shape = new int[left.length+right.length+8];
	int index = 0;
	shape[index++] = x; // first point repeated here because below we reuse shape to draw outline
	shape[index++] = y + height + 1;
	shape[index++] = x;
	shape[index++] = y +  height + 1;
	for (int i = 0; i < left.length/2; i++) {
		shape[index++] = x + left[2*i];
		shape[index++] = y + left[2*i+1] + topOffset;
	}
	for (int i = 0; i < right.length/2; i++) {
		shape[index++] = rightEdge - 1 + right[2*i];
		shape[index++] = y + right[2*i+1] + topOffset;
	}
	shape[index++] = rightEdge - 1;
	shape[index++] = y +  height + 1;
	shape[index++] = rightEdge - 1;
	shape[index++] = y +  height + 1;
	
	Rectangle clipping = gc.getClipping();
	Rectangle bounds = getBounds();
	bounds.height += 1;
	if (parent.onBottom) bounds.y -= 1;
	boolean tabInPaint = clipping.intersects(bounds);
	
	if (tabInPaint) {
		// fill in tab background
		if (parent.selectionGradientColors != null && !parent.selectionGradientVertical) {
			parent.drawBackground(gc, shape, true);
		} else {
			Color defaultBackground = parent.selectionBackground;
			Image image = parent.selectionBgImage;
			Color[] colors = parent.selectionGradientColors;
			int[] percents = parent.selectionGradientPercents;
			boolean vertical = parent.selectionGradientVertical;
			xx = x;
			yy = y + 1;
			ww = width;
			hh = height;
			parent.drawBackground(gc, shape, xx, yy, ww, hh, defaultBackground, image, colors, percents, vertical);
		}
	}

	//Complete the horizontal line below and before/after the selected tab
	shape[0] = Math.max(0, parent.borderLeft - 1);
	shape[shape.length - 2] = size.x - parent.borderRight + 1;

	RGB inside = parent.selectionBackground.getRGB();
	if (parent.selectionBgImage != null || 
	    (parent.selectionGradientColors != null && parent.selectionGradientColors.length > 1)) {
	    inside = null;
	}
	RGB outside = parent.getBackground().getRGB();		
	if (parent.gradientColors != null && parent.gradientColors.length > 1) {
	    outside = null;
	}
	
	Color borderColor = getETabParent().tabKeylineColor;
	parent.antialias(shape, borderColor.getRGB(), inside, outside, gc);
	gc.setForeground(borderColor);
//	debugPrintPolyline(true, shape);
	gc.drawPolyline(shape);
	
	if (!tabInPaint) return;

	// draw Image
	int xDraw = x + LEFT_MARGIN;
	if (parent.single && (parent.showClose || showClose)) xDraw += CTabFolder.BUTTON_SIZE; 
	Image image = getImage();
	if (image != null) {
		Rectangle imageBounds = image.getBounds();
		// only draw image if it won't overlap with close button
		int maxImageWidth = rightEdge - xDraw - RIGHT_MARGIN;
		if (!parent.single && closeRect.width > 0) maxImageWidth -= closeRect.width + INTERNAL_SPACING;
		if (imageBounds.width < maxImageWidth) {
			int imageX = xDraw;
			int imageY = y + getETabParent().getTextMidline() -  imageBounds.height / 2;
			imageY += parent.onBottom ? -1 : 1;
			gc.drawImage(image, imageX, imageY);
			xDraw += imageBounds.width + INTERNAL_SPACING;
		}
	}
	
	// draw Text
	int textWidth = rightEdge - xDraw - RIGHT_MARGIN;
	if (!parent.single && closeRect.width > 0) textWidth -= closeRect.width + INTERNAL_SPACING;
	if (textWidth > 0) {
		Font gcFont = gc.getFont();
		gc.setFont(font == null ? parent.getFont() : font);
		
		if (shortenedText == null || shortenedTextWidth != textWidth) {
			shortenedText = shortenText(gc, getText(), textWidth);
			shortenedTextWidth = textWidth;
		}
		Point extent = gc.textExtent(shortenedText, FLAGS);	
		int textY = y + getETabParent().getTextMidline() - extent.y / 2;
		textY += parent.onBottom ? -1 : 1;
		
		gc.setForeground(parent.selectionForeground);
		gc.drawText(shortenedText, xDraw, textY, FLAGS);
		gc.setFont(gcFont);
		
		// draw a Focus rectangle
		if (parent.isFocusControl()) {
			Display display = getDisplay();
			if (parent.simple || parent.single) {
				gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
				gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
				gc.drawFocus(xDraw-1, textY-1, extent.x+2, extent.y+2);
			} else {
				gc.setForeground(display.getSystemColor(CTabFolder.BUTTON_BORDER));
				gc.drawLine(xDraw, textY+extent.y+1, xDraw+extent.x+1, textY+extent.y+1);
			}
		}
	}
	if (parent.showClose || showClose) drawClose(gc);

}
void drawUnselected(GC gc) {
	// Do not draw partial items
	if (!showing) return;
	
	ETabFolder eTabParent = (ETabFolder) parent;

	if(! eTabParent.webbyStyle || this.parent.onBottom || parent.single) {
		super.drawUnselected(gc);
		return;
	}

	// Do not draw partial items
	if (!showing) return;
	
	Rectangle clipping = gc.getClipping();
	Rectangle bounds = getBounds();
	if (!clipping.intersects(bounds)) return;
	
	int[] shape = getUnselectedShape();
	
	//Fill tab contents
	bounds.height += 1;
	
	// fill in tab background
	Color defaultBackground = getETabParent().unselectedTabBackgroundColor;
	Color[] colors = new Color[] {getETabParent().unselectedTabBackgroundColor};
	int[] percents = new int[0];
	boolean vertical = parent.selectionGradientVertical;
	int xx = x;
	int yy = y + 1;
	int ww = width;
	int hh = height;
	parent.drawBackground(gc, shape, xx, yy, ww, hh, defaultBackground, null, colors, percents, vertical);
	
	// draw border
	drawUnselectedBorder(gc, shape);

	// draw Image
	int xDraw = x + LEFT_MARGIN;
	Image image = getImage();
	if (image != null && parent.showUnselectedImage) {
		Rectangle imageBounds = image.getBounds();
		// only draw image if it won't overlap with close button
		int maxImageWidth = x + width - xDraw - RIGHT_MARGIN;
		if (parent.showUnselectedClose && (parent.showClose || showClose)) {
			maxImageWidth -= closeRect.width + INTERNAL_SPACING;
		}

		if (imageBounds.width < maxImageWidth) {		
			int imageX = xDraw;
			int imageHeight = imageBounds.height;
			int imageY = y + getETabParent().getTextMidline() - imageHeight / 2;
			imageY += parent.onBottom ? -1 : 1;
			int imageWidth = imageBounds.width * imageHeight / imageBounds.height;
			gc.drawImage(image, 
				         imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height,
				         imageX, imageY, imageWidth, imageHeight);
			xDraw += imageWidth + INTERNAL_SPACING;
		}
	}
	// draw Text
	int textWidth = x + width - xDraw - RIGHT_MARGIN;
	if (parent.showUnselectedClose && (parent.showClose || showClose)) {
		textWidth -= closeRect.width + INTERNAL_SPACING;
	}
	if (textWidth > 0) {
		Font gcFont = gc.getFont();
		gc.setFont(font == null ? parent.getFont() : font);
		if (shortenedText == null || shortenedTextWidth != textWidth) {
			shortenedText = shortenText(gc, getText(), textWidth);
			shortenedTextWidth = textWidth;
		}	
		Point extent = gc.textExtent(shortenedText, FLAGS);
		int textY = y + getETabParent().getTextMidline() - extent.y / 2;
		textY += parent.onBottom ? -1 : 1;
		gc.setForeground(parent.getForeground());
		gc.drawText(shortenedText, xDraw, textY, FLAGS);
		gc.setFont(gcFont);
	}
	// draw close
	if (parent.showUnselectedClose && (parent.showClose || showClose)) drawClose(gc);

}

int[] getUnselectedShape() {
	int[] shape = null;

	int[] left = ETabFolder.E_TOP_LEFT_CORNER;
	int[] right = ETabFolder.E_TOP_RIGHT_CORNER;
	
	int topOffset = getETabParent().getUnselectedTabTopOffset();
	shape = new int[left.length + 2 + right.length + 2];
	int index = 0;
	shape[index++] = x;
	shape[index++] = y + height;
	for (int i = 0; i < left.length / 2; i++) {
		shape[index++] = x + left[2 * i];
		shape[index++] = y + left[2 * i + 1] + topOffset;
	}

	int startX = x + width - 1;

	for (int i = 0; i < right.length / 2; i++) {
		shape[index++] = startX + right[2 * i];
		shape[index++] = y + right[2 * i + 1] + topOffset;
	}

	shape[index++] = startX;
	shape[index++] = y + height;
	return shape;
}

void drawUnselectedBorder(GC gc, int shape[]) {
	Color borderColor = getETabParent().tabKeylineColor;
//	parent.antialias(shape, borderColor.getRGB(), inside, outside, gc);
	gc.setForeground(borderColor);
//	debugPrintPolyline(false, shape);
	gc.drawPolyline(shape);
}

int preferredHeight(GC gc) {
	
	if(! getETabParent().webbyStyle || this.parent.onBottom || parent.single) {
		return super.preferredHeight(gc);
	}

	Image image = getImage();
	int h = (image == null) ? 0 : image.getBounds().height;
	String text = getText();
	if (font == null) {
		h = Math.max(h, gc.textExtent(text, FLAGS).y);
	} else {
		Font gcFont = gc.getFont();
		gc.setFont(font);
		h = Math.max(h, gc.textExtent(text, FLAGS).y);
		gc.setFont(gcFont);
	}
	int prefHeight = h
		+ getETabParent().tabTopMargin
		+ getETabParent().getMaxTabTopPadding()
		+ getETabParent().tabBottomPadding;
		//all tabs pick max height
	return prefHeight;
}

public Rectangle getBounds () {
	//checkWidget();
	if(! getETabParent().webbyStyle || this.parent.onBottom || parent.single) {
		return super.getBounds();
	}
	
	int yy = (parent.indexOf(this) == parent.selectedIndex)
		? getETabParent().getSelectedTabTopOffset()
		: getETabParent().getUnselectedTabTopOffset();
	int w = width;
	int h = height - yy;
	return new Rectangle(x, yy, w, h);
}

void debugPrintPolyline(boolean selected, int[] shape) {
	System.out.println(
		(selected ? "selected" : "unselected") + " polyline ("+ getText()+"): " );
	for (int i = 0; i < shape.length; i+=2) {
		System.out.print(shape[i]);
		System.out.print("@");
		System.out.print(shape[i+1]);
		if(i != shape.length -2)
			System.out.print(", ");
		else
			System.out.println();
	}
}
void drawClose(GC gc) {

	if (closeRect.width == 0 || closeRect.height == 0) return;
	Display display = getDisplay();

	// draw X 9x9
	int indent = Math.max(1, (CTabFolder.BUTTON_SIZE-9)/2);
	int x = closeRect.x + indent;
	int y = closeRect.y + indent;
	y += parent.onBottom ? -1 : 1;
	
	Color closeBorder = display.getSystemColor(CTabFolder.BUTTON_BORDER);
	switch (closeImageState) {
		case CTabFolder.NORMAL: {
			int[] shape = new int[] {x,y, x+2,y, x+4,y+2, x+5,y+2, x+7,y, x+9,y, 
					                 x+9,y+2, x+7,y+4, x+7,y+5, x+9,y+7, x+9,y+9,
			                         x+7,y+9, x+5,y+7, x+4,y+7, x+2,y+9, x,y+9,
			                         x,y+7, x+2,y+5, x+2,y+4, x,y+2};
			gc.setBackground(getETabParent().getUnselectedTabBackgroundColor());
			gc.fillPolygon(shape);
			gc.setForeground(closeBorder);
			gc.drawPolygon(shape);
			break;
		}
		case CTabFolder.HOT: {
			int[] shape = new int[] {x,y, x+2,y, x+4,y+2, x+5,y+2, x+7,y, x+9,y, 
					                 x+9,y+2, x+7,y+4, x+7,y+5, x+9,y+7, x+9,y+9,
			                         x+7,y+9, x+5,y+7, x+4,y+7, x+2,y+9, x,y+9,
			                         x,y+7, x+2,y+5, x+2,y+4, x,y+2};
			gc.setBackground(parent.getFillColor());
			gc.fillPolygon(shape);
			gc.setForeground(closeBorder);
			gc.drawPolygon(shape);
			break;
		}
		case CTabFolder.SELECTED: {
			int[] shape = new int[] {x+1,y+1, x+3,y+1, x+5,y+3, x+6,y+3, x+8,y+1, x+10,y+1, 
					                 x+10,y+3, x+8,y+5, x+8,y+6, x+10,y+8, x+10,y+10,
			                         x+8,y+10, x+6,y+8, x+5,y+8, x+3,y+10, x+1,y+10,
			                         x+1,y+8, x+3,y+6, x+3,y+5, x+1,y+3};
			gc.setBackground(parent.getFillColor());
			gc.fillPolygon(shape);
			gc.setForeground(closeBorder);
			gc.drawPolygon(shape);
			break;
		}
		case CTabFolder.NONE: {
			int[] shape = new int[] {x,y, x+10,y, x+10,y+10, x,y+10};
			if (parent.gradientColors != null && !parent.gradientVertical) {
				parent.drawBackground(gc, shape, false);
			} else {
				Color defaultBackground = getETabParent().getUnselectedTabBackgroundColor();
				Color[] colors = parent.gradientColors;
				int[] percents = parent.gradientPercents;
				boolean vertical = parent.gradientVertical; 
				parent.drawBackground(gc, shape, x, y, 10, 10, defaultBackground, null, colors, percents, vertical);
			}
			break;
		}
	}
}
}
