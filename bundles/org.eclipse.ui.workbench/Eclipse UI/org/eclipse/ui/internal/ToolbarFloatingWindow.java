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
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;


import org.eclipse.ui.internal.AssociatedWindow;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * The ToolbarFloatingWindow is a window that opens next to an owning widget and locates itself
 * relative to it.
 */
class ToolbarFloatingWindow extends AssociatedWindow {

	WorkbenchWindow window;
	Composite childControl;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parent
	 * @param associatedControl
	 */
	ToolbarFloatingWindow(Shell parent, Control associatedControl) {
		super(parent, associatedControl);
		setShellStyle(SWT.NO_TRIM | SWT.NO_FOCUS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see AssociatedWindow#(org.eclipse.swt.widgets.Shell,Control, int, int)
	 */
	public ToolbarFloatingWindow(Shell parent, Control associatedControl, int i) {
		super(parent, associatedControl, i, 0);
		setShellStyle(SWT.NO_TRIM | SWT.NO_FOCUS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getLayout()
	 */
	protected Layout getLayout() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		return layout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite root) {
		childControl = new Composite(root, SWT.NONE);
		childControl.setLayout(new GridLayout());
		return childControl;
	}

	/**
	 * Adjust the size of the viewer.
	 */
	private void adjustSize() {
		Shell floatingShell = getShell();
		floatingShell.layout();
		
		moveShell(floatingShell);
		setShellRegion(currentTrackStyle);		
	}


	/**
	 * @param floatingShell
	 */
	protected void setShellRegion(int trackStyle) {
		Shell floatingShell = getShell();
		if (floatingShell == null)
			return;
		if (trackStyle == TRACK_OUTER_TOP_RHS) {
			Point shellSize = floatingShell.getSize();
			Display display = floatingShell.getDisplay();
			Region r = new Region(display);
			Rectangle rect = new Rectangle(0,0, shellSize.x, shellSize.y);
			r.add(rect);
			Region cornerRegion = new Region(display);
			
			//top right corner region
			cornerRegion.add(new Rectangle(shellSize.x - 5, 0, 5 ,1));
			cornerRegion.add(new Rectangle(shellSize.x - 3, 1, 3 ,1));
			cornerRegion.add(new Rectangle(shellSize.x - 2, 2, 2 ,1));
			cornerRegion.add(new Rectangle(shellSize.x - 1, 3, 1 ,2));
			
			//bottom right corner region
			int y = shellSize.y;
			cornerRegion.add(new Rectangle(shellSize.x - 5, y - 1, 5 ,1));
			cornerRegion.add(new Rectangle(shellSize.x - 3, y - 2, 3 ,1));
			cornerRegion.add(new Rectangle(shellSize.x - 2, y - 3, 2 ,1));
			cornerRegion.add(new Rectangle(shellSize.x - 1, y - 5, 1 ,2));		
			
			r.subtract(cornerRegion);
			Region oldRegion = floatingShell.getRegion();
			floatingShell.setRegion(r);
			if (oldRegion != null)
				oldRegion.dispose();
		} else if (currentTrackStyle == TRACK_INNER_TOP_RHS) {
			// remove the corners and make the shell rectagular when inside 
			Region oldRegion = floatingShell.getRegion();
			if (oldRegion != null) {
				floatingShell.setRegion(null);
				oldRegion.dispose();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.AssociatedWindow#getTransparencyValue()
	 */
	protected int getTransparencyValue() {
		return 75;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.AssociatedWindow#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		if (getShell() == null)
			return super.close();
		Region oldRegion = getShell().getRegion();
		boolean result = super.close();
		if (result && oldRegion != null)
			oldRegion.dispose();
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#initializeBounds()
	 */
	protected void initializeBounds() {
		super.initializeBounds();
		adjustSize();
		moveShell(getShell());
	}

	/**
	 * Answer the top level control for the receiver, this will be added to when
	 * filling in the receivers contents
	 * 
	 * @return <code>Composite</control>  the top level parent control
	 */
	public Composite getControl() {
		if (childControl != null)
			return childControl;
		
		create();
		return childControl;
	}
}
