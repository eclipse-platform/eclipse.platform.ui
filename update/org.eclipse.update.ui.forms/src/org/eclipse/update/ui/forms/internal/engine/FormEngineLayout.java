/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;

/**
 * @version 	1.0
 * @author
 */
public class FormEngineLayout extends Layout {

	/*
	 * @see Layout#computeSize(Composite, int, int, boolean)
	 */

	public Point computeSize(
		Composite composite,
		int wHint,
		int hHint,
		boolean changed) {
		FormEngine engine = (FormEngine) composite;
		int innerWidth = wHint;
		if (innerWidth != SWT.DEFAULT)
			innerWidth -= engine.marginWidth * 2;
		Point textSize = computeTextSize(engine, innerWidth);
		int textWidth = textSize.x + 2 * engine.marginWidth;
		int textHeight = textSize.y + 2 * engine.marginHeight;
		Point result = new Point(textWidth, textHeight);
		return result;
	}

	private Point computeTextSize(FormEngine engine, int wHint) {
		IParagraph[] paragraphs = engine.model.getParagraphs();

		GC gc = new GC(engine);
		gc.setFont(engine.getFont());

		Locator loc = new Locator();

		int width = wHint != SWT.DEFAULT ? wHint : 0;

		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();

		for (int i = 0; i < paragraphs.length; i++) {
			IParagraph p = paragraphs[i];

			if (i > 0 && engine.paragraphsSeparated && p.getAddVerticalSpace())
				loc.y += lineHeight;

			loc.rowHeight = 0;
			loc.x = 0;

			IParagraphSegment[] segments = p.getSegments();
			if (segments.length > 0) {
				for (int j = 0; j < segments.length; j++) {
					IParagraphSegment segment = segments[j];
					segment.advanceLocator(gc, wHint, loc, engine.objectTable);
					width = Math.max(width, loc.width);
				}
				loc.y += loc.rowHeight;
			} else {
				// empty new line
				loc.y += lineHeight;
			}
		}
		gc.dispose();
		return new Point(width, loc.y);
	}

	/*
	 * @see Layout#layout(Composite, boolean)
	 */
	protected void layout(Composite composite, boolean flushCache) {
	}
}