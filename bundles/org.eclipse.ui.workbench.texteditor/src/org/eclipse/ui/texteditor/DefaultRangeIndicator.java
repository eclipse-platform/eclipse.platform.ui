/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

package org.eclipse.ui.texteditor;


import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationPresentation;


/**
 * Specialized annotation to indicate a particular range of text lines.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * This class is instantiated automatically by <code>AbstractTextEditor</code>.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DefaultRangeIndicator extends Annotation implements IAnnotationPresentation {

	private static final String RANGE_INDICATOR_COLOR= "org.eclipse.ui.editors.rangeIndicatorColor"; //$NON-NLS-1$
	/** Alpha of the range fill, matching the coverage of the former stipple pattern. */
	private static final int FILL_ALPHA= 128;

	/**
	 * Creates a new range indicator.
	 */
	public DefaultRangeIndicator() {
	}

	@Override
	public void paint(GC gc, Canvas canvas, Rectangle bounds) {
		Point canvasSize= canvas.getSize();

		int x= 0;
		int y= bounds.y;
		int w= canvasSize.x;
		int h= bounds.height;
		int b= 1;

		if (y + h > canvasSize.y) {
			h= canvasSize.y - y;
		}

		if (y < 0) {
			h= h + y;
			y= 0;
		}

		if (h <= 0) {
			return;
		}

		Color rangeIndicatorColor= JFaceResources.getColorRegistry().get(RANGE_INDICATOR_COLOR);
		gc.setBackground(rangeIndicatorColor);

		int alpha= gc.getAlpha();
		gc.setAlpha(FILL_ALPHA);
		gc.fillRectangle(x, y, w, h);
		gc.setAlpha(alpha);

		gc.fillRectangle(x, bounds.y, w, b);
		gc.fillRectangle(x, bounds.y + bounds.height - b, w, b);
	}

	@Override
	public int getLayer() {
		return IAnnotationPresentation.DEFAULT_LAYER;
	}
}
