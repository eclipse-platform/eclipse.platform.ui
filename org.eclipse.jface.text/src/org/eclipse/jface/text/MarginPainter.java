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

package org.eclipse.jface.text;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.source.ISourceViewer;


/**
 * Paints a vertical line at a given column.
 */
public class MarginPainter implements IPainter, PaintListener {
	

	private StyledText fTextWidget;
	
	private int fMarginWidth= 80;
	private Color fColor;
	private int fLineStyle= SWT.LINE_SOLID;
	private int fLineWidth= 1;
	
	private int fCachedWidgetX= -1;
	private boolean fIsActive= false;
	
	
	public MarginPainter(ISourceViewer sourceViewer) {
		fTextWidget= sourceViewer.getTextWidget();
	}
	
	public void setMarginRulerColumn(int width) {
		fMarginWidth= width;
		initialize();
	}
	
	public void setMarginRulerStyle(int lineStyle) {
		fLineStyle= lineStyle;
	}
	
	public void setMarginRulerWidth(int lineWidth) {
		fLineWidth= lineWidth;
	}
	
	/**
	 * Must be called before <code>paint</code> is called the first time.
	 */
	public void setMarginRulerColor(Color color) {
		fColor= color;
	}
	
	/**
	 * Must be called explicitly when font of text widget changes.
	 */
	public void initialize() {
		computeWidgetX();
		fTextWidget.redraw();
	}
	
	private void computeWidgetX() {
		GC gc= new GC(fTextWidget);
		int pixels= gc.getFontMetrics().getAverageCharWidth();
		gc.dispose();
		
		fCachedWidgetX= pixels * fMarginWidth;
	}
	
	/*
	 * @see IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			fCachedWidgetX= -1;
			fTextWidget.removePaintListener(this);
			if (redraw)
				fTextWidget.redraw();
		}	
	}

	/*
	 * @see IPainter#dispose()
	 */
	public void dispose() {
		fTextWidget= null;
	}

	/*
	 * @see IPainter#paint(int)
	 */
	public void paint(int reason) {
		if (!fIsActive) {
			fIsActive= true;
			fTextWidget.addPaintListener(this);
			if (fCachedWidgetX == -1)
				computeWidgetX();
			fTextWidget.redraw();
		} else if (CONFIGURATION == reason || INTERNAL == reason)
			fTextWidget.redraw();
	}
	
	/*
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent e) {
		if (fTextWidget != null) {
			int x= fCachedWidgetX - fTextWidget.getHorizontalPixel();
			if (x >= 0) {
				Rectangle area= fTextWidget.getClientArea();
				e.gc.setForeground(fColor);
				e.gc.setLineStyle(fLineStyle);
				e.gc.setLineWidth(fLineWidth);
				e.gc.drawLine(x, 0, x, area.height);
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.text.IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
	}
}
