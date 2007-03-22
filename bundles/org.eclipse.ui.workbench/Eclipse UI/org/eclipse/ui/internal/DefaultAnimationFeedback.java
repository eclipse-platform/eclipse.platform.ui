/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.3
 *
 */
public class DefaultAnimationFeedback {
	private static final int LINE_WIDTH = 1;
	
	private Display display;
	private Shell theShell;
	private Region shellRegion;
	
	private List startRects = new ArrayList();
	private List endRects = new ArrayList();
	
	public DefaultAnimationFeedback() {}

    /**
	 * @param parentShell
	 */
	public void initialize(Shell parentShell, Rectangle startRect, Rectangle endRect) {
		addStartRect(startRect);
		addEndRect(endRect);

		theShell = new Shell(parentShell, SWT.NO_TRIM | SWT.ON_TOP);
		display = theShell.getDisplay();
        Color color = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        theShell.setBackground(color);

        // Ensure that the background won't show on the initial display
        shellRegion = new Region(display);
        theShell.setRegion(shellRegion);
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
	
	public List getStartRects() {
		return startRects;
	}
	
	public List getEndRects() {
		return endRects;
	}
	
	public void renderStep(double amount) {
		if (shellRegion != null) {
        	shellRegion.dispose();
        	shellRegion = new Region(display);
        }

		// Iterate across the set of start/end rects
        Iterator startIter = startRects.iterator();
        Iterator endIter = endRects.iterator();
        while (startIter.hasNext()) {
            Rectangle start = (Rectangle) startIter.next();
            Rectangle end = (Rectangle) endIter.next();
            
			// Get the bounds of the interpolated rect
			Rectangle curRect = RectangleAnimation.interpolate(start, end, amount);
			
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

	/**
	 * 
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
	public void jobInit() {
    	// Compute the shell's bounds
        Rectangle shellBounds = Geometry.copy((Rectangle) startRects.get(0));
        Iterator startIter = startRects.iterator();
        Iterator endIter = endRects.iterator();
        while (startIter.hasNext()) {
            shellBounds.add((Rectangle) startIter.next());
            shellBounds.add((Rectangle) endIter.next());
        }
        theShell.setBounds(shellBounds);
        
    	// Making the shell visible will be slow on old video cards, so only start
    	// the timer once it is visible.
    	theShell.setVisible(true);
	}
}
