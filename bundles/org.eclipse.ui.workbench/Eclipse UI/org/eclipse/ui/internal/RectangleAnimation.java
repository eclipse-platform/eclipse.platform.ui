/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
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
	private static class AnimationFeedbackFactory {
		/**
		 * Determines whether or not the system being used is
		 * sufficiently fast to support image animations.
		 * 
		 * Assumes that a pixel is ~3 bytes
		 * 
		 * For now we use a base limitation of 50MB/sec as a
		 * 'reverse blt' rate so that a 2MB size shell can be
		 * captured in under 1/25th of a sec. 
		 */
		private static final int IMAGE_ANIMATION_THRESHOLD = 25; // Frame captures / Sec
		private static final int IMAGE_ANIMATION_TEST_LOOP_COUNT = 20; // test the copy 'n' times
	    
		//private static double framesPerSec = 0.0;
		
	    public static double getCaptureSpeed(Shell wb) {
	    	// OK, capture
	    	Rectangle bb = wb.getBounds();
			Image backingStore = new Image(wb.getDisplay(), bb);
			GC gc = new GC(wb);
			
			// Loop 'n' times to average the result
	    	long startTime = System.currentTimeMillis();
			for (int i = 0; i < IMAGE_ANIMATION_TEST_LOOP_COUNT; i++)
				gc.copyArea(backingStore, bb.x, bb.y);			
			gc.dispose();
			long endTime = System.currentTimeMillis();
			
			// get Frames / Sec
			double fps = IMAGE_ANIMATION_TEST_LOOP_COUNT / ((endTime-startTime) / 1000.0);
			double pps = fps * (bb.width*bb.height*4); // 4 bytes/pixel
			System.out.println("FPS: " + fps + " Bytes/sec: " + (long)pps); //$NON-NLS-1$ //$NON-NLS-2$
	    	return fps;
	    }
		
	    public boolean useImageAnimations(Shell wb) {
	    	return getCaptureSpeed(wb) >= IMAGE_ANIMATION_THRESHOLD;
	    }
	    
		public static DefaultAnimationFeedback createAnimationRenderer(Shell parentShell) {
			// on the first call test the animation threshold to determine
			// whether to use image animations or not...
//			if (framesPerSec == 0.0)
//				framesPerSec = getCaptureSpeed(parentShell);
//			
//	        IPreferenceStore preferenceStore = PrefUtil.getAPIPreferenceStore();
//	        boolean useNewMinMax = preferenceStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX);
//
//			if (useNewMinMax && framesPerSec >= IMAGE_ANIMATION_THRESHOLD) {
//				return new ImageAnimationFeedback();
//			}
//			
			return new DefaultAnimationFeedback();
		}
	}
	
	// Constants
	public static final int TICK_TIMER = 1;
	public static final int FRAME_COUNT = 2;

	// Animation Parameters
	private Display display;
	
	private boolean enableAnimations;
    private int timingStyle = TICK_TIMER;
    private int duration;
    
    // Control State
    private DefaultAnimationFeedback feedbackRenderer;
    private long stepCount;
    private long frameCount;
    private long startTime;
    private long curTime;
    private long prevTime;
    
    // Macros
    private boolean done() { return amount() >= 1.0; }

    public static Rectangle interpolate(Rectangle start, Rectangle end,
            double amount) {
        double initialWeight = 1.0 - amount;

        Rectangle result = new Rectangle((int) (start.x * initialWeight + end.x
                * amount), (int) (start.y * initialWeight + end.y * amount),
                (int) (start.width * initialWeight + end.width * amount),
                (int) (start.height * initialWeight + end.height * amount));

        return result;
    }
    
    // Animation Step
    private Runnable animationStep = new Runnable() {

		public void run() {
            // Capture time
            prevTime = curTime;
            curTime = System.currentTimeMillis();

            // Has the system timer 'ticked'?
            if (curTime != prevTime) {
            	clockTick();
            }
            
            if (isUpdateStep()) {
	            updateDisplay();
	            frameCount++;
            }
            
            stepCount++;
        }

    };
    
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

        // if animations aren't on this is a NO-OP
        IPreferenceStore preferenceStore = PrefUtil.getAPIPreferenceStore();
        enableAnimations = preferenceStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS);
        
        if (!enableAnimations) {
        	return;
        }

        // Capture paraeters
        display = parentShell.getDisplay();
        this.duration = duration;

        // Don't show the job in monitors
        setSystem(true);
        
        // Pick the renderer (could be a preference...)
        feedbackRenderer = AnimationFeedbackFactory.createAnimationRenderer(parentShell);
        
        // Set it up
        feedbackRenderer.initialize(parentShell, start, end);
        
        // Set the animation's initial state
        stepCount = 0;
        //long totalFrames = (long) ((duration / 1000.0) * framesPerSec);       
        curTime = startTime = System.currentTimeMillis();
    }

    public RectangleAnimation(Shell parentShell, Rectangle start, Rectangle end) {
        this(parentShell, start, end, 400);
    }
    
    public void addStartRect(Rectangle rect) {
    	if (feedbackRenderer != null)
    		feedbackRenderer.addStartRect(rect);
    }
    
    public void addEndRect(Rectangle rect) {
    	if (feedbackRenderer != null)
    	    feedbackRenderer.addEndRect(rect);
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

    /**
	 * 
	 */
	protected void clockTick() {
	}
    
    /**
	 * @return
	 */
	protected boolean isUpdateStep() {
		switch (timingStyle) {
			case TICK_TIMER:
				return prevTime != curTime;
	
			case FRAME_COUNT:
				return true;
		}
		
		return false;
	}

	private double amount() {
		double amount = 0.0;
		
		switch (timingStyle) {
			case TICK_TIMER:
				amount = (double) (curTime - startTime) / (double) duration;
				break;
	
			case FRAME_COUNT:
				amount = (double)frameCount / (double)duration;
		}
		
		if (amount > 1.0)
			amount = 1.0;
		
		return amount;
    }

    /**
	 * 
	 */
	protected void updateDisplay() {
		feedbackRenderer.renderStep(amount());
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected IStatus run(IProgressMonitor monitor) {
    	
        // We use preference value to indicate that the animation should be skipped on this platform.
        if (!enableAnimations || feedbackRenderer == null) {
            return Status.OK_STATUS;
        }

        // Do we have anything to animate ?
    	boolean isEmpty = feedbackRenderer.getStartRects().size() == 0;
        if (isEmpty) {
            return Status.OK_STATUS;
        }
    	
        // We're starting, initialize
        display.syncExec(new Runnable() {
            public void run() {
                feedbackRenderer.jobInit();
            }
        });
        
        // Only start the animation timer -after- we've initialized
        curTime = startTime = System.currentTimeMillis();
        
        while (!done()) {
            display.syncExec(animationStep);
            // Don't pin the CPU
            Thread.yield();
        }

        //System.out.println("Done: " + (curTime-startTime) + " steps: " + stepCount + " frames:" + frameCount);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        // We're done, clean up
        display.syncExec(new Runnable() {
            public void run() {
                feedbackRenderer.dispose();
            }
        });
    
        return Status.OK_STATUS;
    }
}
