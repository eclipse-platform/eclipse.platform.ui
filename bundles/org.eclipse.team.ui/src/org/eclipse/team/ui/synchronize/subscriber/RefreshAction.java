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
package org.eclipse.team.ui.synchronize.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.jobs.RefreshSubscriberJob;
import org.eclipse.team.internal.ui.synchronize.IRefreshSubscriberListener;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * A general refresh action that will refresh a subscriber in the background.
 */
public class RefreshAction extends Action {
	
	private ISelectionProvider selectionProvider;
	private boolean refreshAll;
	private SubscriberSyncInfoCollector collector;
	private IRefreshSubscriberListener listener;
	private String description;
	private IWorkbenchSite workbenchSite;
	
	public RefreshAction(ISelectionProvider page, String description, SubscriberSyncInfoCollector collector, IRefreshSubscriberListener listener, boolean refreshAll) {
		this.selectionProvider = page;
		this.description = description;
		this.collector = collector;
		this.listener = listener;
		this.refreshAll = refreshAll;
		Utils.initAction(this, "action.refreshWithRemote."); //$NON-NLS-1$
	}
	
	public void run() {
		ISelection selection = selectionProvider.getSelection();
		if(selection instanceof IStructuredSelection) {
			IResource[] resources = Utils.getResources(((IStructuredSelection)selection).toArray());
			if (refreshAll || resources.length == 0) {
				// If no resources are selected, refresh all the subscriber roots
				resources = collector.getRoots();
			}
			run(getWorkbenchSite(), description, resources, collector, listener);
		}					
	}
	
	public static void run(IWorkbenchSite site, String description, IResource[] resources, final SubscriberSyncInfoCollector collector, IRefreshSubscriberListener listener) {
		// Cancel the scheduled background refresh or any other refresh that is happening.
		// The scheduled background refresh will restart automatically.
		Platform.getJobManager().cancel(RefreshSubscriberJob.getFamily());
		RefreshSubscriberJob job = new RefreshSubscriberJob(Policy.bind("SyncViewRefresh.taskName", description), resources, collector); //$NON-NLS-1$
		if (listener != null) {
			RefreshSubscriberJob.addRefreshListener(listener);
		}
		IProgressMonitor group = Platform.getJobManager().createProgressGroup();
		group.beginTask("Refreshing " + description, 100);
		job.setProgressGroup(group, 70);
		collector.setProgressGroup(group, 30);
		
		schedule(job, site);
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				collector.setProgressGroup(null, 0);
			}
		});
	}
	
	public void setWorkbenchSite(IWorkbenchSite part) {
		this.workbenchSite = part;
	}
	
	public IWorkbenchSite getWorkbenchSite() {
		return workbenchSite;
	}
	
	private static void schedule(Job job, IWorkbenchSite site) {
		if(site == null) {
			job.schedule();
		}
		IWorkbenchSiteProgressService siteProgress = (IWorkbenchSiteProgressService) site.getAdapter(IWorkbenchSiteProgressService.class);
		if (siteProgress != null) {
			siteProgress.schedule(job);
		}
	}
}
