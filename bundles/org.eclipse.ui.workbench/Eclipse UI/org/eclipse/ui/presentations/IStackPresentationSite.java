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
package org.eclipse.ui.presentations;

import org.eclipse.swt.graphics.Point;


/**
 * Represents the main interface between an StackPresentation and the workbench.
 * 
 * Not intended to be implemented by clients.
 * 
 * @since 3.0
 */
public interface IStackPresentationSite {
	public static int STATE_MINIMIZED = 0;
	public static int STATE_MAXIMIZED = 1;
	public static int STATE_RESTORED = 2;

	/**
	 * Sets the state of the container. Called by the presentation when the
	 * user causes the the container to be minimized, maximized, etc.
	 * 
	 * @param newState one of the STATE_* constants
	 */
	public void setState(int newState);
	
	/**
	 * Returns the current state of the site (one of the STATE_* constants)
	 * 
	 * @return the current state of the site (one of the STATE_* constants)
	 */
	public int getState();
	
	/**
	 * Begins dragging the given part
	 * 
	 * @param beingDragged
	 * @param keyboard true iff the drag was initiated via mouse dragging,
	 * and false if the drag may be using the keyboard
	 */
	public void dragStart(IPresentablePart beingDragged, Point initialPosition, boolean keyboard);
	
	/**
	 * Closes the given part.
	 * 
	 * @param toClose the part to close
	 */
	public void close(IPresentablePart toClose);
	
	/**
	 * Begins dragging the entire stack of parts
	 * 
	 * @param keyboard true iff the drag was initiated via mouse dragging,
	 * and false if the drag may be using the keyboard	 
	 */
	public void dragStart(Point initialPosition, boolean keyboard);

	/**
	 * Returns true iff this site will allow the given part to be closed
	 * 
	 * @param toClose part to test
	 * @return true iff the part may be closed
	 */
	public boolean isCloseable(IPresentablePart toClose);
	
	/**
	 * Returns true iff this site will allow the given part to be moved.
	 * If the argument is null, this returns whether dragging should
	 * be enabled for the entire stack
	 *
	 * @param toMove part to test, or null if we're testing the entire stack
	 * @return true iff the part may be moved
	 */
	public boolean isMoveable(IPresentablePart toMove);
	
	/**
	 * Makes the given part active
	 * 
	 * @param toSelect
	 */
	public void selectPart(IPresentablePart toSelect);
}
