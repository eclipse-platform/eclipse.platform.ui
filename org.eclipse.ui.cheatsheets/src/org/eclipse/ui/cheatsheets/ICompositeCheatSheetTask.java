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

package org.eclipse.ui.cheatsheets;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;

import org.eclipse.core.runtime.IPath;

/**
 * A task within a composite cheat sheet.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.2
 */

public interface ICompositeCheatSheetTask {
	int NOT_STARTED = 0;
	int IN_PROGRESS = 1;
	int COMPLETED = 2;
	/**
	 * @return the unique identifier of this task.
	 */
	public String getId();
	/**
	 * @return the translatable name of the task.
	 */
	public String getName();
	/**
	 * Returns the kind of the task editor. Tasks without
	 * editor kind are 'informational' tasks because they
	 * cannot be 'completed. 
	 * @return
	 */
	public String getKind();
	/**
	 * The task parameters are used to configure the
	 * task editor with data meaningful to an editor of this kind.
	 * @return the parameter names and values as specified in the
	 * composite cheatsheet content file.
	 */
	public Dictionary getParameters();	
	/**
	 * Returns the description of the task. 
	 * @return a plain String, or XML markup that can 
	 * be understood by FormText widget.
	 * @see org.eclipse.ui.forms.widgets.FormText
	 */
	public String getDescription();
	/**
	 * Returns an array of subtasks that are children of this task.
	 * The array will be empty if this is a leaf task.
	 * @return
	 */
	public ICompositeCheatSheetTask [] getSubtasks();
	/**
	 * get the tasks which are required to be completed 
	 * before this task is started. 
	 * @return an array of tasks that must be completed
	 * before this task can be started.  The array will be
	 * empty if this tasks is independent of other tasks.
	 */
	public ICompositeCheatSheetTask [] getRequiredTasks();
	
	/**
	 * Get the state of this task
	 * @return NOT_STARTED, IN_PROGRESS or COMPLETED.
	 */
	public int getState();
	
	/**
	 * Returns the percentage of this task that has been 
	 * completed. 
	 * @return A task that has not been
	 * started yet has '0' as result. A task that has been
	 * completed must return '100' as a result. Anything
	 * in between represents a task in various stages of
	 * completion.
	 */
	public int getPercentageComplete();
	
	/**
	 * Set the percentage of the task that has been completed
	 * @param percentageComplete an integer between 0 and 100
	 */
	public void setPercentageComplete(int percentageComplete);
	
	/**
	 * Advance the state of this task
	 */
	public void advanceState();
	
	/**
	 * Gets the text to be displayed when this task is completed
	 * @return a plain String, or XML markup that can 
	 * be understood by FormText widget.
	 * @see org.eclipse.ui.forms.widgets.FormText
	 */
	public String getCompletionMessage();
	
	/**
	 * Determine whether this task can be started.
	 * @return true unless this task depends on a required task that has
	 * not been completed.
	 */
	public boolean isStartable();
	
	/**
	 * Gets a location where the state for this task can be saved.
	 * @return the path of a writeable directory on file system where this 
	 * task can save its state. This will always be a subdirectory of the 
	 * directory in which the state is stored for the parent composite cheat sheet.
	 */
	public IPath getStateLocation();
	
	/**
	 * Gets a URL which can be used to open the content file for this 
	 * task if the content file can be specified by a path relative to
	 * the content file for the composite cheat sheet which contains it.
	 * @param path a relative path
	 * @throws MalformedURLException 
	 * @return a URL which represents a location relative to the
	 * location of the content file for the composite cheat sheet.
	 */
	public URL getInputUrl(String path) throws MalformedURLException;
}
