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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The AssociatedWindow is a window that is associated with another shell.
 */
public class AssociatedWindow extends Window 
{

	static final int TRACK_RHS = 0;
	static final int TRACK_LHS = 1;
	static final int TRACK_TOP = 2;
	static final int TRACK_BOTTOM = 3;

	private Control owner;
	private ControlListener controlListener;
	private int trackStyle;
	private int marginWidth;
	private int marginHeight;
	private Point previousPoint;
	boolean initLocationFlag;

	/**
	 * Create a new instance of the receiver parented from parent and
	 * associated with the owning Composite, and initialize class variables.
	 * 
	 * @param parent
	 *            The shell this will be parented from
	 * @param associatedControl
	 *            The Composite that the position of this window will be
	 *            associated with.
	 */
	public AssociatedWindow(Shell parent, Control associatedControl)
	{
		super(parent);
		setShellStyle(SWT.NO_TRIM);
		owner = associatedControl;
		marginWidth = 5;
		marginHeight = 5;
		initLocationFlag = true;
		trackStyle = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		this.associate(newShell);
		newShell.setBackground(newShell.getDisplay().getSystemColor(SWT.COLOR_GREEN));
	}

	/**
	 * Move the shell based on the position of the owner.
	 * 
	 * @param shellToMove
	 * 			The shell to move.
	 */
	protected void moveShell(Shell shellToMove)
	{
		if (shellToMove.isDisposed())
			return;
		
		Rectangle displayRect = owner.getDisplay().getClientArea();
		Point location = getLocationRHS(shellToMove);
		Point shellSize = shellToMove.getSize();
		Point shellPosition = getParentShell().getLocation();

		if (initLocationFlag == true)
		{
			previousPoint = shellPosition;
			initLocationFlag = false;
			
		} 
		else
		{
			trackStyle = getHorizontalTrackStyle(displayRect);

			if (trackStyle == TRACK_RHS)
			{
				if (location.x + shellSize.x > displayRect.width)
					location.x = displayRect.width - shellSize.x - marginWidth;
			} 
			else if (trackStyle == TRACK_LHS)
			{
				if (location.x < displayRect.x)
					location.x = displayRect.x + marginWidth;
			}

			trackStyle = getVerticalTrackStyle(displayRect);
			
			if (trackStyle == TRACK_TOP)
			{
				if (location.y + shellSize.y > displayRect.height)	
					location.y = displayRect.height - shellSize.y - marginHeight;
			} 
			else if (trackStyle == TRACK_BOTTOM)
			{
				if (location.y < displayRect.y)	
					location.y = displayRect.y + marginHeight;
			}

			previousPoint = shellPosition;
			shellToMove.setLocation(location);
		}
	}

	/**
	 * Determine if the shell has moved horizontally.
	 * 
	 * @param shellPosition
	 * 			The position of the newly moved shell.
	 */
	private int getHorizontalTrackStyle(Rectangle shellPosition)
	{
		if (previousPoint.x == shellPosition.x)
			return -1;
		else if (previousPoint.x > shellPosition.x)
			return 0;
		else
			return 1;
	}

	/**
	 * Determine if the shell has moved vertically.
	 * 
	 * @param shellPosition
	 * 			The position of the newly moved shell.
	 */
	private int getVerticalTrackStyle(Rectangle shellPosition)
	{
		if (previousPoint.y == shellPosition.y)
			return -1;
		else if (previousPoint.y > shellPosition.y || getParentShell().getMaximized())
			return 2;
		else
			return 3;
	}

	/**
	 * Return the location of the window's right corner.
	 * 
	 * @param shellToMove
	 * 			The shell to be moved.
	 */
	private Point getLocationRHS(Shell shellToMove)
	{
		Point loc = owner.getDisplay().map(owner, null, 0, 0);
		Point size = owner.getSize();
		return new Point(loc.x + size.x + marginWidth, loc.y);
	}

	/**
	 * Track the following controls location, by locating the receiver along
	 * the right hand side of the control. If the control moves the receiver
	 * should move along with it.
	 * 
	 * @param shell floatingShell
	 * 					The floating window.
	 */
	private void associate(final Shell floatingShell)
	{

		controlListener = new ControlListener()
		{
			public void controlMoved(ControlEvent e)
			{
				moveShell(floatingShell);
			}

			public void controlResized(ControlEvent e)
			{
				moveShell(floatingShell);
			}
		};
		
		owner.addControlListener(controlListener);
		getParentShell().addControlListener(controlListener);

		moveShell(floatingShell);

		Control[] c = getParentShell().getTabList();
		Control[] newTab = new Control[c.length + 1];
		System.arraycopy(c, 0, newTab, 0, c.length);
		newTab[c.length] = floatingShell;
		getParentShell().setTabList(newTab);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
	 */
	protected void handleShellCloseEvent()
	{
		super.handleShellCloseEvent();
		getParentShell().removeControlListener(controlListener);

		owner.removeControlListener(controlListener);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#createContents()
	 */
	protected Control createContents(Composite parent)
	{
		return super.createContents(parent);
	}


}