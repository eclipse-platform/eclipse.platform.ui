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

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.ICheatSheet;


public class CompositeCheatSheetModel extends Observable implements ICompositeCheatSheet, ICheatSheet{

	private String name;
	private String description;
	private String explorerId;
	private ICompositeCheatSheetTask rootTask;
	private TaskDependencies dependencies;
	private String id;
	private CompositeCheatSheetSaveHelper saveHelper;
	private URL contentURL;
	
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
	
	public IPath getStateLocation() {
		return saveHelper.getSavePath();
	}

	public void setSaveHelper(CompositeCheatSheetSaveHelper saveHelper) {
		this.saveHelper = saveHelper;
	}
	
	void notifyStateChanged(ICompositeCheatSheetTask task) {
		setChanged();
		notifyObservers(task);
	}

}
