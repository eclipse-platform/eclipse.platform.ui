/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * A local working set manager can be used to manage a set of
 * working sets.
 * <p>
 * API under construction and subject to change at any time.
 * </p>
 *
 * @since 3.1
 */
public interface ILocalWorkingSetManager extends IWorkingSetManager {

	/**
	 * Saves the state of the working set manager to the given
	 * memento. 
	 * 
	 * @param memento the memento to save the state to
	 */
	public void saveState(IMemento memento);
	
	/**
	 * Restores the state of the working set manager from the given
	 * memento. The method can only be called as long as the working
	 * set manager is still empty.
	 * 
	 * @param memento the memento to restore the state from
	 */
	public void restoreState(IMemento memento);
}
