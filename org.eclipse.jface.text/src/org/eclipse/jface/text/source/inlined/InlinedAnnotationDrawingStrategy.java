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

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * {@link IDrawingStrategy} implementation to render {@link AbstractInlinedAnnotation}.
 *
 * @since 3.13
 */
class InlinedAnnotationDrawingStrategy implements IDrawingStrategy {

	private final ITextViewer viewer;

	public InlinedAnnotationDrawingStrategy(ITextViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length, Color color) {
		if (annotation instanceof AbstractInlinedAnnotation inlinedAnnotation) {
			if (textWidget != viewer.getTextWidget()) {
				throw new IllegalArgumentException("Text widget and Text viewer are not related!"); //$NON-NLS-1$
			}
			InlinedAnnotationSupport support = InlinedAnnotationSupport.getSupport(textWidget);
			inlinedAnnotation.setSupport(support);
			if (support.isInVisibleLines(widgetOffset) && inlinedAnnotation.isFirstVisibleOffset(widgetOffset, viewer)) {
				draw((AbstractInlinedAnnotation) annotation, gc, textWidget, widgetOffset, length,
						color);
			}
		}
	}

	/**
	 * Draw the inlined annotation.
	 *
	 * @param annotation the annotation to be drawn
	 * @param gc the graphics context, <code>null</code> when in clearing mode
	 * @param textWidget the text widget to draw on
	 * @param widgetOffset the offset of the line
	 * @param length the length of the line
	 * @param color the color of the line
	 */
	public static void draw(AbstractInlinedAnnotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length,
			Color color) {
		if (annotation instanceof LineHeaderAnnotation) {
			draw((LineHeaderAnnotation) annotation, gc, textWidget, widgetOffset, length, color);
		} else {
			draw((LineContentAnnotation) annotation, gc, textWidget, widgetOffset, length, color);
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
			// Setting vertical indent first, before computing bounds
			int height= annotation.getHeight();
			if (height != 0) {
				if (height != textWidget.getLineVerticalIndent(line)) {
					if (annotation.oldLine != -1 && annotation.oldLine < textWidget.getLineCount()) {
						textWidget.setLineVerticalIndent(annotation.oldLine, 0);
					}
					textWidget.setLineVerticalIndent(line, height);
				}
				annotation.oldLine= line;
			} else if (textWidget.getLineVerticalIndent(line) > 0) {
				textWidget.setLineVerticalIndent(line, 0);
			}
			// Compute the location of the annotation
			Rectangle bounds= textWidget.getTextBounds(offset, offset);
			int x= bounds.x;
			int y= bounds.y;
			// Draw the line header annotation
			gc.setBackground(textWidget.getBackground());
			annotation.setLocation(x, y);
			annotation.draw(gc, textWidget, offset, length, color, x, y);
		} else if (textWidget.getLineVerticalIndent(line) > 0) {
			// Here vertical indent is done, the redraw of the full line width is done to avoid annotation clipping
			Rectangle bounds= textWidget.getTextBounds(offset, offset);
			Rectangle client= textWidget.getClientArea();
			textWidget.redraw(0, bounds.y, client.width, bounds.height, false);
		} else {
			textWidget.redrawRange(offset, length, true);
		}
	}

	/**
	 * Draw the line content annotation inside line in the empty area computed by
	 * {@link GlyphMetrics}.
	 *
	 * @param annotation the annotation to be drawn
	 * @param gc the graphics context, <code>null</code> when in clearing mode
	 * @param textWidget the text widget to draw on
	 * @param widgetOffset the offset of the line in the widget (not model)
	 * @param length the length of the line
	 * @param color the color of the line
	 */
	private static void draw(LineContentAnnotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length,
			Color color) {
		if (annotation.isEndOfLine(widgetOffset, textWidget)) {
			drawAfterLine(annotation, gc, textWidget, widgetOffset, length, color);
		} else if (LineContentAnnotation.drawRightToPreviousChar(widgetOffset, textWidget)) {
			drawAsRightOfPreviousCharacter(annotation, gc, textWidget, widgetOffset, length, color);
		} else {
			drawAsLeftOf1stCharacter(annotation, gc, textWidget, widgetOffset, length, color);
		}
	}

	private static void drawAfterLine(LineContentAnnotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length, Color color) {
		if (gc == null) {
			return;
		}
		if (textWidget.getCharCount() == 0) {
			annotation.draw(gc, textWidget, widgetOffset, length, color, 0, 0);
		} else {
			int line= textWidget.getLineAtOffset(widgetOffset);
			int lineEndOffset= (line == textWidget.getLineCount() - 1) ? //
					textWidget.getCharCount() - 1 : //
					textWidget.getOffsetAtLine(line + 1) - 1;
			Rectangle bounds= textWidget.getTextBounds(lineEndOffset, lineEndOffset);
			int lineEndX= bounds.x + bounds.width + gc.stringExtent("   ").x; //$NON-NLS-1$
			annotation.setLocation(lineEndX, textWidget.getLinePixel(line) + textWidget.getLineVerticalIndent(line));
			annotation.draw(gc, textWidget, widgetOffset, length, color, lineEndX, textWidget.getLinePixel(line) + textWidget.getLineVerticalIndent(line));
		}
	}

	protected static void drawAsLeftOf1stCharacter(LineContentAnnotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length, Color color) {
		StyleRange style= null;
		try {
			style= textWidget.getStyleRangeAtOffset(widgetOffset);
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
			String hostCharacter= textWidget.getText(widgetOffset, widgetOffset);
			boolean isEndOfLine= ("\r".equals(hostCharacter) || "\n".equals(hostCharacter)); //$NON-NLS-1$ //$NON-NLS-2$

			// Compute the location of the annotation
			Rectangle bounds= textWidget.getTextBounds(widgetOffset, widgetOffset);
			int x= bounds.x + (isEndOfLine ? bounds.width * 2 : 0);
			int y= bounds.y;

			// When line text has line header annotation, there is a space on the top, adjust the y by using char height
			y+= bounds.height - textWidget.getLineHeight();

			// Draw the line content annotation
			annotation.setLocation(x, y);
			annotation.draw(gc, textWidget, widgetOffset, length, color, x, y);
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
					Point charBounds= gc.stringExtent(hostCharacter);
					int charWidth= charBounds.x;

					// FIXME: remove this code when we need not redraw the character (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=531769)
					// START TO REMOVE
					annotation.setRedrawnCharacterWidth(charWidth);
					// END TO REMOVE

					// Annotation takes place, add GlyphMetrics width to the style
					StyleRange newStyle= annotation.updateStyle(style, gc.getFontMetrics(), textWidget.getData() instanceof ITextViewer viewer ? viewer : annotation.getViewer());
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
					int redrawnHostCharX= x + bounds.width - charWidth;
					int redrawnHostCharY= y;
					gc.setForeground(textWidget.getForeground());
					gc.setBackground(textWidget.getBackground());
					gc.setFont(textWidget.getFont());
					if (style != null) {
						if (style.background != null) {
							gc.setBackground(style.background);
							gc.fillRectangle(redrawnHostCharX, redrawnHostCharY, charWidth + 1, bounds.height);
						}
						if (style.foreground != null) {
							gc.setForeground(style.foreground);
						}
						if (style.font != null) {
							gc.setFont(style.font);
						}
					}
					if (textWidget.getSelection().x <= widgetOffset && textWidget.getSelection().y > widgetOffset) {
						gc.setForeground(textWidget.getSelectionForeground());
						gc.setBackground(textWidget.getSelectionBackground());
					}
					gc.drawString(hostCharacter, redrawnHostCharX, redrawnHostCharY, true);
				}
				// END TO REMOVE
			} else if (style != null && style.metrics != null && style.metrics.width != 0) {
				// line content annotation had an , reset it
				style.metrics= null;
				textWidget.setStyleRange(style);
			}
		} else {
			textWidget.redrawRange(widgetOffset, length, true);
		}
	}

	protected static void drawAsRightOfPreviousCharacter(LineContentAnnotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length, Color color) {
		StyleRange style= null;
		try {
			style= textWidget.getStyleRangeAtOffset(widgetOffset - 1);
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
			char hostCharacter= textWidget.getText(widgetOffset - 1, widgetOffset - 1).charAt(0);
			// use gc.stringExtent instead of gc.geCharWidth because of bug 548866
			int redrawnCharacterWidth= hostCharacter != '\t' ? gc.stringExtent(Character.toString(hostCharacter)).x : textWidget.getTabs() * gc.stringExtent(" ").x; //$NON-NLS-1$
			Rectangle charBounds= textWidget.getTextBounds(widgetOffset - 1, widgetOffset - 1);
			Rectangle annotationBounds= new Rectangle(charBounds.x + redrawnCharacterWidth, charBounds.y, annotation.getWidth(), charBounds.height);

			// When line text has line header annotation, there is a space on the top, adjust the y by using char height
			int verticalDrawingOffset= charBounds.height - textWidget.getLineHeight();
			annotationBounds.y+= verticalDrawingOffset;

			// Draw the line content annotation
			annotation.setLocation(annotationBounds.x, annotationBounds.y);
			annotation.draw(gc, textWidget, widgetOffset, length, color, annotationBounds.x, annotationBounds.y);
			int width= annotation.getWidth();
			if (width != 0) {
				// FIXME: remove this code when we need not redraw the character (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=531769)
				// START TO REMOVE
				annotation.setRedrawnCharacterWidth(redrawnCharacterWidth);
				// END TO REMOVE

				// Annotation takes place, add GlyphMetrics width to the style
				StyleRange newStyle= annotation.updateStyle(style, gc.getFontMetrics(), InlinedAnnotationSupport.getSupport(textWidget).getViewer());
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
				gc.setForeground(textWidget.getForeground());
				gc.setBackground(textWidget.getBackground());
				gc.setFont(textWidget.getFont());
				if (style != null) {
					if (style.background != null) {
						gc.setBackground(style.background);
						gc.fillRectangle(charBounds.x, annotationBounds.y, redrawnCharacterWidth, charBounds.height);
					}
					if (style.foreground != null) {
						gc.setForeground(style.foreground);
					}
					if (style.font != null) {
						gc.setFont(style.font);
					}
				}
				int toRedrawCharOffset= widgetOffset - 1;
				if (textWidget.getSelection().x <= toRedrawCharOffset && textWidget.getSelection().y > toRedrawCharOffset) {
					gc.setForeground(textWidget.getSelectionForeground());
					gc.setBackground(textWidget.getSelectionBackground());
				}
				gc.drawString(Character.toString(hostCharacter), charBounds.x, charBounds.y + verticalDrawingOffset, true);
				// END TO REMOVE
			} else if (style != null && style.metrics != null && style.metrics.width != 0) {
				// line content annotation had an , reset it
				style.metrics= null;
				textWidget.setStyleRange(style);
			}
		} else {
			textWidget.redrawRange(widgetOffset, length, true);
		}
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
