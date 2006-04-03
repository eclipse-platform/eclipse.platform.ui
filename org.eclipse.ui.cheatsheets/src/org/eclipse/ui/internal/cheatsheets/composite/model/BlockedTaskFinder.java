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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;

public class BlockedTaskFinder {
	
	private Set stateChangedTasks;
	private Set impactedTasks;
	/**
	 * Find which tasks have either become blocked or unblocked so that they
	 * can be added to the list of change events.
	 * @param stateChangedTasks The set of tasks which has changed
	 * @return The set of tasks which have become blocked or unblocked by the
	 * change of state and were not in the original set. The algorithm will sometimes add tasks to 
	 * the result set which were not actually impacted but this is not a major problem 
	 * since it only means that extra events get sent to the explorer. For updates other
	 * than resets the number of extra events is very low.
	 * 
	 * This takes several steps.
	 * <li> If a  group is completed, skipped or reset add any non-started children. 
     * <li> Determine all successors of tasks whose state has changed that are not in the change set
     * <li> Add the successor and its children to the list if not started
	 */
	
	public Set findBlockedTaskChanges(Set stateChangedTasks) {
		this.stateChangedTasks = stateChangedTasks;
		impactedTasks = new HashSet();
		visitChangedTasks();
		findSuccesors();
		return impactedTasks;
	}

	private void visitChangedTasks() {
		for (Iterator iter = stateChangedTasks.iterator(); iter.hasNext(); ) {
			final ICompositeCheatSheetTask nextTask = (ICompositeCheatSheetTask)iter.next();
			if (nextTask.getState() != ICompositeCheatSheetTask.IN_PROGRESS) {
			    findUnstartedChildren(nextTask);
			}
		}	
	}

	/*
	 * Look for children which we have not seen elsewhere and if they are not started
	 * add them to the list of impacted tasks.
	 */
	private void findUnstartedChildren(ICompositeCheatSheetTask task) {
		ICompositeCheatSheetTask[] children = task.getSubtasks();	
		for (int i = 0; i < children.length; i++) {
			ICompositeCheatSheetTask nextChild = children[i];
			// Ignore if this task has been seen before
			if ((!stateChangedTasks.contains(nextChild)) && !impactedTasks.contains(nextChild)) {
			    if (nextChild.getState() == ICompositeCheatSheetTask.NOT_STARTED) {
				   impactedTasks.add(nextChild);
			    }
			    findUnstartedChildren(nextChild);
			}
		}
	}	

	private void findSuccesors() {
		for (Iterator iter = stateChangedTasks.iterator(); iter.hasNext(); ) {
			final AbstractTask nextTask = (AbstractTask)iter.next();
			ICompositeCheatSheetTask[] successors = nextTask.getSuccessorTasks();
			for (int i = 0; i < successors.length; i++) {
				ICompositeCheatSheetTask nextSuccessor = successors[i];
				if (nextSuccessor.getState() == ICompositeCheatSheetTask.NOT_STARTED) {
					impactedTasks.add(nextSuccessor);
				}
			    findUnstartedChildren(nextSuccessor);
			}
		}		
	}

}
