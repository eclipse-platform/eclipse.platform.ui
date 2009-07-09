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
package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class ETabFolder extends CTabFolder {

	boolean webbyStyle = false;

	static final int[] E_TOP_LEFT_CORNER = new int[] {0,5, 1,4, 1,3, 2,2, 3,1, 4,1, 5,0};
	static final int[] E_TOP_RIGHT_CORNER = new int[] {-5,0, -4,1, -3,1, -2,2, -1,3, -1,4, 0,5};

	static final int[] E_TOP_LEFT_CORNER_BORDERLESS = new int[] {};
	static final int[] E_TOP_RIGHT_CORNER_BORDERLESS = new int[] {};
	
	int topMargin = 0;  //The space above the highest (selected) tab
	int selectionMargin = 3;  //bonus margin for selected tabs
	int tabTopMargin = 6;  //margin within tab above text below line
	int tabBottomMargin = 6; //bottom margin within tab
	int hSpace = 2;  //horizontal spacing between tabs
	int leftMargin = 4;  //first horizontal space
	
	Color exteriorKeyLineColor;
	Color interiorKeyLineColor;
	
/**
 * @param parent
 * @param style
 */
public ETabFolder(Composite parent, int style) {
	super(parent, style);
}

void init(int style) {
	super.init(style);
	RGB exteriorKeyLineRGB = new RGB(201,200, 204);
	RGB interiorKeyLineRGB = new RGB(208, 207, 212);
	
	exteriorKeyLineColor = new Color(getDisplay(), exteriorKeyLineRGB);
	interiorKeyLineColor = new Color(getDisplay(), interiorKeyLineRGB);
}

public void dispose() {
	super.dispose();
	
	if(exteriorKeyLineColor != null) {
		exteriorKeyLineColor.dispose();
		exteriorKeyLineColor = null;
	}
	if(interiorKeyLineColor != null) {
		interiorKeyLineColor.dispose();
		interiorKeyLineColor = null;
	}
}

public boolean getWebbyStyle() {
	return webbyStyle;
}

public void setWebbyStyle(boolean webbyStyle) {
	checkWidget();
	this.webbyStyle = webbyStyle;
	layout();
	redrawTabs();
	redraw();
	if(this.webbyStyle != webbyStyle) {
		this.webbyStyle = webbyStyle;
		updateTabHeight(true);
		if(webbyStyle && single) {
			setSingle(false); //will cause update
			return; //no update needed
		}
		Rectangle rectBefore = getClientArea();
		updateItems();
		Rectangle rectAfter = getClientArea();
		if (!rectBefore.equals(rectAfter)) {
			notifyListeners(SWT.Resize, new Event());
		}
		redraw();
	}
}

int getTextMidline() {
	return (tabHeight - topMargin - selectionMargin) /2 + topMargin + selectionMargin;
}

void drawBody(Event event) {
	GC gc = event.gc;
	Point size = getSize();
	
	// fill in body
	if (!minimized){
		int width = size.x  - borderLeft - borderRight - 2*highlight_margin;
		int height = size.y - borderTop - borderBottom - tabHeight - highlight_header - highlight_margin;
		// Draw highlight margin
		if (highlight_margin > 0) {
			int[] shape = null;
			if (onBottom) {
				int x1 = borderLeft;
				int y1 = borderTop;
				int x2 = size.x - borderRight;
				int y2 = size.y - borderBottom - tabHeight - highlight_header;
				shape = new int[] {x1,y1, x2,y1, x2,y2, x2-highlight_margin,y2,
						           x2-highlight_margin, y1+highlight_margin, x1+highlight_margin,y1+highlight_margin,
								   x1+highlight_margin,y2, x1,y2};
			} else {	
				int x1 = borderLeft;
				int y1 = borderTop + tabHeight + highlight_header;
				int x2 = size.x - borderRight;
				int y2 = size.y - borderBottom;
				shape = new int[] {x1,y1, x1+highlight_margin,y1, x1+highlight_margin,y2-highlight_margin, 
						           x2-highlight_margin,y2-highlight_margin, x2-highlight_margin,y1,
								   x2,y1, x2,y2, x1,y2};
			}
			// If horizontal gradient, show gradient across the whole area
			if (selectedIndex != -1 && selectionGradientColors != null && selectionGradientColors.length > 1 && !selectionGradientVertical) {
				drawBackground(gc, shape, true);
			} else if (selectedIndex == -1 && gradientColors != null && gradientColors.length > 1 && !gradientVertical) {
				drawBackground(gc, shape, false);
			} else {
				gc.setBackground(selectedIndex == -1 ? getBackground() : selectionBackground);
				gc.fillPolygon(shape);
			}
		}
		//Draw client area
		if ((getStyle() & SWT.NO_BACKGROUND) != 0) {
			gc.setBackground(getBackground());
			gc.fillRectangle(xClient - marginWidth, yClient - marginHeight, width, height);
		}
	} else {
		if ((getStyle() & SWT.NO_BACKGROUND) != 0) {
			int height = borderTop + tabHeight + highlight_header + borderBottom;
			if (size.y > height) {
				gc.setBackground(getParent().getBackground());
				gc.fillRectangle(0, height, size.x, size.y - height);
			}
		}
	}
	
	//draw 1 pixel border around outside
	if (borderLeft > 0) {
		gc.setForeground(exteriorKeyLineColor);
		int x1 = borderLeft - 1;
		int x2 = size.x - borderRight;
		int y1 = onBottom ? borderTop - 1 : borderTop + tabHeight;
		int y2 = onBottom ? size.y - tabHeight - borderBottom - 1 : size.y - borderBottom;
		gc.drawLine(x1, y1, x1, y2); // left
		gc.drawLine(x2, y1, x2, y2); // right
		if (onBottom) {
			gc.drawLine(x1, y1, x2, y1); // top
		} else {
			gc.drawLine(x1, y2, x2, y2); // bottom
		}
	}
}
void drawTabArea(Event event) {
	if (!webbyStyle || onBottom || single) {
		super.drawTabArea(event);
		return;
	}

	GC gc = event.gc;
	Point size = getSize();
	int[] shape = null;
	Color borderColor = exteriorKeyLineColor;

	if (tabHeight == 0) {
		int style = getStyle();
		if ((style & SWT.FLAT) != 0 && (style & SWT.BORDER) == 0)
			return;
		int x1 = borderLeft - 1;
		int x2 = size.x - borderRight;
		int y1 = borderTop + highlight_header;
		int y2 = borderTop;
		if (borderLeft > 0 && onBottom)
			y2 -= 1;

		shape = new int[] { x1, y1, x1, y2, x2, y2, x2, y1 };

		// If horizontal gradient, show gradient across the whole area
		if (selectedIndex != -1 && selectionGradientColors != null
				&& selectionGradientColors.length > 1
				&& !selectionGradientVertical) {
			drawBackground(gc, shape, true);
		} else if (selectedIndex == -1 && gradientColors != null
				&& gradientColors.length > 1 && !gradientVertical) {
			drawBackground(gc, shape, false);
		} else {
			gc.setBackground(selectedIndex == -1 ? getBackground()
					: selectionBackground);
			gc.fillPolygon(shape);
		}

		// draw 1 pixel border
		if (borderLeft > 0) {
			gc.setForeground(borderColor);
			gc.drawPolyline(shape);
		}
		return;
	}

	int x = Math.max(0, borderLeft - 1);
	int y = borderTop;
	int width = size.x - borderLeft - borderRight + 1;
	int height = tabHeight - 1;

	// Draw Tab Header
	int[] left, right;
	if ((getStyle() & SWT.BORDER) != 0) {
		left = E_TOP_LEFT_CORNER;
		right = E_TOP_RIGHT_CORNER;
	} else {
		left = E_TOP_LEFT_CORNER_BORDERLESS;
		right = E_TOP_RIGHT_CORNER_BORDERLESS;
	}
	
	shape = new int[left.length + right.length + 4];
	int index = 0;
	shape[index++] = x;
	shape[index++] = y + height + highlight_header + 1;
	for (int i = 0; i < left.length / 2; i++) {
		shape[index++] = x + left[2 * i];
		shape[index++] = y + left[2 * i + 1];
	}
	for (int i = 0; i < right.length / 2; i++) {
		shape[index++] = x + width + right[2 * i];
		shape[index++] = y + right[2 * i + 1];
	}
	shape[index++] = x + width;
	shape[index++] = y + height + highlight_header + 1;

	// Fill in background
	boolean bkSelected = single && selectedIndex != -1;
	drawBackground(gc, shape, bkSelected);
	// Fill in parent background for non-rectangular shape
	Region r = new Region();
	r.add(new Rectangle(x, y, width + 1, height + 1));
	r.subtract(shape);
	gc.setBackground(getParent().getBackground());
	fillRegion(gc, r);
	r.dispose();

	// Draw the unselected tabs.
	for (int i = 0; i < items.length; i++) {
		if (i != selectedIndex
				&& event.getBounds().intersects(items[i].getBounds())) {
			items[i].onPaint(gc, false);
		}
	}

	// Draw selected tab
	if (selectedIndex != -1) {
		CTabItem item = items[selectedIndex];
		item.onPaint(gc, true);
	} else {
		// if no selected tab - draw line across bottom of all tabs
		int x1 = borderLeft;
		int y1 = borderTop + tabHeight;
		int x2 = size.x - borderRight;
		gc.setForeground(borderColor);
		gc.drawLine(x1, y1, x2, y1);
	}

	// Draw Buttons
	drawChevron(gc);
	drawMinimize(gc);
	drawMaximize(gc);

	// Draw border line
	if (borderLeft > 0) {
		RGB outside = getParent().getBackground().getRGB();
		antialias(shape, borderColor.getRGB(), null, outside, gc);
		gc.setForeground(borderColor);
		gc.drawPolyline(shape);
	}
}
	
boolean setItemLocation() {
	if(!webbyStyle || onBottom || single) {
		return super.setItemLocation();
	}
	
	boolean changed = false;
	if (items.length == 0) return false;
	int y = borderTop;

	int rightItemEdge = getRightItemEdge();
	int maxWidth = rightItemEdge - borderLeft;
	int width = 0;
	for (int i = 0; i < priority.length; i++) {
		CTabItem item = items[priority[i]];
		width += item.width;
		item.showing = i == 0 ? true : item.width > 0 && width <= maxWidth;
	}
	int x = leftMargin;
	int defaultX = getDisplay().getBounds().width + 10; // off screen
	firstIndex = items.length - 1;
	for (int i = 0; i < items.length; i++) {
		ETabItem item = (ETabItem) items[i];
		if (!item.showing) {
			if (item.x != defaultX) changed = true;
			item.x = defaultX;
		} else {
			firstIndex = Math.min(firstIndex, i);
			if (item.x != x || item.y != y) changed = true;
			item.x = x;
			item.y = y;
			if (i == selectedIndex) {
				int edge = Math.min(item.x + item.width, rightItemEdge);
				item.closeRect.x = edge - CTabItem.RIGHT_MARGIN - BUTTON_SIZE;
				item.y = item.y;
			} else {
				item.closeRect.x = item.x + item.width - CTabItem.RIGHT_MARGIN - BUTTON_SIZE;
				item.y = item.y;
			}
			item.closeRect.y = getTextMidline() - BUTTON_SIZE /2;
			x = x + item.width + hSpace;
		}
	}
	
	return changed;
}


//The space above the selected tab
int getSelectedTabTopOffset() {
	return topMargin;
}
//The space above the unselected tab
int getUnselectedTabTopOffset() {
	return topMargin + selectionMargin;
}

}
