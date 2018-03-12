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

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;

/**
 * {@link IDrawingStrategy} implementation to render {@link AbstractInlinedAnnotation}.
 *
 * @since 3.13
 */
class InlinedAnnotationDrawingStrategy implements IDrawingStrategy {

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		if (!(annotation instanceof AbstractInlinedAnnotation)) {
			return;
		}
		if (!((AbstractInlinedAnnotation) annotation).isInVisibleLines()) {
			// The annotation is not in visible lines, don't draw it.
			return;
		}
		InlinedAnnotationDrawingStrategy.draw((AbstractInlinedAnnotation) annotation, gc, textWidget, offset, length,
				color);
	}

	/**
	 * Draw the inlined annotation.
	 *
	 * @param annotation the annotation to be drawn
	 * @param gc the graphics context, <code>null</code> when in clearing mode
	 * @param textWidget the text widget to draw on
	 * @param offset the offset of the line
	 * @param length the length of the line
	 * @param color the color of the line
	 */
	public static void draw(AbstractInlinedAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		if (annotation instanceof LineHeaderAnnotation) {
			draw((LineHeaderAnnotation) annotation, gc, textWidget, offset, length, color);
		} else {
			draw((LineContentAnnotation) annotation, gc, textWidget, offset, length, color);
		}
	}

	/**
	 * Draw the line header annotation in the line spacing of the previous line.
	 *
	 * @param annotation the annotation to be drawn
	 * @param gc the graphics context, <code>null</code> when in clearing mode
	 * @param textWidget the text widget to draw on
	 * @param offset the offset of the line
	 * @param length the length of the line
	 * @param color the color of the line
	 */
	private static void draw(LineHeaderAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		if (annotation.isMarkedDeleted()) {
			// When annotation is deleted, redraw the styled text to hide old draw of
			// annotations
			textWidget.redraw();
			// update caret offset since line spacing has changed.
			textWidget.setCaretOffset(textWidget.getCaretOffset());
			return;
		}
		// compute current, previous line index.
		int lineIndex= -1;
		try {
			lineIndex= textWidget.getLineAtOffset(offset);
		} catch (Exception e) {
			return;
		}
		int previousLineIndex= lineIndex - 1;
		if (gc != null) {
			// Compute the location of the annotation
			int x= textWidget.getLocationAtOffset(offset).x;
			int y= 0;
			int height= annotation.getHeight();
			if (lineIndex > 0) {
				int previousOffset= textWidget.getOffsetAtLine(previousLineIndex);
				y= textWidget.getLocationAtOffset(previousOffset).y + height;
			}
			Rectangle clipping= gc.getClipping();
			if (clipping.contains(x, y)) {
				// GC clipping contains the x, y where annotation must be drawn.

				// Colorize line spacing area with the background of StyledText to avoid having highlighted line color
				gc.setBackground(textWidget.getBackground());
				Rectangle client= textWidget.getClientArea();
				textWidget.drawBackground(gc, x, y, client.width, height);

				// draw the annotation
				annotation.draw(gc, textWidget, offset, length, color, x, y);
				return;
			} else {
				if (!(clipping.y - height == y)) {
					// Clipping doesn't include the y of previous line spacing, stop the redraw
					// range.
					return;
				}
			}
		}

		if (previousLineIndex < 0) {
			// There are none previous line, do nothing
			return;
		}
		// refresh the previous line range where line header annotation must be drawn.
		int previousOffset= textWidget.getOffsetAtLine(previousLineIndex);
		int lineLength= offset - previousOffset;
		textWidget.redrawRange(previousOffset, lineLength, true);
	}

	/**
	 * Draw the line content annotation inside line in the empty area computed by
	 * {@link GlyphMetrics}.
	 *
	 * @param annotation the annotation to be drawn
	 * @param gc the graphics context, <code>null</code> when in clearing mode
	 * @param textWidget the text widget to draw on
	 * @param offset the offset of the line
	 * @param length the length of the line
	 * @param color the color of the line
	 */
	private static void draw(LineContentAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		StyleRange style= null;
		try {
			style= textWidget.getStyleRangeAtOffset(offset);
		} catch (Exception e) {
			return;
		}
		if (annotation.isMarkedDeleted()) {
			// When annotation is deleted, update metrics to null to remove extra spaces of the line content annotation.
			if (style != null) {
				style.metrics= null;
				textWidget.setStyleRange(style);
			}
			return;
		}
		if (gc != null) {
			// Compute the location of the annotation
			Rectangle bounds= textWidget.getTextBounds(offset, offset);
			int x= bounds.x;
			int y= bounds.y;

			// Get size of the character where GlyphMetrics width is added
			String s= textWidget.getText(offset, offset);
			Point charBounds= gc.stringExtent(s);
			int charWidth= charBounds.x;

			// Draw the line content annotation
			annotation.draw(gc, textWidget, offset, length, color, x, y);
			int width= annotation.getWidth();
			if (width != 0) {
				// FIXME: remove this code when we need not redraw the character (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=531769)
				// START TO REMOVE
				annotation.setRedrawnCharacterWidth(charWidth);
				// END TO REMOVE

				// Annotation takes place, add GlyphMetrics width to the style
				StyleRange newStyle= updateStyle(annotation, style);
				if (newStyle != null) {
					textWidget.setStyleRange(newStyle);
					return;
				}

				// FIXME: remove this code when we need not redraw the character (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=531769)
				// START TO REMOVE
				// The inline annotation replaces one character by taking a place width
				// GlyphMetrics
				// Here we need to redraw this first character because GlyphMetrics clip this
				// character.
				int charX= x + bounds.width - charWidth;
				int charY= y;
				if (style != null) {
					if (style.background != null) {
						gc.setBackground(style.background);
						gc.fillRectangle(charX, charY, charWidth + 1, bounds.height);
					}
					if (style.foreground != null) {
						gc.setForeground(style.foreground);
					} else {
						gc.setForeground(textWidget.getForeground());
					}
					gc.setFont(annotation.getFont(style.fontStyle));
				}
				gc.drawString(s, charX, charY, true);
				// END TO REMOVE
			}
		} else {
			textWidget.redrawRange(offset, length, true);
		}
	}

	/**
	 * Returns the style to apply with GlyphMetrics width only if needed.
	 *
	 * @param annotation the line content annotation
	 * @param style the current style and null otherwise.
	 * @return the style to apply with GlyphMetrics width only if needed.
	 */
	static StyleRange updateStyle(LineContentAnnotation annotation, StyleRange style) {
		int width= annotation.getWidth();
		if (width == 0) {
			return null;
		}
		int fullWidth= width + annotation.getRedrawnCharacterWidth();
		if (style == null) {
			style= new StyleRange();
			Position position= annotation.getPosition();
			style.start= position.getOffset();
			style.length= 1;
		}
		GlyphMetrics metrics= style.metrics;
		if (!annotation.isMarkedDeleted()) {
			if (metrics == null) {
				metrics= new GlyphMetrics(0, 0, fullWidth);
			} else {
				if (metrics.width == fullWidth) {
					return null;
				}
				metrics.width= fullWidth;
			}
		} else {
			metrics= null;
		}
		style.metrics= metrics;
		return style;
	}
}
