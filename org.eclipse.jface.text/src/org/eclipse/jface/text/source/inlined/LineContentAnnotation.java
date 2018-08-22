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

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import org.eclipse.jface.text.Position;
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
		super(position, viewer);
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

}
