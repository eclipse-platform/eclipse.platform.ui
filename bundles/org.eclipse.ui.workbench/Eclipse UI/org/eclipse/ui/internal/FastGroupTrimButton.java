/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Implements the 'group' handling for trim groups
 * 
 * @since 3.2
 *
 */
public class FastGroupTrimButton {
	private Canvas button;
	private FastViewBar fvb;
	private int side;
	private Color btnColor;
	
	private Rectangle closeRect;
	private Rectangle restoreRect;
	private Rectangle collapseRect;

	private static final int CTRL_AREA_NONE = 0;
	private static final int CTRL_AREA_CLOSE = 1;
	private static final int CTRL_AREA_RESTORE = 2;
	private static final int CTRL_AREA_COLLAPSE = 3;
	
	private int curCtrlArea = CTRL_AREA_NONE;
	private boolean inControl = false;
	private String toolTip;
	
	public FastGroupTrimButton(Composite parent, FastViewBar fvb) {
		this.fvb = fvb;
		
		if (fvb != null)
			side = fvb.getSide();
		else
			side = SWT.BOTTOM;
		
		btnColor = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		
		button = new Canvas(parent, SWT.NONE);
		button.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				e.gc.setForeground(btnColor);
				paintButtons(e);
			}
		});
		
		button.addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
				inControl = true;
				// Provide 'track' feedback?
			}
			public void mouseExit(MouseEvent e) {
				inControl = false;
				// remove 'track' feedback?
			}
			public void mouseHover(MouseEvent e) {
				button.setToolTipText(toolTip);
			}			
		});
		
		button.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (closeRect.contains(e.x,e.y)) {
					curCtrlArea = CTRL_AREA_CLOSE;
					toolTip = "Close Group"; //$NON-NLS-1$
				}
				else if (restoreRect.contains(e.x,e.y)) {
					curCtrlArea = CTRL_AREA_RESTORE;
					toolTip = "Restore Group"; //$NON-NLS-1$
				}
				else if (collapseRect.contains(e.x,e.y)) {
					curCtrlArea = CTRL_AREA_COLLAPSE;
					toolTip = "Collapse Group"; //$NON-NLS-1$
				}
				else
					curCtrlArea = CTRL_AREA_NONE;
			}
		});
		button.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseUp(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {
				if (inControl && curCtrlArea != CTRL_AREA_NONE) {
					switch (curCtrlArea) {
					case CTRL_AREA_CLOSE:
						FastGroupTrimButton.this.fvb.closeGroup();
						break;
					case CTRL_AREA_COLLAPSE:
						FastGroupTrimButton.this.fvb.collapseGroup();
						break;
					case CTRL_AREA_RESTORE:
						FastGroupTrimButton.this.fvb.restoreGroup();
						break;
					}
				}
			}			
		});
	}

	public void setSize(int size) {
		button.setSize(size, size);
	}
	
	protected void paintButtons(PaintEvent e) {
		Rectangle bb = button.getBounds();
		setButtonRects(bb);
		
		paintClose(e.gc);
		paintRestore(e.gc);
		paintCollapse(e.gc);
	}

	private void paintCollapse(GC gc) {
		switch(side) {
		case SWT.BOTTOM:
			drawDownArrow(gc, collapseRect);
			break;
		case SWT.TOP:
			drawUpArrow(gc, collapseRect);
			break;
		case SWT.LEFT:
			drawLeftArrow(gc, collapseRect);
			break;
		case SWT.RIGHT:
			drawRightArrow(gc, collapseRect);
			break;
		}
	}

	private void paintRestore(GC gc) {
		switch(side) {
		case SWT.BOTTOM:
			drawUpArrow(gc, restoreRect);
			break;
		case SWT.TOP:
			drawDownArrow(gc, restoreRect);
			break;
		case SWT.LEFT:
			drawRightArrow(gc, restoreRect);
			break;
		case SWT.RIGHT:
			drawLeftArrow(gc, restoreRect);
			break;
		}
	}

	private void paintClose(GC gc) {
		int border = 2;
		gc.drawLine(closeRect.x+border, closeRect.y+border, (closeRect.x+closeRect.width)-border, (closeRect.y+closeRect.height)-border);
		gc.drawLine((closeRect.x+closeRect.width)-border, closeRect.y+border, closeRect.x+border, (closeRect.y+closeRect.height)-border);
//		int minX = closeRect.x + 2;
//		int maxX = (closeRect.x+closeRect.width) - 2;
//		int stopMax = maxX;
//		int y = (closeRect.y + (closeRect.height)/2) - ((maxX-minX)/2);
//
//		while (minX <= stopMax) {
//			gc.drawLine(minX, y, minX, y);
//			gc.drawLine(maxX, y, maxX, y);
//			minX++; maxX--; y++;
//		}
	}

	private void drawDownArrow(GC gc, Rectangle rect) {
		int y = rect.y + 1;
		int minX = rect.x + 2;
		int maxX = (rect.x+rect.width) - 2;
		
		while (minX <= maxX) {
			gc.drawLine(minX, y, maxX, y);
			y++;
			gc.drawLine(minX, y, maxX, y);
			minX++; maxX--; y++;
		}
	}

	private void drawRightArrow(GC gc, Rectangle rect) {
		int x = rect.x + 1;
		int minY = rect.y + 2;
		int maxY = (rect.y+rect.height) - 2;
		
		while (minY <= maxY) {
			gc.drawLine(x, minY, x, maxY);
			x++;
			gc.drawLine(x, minY, x, maxY);
			minY++; maxY--; x++;
		}
	}

	private void drawUpArrow(GC gc, Rectangle rect) {
		int y = (rect.y+rect.height) - 1;
		int minX = rect.x + 2;
		int maxX = (rect.x+rect.width) - 2;
		
		while (minX <= maxX) {
			gc.drawLine(minX, y, maxX, y);
			y--;
			gc.drawLine(minX, y, maxX, y);
			minX++; maxX--; y--;
		}
	}

	private void drawLeftArrow(GC gc, Rectangle rect) {
		int x = (rect.x+rect.width) - 1;
		int minY = rect.y + 2;
		int maxY = (rect.y+rect.height) - 2;
		
		while (minY <= maxY) {
			gc.drawLine(x, minY, x, maxY);
			x--;
			gc.drawLine(x, minY, x, maxY);
			minY++; maxY--; x--;
		}
	}

	private void setButtonRects(Rectangle bb) {
		int hw = bb.width/2;
		int hh = bb.height/2;

		switch (side) {
		case SWT.BOTTOM:
			closeRect = new Rectangle(bb.x, bb.y+(bb.height/4), hw, hh);
			restoreRect = new Rectangle(bb.x+hw, bb.y, hw, hh);
			collapseRect = new Rectangle(bb.x+hw, bb.y+hh, hw, hh);
			break;
		case SWT.TOP:
			closeRect = new Rectangle(bb.x, bb.y+(bb.height/4), hw, hh);
			collapseRect = new Rectangle(bb.x+hw, bb.y, hw, hh);
			restoreRect = new Rectangle(bb.x+hw, bb.y+hh, hw, hh);
			break;
		case SWT.LEFT:
			closeRect = new Rectangle(bb.x+(bb.width/4), bb.y, hw, hh);
			collapseRect = new Rectangle(bb.x, bb.y+hh, hw, hh);
			restoreRect = new Rectangle(bb.x+hw, bb.y+hh, hw, hh);
			break;
		case SWT.RIGHT:
			closeRect = new Rectangle(bb.x+(bb.width/4), bb.y, hw, hh);
			restoreRect = new Rectangle(bb.x, bb.y+hh, hw, hh);
			collapseRect = new Rectangle(bb.x+hw, bb.y+hh, hw, hh);
			break;
		}
	}

	public Control getControl() {
		return button;
	}
}
