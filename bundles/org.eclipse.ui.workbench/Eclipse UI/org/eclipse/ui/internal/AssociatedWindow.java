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

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The AssociatedWindow is a window that is associated with another shell.
 */
public class AssociatedWindow extends Window {

	private Control owner;
	private ControlListener controlListener;
	
	/**
	 * Create a new instance of the receiver parented from parent and
	 * associated with the owning Composite.
	 * 
	 * @param parent
	 *            The shell this will be parented from
	 * @param associatedControl
	 *            The Composite that the position of this window will be
	 *            associated with.
	 */
	public AssociatedWindow(Shell parent, Control associatedControl) {
		super(parent);
		setShellStyle(SWT.NO_TRIM);
		owner = associatedControl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setTransparent(getTransparencyValue());
		this.associate(newShell);
		newShell.setBackground(newShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));
	}

	/**
	 * Return the value for the transparency for the receiver.
	 * @return int
	 */
	protected int getTransparencyValue() {
		return 70;
	}

	/**
	 * Move the shell based on the position of the owner.
	 * 
	 * @param shellToMove
	 */
	protected void moveShell(Shell shellToMove) {
			
		if(shellToMove.isDisposed())
			return;
		
		Point shellPosition = getParentShell().getLocation();
		Point itemLocation = owner.getLocation();
		itemLocation.x += shellPosition.x;
		itemLocation.y += shellPosition.y;
		
		
		Point size  =  shellToMove.getSize();
		
		Point windowLocation = new Point(itemLocation.x - size.x, itemLocation.y );
		
		shellToMove.setLocation(windowLocation);
		
	}

	/**
	 * Track the following controls location, by locating the receiver along
	 * the right hand side of the control. If the control moves the reciever
	 * should move along with it.
	 * 
	 * @param shell floatingShell
	 */
	private void associate(final Shell floatingShell) {

		controlListener = new ControlListener() {
			public void controlMoved(ControlEvent e) {
				moveShell(floatingShell);
			}

			public void controlResized(ControlEvent e) {
				moveShell(floatingShell);
			}
		};

		owner.addControlListener(controlListener);
		getParentShell().addControlListener(controlListener);

		//set initial location
		moveShell(floatingShell);

		//Add the floating shell to the tab list
		Control[] c = getParentShell().getTabList();
		Control[] newTab = new Control[c.length + 1];
		System.arraycopy(c, 0, newTab, 0, c.length);
		newTab[c.length] = owner;
		getParentShell().setTabList(newTab);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
	 */
	protected void handleShellCloseEvent() {
		super.handleShellCloseEvent();
		owner.removeControlListener(controlListener);
	}
}