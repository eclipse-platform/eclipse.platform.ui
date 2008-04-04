/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * A local working set manager can be used to manage a set of
 * working sets independently from the working sets managed by 
 * the global working set manager.  A local working set manager 
 * can be saved and restored using the methods <code>saveState</code>  
 * and <code>restoreState</code>.  A new local working set manager can be created  
 * using {@link org.eclipse.ui.IWorkbench#createLocalWorkingSetManager()}.
 * Clients of local working set managers are responsible for calling 
 * {@link IWorkingSetManager#dispose()} when the working sets it manages 
 * are no longer needed.
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 *
 * @see org.eclipse.ui.IWorkbench#createLocalWorkingSetManager()
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
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
