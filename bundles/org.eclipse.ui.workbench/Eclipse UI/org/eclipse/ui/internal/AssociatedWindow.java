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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.window.Window;

/**
 * The AssociatedWindow is a window that is associated with another shell.
 *
 * @since 3.0
 */
public class AssociatedWindow extends Window {

	/*
	 * Track the outer top right hand side of the owner, keeping this
	 * window on the screen so long as the owner is also visible.
	 */
	static final int TRACK_OUTER_TOP_RHS = 0;
	static final int TRACK_INNER_TOP_RHS = 1;
	static final int TRACK_INNER_BOTTOM_RHS = 2;

	private Control owner;
	private ControlListener controlListener;
	private int trackStyle;
	// if the receiver has had to assume a different trackStyle the current may be different
	// then the requested style.
	int currentTrackStyle;
	private int marginWidth;

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
		this(parent, associatedControl, TRACK_OUTER_TOP_RHS, 0);
	}

	/**
	 * Create a new instance of the receiver parented from parent and
	 * associated with the owning Composite.
	 * 
	 * @param parent
	 *            The shell this will be parented from
	 * @param associatedControl
	 *            The Composite that the position of this window will be
	 *            associated with.
	 * @param trackStyle
	 *            The behaviour to use while tracking the associatedControl
	 */
	public AssociatedWindow(
		Shell parent,
		Control associatedControl,
		int trackStyle,
		int marginWidth) {
		super(parent);
		setShellStyle(SWT.NO_TRIM);
		owner = associatedControl;
		this.trackStyle = trackStyle;
		this.marginWidth = marginWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		//newShell.setTransparent(getTransparencyValue());
		this.associate(newShell);
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

		if (shellToMove == null || shellToMove.isDisposed())
			return;
		if (trackStyle == TRACK_OUTER_TOP_RHS) {
			Point loc = getLocationOuterTopRHS(shellToMove);

			Rectangle displayRect = owner.getDisplay().getBounds();
			Point shellSize = shellToMove.getSize();

			// off the screen RHS, switch to inner right hand side
			if (loc.x + shellSize.x > displayRect.width) {
				loc = getLocationInnerTopRHS(shellToMove);
				// since top rhs is being used as a backup, we do not waste the 
				// margin space in that case
				loc.x += marginWidth;
				currentTrackStyle = TRACK_INNER_TOP_RHS;
			} else
				currentTrackStyle = TRACK_OUTER_TOP_RHS;
			setShellRegion(currentTrackStyle);
			shellToMove.setLocation(loc);
			return;
		}

		Point shellPosition = getParentShell().getLocation();
		Point itemLocation = owner.getLocation();
		itemLocation.x += shellPosition.x;
		itemLocation.y += shellPosition.y;

		Point size = shellToMove.getSize();

		Point windowLocation = new Point(itemLocation.x - size.x, itemLocation.y);

		shellToMove.setLocation(windowLocation);
	}

	/**
	 * Allow subclasses to update the region based on the location that the shell will be 
	 * drawn
	 * 
	 * @param currentTrackStyle2
	 */
	protected void setShellRegion(int trackStyle) {
		// do nothing in base class

	}

	/**
	 * Answer the location to position the receiver relative to the inner
	 * top right hand side of the owner
	 * @param shellToMove
	 * @return
	 */
	private Point getLocationInnerTopRHS(Shell shellToMove) {
		Point loc = owner.getDisplay().map(owner, null, 0, 0);
		Point ownerSize = owner.getSize();
		Point size = shellToMove.getSize();
		return new Point(loc.x + ownerSize.x - size.x - marginWidth, loc.y);
	}

	/**
	 * Answer the location to position the receiver relative to the outer
	 * top right hand side of the owner
	 * @param shellToMove
	 * @return
	 */
	private Point getLocationOuterTopRHS(Shell shellToMove) {
		Point loc = owner.getDisplay().map(owner, null, 0, 0);
		Point size = owner.getSize();
		return new Point(loc.x + size.x + marginWidth, loc.y);
	}

	/**
	 * Track the following controls location, by locating the receiver along
	 * the right hand side of the control. If the control moves the reciever
	 * should move along with it.
	 * 
	 * @param shell floatingShell
	 */
	private void associate(Shell floatingShell) {

		controlListener = new ControlListener() {
			public void controlMoved(ControlEvent e) {
				moveShell(getShell());
			}

			public void controlResized(ControlEvent e) {
				moveShell(getShell());
			}
		};

		owner.addControlListener(controlListener);
		getParentShell().addControlListener(controlListener);

		DisposeListener dl = new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {

				Shell shell = getShell();
				if (shell == null || shell.isDisposed())
					return;

				shell.removeDisposeListener(this);
				shell.removeControlListener(controlListener);

				if (e.widget.isDisposed()) {
					owner = null;
					shell.dispose();
				}
			}
		};
		owner.addDisposeListener(dl);

		//set initial location
		moveShell(floatingShell);

		//Add the floating shell to the tab list
		Control[] c = getParentShell().getTabList();
		Control[] newTab = new Control[c.length + 1];
		System.arraycopy(c, 0, newTab, 0, c.length);
		newTab[c.length] = floatingShell;
		getParentShell().setTabList(newTab);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
	 */
	protected void handleShellCloseEvent() {
		super.handleShellCloseEvent();
		owner.removeControlListener(controlListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		if (getShell() == null) {
			// create the window
			create();
		}
		// limit the shell size to the display size
		constrainShellSize();
		// open the window
		getShell().setVisible(true);
		return getReturnCode();
	}

}