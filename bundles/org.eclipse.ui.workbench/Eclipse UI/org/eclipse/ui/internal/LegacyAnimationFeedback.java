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

import java.util.Iterator;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Creates an animation feedback that will morph the start rectangle to the end rectangle
 * for AnimationEngine.
 * 
 * @since 3.3
 *
 */
public class LegacyAnimationFeedback extends RectangleAnimationFeedbackBase {
	private static final int LINE_WIDTH = 1;

	private Display display;
	private Region shellRegion;

	private Shell theShell;

	public LegacyAnimationFeedback(Shell parentShell, Rectangle start,
			Rectangle end) {
		super(parentShell, start, end);
	}

	public void renderStep(AnimationEngine engine) {
		if (shellRegion != null) {
			shellRegion.dispose();
			shellRegion = new Region(display);
		}

		// Iterate across the set of start/end rects
		Iterator currentRects = getCurrentRects(engine.amount()).iterator();
		while (currentRects.hasNext()) {
			Rectangle curRect = (Rectangle) currentRects.next();
			Rectangle rect = Geometry.toControl(theShell, curRect);
			shellRegion.add(rect);
			rect.x += LINE_WIDTH;
			rect.y += LINE_WIDTH;
			rect.width = Math.max(0, rect.width - 2 * LINE_WIDTH);
			rect.height = Math.max(0, rect.height - 2 * LINE_WIDTH);

			shellRegion.subtract(rect);
		}

		theShell.setRegion(shellRegion);

		display.update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.AnimationFeedbackBase#initialize(org.eclipse.ui.internal.AnimationEngine)
	 */
	public void initialize(AnimationEngine engine) {

		theShell = new Shell(getAnimationShell(), SWT.NO_TRIM | SWT.ON_TOP);
		display = theShell.getDisplay();
		Color color = display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
		theShell.setBackground(color);

		// Ensure that the background won't show on the initial display
		shellRegion = new Region(display);
		theShell.setRegion(shellRegion);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.AnimationFeedbackBase#dispose()
	 */
	public void dispose() {
		theShell.setVisible(false);
		theShell.dispose();
		shellRegion.dispose();
	}

	/**
	 * Perform any initialization you want to have happen -before- the
	 * amination starts
	 */
	public boolean jobInit(AnimationEngine engine) {
		if (!super.jobInit(engine))
			return false;
		
		// Compute the shell's bounds
		Rectangle shellBounds = Geometry.copy((Rectangle) getStartRects()
				.get(0));
		Iterator startIter = getStartRects().iterator();
		Iterator endIter = getEndRects().iterator();
		while (startIter.hasNext()) {
			shellBounds.add((Rectangle) startIter.next());
			shellBounds.add((Rectangle) endIter.next());
		}
		theShell.setBounds(shellBounds);
		// Making the shell visible will be slow on old video cards, so only start
		// the timer once it is visible.
		theShell.setVisible(true);
		
		return true;  // OK to go...
	}

}
