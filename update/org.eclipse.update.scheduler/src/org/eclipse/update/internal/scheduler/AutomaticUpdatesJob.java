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
package org.eclipse.update.internal.scheduler;

import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.operations.UpdateUtils;
import org.eclipse.update.internal.search.UpdatesSearchCategory;
import org.eclipse.update.search.*;

public class AutomaticUpdatesJob
	extends Job
	implements IUpdateSearchResultCollector {
	private static final IStatus OK_STATUS =
		new Status(
			IStatus.OK,
			UpdateScheduler.getPluginId(),
			IStatus.OK,
			"",
			null);
	private UpdateSearchRequest searchRequest;
	private ArrayList updates;
	
	public AutomaticUpdatesJob() {
		updates = new ArrayList();
	}

	public void accept(IFeature match) {
		updates.add(match);
	}

	public IStatus run(IProgressMonitor monitor) {
		UpdateSearchScope scope = new UpdateSearchScope();
		scope.setUpdateMapURL(UpdateUtils.getUpdateMapURL());
		UpdatesSearchCategory category = new UpdatesSearchCategory();
		searchRequest = new UpdateSearchRequest(category, scope);
		searchRequest.addFilter(new EnvironmentFilter());
		try {
			searchRequest.performSearch(this, monitor);
			if (updates.size()>0) {
				// prompt the user
				asyncNotifyUser();
				return Job.ASYNC_FINISH;
			}
		} catch (CoreException e) {
			return e.getStatus();
		}
		return OK_STATUS;
	}
	
	private void asyncNotifyUser() {
		// ask the user to install updates
		// notify the manager that the job is done
		done(OK_STATUS);
	}
}