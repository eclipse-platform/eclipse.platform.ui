package org.eclipse.ui.externaltools.internal.ant.view.actions;

/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;

/**
 * Action that switches the orientation of the Ant View, toggling it between
 * vertical and horizontal alignment.
 */
public class SwitchAntViewOrientation extends Action {
	
	private AntView view;
	private int orientation;
	
	public SwitchAntViewOrientation(AntView view, int orientation) {
		super();
		this.view= view;
		this.orientation= orientation;
		if (orientation == SWT.HORIZONTAL) {
			setText("Horizontal View Orientation");
			setToolTipText("Align the view components horizontally");
			setDescription("Align the view components horizontally");
		} else if (orientation == SWT.VERTICAL) {
			setText("Vertical View Orientation");
			setToolTipText("Align the view components vertically");
			setDescription("Align the view components vertically");
		} else {
			Assert.isTrue(false, "Invalid orientation specified for Ant view orientation action");
		}
	}

	/**
	 * Toggle's the ant view's orientation
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		view.setViewOrientation(orientation);
	}

}
