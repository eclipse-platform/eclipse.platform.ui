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
package org.eclipse.team.internal.ui.sync.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.subscribers.SyncInfo;

/**
 * A progress bar with a red/green indication for success or failure.
 */
public class StatisticsCounterBar extends Canvas {
	private static final int DEFAULT_WIDTH = 160;
	private static final int DEFAULT_HEIGHT = 20;

	private int outgoingCount = 0;
	private int incomingCount = 0;	
	private int conflicCount = 0;
	
	private int totalCount = 0;
	
	private int fColorBarWidth = 0;

	private Color outgoingColor;
	private Color incomingColor;
	private Color conflictColor;
	
	private int activeMode = SyncInfo.IN_SYNC;
	
	private final Cursor counterBarCursor = new Cursor(getDisplay(), SWT.CURSOR_HAND);
	
	public StatisticsCounterBar(Composite parent, int cSpan) {
		super(parent, SWT.NONE);
		
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				redraw();
			}
		});	
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});
		
		addMouseMoveListener(
			new MouseMoveListener() {		
				private Cursor fLastCursor;		
				public void mouseMove(MouseEvent e) {
					Cursor cursor= null;
					int mode = handlemouseInCounterBar(e.x, e.y);
					if (mode != SyncInfo.IN_SYNC)
						cursor= counterBarCursor;
					if (fLastCursor != cursor) {
						setCursor(cursor);
						fLastCursor= cursor;
					}
				}
			}
		);
		
		addMouseListener(
			new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					int mode = handlemouseInCounterBar(e.x, e.y);
					if(mode != activeMode) {
						activeMode = mode;
						redraw();
					}
				}
			}
		);
		
		Display display= parent.getDisplay();
		conflictColor = new Color(display, 204, 51, 51);
		incomingColor = new Color(display, 51, 102, 204);
		outgoingColor = new Color(display, 102, 102, 102);
		
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = cSpan;
		setLayoutData(data);
	}

	protected int handlemouseInCounterBar(int x, int y) {
		int nextX = 1;
		int sizeX = 0;
		if(conflicCount > 0) {
			sizeX = scale(conflicCount);
			if(x >= nextX && x <= sizeX) return SyncInfo.CONFLICTING;
			nextX = (sizeX - 1);
		}
		if(incomingCount > 0) {
			sizeX = scale(incomingCount) + nextX;
			if(x >= nextX && x <= sizeX) return SyncInfo.INCOMING;
			nextX = (sizeX - 1);
		}
		if(outgoingCount > 0) {
			sizeX = scale(outgoingCount) + nextX;
			if(x >= nextX && x <= sizeX) return SyncInfo.OUTGOING;			
		}
		return SyncInfo.IN_SYNC;
	}

	public void reset() {
		conflicCount = 0;
		incomingCount = 0;
		outgoingCount = 0;
		fColorBarWidth = 0;
		redraw();
	}
	
	private void paintStep(int startX, int endX, int direction) {
		GC gc = new GC(this);	
		setStatusColor(gc, direction);
		Rectangle rect= getClientArea();
		startX= Math.max(1, startX);
		gc.fillRectangle(startX, 1, endX, rect.height-2);
		gc.dispose();		
	}

	public void dispose() {
		super.dispose();
		conflictColor.dispose();
		outgoingColor.dispose();
		incomingColor.dispose();
	}
	
	private void setStatusColor(GC gc, int direction) {
		switch(direction) {
			case SyncInfo.OUTGOING:
				gc.setBackground(outgoingColor); break;
			case SyncInfo.INCOMING:
				gc.setBackground(incomingColor); break;
			case SyncInfo.CONFLICTING:
				gc.setBackground(conflictColor); break;
		}
	}

	private int scale(int value) {
		if (totalCount > 0) {
			Rectangle r= getClientArea();
			if (r.width != 0)
				return Math.max(0, value*(r.width-2)/totalCount);
		}
		return value; 
	}
	
	private void drawBevelRect(GC gc, int x, int y, int w, int h, Color topleft, Color bottomright) {
		gc.setForeground(topleft);
		gc.drawLine(x, y, x+w-1, y);
		gc.drawLine(x, y, x, y+h-1);
		
		gc.setForeground(bottomright);
		gc.drawLine(x+w, y, x+w, y+h);
		gc.drawLine(x, y+h, x+w, y+h);
	}
	
	private void paint(PaintEvent event) {
		GC gc = event.gc;
		Display disp= getDisplay();
			
		Rectangle rect= getClientArea();
		gc.fillRectangle(rect);
		drawBevelRect(gc, rect.x, rect.y, rect.width-1, rect.height-1,
			disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
			disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		
		int nextX = 1;
		int sizeX = 0;
		if(conflicCount > 0) {
			sizeX = scale(conflicCount);
			paintStep(nextX, sizeX, SyncInfo.CONFLICTING);
			if(activeMode == SyncInfo.CONFLICTING) {
				drawBorder(gc, rect, nextX, sizeX);
			}
			nextX = (sizeX - 1);
		}
		if(incomingCount > 0) {
			sizeX = scale(incomingCount) + nextX;
			paintStep(nextX, sizeX, SyncInfo.INCOMING);
			if(activeMode == SyncInfo.INCOMING) {
				drawBorder(gc, rect, nextX, sizeX);
			}
			nextX = (sizeX - 1);
		}
		if(outgoingCount > 0) {
			sizeX = scale(outgoingCount) + nextX;
			paintStep(nextX, sizeX, SyncInfo.OUTGOING);			
			if(activeMode == SyncInfo.OUTGOING) {
				drawBorder(gc, rect, nextX, sizeX);
			}
		}		
	}	
	
	private void drawBorder(GC gc, Rectangle rect, int nextX, int sizeX) {
		int lineWidth = 3;
		gc.setLineWidth(lineWidth);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		gc.drawRectangle(nextX, 0, (sizeX - lineWidth) + 1, rect.height - lineWidth);
	}

	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		Point size= new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		if (wHint != SWT.DEFAULT) size.x= wHint;
		if (hHint != SWT.DEFAULT) size.y= hHint;
		return size;
	}
	
	public void update(int conflicts, int outgoing, int incoming) {
		totalCount = conflicts + outgoing + incoming;
		conflicCount = conflicts;
		incomingCount = incoming;
		outgoingCount = outgoing;
		redraw();		
	}
}
