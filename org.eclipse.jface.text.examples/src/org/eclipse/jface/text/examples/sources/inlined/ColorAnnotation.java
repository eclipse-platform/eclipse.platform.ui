/**s
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
package org.eclipse.jface.text.examples.sources.inlined;

import java.util.function.Consumer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.LineContentAnnotation;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Color annotation displays a colorized square before the rgb declaration.
 */
public class ColorAnnotation extends LineContentAnnotation {

	private Color color;
	
	private Consumer<MouseEvent> action = e -> {
		StyledText styledText = super.getTextWidget();
		Shell shell = new Shell(styledText.getDisplay());
		Rectangle location = Geometry.toDisplay(styledText, new Rectangle(e.x, e.y, 1, 1));
		shell.setLocation(location.x, location.y);
		// Open color dialog
		ColorDialog dialog = new ColorDialog(shell);
		// dialog.setRGB(annotation.getRGBA().rgb);
		RGB color = dialog.open();
		if (color != null) {
			// Color was selected, update the viewer
			try {
				int offset = getPosition().getOffset();
				IDocument document = getViewer().getDocument();
				IRegion line = document.getLineInformation(document.getLineOfOffset(offset));
				int length = line.getLength() - (offset - line.getOffset());
				String rgb = formatToRGB(color);
				document.replace(offset, length, rgb);
			} catch (BadLocationException e1) {

			}
		}
	};
	
	/**
	 * Format the given rgb to hexa color.
	 * 
	 * @param rgb
	 * @return the hexa color from the given rgb.
	 */
	private static String formatToRGB(RGB rgb) {
		return new StringBuilder("rgb(").append(rgb.red).append(",").append(rgb.green).append(",").append(rgb.blue)
				.append(")").toString();
	}

	public ColorAnnotation(Position pos, ISourceViewer viewer) {
		super(pos, viewer);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	protected int drawAndComputeWidth(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		FontMetrics fontMetrics = gc.getFontMetrics();
		int size = getSquareSize(fontMetrics);
		x += fontMetrics.getLeading();
		y += fontMetrics.getDescent();

		Rectangle rect = new Rectangle(x, y, size, size);

		// Fill square
		gc.setBackground(this.color);
		gc.fillRectangle(rect);

		// Draw square box
		gc.setForeground(textWidget.getForeground());
		gc.drawRectangle(rect);
		return getSquareWidth(gc.getFontMetrics());
	}

	/**
	 * Returns the colorized square size.
	 *
	 * @param fontMetrics
	 * @return the colorized square size.
	 */
	public static int getSquareSize(FontMetrics fontMetrics) {
		return fontMetrics.getHeight() - 2 * fontMetrics.getDescent();
	}

	/**
	 * Compute width of square
	 *
	 * @param styledText
	 * @return the width of square
	 */
	private static int getSquareWidth(FontMetrics fontMetrics) {
		// width = 2 spaces + size width of square
		int width = 2 * fontMetrics.getAverageCharWidth() + getSquareSize(fontMetrics);
		return width;
	}
	
	@Override
	public Consumer<MouseEvent> getAction(MouseEvent e) {
		return action;
	}
}
