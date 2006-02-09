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

import java.net.URL;
import java.util.Observable;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheet;
import org.eclipse.ui.cheatsheets.ITaskEditor;
import org.eclipse.ui.internal.cheatsheets.data.ICheatSheet;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetManager;


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
	
	public void setRootTask(ICompositeCheatSheetTask task) {
		rootTask = task;
	}
	
	public CompositeCheatSheetModel(String name, String description, String explorerId) {
		this.name = name;
	    this.description = description;
	    this.explorerId = explorerId;
	    this.dependencies = new TaskDependencies();
	    // TODO initialize the CheatSheetManager
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
	
	void notifyStateChanged(ICompositeCheatSheetTask task) {
		setChanged();
		notifyObservers(task);
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

	public void loadState() {
		saveHelper.loadCompositeState(this);	
	}
	
	/*
	 * Reset the state of a task and it's children
	 */
	private void resetTask(ICompositeCheatSheetTask task) {
		CheatSheetTask csTask = (CheatSheetTask)task;
		csTask.setState(0);
		csTask.setPercentageComplete(0);
		ITaskEditor editor = csTask.getEditor();
	    if (editor != null) {
	    	editor.setInput(task, null);
	    }
		ICompositeCheatSheetTask[] subtasks = csTask.getSubtasks();
		for (int i = 0; i < subtasks.length; i++) {
			resetTask(subtasks[i]);
		}
	}

	public void resetAllTasks() {
	    resetTask(getRootTask());
        saveHelper.clearTaskMementos();
		
	}

}
