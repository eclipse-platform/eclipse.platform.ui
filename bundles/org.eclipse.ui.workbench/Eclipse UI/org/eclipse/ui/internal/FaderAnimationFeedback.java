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

import org.eclipse.swt.SWT;
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
	private Shell theShell;
	private Display display;
	static boolean useCopy = true;

	public FaderAnimationFeedback(Shell parentShell) {
		super(parentShell);
	}

	public void dispose() {
		backingStore.dispose();
		theShell.setVisible(false);
		theShell.dispose();
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
		display = getAnimationShell().getDisplay();

		Rectangle psRect = getAnimationShell().getBounds();
		theShell = new Shell(getAnimationShell(), SWT.NO_TRIM | SWT.ON_TOP);
		theShell.setBounds(getAnimationShell().getBounds());

		// Capture the background image
		System.out.println("Start time = " + System.currentTimeMillis()); //$NON-NLS-1$
		if (useCopy) {
			backingStore = new Image(theShell.getDisplay(), psRect);
			GC gc = new GC(display);
			gc.copyArea(backingStore, psRect.x, psRect.y);
			gc.dispose();
		}
		else {
			System.out.println("use printImage"); //$NON-NLS-1$
			//backingStore = printImage(getAnimationShell());
		}
		
		theShell.setAlpha(254);
		theShell.setBackgroundImage(backingStore);
		theShell.setVisible(true);
		System.out.println("End time = " + System.currentTimeMillis()); //$NON-NLS-1$
		//display.update();

	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.RectangleAnimationFeedbackBase#jobInit(org.eclipse.ui.internal.AnimationEngine)
	 */
	public boolean jobInit(AnimationEngine engine) {
		return super.jobInit(engine);
	}

	public void renderStep(AnimationEngine engine) {
		//System.out.println("render: " + System.currentTimeMillis() + " amount" + engine.amount()); //$NON-NLS-1$ //$NON-NLS-2$
		theShell.setAlpha((int) (255 - (engine.amount()*255)));
	}
}
