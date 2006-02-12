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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.cheatsheets.ICompositeCheatSheet;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ITaskParseStrategy;

/**
 * A single task within a composite cheatsheet. This class encapsulates the
 * behavior common to editable tasks and taskGroups
 */

public abstract class AbstractTask implements ICompositeCheatSheetTask {
	protected CompositeCheatSheetModel model;
	protected int state = NOT_STARTED;
	private String id;

	private String name;

	protected String kind;

	private Dictionary parameters;
	
	private String description;
		
	private String completionMessage;

	private ArrayList requiredTasks;
	
	private ArrayList successorTasks;
	
	private boolean skippable;
	
	private TaskGroup parent;

	protected static final ICompositeCheatSheetTask[] EMPTY = new ICompositeCheatSheetTask[0];

	public AbstractTask(CompositeCheatSheetModel model, String id, String name, String kind) {
		this.model = model;
		this.id = id;
		this.name = name;
		this.kind = kind;
		this.parameters = new Hashtable();
		this.description = name;
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
	
	public void setCompletionMessage(String completionMessage) {
		this.completionMessage = completionMessage;
	}

	public String getCompletionMessage() {
		return completionMessage;
	}

	public ICompositeCheatSheetTask[] getRequiredTasks() {
		if (requiredTasks==null) return EMPTY;
		return (ICompositeCheatSheetTask[])requiredTasks.toArray(new ICompositeCheatSheetTask[requiredTasks.size()]);
	}

	public ICompositeCheatSheetTask[] getSuccessorTasks() {
		if (successorTasks==null) return EMPTY;
		return (ICompositeCheatSheetTask[])successorTasks.toArray(new ICompositeCheatSheetTask[successorTasks.size()]);
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

	public int getState() {
		return state;
	}

	public void complete() {
		// Find out all successor tasks which were blocked
		List blockedTasks = new ArrayList();
		ICompositeCheatSheetTask[] successorTasks = getSuccessorTasks();
		for (int i = 0; i < successorTasks.length; i++) {
			if (!successorTasks[i].requiredTasksCompleted()) {
				blockedTasks.add(successorTasks[i]);
			}
		}
		// Did any tasks get unblocked
		for (Iterator iter = blockedTasks.iterator(); iter.hasNext();) {
			ICompositeCheatSheetTask nextTask = (ICompositeCheatSheetTask)iter.next();
			if (nextTask.requiredTasksCompleted()) {
			    model.notifyStateChanged(nextTask);
			}
		}
		setState(COMPLETED);
	}

	public boolean requiredTasksCompleted() {
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
		if (parent != null) {
		    parent.checkState();
		}
	}

	public URL getInputUrl(String path) throws MalformedURLException {
		return new URL(model.getContentUrl(), path);
	}

	public ICompositeCheatSheet getCompositeCheatSheet() {
		return model;
	}
	
	public abstract ITaskParseStrategy getParserStrategy();

	public abstract ICompositeCheatSheetTask[] getSubtasks();

	public void setSkippable(boolean skippable) {
		this.skippable = skippable;
	}

	public boolean isSkippable() {
		return skippable;
	}

	protected void setParent(TaskGroup parent) {
		this.parent = parent;
	}

	public TaskGroup getParent() {
		return parent;
	}

}
