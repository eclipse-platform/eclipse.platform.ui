/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Creates an animation effect where the Shell's image is captured and
 * over-lain (in its own shell) on top of the real one. This image
 * masks the changes to the 'real' shell and then the covering image
 * fades to transparent, revealing the new state.
 * 
 * This provides a nice cross-fade effect for operations like a
 * perspective change (where the overall effect on the shell is large.
 * 
 * @since 3.3
 *
 */
public class FaderAnimationFeedback extends	AnimationFeedbackBase {
	private Image backingStore;

	public FaderAnimationFeedback(Shell parentShell) {
		super(parentShell);
	}

	public void dispose() {
		super.dispose();

		if (!backingStore.isDisposed())
			backingStore.dispose();
	}

	public void initialize(AnimationEngine engine) {
		Rectangle psRect = getBaseShell().getBounds();
		getAnimationShell().setBounds(psRect);

		// Capture the background image
		Display display = getBaseShell().getDisplay();
		backingStore = new Image(display, psRect);
		GC gc = new GC(display);
		// gc.copyArea(backingStore, psRect.x, psRect.y);
		gc.copyArea(backingStore, psRect.x, psRect.y);
		gc.dispose();
		
		getAnimationShell().setAlpha(254);
		getAnimationShell().setBackgroundImage(backingStore);
		getAnimationShell().setVisible(true);
	}

	public void renderStep(AnimationEngine engine) {
		getAnimationShell().setAlpha((int) (255 - (engine.amount()*255)));
	}
	
}
