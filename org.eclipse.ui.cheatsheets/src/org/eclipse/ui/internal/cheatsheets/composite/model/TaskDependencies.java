/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.composite.parser.IStatusContainer;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;

/**
 * Class to keep track of dependencies between tasks
 */

public class TaskDependencies {
	
	private class Dependency {
		private AbstractTask sourceTask;
		
		private String requiredTaskId;
		
		public Dependency(AbstractTask sourceTask, String requiredTaskId) {
			this.sourceTask = sourceTask;
			this.requiredTaskId = requiredTaskId;
		}
		
		public AbstractTask getSourceTask() {
			return sourceTask;
		}
		
		public String getRequiredTaskId() {
			return requiredTaskId;
		}
	}
	
	private List dependencies;
	
	private Map taskIdMap = new HashMap();

	public void saveId(AbstractTask task) {
		String id = task.getId();
		if (id != null) {
			taskIdMap.put(id, task);
		}		
	}
	
	public AbstractTask getTask(String id) {
		return (AbstractTask)taskIdMap.get(id);
	}
	
	public TaskDependencies() {
		dependencies = new ArrayList();
	}
	
	/**
	 * Register a dependency between tasks
	 * @param sourceTask a task which cannot be started until another task is completed
	 * @param requiredTaskId the id of the task which must be completed first
	 */
	public void addDependency(AbstractTask sourceTask, String requiredTaskId) {
		dependencies.add(new Dependency(sourceTask, requiredTaskId));
	}
	
	/**
	 * Resolve all of the dependencies updating the individual tasks
	 * @param model The composite cheat sheet
	 * @param status An object used to add error status
	 */
	public void resolveDependencies(IStatusContainer status) {
		for (Iterator dependencyIterator = dependencies.iterator(); dependencyIterator.hasNext();) {
			 Dependency dep = (Dependency)dependencyIterator.next();
			 AbstractTask sourceTask = dep.getSourceTask();
			 AbstractTask requiredTask = getTask(dep.requiredTaskId);
			 if (requiredTask == null) {
					String message = NLS.bind(Messages.ERROR_PARSING_INVALID_ID, (new Object[] {dep.getRequiredTaskId()}));	
					status.addStatus(IStatus.ERROR, message, null);
			 } else if (!sourceTask.requiresTask(requiredTask)) {
				 sourceTask.addRequiredTask(requiredTask);
			 }
		}
		checkForCircularities (status);
	}

	/**
	 * Check for circular dependencies using the following algorithm.
	 * 1. Create a set of all the tasks which have an id (tasks without id cannot be in a cycle).;
	 * 2. Remove from the set any tasks which depend on no other task, these cannot be part of a cycle
	 * 3. Remove from the set any tasks which only depend on tasks already removed, these cannot be
	 * part of a cycle.
	 * 4. Repeat step 3 until not further tasks can be removed
	 * 5. Any tasks remaining are part of a cycle or depend on a task in a cycle
	 * @param model
	 * @param status
	 */
	private void checkForCircularities (IStatusContainer status) {
		Set tasks = new HashSet();
		// Combine steps 1 + 2
		for (Iterator idIterator = taskIdMap.values().iterator(); idIterator.hasNext(); ) {
			AbstractTask nextTask = (AbstractTask)idIterator.next();
			if (nextTask.getRequiredTasks().length > 0) {
				tasks.add(nextTask);
			}
		}
		boolean makingProgress = true;
		while (makingProgress) {
			// Use a new set to store the tasks which are still cycle candidates to avoid
			// iterating over and deleting from the same set.
			Set remainingTasks = new HashSet();
			makingProgress = false;
			for (Iterator taskIterator = tasks.iterator(); taskIterator.hasNext() && !makingProgress; ) {
				boolean mayBeInCycle = false;
				ICompositeCheatSheetTask nextTask = (ICompositeCheatSheetTask)taskIterator.next();
				ICompositeCheatSheetTask[] requiredTasks = nextTask.getRequiredTasks();
				for (int i = 0; i < requiredTasks.length; i++) {
					if (tasks.contains(requiredTasks[i])) {
						mayBeInCycle = true;
					}
				}
				if (mayBeInCycle) {
					remainingTasks.add(nextTask);
				} else {
					makingProgress = true;
				}				
			}
			tasks = remainingTasks;
		}
		if (!tasks.isEmpty()) {
			status.addStatus(IStatus.ERROR, Messages.ERROR_PARSING_CYCLE_DETECTED, null);
			// Detect one of the cycles and report its members
			List cycle = new ArrayList();
			ICompositeCheatSheetTask cycleStartTask = (ICompositeCheatSheetTask)tasks.iterator().next();
			while (!cycle.contains(cycleStartTask)) {
				cycle.add(cycleStartTask);ICompositeCheatSheetTask[] requiredTasks = cycleStartTask.getRequiredTasks();
				for (int i = 0; i < requiredTasks.length; i++) {
					if (tasks.contains(requiredTasks[i])) {
						cycleStartTask=requiredTasks[i];
					}
				}
			}
			// Now the list contains a cycle and possibly additional tasks at the start
			// of the list
			boolean cycleStarted = false;
			String thisTask = null;
			String lastTask = null;
			String firstTask = null;
			for (Iterator cycleIterator = cycle.iterator(); cycleIterator.hasNext();) {
				ICompositeCheatSheetTask task = (ICompositeCheatSheetTask)cycleIterator.next();
				if (task == cycleStartTask) {
					cycleStarted = true;
					firstTask = task.getName();
				}
				if (cycleStarted) {
					// Save the name of this task
					lastTask = thisTask;
					thisTask = task.getName();
					if (lastTask != null) {
					    String message = NLS.bind(Messages.ERROR_PARSING_CYCLE_CONTAINS, (new Object[] {lastTask, thisTask}));	
					    status.addStatus(IStatus.ERROR, message, null);
					}
				}
			}
			String message = NLS.bind(Messages.ERROR_PARSING_CYCLE_CONTAINS, (new Object[] {thisTask, firstTask}));	
		    status.addStatus(IStatus.ERROR, message, null);
		}
	}

}
