/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.gestures;

public final class GestureSupport {

	public static String recognize(Point[] points, int sensitivity) {
		char stroke = '\0';
		StringBuffer sequence = new StringBuffer();
		int x0 = 0;
		int y0 = 0;

		for (int i = 0; i < points.length; i++) {
			Point point = points[i];

			if (i == 0) {
				x0 = point.getX();
				y0 = point.getY();
				continue;
			}

			int x1 = point.getX();
			int y1 = point.getY();
			int dx = (x1 - x0) / sensitivity;
			int dy = (y1 - y0) / sensitivity;

			if (dx != 0 || dy != 0) {
				if (dx > 0 && stroke != 'R')
					sequence.append(stroke = 'R');
				else if (dx < 0 && stroke != 'L')
					sequence.append(stroke = 'L');
				else if (dy > 0 && stroke != 'D')
					sequence.append(stroke = 'D');
				else if (dy < 0 && stroke != 'U')
					sequence.append(stroke = 'U');

				x0 = x1;
				y0 = y1;
			}
		}

		return sequence.toString();
	}

    private GestureSupport() {
    }
}