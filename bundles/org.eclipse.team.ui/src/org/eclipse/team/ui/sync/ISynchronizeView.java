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
package org.eclipse.team.ui.sync;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.ui.IWorkingSet;

public interface ISynchronizeView {

	 /**
	  * View type constant (value 0) indicating that the synchronize view will be shown
	  * as a tree.
	  */
	public static final int TREE_VIEW = 0;
	
	/**
	 * View type constant (value 1) indicating that the synchronize view will be shown
	 * as a table.
	 */
	public static final int TABLE_VIEW = 1;

	/**
	 * Called to set the current selection in the sync viewer for the given subscriber. The viewType 
	 * parameter determines in which configuration of the view the selection should be made in. If
	 * there are no resources passed in the given subscriber is activate.
	 * 
	 * @param resources the resources to select
	 * @param viewType the view configuration in which to select the resources
	 */
	public void selectSubscriber(TeamSubscriber subscriber);
	
	/**
	 * Refreshes the resources from the specified subscriber. The working set or filters applied
	 * to the synchronize view are ignored. 
	 */
	public void refreshWithRemote(TeamSubscriber subscriber, IResource[] resources);

	/**
	 * Refreshes the resources in the current input for the given subscriber.
	 */	
	public void refreshWithRemote(TeamSubscriber subscriber);
	
	/**
	 * Refreshes the active subscriber.
	 */	
	public void refreshWithRemote();
	
	/**
	 * Returns the current view configuration.
	 */
	public int getCurrentViewType();

	/**
	 * Sets the working set used by the view to the given working set
	 */
	public void setWorkingSet(IWorkingSet workingSet);
}
