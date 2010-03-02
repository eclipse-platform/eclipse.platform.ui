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
package org.eclipse.ui.internal;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
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
	static boolean useCopy = true;

	public FaderAnimationFeedback(Shell parentShell) {
		super(parentShell);
	}

	public void dispose() {
		super.dispose();

		if (!backingStore.isDisposed())
			backingStore.dispose();
	}

//	private static Image printImage(Control control) {
//		Rectangle r = control.getBounds();
//		final Image image = new Image(control.getDisplay(), r.width, r.height);
//		GC gc = new GC(image);
//		int hDC = gc.handle;
//		int hwnd = control.handle;
//		int bits = OS.GetWindowLong (hwnd, OS.GWL_STYLE);
//		if ((bits & OS.WS_VISIBLE) == 0) {
//			OS.DefWindowProc(hwnd, OS.WM_SETREDRAW, 1, 0);
//		} 
//		OS.RedrawWindow (hwnd, null, 0, OS.RDW_UPDATENOW | OS.RDW_ALLCHILDREN);
//		final long t0 = System.currentTimeMillis();
//		OS.PrintWindow (hwnd, hDC, 0);
//		final long t1 = System.currentTimeMillis();
//		System.out.println("Time: " + (t1 - t0) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
//		if ((bits & OS.WS_VISIBLE) == 0) {
//			OS.DefWindowProc(hwnd, OS.WM_SETREDRAW, 0, 0);
//		}
//		gc.dispose();
//		
//		return image;
//	}

	public void initialize(AnimationEngine engine) {
		Rectangle psRect = getBaseShell().getBounds();
		getAnimationShell().setBounds(psRect);

		// Capture the background image
		System.out.println("Start time = " + System.currentTimeMillis()); //$NON-NLS-1$
		if (useCopy) {
			backingStore = new Image(getAnimationShell().getDisplay(), psRect);
			GC gc = new GC(getAnimationShell());
			gc.copyArea(backingStore, psRect.x, psRect.y);
			gc.dispose();
		}
		else {
			System.out.println("use printImage"); //$NON-NLS-1$
			//backingStore = printImage(getAnimationShell());
		}
		
		getAnimationShell().setAlpha(254);
		getAnimationShell().setBackgroundImage(backingStore);
		getAnimationShell().setVisible(true);
		System.out.println("End time = " + System.currentTimeMillis()); //$NON-NLS-1$
		//display.update();

	}

	public void renderStep(AnimationEngine engine) {
		getAnimationShell().setAlpha((int) (255 - (engine.amount()*255)));
	}
	
}
