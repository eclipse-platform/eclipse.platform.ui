/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * This job creates an animated rectangle that moves from a source rectangle to
 * a target in a fixed amount of time. To begin the animation, instantiate this
 * object then call schedule().
 *  
 * @since 3.0
 */
public class RectangleAnimation extends Job {
    private static final int LINE_WIDTH = 2;

    private Rectangle start;

    private int duration;

    private long startTime = 0;

    private Rectangle end;

    private boolean done = false;

    private Shell theShell;

    private Display display;

    private Region shellRegion;

    private boolean first = true;

    private static Rectangle interpolate(Rectangle start, Rectangle end,
            double amount) {
        double initialWeight = 1.0 - amount;

        Rectangle result = new Rectangle((int) (start.x * initialWeight + end.x
                * amount), (int) (start.y * initialWeight + end.y * amount),
                (int) (start.width * initialWeight + end.width * amount),
                (int) (start.height * initialWeight + end.height * amount));

        return result;
    }

    private Runnable paintJob = new Runnable() {

        public void run() {

            if (theShell == null || theShell.isDisposed()) {
                done = true;
                return;
            }

            if (first) {
                // Wait until the first syncExec before we make the shell visible and start
            	// the timer.
            	setCurrentRectangle(start);
            	theShell.setVisible(true);
            	// Making the shell visible will be slow on old video cards, so only start
            	// the timer once it is visible.
            	startTime = System.currentTimeMillis();
            	first = false;
            	return;
            }
            
            long currentTime = System.currentTimeMillis();
            
            double amount = (double) (currentTime - startTime)
                    / (double) duration;

            if (amount > 1.0) {
                amount = 1.0;
                done = true;
            }

            Rectangle toPaint = interpolate(start, end, amount);

            setCurrentRectangle(toPaint);
            
            display.update();
        }

    };

    public RectangleAnimation(Shell parentShell, Rectangle start, Rectangle end) {
        this(parentShell, start, end, 400);
    }

    private void setCurrentRectangle(Rectangle newRegion) {
        if (shellRegion != null) {
            shellRegion.dispose();
            shellRegion = new Region(display);
        }
        
        Rectangle rect = Geometry.toControl(theShell, newRegion);
        shellRegion.add(rect);
        rect.x += LINE_WIDTH;
        rect.y += LINE_WIDTH;
        rect.width = Math.max(0, rect.width - 2 * LINE_WIDTH);
        rect.height = Math.max(0, rect.height - 2 * LINE_WIDTH);

        shellRegion.subtract(rect);

        theShell.setRegion(shellRegion);
    }
    
    /**
     * Creates an animation that will morph the start rectangle to the end rectangle in the
     * given number of milliseconds. The animation will take the given number of milliseconds to
     * complete.
     * 
     * Note that this is a Job, so you must invoke schedule() before the animation will begin 
     * 
     * @param whereToDraw specifies the composite where the animation will be drawn. Note that
     * although the start and end rectangles can accept any value in display coordinates, the
     * actual animation will be clipped to the boundaries of this composite. For this reason,
     * it is good to select a composite that encloses both the start and end rectangles.
     * @param start initial rectangle (display coordinates)
     * @param end final rectangle (display coordinates)
     * @param duration number of milliseconds over which the animation will run 
     */
    public RectangleAnimation(Shell parentShell, Rectangle start,
            Rectangle end, int duration) {
        super(WorkbenchMessages.RectangleAnimation_Animating_Rectangle);
        this.duration = duration;
        this.start = start;
        this.end = end;

        display = parentShell.getDisplay();

        setSystem(true);

        IPreferenceStore preferenceStore = PrefUtil.getAPIPreferenceStore();
        boolean enableAnimations = preferenceStore
        	.getBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS);
        	
        if (enableAnimations) {
	        theShell = new Shell(parentShell, SWT.NO_TRIM | SWT.ON_TOP);
	        Color color = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
	        theShell.setBackground(color);
	
	        Rectangle shellBounds = Geometry.copy(start);
	        shellBounds.add(end);
	        theShell.setBounds(shellBounds);
	
	        shellRegion = new Region(display);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected IStatus run(IProgressMonitor monitor) {

        // We use canvas = null to indicate that the animation should be skipped on this platform.
        if (theShell == null) {
            return Status.OK_STATUS;
        }

        startTime = 0;

        while (!done) {
            if (!theShell.isDisposed()) {
                display.syncExec(paintJob);
                // Don't pin the CPU
                Thread.yield();
            }
        }

        if (!theShell.isDisposed()) {
            display.syncExec(new Runnable() {
                public void run() {
                    theShell.dispose();
                }
            });
        }

        if (!shellRegion.isDisposed()) {
            display.syncExec(new Runnable() {
                public void run() {
                    shellRegion.dispose();
                }
            });
        }

        return Status.OK_STATUS;
    }
}
