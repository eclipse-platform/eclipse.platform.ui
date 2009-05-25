/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;

/**
 * AnimationFeedBackBase is an abstract class which provides renderStep(), jobInit() and 
 * initialize() methods for AnimationEngine. 
 * Its the base class for all the
 * animationFeedbacks
 * 
 * @since 3.3
 * 
 */
public abstract class AnimationFeedbackBase {
	private AnimationEngine engine;
	private Shell baseShell;
	private Shell animationShell = null;

	/**
	 * Creates an AnimationFeedback
	 * 
	 * @param parentShell specifies the composite where the animation will be drawn
	 */
	public AnimationFeedbackBase(Shell parentShell) {
		baseShell = parentShell;
		
		baseShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (engine != null)
					engine.cancelAnimation();
			}
		});
	}

	/**
	 * Perform any initialization you want to do -prior- to the Job actually
	 * gets scheduled.
	 * 
	 * @param animationEngine The engine we're hosted in.
	 */
	public abstract void initialize(AnimationEngine animationEngine);

	/**
	 * Its a draw method. All the code to render an animation goes in this
	 * method.
	 * 
	 * @param engine
	 */
	public abstract void renderStep(AnimationEngine engine);

	/**
	 * Perform any initialization you want to have happen -before- the animation
	 * starts. Subclasses may subclass but not override (i.e. you have to call super).
	 * 
	 * @param engine The AnimationEngine hosting the feedback
	 * @return 'true' iff the animation is capable of running
	 */
	public boolean jobInit(AnimationEngine engine) {
		this.engine = engine;
		return engine != null;
	}

	/**
	 * Dispose any locally created resources
	 */
	public void dispose() {
		if (animationShell != null && !animationShell.isDisposed())
			animationShell.dispose();
	}

	/**
	 * @return The shell this animation is being rendered 'on'
	 */
	public Shell getBaseShell() {
		return baseShell;
	}

	/**
	 * @return A shell that can be used to render the animation on
	 */
	public Shell getAnimationShell() {
		if (animationShell == null) {
			animationShell = new Shell(getBaseShell(), SWT.NO_TRIM | SWT.ON_TOP);			
			
			animationShell.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (engine != null)
						engine.cancelAnimation();
				}
			});
		}
		
		return animationShell;
	}

}
