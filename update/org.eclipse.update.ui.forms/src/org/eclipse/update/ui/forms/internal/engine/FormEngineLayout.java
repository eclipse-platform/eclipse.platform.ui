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
package org.eclipse.update.ui.forms.internal.engine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.ILayoutExtension;

/**
 * @version 	1.0
 * @author
 */
public class FormEngineLayout extends Layout implements ILayoutExtension {

	public int getMaximumWidth(Composite parent, boolean changed) {
		return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
	}

	public int getMinimumWidth(Composite parent, boolean changed) {
		return 30;
	}

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
		if (engine.isLoading()) {
			return computeLoading(engine);
		}
		if (innerWidth != SWT.DEFAULT)
			innerWidth -= engine.marginWidth * 2;
		Point textSize = computeTextSize(engine, innerWidth);
		int textWidth = textSize.x + 2 * engine.marginWidth;
		int textHeight = textSize.y + 2 * engine.marginHeight;
		Point result = new Point(textWidth, textHeight);
		return result;
	}
	
	private Point computeLoading(FormEngine engine) {
		GC gc = new GC(engine);
		gc.setFont(engine.getFont());
		String loadingText = engine.getLoadingText();
		Point size = gc.textExtent(loadingText);
		gc.dispose();
		size.x += 2 * engine.marginWidth;
		size.y += 2 * engine.marginHeight;
		return size;
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
				loc.y += engine.getParagraphSpacing(lineHeight);

			loc.rowHeight = 0;
			loc.indent = p.getIndent();
			loc.x = p.getIndent();

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
