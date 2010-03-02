/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.AnimationEngine;
import org.eclipse.ui.internal.AnimationFeedbackBase;

/**
 * RectangleAnimationFeedbackBase is an abstract base class for all the 
 * rectangle animations. 
 * @since 3.3
 *
 */
public abstract class RectangleAnimationFeedbackBase extends AnimationFeedbackBase {

	private List startRects = new ArrayList();
	private List endRects = new ArrayList();

	/**
	 * Creates a Rectangle Animation Feedback
	 * 
	 * @param parentShell specifies the composite where the animation will be drawn
	 * @param start initial rectangle (display coordinates)
	 * @param end final rectangle (display coordinates)
	 */
	public RectangleAnimationFeedbackBase(Shell parentShell, Rectangle start,
			Rectangle end) {
		super(parentShell);
		addStartRect(start);
		addEndRect(end);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.AnimationFeedbackBase#jobInit(org.eclipse.ui.internal.AnimationEngine)
	 * 
	 * Prevent execution if there are no rects to draw or there's a mismatch in the count
	 */
	public boolean jobInit(AnimationEngine engine) {
		if (!super.jobInit(engine))
			return false;
		
		return startRects.size() > 0 && startRects.size() == endRects.size();
	}

	public void addStartRect(Rectangle rect) {
		if (rect != null) {
			startRects.add(rect);
		}
	}

	public void addEndRect(Rectangle rect) {
		if (rect != null) {
			endRects.add(rect);
		}
	}

    public void addStartRect(Control ctrl) {
    	Rectangle ctrlBounds = ctrl.getBounds();
    	Rectangle startRect = Geometry.toDisplay(ctrl.getParent(), ctrlBounds);
    	addStartRect(startRect);
    }

    public void addEndRect(Control ctrl) {
    	Rectangle ctrlBounds = ctrl.getBounds();
    	Rectangle endRect = Geometry.toDisplay(ctrl.getParent(), ctrlBounds);
    	addEndRect(endRect);
    }

	public static Rectangle interpolate(Rectangle start, Rectangle end,
			double amount) {
		double initialWeight = 1.0 - amount;

		Rectangle result = new Rectangle((int) (start.x * initialWeight + end.x
				* amount), (int) (start.y * initialWeight + end.y * amount),
				(int) (start.width * initialWeight + end.width * amount),
				(int) (start.height * initialWeight + end.height * amount));

		return result;
	}

	public List getStartRects() {
		return startRects;
	}

	public List getEndRects() {
		return endRects;
	}

	public List getCurrentRects(double amount) {
		List currentRects = new ArrayList();
		Iterator startIter = getStartRects().iterator();
		Iterator endIter = getEndRects().iterator();
		while (startIter.hasNext()) {
			Rectangle start = (Rectangle) startIter.next();
			Rectangle end = (Rectangle) endIter.next();

			// Get the bounds of the interpolated rect
			Rectangle curRect = interpolate(start, end, amount);
			currentRects.add(curRect);
		}
		return currentRects;
	}

}
