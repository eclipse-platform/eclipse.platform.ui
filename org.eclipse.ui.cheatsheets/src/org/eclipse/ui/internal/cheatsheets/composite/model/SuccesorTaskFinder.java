/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

public class SuccesorTaskFinder {
	
	private CheatSheetTask csTask;
	ICompositeCheatSheetTask bestSuccessor;
	ICompositeCheatSheetTask bestPredecessor;
	private boolean seenThisTask;

	public SuccesorTaskFinder(ICompositeCheatSheetTask task) {
		csTask = (CheatSheetTask)task;
	}
	
	/**
	 * Find the next recommended task or tasks to be completed.
	 * Algorithm - visualize the tree as having its root at the top,
	 * children below and to the left of their parents and then
	 * search the tree from left to right. Look for 
	 * the best predecessor which is the first task to the
	 * left of this task that is runnable and the best successor 
	 * which is the first task to the
	 * right of this task which is runnable. 
	 * @param task The task which was just completed
	 * @return An array of tasks which can be started
	 */
    public ICompositeCheatSheetTask[] getRecommendedSuccessors() 
    {	
    	bestSuccessor = null;
    	bestPredecessor = null;
    	seenThisTask = false;
		searchRunnableChildren(csTask.getModel().getRootTask());
		// If there is a task which is found later in the tree return
		// that, otherwise an earlier task.
		if (bestSuccessor != null) {
			return new ICompositeCheatSheetTask[] {bestSuccessor};
		}
		if (bestPredecessor != null) {
			return new ICompositeCheatSheetTask[] {bestPredecessor};
		}
		return new ICompositeCheatSheetTask[0];
    }

	private void searchRunnableChildren(ICompositeCheatSheetTask task) {
		if (bestSuccessor != null) {
			return;
		}
		if (task == csTask) {
			seenThisTask = true;
		} 
			
		if (task.getKind() != null && task.isStartable() 
				&& task.getState() != ICompositeCheatSheetTask.COMPLETED) {
			if (seenThisTask) {
				if (bestSuccessor == null) {
					bestSuccessor = task;
				}
			} else {
				if (bestPredecessor == null) {
					bestPredecessor = task;
				}
			}
		}
		ICompositeCheatSheetTask[] subtasks = task.getSubtasks();
		for (int i = 0; i < subtasks.length; i++) {
			searchRunnableChildren(subtasks[i]);
		}	
	}
}
