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
package org.eclipse.ui.preferences;

import org.eclipse.core.runtime.jobs.Job;

/**
 * IWorkbenchPreferenceContainer is the class that specifies
 * the workbench specific preferences support.
 * @since 3.1
 *
 */
public interface IWorkbenchPreferenceContainer {
	
	/**
	 * Open the page specified in the org.eclipse.ui.preferencePage
	 * extension point with id pageId. Apply data to it 
	 * when it is opened.
	 * @param preferencePageId String the id specified for a page in
	 *    the plugin.xml of its defining plug-in.
	 * @param data The data to be applied to the page when it 
	 * 		opens.
	 * @return boolean <code>true</code> if the page was
	 * opened successfully and data was applied.
	 */
	public boolean openPage(String preferencePageId, Object data);
	
	/**
	 * Get the working copy manager in use by this preference
	 * page container. This IWorkingCopyManager will have 
	 * IWorkingCopyManager#applyChanges()
	 * @return IWorkingCopyManager
	 */
	public IWorkingCopyManager getWorkingCopyManager();
	
	/**
	 * Register a job to be run after the container has been closed.
	 * @param job
	 */
	public void registerUpdateJob(Job job);

}
