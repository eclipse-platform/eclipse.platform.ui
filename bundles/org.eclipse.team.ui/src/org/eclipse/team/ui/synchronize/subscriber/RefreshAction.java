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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.jobs.RefreshSubscriberJob;
import org.eclipse.ui.IWorkbenchSite;

/**
 * A general refresh action that will refresh a subscriber in the background.
 * 
 * @since 3.0
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
	
	/**
	 * Policy.bind("SyncViewRefresh.taskName");
	 * 
	 * @param site
	 * @param taskName
	 * @param description
	 * @param resources
	 * @param collector
	 * @param listener
	 */
	public static void run(IWorkbenchSite site, String taskName, IResource[] resources, final SubscriberSyncInfoCollector collector, final IRefreshSubscriberListener listener) {
		RefreshSubscriberJob job = new RefreshSubscriberJob(taskName, resources, collector); //$NON-NLS-1$
		IRefreshSubscriberListener autoListener = new IRefreshSubscriberListener() {
			public void refreshStarted(IRefreshEvent event) {
				if(listener != null) {
					listener.refreshStarted(event);
				}
			}
			public void refreshDone(IRefreshEvent event) {
				if(listener != null) {
					listener.refreshDone(event);
					RefreshSubscriberJob.removeRefreshListener(this);
				}
			}
		};
		
		if (listener != null) {
			RefreshSubscriberJob.addRefreshListener(autoListener);
		}	
		Utils.schedule(job, site);
	}
	
	public void setWorkbenchSite(IWorkbenchSite part) {
		this.workbenchSite = part;
	}
	
	public IWorkbenchSite getWorkbenchSite() {
		return workbenchSite;
	}
}
