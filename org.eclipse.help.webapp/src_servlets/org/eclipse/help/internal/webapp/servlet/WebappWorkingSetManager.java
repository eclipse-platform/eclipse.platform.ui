/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import javax.servlet.http.*;

import org.eclipse.help.internal.*;
import org.eclipse.help.internal.workingset.*;

/**
 * Proxy for WorkingSetManager or InfocenterWorkingSetManager.
 * @since 3.0
 */
public class WebappWorkingSetManager implements IHelpWorkingSetManager {
	IHelpWorkingSetManager wSetManager;
	// for keeping track if working set synchronized with working sets in UI
	private static boolean workingSetsSynchronized = false;

	/**
	 * Constructor
	 * @param locale
	 */
	public WebappWorkingSetManager(
		HttpServletRequest request,
		HttpServletResponse response,
		String locale) {
		if (HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER) {
			wSetManager =
				new InfocenterWorkingSetManager(request, response, locale);
		} else {
			wSetManager = HelpSystem.getWorkingSetManager(locale);
			if (!workingSetsSynchronized
				&& HelpSystem.getMode() == HelpSystem.MODE_WORKBENCH) {
				// upon startup in workbench mode, make sure working sets are in synch with those from UI
				workingSetsSynchronized = true;
				((WorkingSetManager)wSetManager).synchronizeWorkingSets();
			}
		}

	}

	public AdaptableTocsArray getRoot() {
		return wSetManager.getRoot();
	}
	/**
	 * Adds a new working set and saves it
	 */
	public void addWorkingSet(WorkingSet workingSet) {
		wSetManager.addWorkingSet(workingSet);
	}

	/**
	 * Creates a new working set
	 */
	public WorkingSet createWorkingSet(
		String name,
		AdaptableHelpResource[] elements) {
		return wSetManager.createWorkingSet(name, elements);
	}

	/**
	 * Returns a working set by name
	 * 
	 */
	public WorkingSet getWorkingSet(String name) {
		return wSetManager.getWorkingSet(name);
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#getWorkingSets()
	 */
	public WorkingSet[] getWorkingSets() {
		return wSetManager.getWorkingSets();
	}
	/**
	 * Removes specified working set
	 */
	public void removeWorkingSet(WorkingSet workingSet) {
		wSetManager.removeWorkingSet(workingSet);
	}

	/**
	 * Persists all working sets. Should only be called by the webapp working
	 * set dialog.
	 * 
	 * @param changedWorkingSet the working set that has changed
	 */
	public void workingSetChanged(WorkingSet changedWorkingSet) {
		wSetManager.workingSetChanged(changedWorkingSet);
	}

	public AdaptableToc getAdaptableToc(String href) {
		return wSetManager.getAdaptableToc(href);
	}

	public AdaptableTopic getAdaptableTopic(String id) {
		return wSetManager.getAdaptableTopic(id);
	}

	public String getCurrentWorkingSet() {
		return wSetManager.getCurrentWorkingSet();
	}

	public void setCurrentWorkingSet(String scope) {
		wSetManager.setCurrentWorkingSet(scope);
	}

}
