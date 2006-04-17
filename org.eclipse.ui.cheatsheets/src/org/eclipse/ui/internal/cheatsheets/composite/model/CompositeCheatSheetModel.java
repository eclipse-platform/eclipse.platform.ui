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

import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.internal.cheatsheets.data.ICheatSheet;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetManager;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheet;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;

public class CompositeCheatSheetModel extends Observable implements ICompositeCheatSheet, ICheatSheet{

	private String name;
	private String description;
	private String explorerId;
	private ICompositeCheatSheetTask rootTask;
	private TaskDependencies dependencies;
	private String id;
	private CompositeCheatSheetSaveHelper saveHelper;
	private URL contentURL;
	private CheatSheetManager manager;
	private Set stateChangedSet = new HashSet();
	
	public void setRootTask(ICompositeCheatSheetTask task) {
		rootTask = task;
	}
	
	public CompositeCheatSheetModel(String name, String description, String explorerId) {
		this.name = name;
	    this.description = description;
	    this.explorerId = explorerId;
	    this.dependencies = new TaskDependencies();
	}
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getTaskExplorerId() {
		return explorerId;
	}
	
	public ICompositeCheatSheetTask getRootTask() {
		return rootTask;
	}

	public void setDependencies(TaskDependencies dependencies) {
		this.dependencies = dependencies;
	}

	public TaskDependencies getDependencies() {
		return dependencies;
	}
	
	public URL getContentUrl() {
		return contentURL;
	}

	public void setContentUrl(URL newUrl) {
		contentURL=newUrl;		
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setSaveHelper(CompositeCheatSheetSaveHelper saveHelper) {
		this.saveHelper = saveHelper;
	}
	
	/**
	 * Maintain a set of tasks which have been changed which will
	 * be used to send events to observers.
	 * @param task
	 */
	void stateChanged(ICompositeCheatSheetTask task) {
		if (!stateChangedSet.contains(task)) {
			stateChangedSet.add(task);
		}
	}
	
	/**
	 * At this point we need to determine which blocked tasks  have 
	 * become unblocked and which unblocked tasks are now blocked and
	 * send events for those also.
	 */
	public void sendTaskChangeEvents() {
		Set blockedStateChanged = new BlockedTaskFinder().findBlockedTaskChanges(stateChangedSet);
		for (Iterator iter = stateChangedSet.iterator(); iter.hasNext();) {
			setChanged();
			notifyObservers(iter.next());
		}
		for (Iterator iter = blockedStateChanged.iterator(); iter.hasNext();) {
			setChanged();
			notifyObservers(iter.next());
		}
		stateChangedSet.clear();
	}
	
	public IMemento getTaskMemento(String id) {
		 return saveHelper.getTaskMemento(id);
	}

	public ICheatSheetManager getCheatSheetManager() {
		return manager;
	}
	
	public void setCheatSheetManager(CheatSheetManager manager) {
		this.manager = manager;	
	}

	public void loadState(Map layoutData) {
		saveHelper.loadCompositeState(this, layoutData);	
	}
	
	/*
	 * Reset the state of a task and it's children
	 */
	private void resetTask(ICompositeCheatSheetTask task) {
		if (task instanceof EditableTask) {
		    EditableTask editable = (EditableTask)task;
			editable.reset();
			if (saveHelper != null) {
			    saveHelper.clearTaskMemento(task.getId());
			}
		} else if (task instanceof TaskGroup) { 
			TaskGroup group = (TaskGroup)task;
		    ICompositeCheatSheetTask[] subtasks = group.getSubtasks();
		    for (int i = 0; i < subtasks.length; i++) {
			    resetTask(subtasks[i]);
		    }
		   group.setStateNoNotify(ICompositeCheatSheetTask.NOT_STARTED);
		}
	}

	public void resetAllTasks(Map cheatSheetData) {
        if (manager != null) {
    		if (cheatSheetData == null) {
            	manager.setData(new Hashtable());
    		} else {
    			manager.setData(cheatSheetData);
    		}
        }
        saveHelper.clearTaskMementos();	
	    resetTask(getRootTask());
	    sendTaskChangeEvents();
	}

	/**
	 * Restart one or more tasks
	 * @param restartTasks An array of the tasks to be restarted
	 */
	public void resetTasks(ICompositeCheatSheetTask[] restartTasks) {
		for (int i = 0; i < restartTasks.length; i++) {
			resetTask(restartTasks[i]);
		}	
		sendTaskChangeEvents();
	}

}
