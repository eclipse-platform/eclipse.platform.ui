/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ITaskParseStrategy;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheet;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.provisional.cheatsheets.ITaskGroup;
import org.osgi.framework.Bundle;

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
		this.description = ""; //$NON-NLS-1$
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
	
	public void addRequiredTask(AbstractTask task) {
		if (requiredTasks==null)
			requiredTasks = new ArrayList();
		requiredTasks.add(task);
		if (task.successorTasks==null)
			task.successorTasks = new ArrayList();
		task.successorTasks.add(this);
	}

	public int getState() {
		return state;
	}

	public void complete() { 
		setState(COMPLETED);
	}

	public boolean requiredTasksCompleted() {
		boolean startable = true;
		ICompositeCheatSheetTask[] requiredTasks = getRequiredTasks();
		for (int i = 0; i < requiredTasks.length; i++) {
			if (requiredTasks[i].getState() != COMPLETED &&
				requiredTasks[i].getState() != SKIPPED	) {
				startable = false;
			}
		}
		return startable;
	}
	
	/**
	 * Determine whether the candidate task is a required task for this task.
	 * This function does not test for indirectly required tasks
	 * @param candidateTask a task which may be a required task
	 * @return true if candidateTask is in the list of required tasks.
	 */
	public boolean requiresTask(ICompositeCheatSheetTask candidateTask) {
		return (requiredTasks.contains(candidateTask));
	}

	/**
	 * Interface used when restoring state from a file. 
	 * Not intended to be called from task editors.
	 * @param state
	 */
	public void setState(int state) {
	    setStateNoNotify(state);
		model.sendTaskChangeEvents();
	}
	
	/**
	 * Set the state of a task but don't send out any events yet,
	 * let them collect so we don't send out multiple events for 
	 * one task
	 * @param state
	 */
	public void setStateNoNotify(int state) {
		this.state = state;	
		if (parent != null) {
		    parent.checkState();
		}
		model.stateChanged(this);
	}

	/*
	 * Resolves the given path to a URL. The path can either be fully qualified with
	 * the plugin id, e.g. "/plugin_id/path/file.xml" or relative to the composite cheat
	 * sheet file, e.g. "tasks/task1.xml".
	 */
	public URL getInputUrl(String path) throws MalformedURLException {
		int index = path.indexOf('/', 1);
		if (index >= 1 && path.charAt(0) == '/') {
			String bundleName = path.substring(1, index);
			String relativePath = path.substring(index + 1);
			Bundle bundle = Platform.getBundle(bundleName);
			if (bundle != null) {
				return FileLocator.find(bundle, new Path(relativePath), null);
			}
		}
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

	public ITaskGroup getParent() {
		return parent;
	}
	
	public int hashCode() {
		return getId().hashCode();
	}

}
