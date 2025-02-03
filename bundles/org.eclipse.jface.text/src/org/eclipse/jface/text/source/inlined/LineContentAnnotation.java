/**
 *  Copyright (c) 2017, 2018 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.source.inlined;

import java.util.function.Consumer;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Inlined annotation which is drawn in the line content and which takes some place with a given
 * width.
 *
 * @since 3.13
 */
public class LineContentAnnotation extends AbstractInlinedAnnotation {

	/**
	 * The annotation width
	 */
	private int width;

	private int redrawnCharacterWidth;

	/**
	 * Line content annotation constructor.
	 *
	 * @param position the position where the annotation must be drawn.
	 * @param viewer   the {@link ISourceViewer} where the annotation must be drawn.
	 */
	public LineContentAnnotation(Position position, ISourceViewer viewer) {
		this(position, viewer, null, null, null);
	}

	/**
	 * Line content annotation constructor.
	 *
	 * @param position the position where the annotation must be drawn.
	 * @param viewer the {@link ISourceViewer} where the annotation must be drawn.
	 * @param onMouseHover the consumer to be called on mouse hover. If set, the implementor needs
	 *            to take care of setting the cursor if wanted.
	 * @param onMouseOut the consumer to be called on mouse out. If set, the implementor needs to
	 *            take care of resetting the cursor.
	 * @param onMouseMove the consumer to be called on mouse move
	 * @since 3.28
	 */
	public LineContentAnnotation(Position position, ISourceViewer viewer, Consumer<MouseEvent> onMouseHover, Consumer<MouseEvent> onMouseOut, Consumer<MouseEvent> onMouseMove) {
		super(position, viewer, onMouseHover, onMouseOut, onMouseMove);
	}

	/**
	 * Returns the annotation width. By default it computes the well width for the text annotation.
	 *
	 * @return the annotation width.
	 */
	public final int getWidth() {
		return width;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After drawn, compute the text width and update it.
	 * </p>
	 */
	@Override
	public final void draw(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		width= drawAndComputeWidth(gc, textWidget, offset, length, color, x, y);
	}

	/**
	 * Draw the inlined annotation. By default it draws the text of the annotation with gray color.
	 * User can override this method to draw anything.
	 *
	 * @param gc         the graphics context
	 * @param textWidget the text widget to draw on
	 * @param offset     the offset of the line
	 * @param length     the length of the line
	 * @param color      the color of the line
	 * @param x          the x position of the annotation
	 * @param y          the y position of the annotation
	 * @return the text width.
	 */
	protected int drawAndComputeWidth(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		// Draw the text annotation and returns the width
		super.draw(gc, textWidget, offset, length, color, x, y);
		return (int) (gc.stringExtent(getText()).x + 2 * gc.getFontMetrics().getAverageCharacterWidth());
	}

	int getRedrawnCharacterWidth() {
		return redrawnCharacterWidth;
	}

	void setRedrawnCharacterWidth(int redrawnCharacterWidth) {
		this.redrawnCharacterWidth= redrawnCharacterWidth;
	}

	@Override
	boolean contains(int x, int y) {
		return (x >= this.fX && x <= this.fX + width && y >= this.fY && y <= this.fY + getTextWidget().getLineHeight());
	}

	/**
	 * Returns the style to apply with GlyphMetrics width only if needed.
	 *
	 * As it's using Widget position, the results can be passed directly to
	 * {@link StyledText#setStyleRange(StyleRange)} and family. However, in case of a Viewer
	 * providing project/folder with {@link ITextViewerExtension5}, the range must be transformed to
	 * model position before passing it to a {@link TextPresentation}.
	 *
	 * @param style the current style and null otherwise.
	 * @param fontMetrics font metrics
	 * @param viewer the viewer where the annotation is to be rendered
	 * @return the style to apply with GlyphMetrics width only if needed. It uses widget position,
	 *         not model position.
	 */
	StyleRange updateStyle(StyleRange style, FontMetrics fontMetrics, ITextViewer viewer, boolean afterPosition) {
		Position widgetPosition= computeWidgetPosition(viewer);
		if (widgetPosition == null) {
			return null;
		}
		StyledText textWidget = viewer.getTextWidget();
		boolean usePreviousChar= false;
		if (!afterPosition) {
			usePreviousChar= drawRightToPreviousChar(widgetPosition.getOffset(), textWidget);
		}
		if (width == 0 || getRedrawnCharacterWidth() == 0) {
			return null;
		}
		int fullWidth= width + getRedrawnCharacterWidth();
		if (style == null) {
			style= new StyleRange();
			style.start= widgetPosition.getOffset();
			if (usePreviousChar) {
				style.start--;
			}
			style.length= 1;
		}
		GlyphMetrics metrics= style.metrics;
		if (!isMarkedDeleted()) {
			if (metrics == null) {
				metrics= new GlyphMetrics(fontMetrics.getAscent(), fontMetrics.getDescent(), fullWidth);
			} else {
				if (metrics.width == fullWidth) {
					return null;
				}
				/**
				 * We must create a new GlyphMetrics instance because comparison with similarTo used
				 * later in StyledText#setStyleRange will compare the same (modified) and won't
				 * realize an update happened.
				 */
				metrics= new GlyphMetrics(fontMetrics.getAscent(), fontMetrics.getDescent(), fullWidth);
			}
		} else {
			metrics= null;
		}
		style.metrics= metrics;
		return style;
	}

	static boolean drawRightToPreviousChar(int widgetOffset, StyledText textWidget) {
		return widgetOffset > 0 && widgetOffset < textWidget.getCharCount() &&
				textWidget.getLineAtOffset(widgetOffset) == textWidget.getLineAtOffset(widgetOffset - 1);
	}

	boolean isEmptyLine(int widgetOffset, StyledText text) {
		if (text.getCharCount() <= widgetOffset) { // Assuming widgetOffset >= 0
			return true;
		}
		int line= text.getLineAtOffset(widgetOffset);
		String lineStr= text.getLine(line);
		return lineStr.length() == 0;
	}
}
