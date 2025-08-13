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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.internal.text.codemining.CodeMiningDocumentFooterAnnotation;
import org.eclipse.jface.internal.text.codemining.CodeMiningLineContentAnnotation;
import org.eclipse.jface.internal.text.codemining.CodeMiningLineHeaderAnnotation;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;

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

	private static final String INLINE_ANNOTATION_FONT = InlinedAnnotationDrawingStrategy.class.getSimpleName() + ".font"; //$NON-NLS-1$

	private record GCConfig(Color foreground, Color background, Font font) {

		private static final GCConfig NO_CONFIG = new GCConfig(null, null, null);
		public static GCConfig fromGC(GC gc) {
			return gc != null ? new GCConfig(gc.getForeground(), gc.getBackground(), gc.getFont()) : NO_CONFIG;
		}

		public void applyTo(GC gc) {
			if (gc == null) {
				return;
			}
			if (foreground != null) {
				gc.setForeground(foreground);
			}
			if (background != null) {
				gc.setBackground(background);
			}
			if (font != null) {
				gc.setFont(font);
			}
		}
	}

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length, Color color) {
		if (annotation instanceof AbstractInlinedAnnotation inlinedAnnotation) {
			if (textWidget != viewer.getTextWidget()) {
				throw new IllegalArgumentException("Text widget and Text viewer are not related!"); //$NON-NLS-1$
			}
			InlinedAnnotationSupport support = InlinedAnnotationSupport.getSupport(textWidget);
			inlinedAnnotation.setSupport(support);
			if (support.isInVisibleLines(inlinedAnnotation.getPosition().offset) && inlinedAnnotation.isFirstVisibleOffset(widgetOffset, viewer)) {
				GCConfig initialGCConfig = GCConfig.fromGC(gc);
				GCConfig annotationGCConfig = new GCConfig(color, textWidget.getBackground(), getAnnotationFont(textWidget));
				annotationGCConfig.applyTo(gc);

				draw(inlinedAnnotation, gc, textWidget, widgetOffset, length, color);
				initialGCConfig.applyTo(gc);
			}
		}
	}

	private Font getAnnotationFont(StyledText textWidget) {
		Font annotationFont = (Font)textWidget.getData(INLINE_ANNOTATION_FONT);
		if (!match(annotationFont, textWidget)) {
			if (annotationFont != null) {
				annotationFont.dispose();
			}
			annotationFont = null;
		}
		if (annotationFont == null) {
			annotationFont = createInlineAnnotationFont(textWidget);
			textWidget.setData(INLINE_ANNOTATION_FONT, annotationFont);
			textWidget.addDisposeListener(e -> ((Font) textWidget.getData(INLINE_ANNOTATION_FONT)).dispose());
		}
		return annotationFont;
	}

	private Font createInlineAnnotationFont(StyledText widget) {
		Font initialFont = widget.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (FontData data : fontData) {
			data.setStyle(data.getStyle() | SWT.ITALIC);
		}
		return new Font(initialFont.getDevice(), fontData);
	}

	private boolean match(Font annotationFont, StyledText widget) {
		if (annotationFont == null) {
			return false;
		}
		int widgetFontHeight = widget.getFont().getFontData()[0].getHeight();
		int annotationFontHeight = annotationFont.getFontData()[0].getHeight();
		return annotationFontHeight == widgetFontHeight;
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
		if (annotation instanceof LineHeaderAnnotation lha) {
			draw(lha, gc, textWidget, widgetOffset, length, color);
		} else if (annotation instanceof LineFooterAnnotation lfa) {
			draw(lfa, gc, textWidget, widgetOffset, length, color);
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
		if (isInlinedAnnotationDeleted(textWidget, offset, annotation)) {
			return;
		}
		int line= textWidget.getLineAtOffset(offset);
		int charCount= textWidget.getCharCount();
		if (gc != null) {
			// Setting vertical indent first, before computing bounds
			int height;
			if (annotation instanceof CodeMiningLineHeaderAnnotation cmmla) {
				height= cmmla.getHeight(gc);
			} else {
				height= annotation.getHeight();
			}
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
			int x, y;
			if (offset < charCount) {
				Rectangle bounds= textWidget.getTextBounds(offset, offset);
				x= bounds.x;
				y= bounds.y;
			} else {
				Point locAtOff= textWidget.getLocationAtOffset(offset);
				x= locAtOff.x;
				y= locAtOff.y - height;
			}
			// Draw the line header annotation
			gc.setBackground(textWidget.getBackground());
			annotation.setLocation(x, y);
			annotation.draw(gc, textWidget, offset, length, color, x, y);
		} else if (textWidget.getLineVerticalIndent(line) > 0) {
			redrawLine(textWidget, offset, charCount);
		} else {
			redrawAll(textWidget, offset, charCount, length);
		}
	}

	private static void draw(LineFooterAnnotation annotation, GC gc, StyledText textWidget, int offset, int length,
			Color color) {
		if (isInlinedAnnotationDeleted(textWidget, offset, annotation)) {
			return;
		}
		int line= textWidget.getLineAtOffset(offset);
		int charCount= textWidget.getCharCount();
		if (gc != null) {
			// Setting vertical indent first, before computing bounds
			int height;
			if (annotation instanceof CodeMiningDocumentFooterAnnotation) { // this is different here
				height= 0;
			} else {
				height= annotation.getHeight();
			}
			if (height != 0) {
				if (height != textWidget.getLineVerticalIndent(line)) {
					textWidget.setLineVerticalIndent(line, height);
				}
			} else if (textWidget.getLineVerticalIndent(line) > 0) {
				textWidget.setLineVerticalIndent(line, 0);
			}
			// Compute the location of the annotation
			int x, y;
			if (offset < charCount) {
				Rectangle bounds= textWidget.getTextBounds(offset, offset);
				x= bounds.x;
				y= bounds.y;
			} else {
				int lineAtOffset= textWidget.getLineAtOffset(offset);
				int offsetAtBeginningOfLine= textWidget.getOffsetAtLine(lineAtOffset);
				if (offsetAtBeginningOfLine >= charCount) {
					Point locAtOff= textWidget.getLocationAtOffset(offsetAtBeginningOfLine);
					x= locAtOff.x;
					y= locAtOff.y;
				} else {
					Rectangle bounds= textWidget.getTextBounds(offsetAtBeginningOfLine, offsetAtBeginningOfLine);
					int lineSpacing= textWidget.getLineSpacing();
					x= bounds.x;
					y= bounds.y + bounds.height + lineSpacing;
				}
			}
			// Draw the line footer annotation
			gc.setBackground(textWidget.getBackground());
			annotation.setLocation(x, y);
			annotation.draw(gc, textWidget, offset, length, color, x, y);
		} else if (textWidget.getLineVerticalIndent(line) > 0) {
			redrawLine(textWidget, offset, charCount);
		} else {
			redrawAll(textWidget, offset, charCount, length);
		}
	}

	private static boolean isInlinedAnnotationDeleted(StyledText textWidget, int offset, AbstractInlinedAnnotation annotation) {
		int line= textWidget.getLineAtOffset(offset);
		int charCount= textWidget.getCharCount();
		if (isDeleted(annotation, charCount)) {
			// When annotation is deleted, update metrics to null to remove extra spaces of the line header annotation.
			if (textWidget.getLineVerticalIndent(line) > 0) {
				textWidget.setLineVerticalIndent(line, 0);
			}
			return true;
		}
		return false;
	}

	private static void redrawAll(StyledText textWidget, int offset, int charCount, int length) {
		if (offset >= charCount) {
			if (charCount > 0) {
				textWidget.redrawRange(charCount - 1, 1, true);
			} else {
				Rectangle client= textWidget.getClientArea();
				textWidget.redraw(0, 0, client.width, client.height, false);
			}
		} else {
			textWidget.redrawRange(offset, length, true);
		}
	}

	private static void redrawLine(StyledText textWidget, int offset, int charCount) {
		// Here vertical indent is done, the redraw of the full line width is done to avoid annotation clipping
		Rectangle client= textWidget.getClientArea();
		int y, height;
		if (offset < charCount) {
			Rectangle bounds= textWidget.getTextBounds(offset, offset);
			y= bounds.y;
			height= bounds.height;
		} else {
			y= 0;
			height= client.height;
		}
		textWidget.redraw(0, y, client.width, height, false);
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
		if (annotation instanceof CodeMiningLineContentAnnotation a) {
			if (a.isAfterPosition()) {
				if (widgetOffset < textWidget.getCharCount()) {
					drawAsLeftOf1stCharacter(annotation, gc, textWidget, widgetOffset, length, color);
				} else {
					drawAtEndOfDocumentInFirstColumn(annotation, gc, textWidget, widgetOffset, length, color);
				}
				return;
			}
		}
		if (annotation.isEmptyLine(widgetOffset, textWidget)) {
			drawAfterLine(annotation, gc, textWidget, widgetOffset, length, color);
		} else if (LineContentAnnotation.drawRightToPreviousChar(widgetOffset, textWidget)) {
			drawAsRightOfPreviousCharacter(annotation, gc, textWidget, widgetOffset, length, color);
		} else {
			drawAsLeftOf1stCharacter(annotation, gc, textWidget, widgetOffset, length, color);
		}
	}

	private static void drawAfterLine(LineContentAnnotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length, Color color) {
		if (isDeleted(annotation, textWidget.getCharCount())) {
			return;
		}
		if (gc != null) {
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
		} else {
			textWidget.redrawRange(widgetOffset, length, true);
		}
	}

	private static void drawAtEndOfDocumentInFirstColumn(LineContentAnnotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length, Color color) {
		if (isDeleted(annotation, textWidget.getCharCount())) {
			return;
		}
		if (gc != null) {
			Point locAtOff= textWidget.getLocationAtOffset(widgetOffset);
			int x= locAtOff.x;
			int y= locAtOff.y;
			annotation.setLocation(x, y);
			annotation.draw(gc, textWidget, widgetOffset, length, color, x, y);
			int width= annotation.getWidth();
			if (width != 0) {
				if (!gc.getClipping().contains(x, y)) {
					Rectangle client= textWidget.getClientArea();
					int height= textWidget.getLineHeight();
					textWidget.redraw(x, y, client.width, height, false);
				}
			}
		} else {
			int charCount= textWidget.getCharCount();
			if (charCount > 0) {
				textWidget.redrawRange(charCount - 1, 1, true);
			} else {
				Rectangle client= textWidget.getClientArea();
				textWidget.redraw(0, 0, client.width, client.height, false);
			}
		}
	}

	protected static void drawAsLeftOf1stCharacter(LineContentAnnotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length, Color color) {
		StyleRange style= null;
		try {
			style= textWidget.getStyleRangeAtOffset(widgetOffset);
		} catch (Exception e) {
			return;
		}
		if (isDeleted(annotation, textWidget.getCharCount())) {
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

			int x;
			if (isEndOfLine) {
				// getTextBounds at offset with char '\r' or '\n' returns incorrect x position, use getLocationAtOffset instead
				x= textWidget.getLocationAtOffset(widgetOffset).x;
			} else {
				x= bounds.x;
			}
			int y= bounds.y;
			if (isAfterPosition(annotation)) {
				isEndOfLine= false;
			}
			// When line text has line header annotation, there is a space on the top, adjust the y by using char height
			int verticalDrawingOffset= bounds.height - textWidget.getLineHeight();
			if (verticalDrawingOffset > 0) {
				y+= verticalDrawingOffset;
			}

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
					if (charWidth == 0 && ("\r".equals(hostCharacter) || "\n".equals(hostCharacter))) { //$NON-NLS-1$ //$NON-NLS-2$
						// charWidth is 0 for '\r' on font Consolas, but not on other fonts, why?
						charWidth= gc.stringExtent(" ").x; //$NON-NLS-1$
					}
					// FIXME: remove this code when we need not redraw the character (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=531769)
					// START TO REMOVE
					annotation.setRedrawnCharacterWidth(charWidth);
					// END TO REMOVE

					// Annotation takes place, add GlyphMetrics width to the style
					StyleRange newStyle= annotation.updateStyle(style, gc.getFontMetrics(), textWidget.getData() instanceof ITextViewer viewer ? viewer : annotation.getViewer(),
							isAfterPosition(annotation));
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
					String characterToBeDrawn= hostCharacter;
					if ("\n".equals(characterToBeDrawn)) { //$NON-NLS-1$
						characterToBeDrawn= ""; //$NON-NLS-1$
					}
					gc.drawString(characterToBeDrawn, redrawnHostCharX, redrawnHostCharY, true);
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

	private static boolean isAfterPosition(LineContentAnnotation annotation) {
		if (annotation instanceof CodeMiningLineContentAnnotation a) {
			return a.isAfterPosition();
		}
		return false;
	}

	protected static void drawAsRightOfPreviousCharacter(LineContentAnnotation annotation, GC gc, StyledText textWidget, int widgetOffset, int length, Color color) {
		StyleRange style= null;
		try {
			style= textWidget.getStyleRangeAtOffset(widgetOffset - 1);
		} catch (Exception e) {
			return;
		}
		if (isDeleted(annotation, textWidget.getCharCount())) {
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
			if (verticalDrawingOffset > 0) {
				annotationBounds.y+= verticalDrawingOffset;
			}

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
				StyleRange newStyle= annotation.updateStyle(style, gc.getFontMetrics(), InlinedAnnotationSupport.getSupport(textWidget).getViewer(), isAfterPosition(annotation));
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
				String characterToBeDrawn= Character.toString(hostCharacter);
				if ("\n".equals(characterToBeDrawn)) { //$NON-NLS-1$
					characterToBeDrawn= ""; //$NON-NLS-1$
				}
				gc.drawString(characterToBeDrawn, charBounds.x, charBounds.y + verticalDrawingOffset, true);
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
	private static boolean isDeleted(AbstractInlinedAnnotation annotation,int maxOffset) {
		if (annotation.isMarkedDeleted()) {
			return true;
		}
		Position pos= annotation.getPosition();
		if (pos.isDeleted()) {
			return true;
		}
		if (pos.getLength() == 0 && pos.getOffset() < maxOffset) {
			return true;
		}
		return false;
	}
}
