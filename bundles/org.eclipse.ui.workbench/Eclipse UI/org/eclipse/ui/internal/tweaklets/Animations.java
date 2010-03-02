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

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.RectangleAnimationFeedbackBase;
import org.eclipse.ui.internal.tweaklets.Tweaklets.TweakKey;

/**
 * Animation tweaklet base class. Create (and register) a tweaklet
 * extension derived from this class to provide alternate animation
 * behavior. Currently only affects animations produced by the new
 * min / max behavior. 
 * 
 * @since 3.3
 *
 */
public abstract class Animations {
	public static TweakKey KEY = new Tweaklets.TweakKey(Animations.class);

	static {
		Tweaklets.setDefault(Animations.KEY, new LegacyAnimations());
	}

	/** Default c'tor */
	public Animations() {}
	
	/**
	 * Create and return the animation feedback you want to use.
	 * 
	 * @param shell The shell that the animation will be in
	 * @return The feedback renderer to use.
	 */
	public abstract RectangleAnimationFeedbackBase createFeedback(Shell shell);
}
