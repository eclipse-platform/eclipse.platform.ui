/**
 *  Copyright (c) 2017 Angelo ZERR.
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
		int line= textWidget.getLineAtOffset(offset);
		if (isDeleted(annotation)) {
			// When annotation is deleted, update metrics to null to remove extra spaces of the line header annotation.
			if (textWidget.getLineVerticalIndent(line) > 0)
				textWidget.setLineVerticalIndent(line, 0);
			return;
		}
		if (gc != null) {
			// Compute the location of the annotation
			Rectangle bounds= textWidget.getTextBounds(offset, offset);
			int x= bounds.x;
			int y= bounds.y;

			gc.setBackground(textWidget.getBackground());

			// Draw the line header annotation
			annotation.setLocation(x, y);
			annotation.draw(gc, textWidget, offset, length, color, x, y);
			int height= annotation.getHeight();
			if (height != 0) {
				if (height != textWidget.getLineVerticalIndent(line)) {
					if (annotation.oldLine != -1 && annotation.oldLine < textWidget.getLineCount()) {
						textWidget.setLineVerticalIndent(annotation.oldLine, 0);
					}
					textWidget.setLineVerticalIndent(line, height);
				}
				annotation.oldLine= line;
				return;
			} else if (textWidget.getLineVerticalIndent(line) > 0) {
				textWidget.setLineVerticalIndent(line, 0);
			}
		} else {
			if (textWidget.getLineVerticalIndent(line) > 0) {
				// Here vertical indent is done, the redraw of the full line width is done to avoid annotation clipping
				Rectangle bounds= textWidget.getTextBounds(offset, offset);
				Rectangle client= textWidget.getClientArea();
				textWidget.redraw(0, bounds.y, client.width, bounds.height, false);
			} else {
				textWidget.redrawRange(offset, length, true);
			}
		}
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
		if (isDeleted(annotation)) {
			// When annotation is deleted, update metrics to null to remove extra spaces of the line content annotation.
			if (style != null && style.metrics != null) {
				style.metrics= null;
				textWidget.setStyleRange(style);
			}
			return;
		}
		if (gc != null) {
			String s= textWidget.getText(offset, offset);
			boolean isEndOfLine= ("\r".equals(s) || "\n".equals(s)); //$NON-NLS-1$ //$NON-NLS-2$

			// Compute the location of the annotation
			Rectangle bounds= textWidget.getTextBounds(offset, offset);
			int x= bounds.x + (isEndOfLine ? bounds.width * 2 : 0);
			int y= bounds.y;

			// When line text has line header annotation, there is a space on the top, adjust the y by using char height
			y+= bounds.height - textWidget.getLineHeight();

			// Draw the line content annotation
			annotation.setLocation(x, y);
			annotation.draw(gc, textWidget, offset, length, color, x, y);
			int width= annotation.getWidth();
			if (width != 0) {
				if (isEndOfLine) {
					if (!gc.getClipping().contains(x, y)) {
						// The draw of mining is not inside the gc clipping, redraw the area which contains the mining to draw.
						Rectangle client= textWidget.getClientArea();
						textWidget.redraw(x, y, client.width, bounds.height, false);
					}
				} else {
					// Get size of the character where GlyphMetrics width is added
					Point charBounds= gc.stringExtent(s);
					int charWidth= charBounds.x;

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
				}
				// END TO REMOVE
			} else if (style != null && style.metrics != null && style.metrics.width != 0) {
				// line content annotation had an , reset it
				style.metrics= null;
				textWidget.setStyleRange(style);
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
		if (width == 0 || annotation.getRedrawnCharacterWidth() == 0) {
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
				/**
				 * We must create a new GlyphMetrics instance because comparison with similarTo used
				 * later in StyledText#setStyleRange will compare the same (modified) and won't
				 * realize an update happened.
				 */
				metrics= new GlyphMetrics(0, 0, fullWidth);
			}
		} else {
			metrics= null;
		}
		style.metrics= metrics;
		return style;
	}

	/**
	 * Returns <code>true</code> if inlined annotation is deleted and <code>false</code> otherwise.
	 *
	 * @param annotation the inlined annotation to check
	 * @return <code>true</code> if inlined annotation is deleted and <code>false</code> otherwise.
	 */
	private static boolean isDeleted(AbstractInlinedAnnotation annotation) {
		return annotation.isMarkedDeleted() || annotation.getPosition().isDeleted() || annotation.getPosition().getLength() == 0;
	}
}
