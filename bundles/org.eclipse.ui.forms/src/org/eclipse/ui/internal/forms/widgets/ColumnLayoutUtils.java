/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.forms.widgets;

import org.eclipse.swt.graphics.Point;

public class ColumnLayoutUtils {

	/*
	 * Compute the minimum required height by iteration. The first guess is to
	 *
	 * This method is public to allow for JUnit testing
	 */
	public static int computeColumnHeight(int ncolumns, Point[] sizes, int totalHeight, int verticalMargin) {
		int averageHeight = ( totalHeight + sizes.length * verticalMargin ) / ncolumns;
		int requiredHeight = computeActualHeight(ncolumns, sizes, averageHeight, verticalMargin);
		if (averageHeight == requiredHeight) {
			return requiredHeight;
		}
		// Try making the columns shorter, repeat up to 10 times, usually one or two iterations will be sufficient
		for ( int i = 0; i < 10; i++ ) {
			int candidateHeight = computeActualHeight(ncolumns, sizes, requiredHeight - 1, verticalMargin);
			if ( candidateHeight >= requiredHeight ) {
				return requiredHeight;
			}
			requiredHeight = candidateHeight;
		}
		return requiredHeight;
	}

	private static int computeActualHeight(int ncolumns, Point[] sizes, int candidateHeight, int verticalMargin ) {
		int colHeight = 0;
		int maxHeight = 0;
		int column = 1;
		for (int i = 0; i < sizes.length; i++) {
			int childHeight = sizes[i].y;
			if (i > 0 && column < ncolumns && colHeight + childHeight + verticalMargin > candidateHeight) {
				maxHeight = Math.max(colHeight, maxHeight);
				column++;
				colHeight = 0;
			}
			if (colHeight > 0) {
				colHeight += verticalMargin;
			}
			colHeight += childHeight;
		}
		return Math.max(colHeight, maxHeight);
	}
}
