/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt.dom.scrollbar;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Helper class to compute the positions of the scrollbar. Note that it should
 * be agnostic of the scrollbar orientation.
 */
public abstract class ScrollBarPositions {

	/**
	 * The scrollbar minimum scroll value (ScrollBar.getMinimum())
	 */
	protected int fMinimum;

	/**
	 * The scrollbar maximum scroll value (ScrollBar.getMaximum())
	 */
	protected int fMaximum;

	/**
	 * The pixel which identifies the top pixel (if vertical) or horizontal
	 * pixel (if horizontal).
	 */
	protected int fPixel;

	/**
	 * The size of the current client area -- if it's a vertical scroll bar,
	 * that's its height, if it's a horizontal scroll bar, that's its width.
	 */
	protected int fSize;

	/**
	 * How large the scrollbar is -- if it's a vertical scroll bar, that's its
	 * width, if it's a horizontal scroll bar, that's its height.
	 */
	protected int fLargeness;

	/**
	 * The computed scrollbar position in the client area.
	 */
	protected int fScrollBarPos;

	/**
	 * The computed scrollbar size in the client area.
	 */
	protected int fScrollBarSize;

	/**
	 * This is a percentage which identifies a proportion of how much a space is
	 * in the client area relative to the total size of the control.
	 */
	protected double fPercentageOfClientAreaFromTotalArea;

	/**
	 * Minimum size for the scrollbar.
	 */
	private int fMinimumScrollBarSize = 30;

	/**
	 * The difference from minimum scrollbar size minus the computed size of the
	 * scrollbar (if the computed size of the scrollbar is lower than the
	 * minimum scrollbar size).
	 */
	private int fScrollBarDiff;

	/**
	 *
	 * @param minimum
	 *            The minimum value of the scroll bar.
	 * @param maximum
	 *            The maximum value of the scroll bar.
	 * @param pixel
	 *            The pixel which identifies the top pixel (if vertical) or
	 *            horizontal pixel (if horizontal).
	 * @param size
	 *            The size of the current client area -- if it's a vertical
	 *            scroll bar, that's its height, if it's a horizontal scroll
	 *            bar, that's its width.
	 * @param largeness
	 *            How large the current client area is -- if it's a vertical
	 *            scroll bar, that's its width, if it's a horizontal scroll bar,
	 *            that's its height.
	 */
	public ScrollBarPositions(int minimum, int maximum, int pixel, int size, int largeness) {
		this.fMinimum = minimum;
		this.fMaximum = maximum;
		this.fPixel = pixel;
		this.fSize = size;
		this.fLargeness = largeness;

		double total = maximum - minimum;
		double percentageOfClientAreaFromTotalArea = size / total;

		int scrollBarPos;
		int scrollBarSize = (int) Math.round((size * percentageOfClientAreaFromTotalArea));
		if (scrollBarSize < fMinimumScrollBarSize) {
			// We have to do things relative considering the new
			int diff = fMinimumScrollBarSize - scrollBarSize;
			percentageOfClientAreaFromTotalArea = (fSize - diff) / total;
			scrollBarPos = (int) (fPixel * percentageOfClientAreaFromTotalArea);
			scrollBarSize = fMinimumScrollBarSize;
			this.fScrollBarDiff = diff;
		} else {
			percentageOfClientAreaFromTotalArea = fSize / total;
			this.fScrollBarDiff = 0;
			scrollBarPos = (int) Math.round((fPixel * percentageOfClientAreaFromTotalArea));
		}

		this.fPercentageOfClientAreaFromTotalArea = percentageOfClientAreaFromTotalArea;
		this.fScrollBarPos = scrollBarPos;
		this.fScrollBarSize = scrollBarSize;
	}

	public double convertFromScrollBarPosToControlPixel(int pos) {
		return pos * (double) (fMaximum) / (fSize - fScrollBarDiff);
	}

	public abstract Rectangle getHandleDrawRect(int lineWidth);

	/**
	 * Computations considering a vertical scrollbar.
	 */
	public static class ScrollBarPositionsVertical extends ScrollBarPositions {

		public ScrollBarPositionsVertical(int minimum, int maximum, int topPixel, int clientAreaHeight,
				int clientAreaWidth) {
			super(minimum, maximum, topPixel, clientAreaHeight, clientAreaWidth);
		}

		@Override
		public Rectangle getHandleDrawRect(int lineWidth) {
			return new Rectangle(fLargeness - lineWidth, fScrollBarPos, lineWidth, fScrollBarSize);
		}

	}

	/**
	 * Computations considering a horizontal scrollbar.
	 */
	public static class ScrollBarPositionsHorizontal extends ScrollBarPositions {

		public ScrollBarPositionsHorizontal(int minimum, int maximum, int horizontalPixel, int clientAreaHeight,
				int clientAreaWidth) {
			super(minimum, maximum, horizontalPixel, clientAreaWidth, clientAreaHeight);
		}

		@Override
		public Rectangle getHandleDrawRect(int lineWidth) {
			return new Rectangle(fScrollBarPos, fLargeness - lineWidth, fScrollBarSize, lineWidth);
		}
	}

}
