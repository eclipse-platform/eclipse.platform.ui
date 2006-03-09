/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.model;

import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.cheatsheets.ITaskGroup;

/**
 * This class contains utility functions to determine the state of a task based on
 * dependencies, parent state etc.
 */

public class TaskStateUtilities {
	
	/**
	 * Find the most recent ancestor of this task that is blocked
	 * @param task
	 * @return A blocked task or null if no ancestors are blocked
	 */
	public static ICompositeCheatSheetTask findBlockedAncestor(ICompositeCheatSheetTask task) {
		ITaskGroup parent = ((AbstractTask)task).getParent();
		if (parent == null) {
			return null;
		} 
        if (!parent.requiredTasksCompleted()) {
			return parent;
		}
		return findBlockedAncestor(parent);		
	}
	
	/**
	 * Find the most recent ancestor of this task that is skipped
	 * @param task
	 * @return A skipped task or null if no ancestors are skipped
	 */
	public static ICompositeCheatSheetTask findSkippedAncestor(ICompositeCheatSheetTask task) {
		ITaskGroup parent = ((AbstractTask)task).getParent();
		if (parent == null) {
			return null;
		} 
        if (parent.getState() == ICompositeCheatSheetTask.SKIPPED) {
			return parent;
		}
		return findSkippedAncestor(parent);			
	}
	
	/**
	 * Find the most recent ancestor of this task that is completed
	 * @param task
	 * @return A completed task or null if no ancestors are completed
	 */
	public static ICompositeCheatSheetTask findCompletedAncestor(ICompositeCheatSheetTask task) {
		ITaskGroup parent = ((AbstractTask)task).getParent();
		if (parent == null) {
			return null;
		} 
        if (parent.getState() == ICompositeCheatSheetTask.COMPLETED) {
			return parent;
		}
		return findCompletedAncestor(parent);		
	}

}
