/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;


/**
 * Paints a vertical line (margin line) after a given column respecting the text
 * viewer's font.
 * <p>
 * Clients usually instantiate and configure objects of this class.</p>
 * <p>
 * This class is not intended to be subclassed.</p>
 *
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MarginPainter implements IPainter, PaintListener {

	/** The widget of the text viewer */
	private StyledText fTextWidget;

	/** The column after which to paint the line, default value <code>80</code> */
	private int fMarginWidth= 80;
	/** The color in which to paint the line */
	private Color fColor;
	/** The line style of the line to be painted, default value <code>SWT.LINE_SOLID</code> */
	private int fLineStyle= SWT.LINE_SOLID;
	/** The line width of the line to be painted, default value <code>1</code> */
	private int fLineWidth= 0; // NOTE: 0 means width is 1 but with optimized performance
	/** The cached x-offset of the <code>fMarginWidth</code> for the current font */
	private int fCachedWidgetX= -1;
	private Font fFontForCachedWidgetX;
	/** The active state of this painter */
	private boolean fIsActive= false;

	/**
	 * Creates a new painter for the given text viewer.
	 *
	 * @param textViewer the text viewer
	 */
	public MarginPainter(ITextViewer textViewer) {
		fTextWidget= textViewer.getTextWidget();
	}

	/**
	 * Sets the column after which to draw the margin line.
	 *
	 * @param width the column
	 */
	public void setMarginRulerColumn(int width) {
		fMarginWidth= width;
		initialize();
	}

	/**
	 * Sets the line style of the margin line.
	 *
	 * @param lineStyle a <code>SWT</code> style constant describing the line style
	 */
	public void setMarginRulerStyle(int lineStyle) {
		fLineStyle= lineStyle;
	}

	/**
	 * Sets the line width of the margin line.
	 *
	 * @param lineWidth the line width
	 */
	public void setMarginRulerWidth(int lineWidth) {
		if (lineWidth == 1) {
			lineWidth= 0; // NOTE: 0 means width is 1 but with optimized performance
		}
		fLineWidth= lineWidth;
	}

	/**
	 * Sets the color of the margin line. Must be called before <code>paint</code> is called the first time.
	 *
	 * @param color the color
	 */
	public void setMarginRulerColor(Color color) {
		fColor= color;
	}

	/**
	 * Initializes this painter, by flushing and recomputing all caches and causing the widget to be
	 * redrawn.
	 */
	public void initialize() {
		computeWidgetX();
		fTextWidget.redraw();
	}

	/**
	 * Computes and remembers the x-offset of the margin column for the
	 * current widget font.
	 */
	private void computeWidgetX() {
		GC gc= new GC(fTextWidget);
		fFontForCachedWidgetX= fTextWidget.getFont();
		double pixels= gc.getFontMetrics().getAverageCharacterWidth();
		gc.dispose();

		fCachedWidgetX= (int) (pixels * fMarginWidth + fTextWidget.getLeftMargin() + 0.5);
	}

	@Override
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			fCachedWidgetX= -1;
			fTextWidget.removePaintListener(this);
			if (redraw) {
				fTextWidget.redraw();
			}
		}
	}

	@Override
	public void dispose() {
		fTextWidget= null;
	}

	@Override
	public void paint(int reason) {
		if (!fIsActive) {
			fIsActive= true;
			fTextWidget.addPaintListener(this);
			if (fCachedWidgetX == -1) {
				computeWidgetX();
			}
			fTextWidget.redraw();
		} else if (CONFIGURATION == reason || INTERNAL == reason) {
			fTextWidget.redraw();
		}
	}

	@Override
	public void paintControl(PaintEvent e) {
		if (fTextWidget != null) {
			if (fFontForCachedWidgetX != fTextWidget.getFont()) {
				computeWidgetX();
			}
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

	@Override
	public void setPositionManager(IPaintPositionManager manager) {
	}
}
