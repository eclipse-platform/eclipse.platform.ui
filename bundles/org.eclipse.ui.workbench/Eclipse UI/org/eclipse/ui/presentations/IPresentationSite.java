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
 * 
 * 
 * @since 3.0
 */
public interface IPresentationSite {
	public static int STATE_MINIMIZED = 0;
	public static int STATE_MAXIMIZED = 1;
	public static int STATE_RESTORED = 2;
	
	/**
	 * Sets the state of the container. Called by the skin when the
	 * user causes the the container to be minimized, maximized, etc.
	 * 
	 * @param newState
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
	 * Closes the given part
	 * 
	 * @param toClose
	 */
	public void close(IPresentablePart toClose);
	
	/**
	 * Begins dragging the entire stack of parts
	 * 
	 * @param keyboard true iff the drag was initiated via mouse dragging,
	 * and false if the drag may be using the keyboard	 
	 */
	public void dragStart(boolean keyboard);

	/**
	 * Returns true iff this site will allow the given part to be closed
	 * 
	 * @param toClose part to test
	 * @return true iff the part may be closed
	 */
	public boolean isClosable(IPresentablePart toClose);
	
	/**
	 * Returns true iff this site will allow the given part to be moved
	 *
	 * @param toMove part to test
	 * @return true iff the part may be moved
	 */
	public boolean isMovable(IPresentablePart toMove);
}
