/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.views;

public interface INavigableControl {

	/**
	 * Direction to naviate
	 */
	final public static int NEXT = 1;
	final public static int PREVIOUS = 2;
	
	/**
	 * Returns true if at end or beginning.
	 */
	boolean gotoDifference(int direction);
	
	/**
	 * Preserve the selection for the given direction
	 * filter.
	 * @param the direction to preserve
	 */
	void preserveState(int direction);
	
	/**
	 * Restore the selection and expansion for the 
	 * given direction.
	 * @param direction the direction to restore
	 */
	void restoreState(int direction);
}
