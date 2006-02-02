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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;

/**
 * A task that represents a single cheatsheet within a composite cheatsheet.
 */

public class CheatSheetTask implements ICompositeCheatSheetTask {
	private CompositeCheatSheetModel model;
	private int state = NOT_STARTED;
	private String id;

	private String name;

	private String kind;

	private Dictionary parameters;

	private String description;
	
	private String completionMessage;

	private ArrayList subtasks;

	private ArrayList requiredTasks;
	
	private ArrayList successorTasks;
	
	private int percentageComplete;

	private static final ICompositeCheatSheetTask[] EMPTY = new ICompositeCheatSheetTask[0];

	public CheatSheetTask(CompositeCheatSheetModel model, String id, String name, String kind,
			Dictionary parameters, String description) {
		this.model = model;
		this.id = id;
		this.name = name;
		this.kind = kind;
		this.parameters = parameters;
		this.description = description;
		requiredTasks = new ArrayList();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getKind() {
		return kind;
	}

	public Dictionary getParameters() {
		return parameters;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public ICompositeCheatSheetTask[] getSubtasks() {
		if (subtasks==null) return EMPTY;
		return (ICompositeCheatSheetTask[])subtasks.toArray(new ICompositeCheatSheetTask[subtasks.size()]);
	}

	public ICompositeCheatSheetTask[] getRequiredTasks() {
		if (requiredTasks==null) return EMPTY;
		return (ICompositeCheatSheetTask[])requiredTasks.toArray(new ICompositeCheatSheetTask[requiredTasks.size()]);
	}

	public ICompositeCheatSheetTask[] getSuccessorTasks() {
		if (successorTasks==null) return EMPTY;
		return (ICompositeCheatSheetTask[])successorTasks.toArray(new ICompositeCheatSheetTask[successorTasks.size()]);
	}

	public int getPercentageComplete() {
		return percentageComplete;
	}
	
	public void addSubtask(ICompositeCheatSheetTask task) {
		if (subtasks==null)
			subtasks = new ArrayList();
		subtasks.add(task);
		
	}
	
	public void addRequiredTask(ICompositeCheatSheetTask task) {
		if (requiredTasks==null)
			requiredTasks = new ArrayList();
		requiredTasks.add(task);
	}
	
	public void addSuccessorTask(ICompositeCheatSheetTask task) {
		if (successorTasks==null)
			successorTasks = new ArrayList();
		successorTasks.add(task);
	}

	public void setPercentageComplete(int percentageComplete) {
		if (percentageComplete>=0 && percentageComplete<=100) {
		    this.percentageComplete = percentageComplete;
		    model.notifyStateChanged(this);
		}
	}

	public int getState() {
		return state;
	}
	
	public void advanceState() {
		if (state==NOT_STARTED)
			state = IN_PROGRESS;
		else if (state==IN_PROGRESS) {
			completeTask();
		}
		model.notifyStateChanged(this);
	}

	private void completeTask() {
		// Find out all successor tasks which were blocked
		List blockedTasks = new ArrayList();
		ICompositeCheatSheetTask[] successorTasks = getSuccessorTasks();
		for (int i = 0; i < successorTasks.length; i++) {
			if (!successorTasks[i].isStartable()) {
				blockedTasks.add(successorTasks[i]);
			}
		}
		state = COMPLETED;
		// Did any tasks get unblocked
		for (Iterator iter = blockedTasks.iterator(); iter.hasNext();) {
			ICompositeCheatSheetTask nextTask = (ICompositeCheatSheetTask)iter.next();
			if (nextTask.isStartable()) {
			    model.notifyStateChanged(nextTask);
			}
		}
	}

	public void setCompletionMessage(String completionMessage) {
		this.completionMessage = completionMessage;
	}

	public String getCompletionMessage() {
		return completionMessage;
	}
	
	public boolean isStartable() {
		boolean startable = true;
		ICompositeCheatSheetTask[] requiredTasks = getRequiredTasks();
		for (int i = 0; i < requiredTasks.length; i++) {
			if (requiredTasks[i].getState() != COMPLETED) {
				startable = false;
			}
		}
		return startable;
	}

	/**
	 * Interface used when restoring state from a file. 
	 * Not intended to be called from task editors.
	 * @param state
	 */
	public void setState(int state) {
		this.state = state;	
		model.notifyStateChanged(this);
	}

	public IPath getStateLocation() {
		if (model != null) {
			IPath statePath = model.getStateLocation().append(getId());
			File statePathAsFile = statePath.toFile();
			// Create directory if necessary
			if (!statePathAsFile.exists()) {
				statePathAsFile.mkdirs();
			}
			if (statePathAsFile.exists()) {
				return statePath;
			}
		}
		return null;
	}

	public URL getInputUrl(String path) throws MalformedURLException {
		return new URL(model.getContentUrl(), path);
	}

	public CompositeCheatSheetModel getModel() {
		return model;
	}
}
