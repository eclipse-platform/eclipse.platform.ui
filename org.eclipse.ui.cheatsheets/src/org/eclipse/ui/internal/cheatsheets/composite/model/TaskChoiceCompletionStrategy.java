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

import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;

public class TaskChoiceCompletionStrategy implements TaskGroup.CompletionStrategy {

	/**
	 * Determine the state based on the state of the children, which is
	 * NOT_STARTED if all children are not started
	 * COMPLETED if one children is completed or skipped
	 * IN_PROGRESS otherwise
	 * @return
	 */
	public int computeState(TaskGroup taskGroup) {
		boolean noChildrenStarted = true;
		boolean atLeastOneChildCompleted = false;
		ICompositeCheatSheetTask[] children = taskGroup.getSubtasks();
		for (int i = 0; i < children.length; i++) {
			switch(children[i].getState()) {
			   case ICompositeCheatSheetTask.NOT_STARTED:
				   break;
			   case ICompositeCheatSheetTask.IN_PROGRESS:
				   noChildrenStarted = false;
				   break;
			   case ICompositeCheatSheetTask.SKIPPED:
			   case ICompositeCheatSheetTask.COMPLETED:
				   noChildrenStarted = false;
				   atLeastOneChildCompleted = true;
				   break;
			}
		}
		if (atLeastOneChildCompleted || children.length == 0) {
			return ICompositeCheatSheetTask.COMPLETED;
		}
		if (taskGroup.getState() == ICompositeCheatSheetTask.SKIPPED) {
			return ICompositeCheatSheetTask.SKIPPED;
		}
		if (noChildrenStarted) {
			return ICompositeCheatSheetTask.NOT_STARTED;
		}
		return ICompositeCheatSheetTask.IN_PROGRESS;
	}
}
