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
package org.eclipse.team.internal.ui.sync.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.RefreshSubscriberJob;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.internal.ui.sync.views.SubscriberInput;
import org.eclipse.team.internal.ui.sync.views.SyncViewer;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.ui.actions.ActionContext;

class RefreshAction extends Action {
	private final SyncViewerActions actions;
	private boolean refreshAll;
	
	public RefreshAction(SyncViewerActions actions, boolean refreshAll) {
		this.refreshAll = refreshAll;
		this.actions = actions;
		setText("Refresh with Repository");
		setToolTipText("Refresh with the repository");
		setImageDescriptor(TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_REFRESH_ENABLED));
		setDisabledImageDescriptor(TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_REFRESH_DISABLED));
		setHoverImageDescriptor(TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_REFRESH));
	}
	
	public void run() {
		final SyncViewer view = actions.getSyncView();
		try {
			ActionContext context = actions.getContext();
			if(context != null) {
				getResources(context.getSelection());
				SubscriberInput input = (SubscriberInput)context.getInput();
				IResource[] resources = getResources(context.getSelection());
				if (refreshAll || resources.length == 0) {
					// If no resources are selected, refresh all the subscriber roots
					resources = input.roots();
				}
				if(TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_BACKGROUND_SYNC)) {
					Platform.getJobManager().cancel(RefreshSubscriberJob.getFamily());
					RefreshSubscriberJob job = new RefreshSubscriberJob(input.getSubscriber(), resources);
					job.schedule();
				} else {
					runBlocking(input.getSubscriber(), resources);
				}					
			}
		} catch(TeamException e) {
			Utils.handle(e);
		}
	}
	
	private IResource[] getResources(ISelection selection) {
		if(selection == null) {
			return new IResource[0];
		}
		return (IResource[])TeamAction.getSelectedAdaptables(selection, IResource.class);					
	}
	
	private void runBlocking(final TeamSubscriber s, final IResource[] resources) {
		actions.getSyncView().run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask(null, 100);
					s.refresh(resources, IResource.DEPTH_INFINITE, Policy.subMonitorFor(monitor, 100));
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		});
	}
}