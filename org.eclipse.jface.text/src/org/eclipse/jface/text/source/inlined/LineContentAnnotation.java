/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.source.inlined;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;

import org.eclipse.jface.text.Position;

/**
 * Inlined annotation which is drawn in the line content and which takes some place with a given
 * width.
 *
 * @since 3.13.0
 */
public class LineContentAnnotation extends AbstractInlinedAnnotation {

	/**
	 * Line content annotation constructor.
	 *
	 * @param position the position where the annotation must be drawn.
	 * @param textWidget the {@link StyledText} widget where the annotation must be drawn.
	 */
	public LineContentAnnotation(Position position, StyledText textWidget) {
		super(position, textWidget);
	}

	/**
	 * Returns an instance of GlyphMetrics used to takes 'width' place when the annotation is drawn
	 * inside the line.
	 *
	 * @return an instance of GlyphMetrics used to takes 'width' place when the annotation is drawn
	 *         inside the line.
	 */
	public GlyphMetrics createMetrics() {
		return new GlyphMetrics(0, 0, getWidth());
	}

	/**
	 * Returns the annotation width. By default it computes the well width for the text annotation.
	 *
	 * @return the annotation width.
	 */
	public int getWidth() {
		String text= super.getText();
		if (text == null) {
			return 0;
		}
		int nbChars= text.length() + 1;
		StyledText styledText= super.getTextWidget();
		GC gc= new GC(styledText);
		FontMetrics fontMetrics= gc.getFontMetrics();
		int width= nbChars * fontMetrics.getAverageCharWidth();
		gc.dispose();
		return width;
	}
}
