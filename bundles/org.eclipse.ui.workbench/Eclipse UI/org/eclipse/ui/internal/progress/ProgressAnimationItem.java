/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * Create an instance of the receiver in the window.
 * 
 * @param workbenchWindow
 *            The window this is being created in.
 */
public class ProgressAnimationItem extends AnimationItem {

	ProgressBar bar;
	MouseListener mouseListener;
	StackLayout layout;
	Composite top;

	/**
	 * Create an instance of the receiver in the supplied region.
	 * 
	 * @param region. The ProgressRegion that contains the receiver.
	 */
	ProgressAnimationItem(final ProgressRegion region) {
		super(region.workbenchWindow);
		mouseListener = new MouseAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent e) {
				region.processDoubleClick();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AnimationItem#createAnimationItem(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createAnimationItem(Composite parent) {
		
		top = new Composite(parent,SWT.NULL);
		layout = new StackLayout();
		top.setLayout(layout);
		
		bar = new ProgressBar(top,SWT.HORIZONTAL | SWT.INDETERMINATE);
		bar.addMouseListener(mouseListener);
		
		layout.topControl = null;
		return top;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AnimationItem#getControl()
	 */
	public Control getControl() {
		return top;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AnimationItem#animationDone()
	 */
	void animationDone() {
		super.animationDone();
		if(bar.isDisposed())
			return;
		
		layout.topControl = null;
		top.layout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AnimationItem#animationStart()
	 */
	void animationStart() {
		super.animationStart();
		if(bar.isDisposed())
			return;
		layout.topControl = bar;
		top.layout();
	}


}
