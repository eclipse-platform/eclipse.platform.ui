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
package org.eclipse.team.internal.ui.synchronize.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.jobs.JobStatusHandler;
import org.eclipse.team.internal.ui.jobs.RefreshSubscriberJob;
import org.eclipse.team.internal.ui.synchronize.sets.SubscriberInput;
import org.eclipse.team.internal.ui.synchronize.views.SyncSetContentProvider;
import org.eclipse.team.ui.synchronize.TeamSubscriberParticipant;
import org.eclipse.team.ui.synchronize.actions.SubscriberAction;
import org.eclipse.ui.IWorkbenchPage;

public class RefreshAction extends Action {
	private boolean refreshAll;
	private IWorkbenchPage page;
	private TeamSubscriberParticipant participant;
	
	public RefreshAction(IWorkbenchPage page, TeamSubscriberParticipant participant, boolean refreshAll) {
		this.participant = participant;
		this.page = page;
		this.refreshAll = refreshAll;
		Utils.initAction(this, "action.refreshWithRemote."); //$NON-NLS-1$
	}
	
	public void run() {
		ISelection selection = page.getSelection();
		if(selection instanceof IStructuredSelection) {
			IResource[] resources = getResources((IStructuredSelection)selection);
			SubscriberInput input = participant.getInput();
			if (refreshAll || resources.length == 0) {
				// If no resources are selected, refresh all the subscriber roots
				resources = input.workingSetRoots();
			}
			run(resources, participant);
		}					
	}
	
	private IResource[] getResources(IStructuredSelection selection) {
		if(selection == null) {
			return new IResource[0];
		}
		List resources = new ArrayList();
		Iterator it = selection.iterator();
		while(it.hasNext()) {
			IResource resource = SyncSetContentProvider.getResource(it.next());
			if(resource != null) {
				resources.add(resource);
			}
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);					
	}
	
	public static void run(IResource[] resources, TeamSubscriberParticipant participant) {
		// Cancel the scheduled background refresh or any other refresh that is happening.
		// The scheduled background refresh will restart automatically.
		Platform.getJobManager().cancel(RefreshSubscriberJob.getFamily());
		if(TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_BACKGROUND_SYNC)) {			
			RefreshSubscriberJob job = new RefreshSubscriberJob(Policy.bind("SyncViewRefresh.taskName", participant.getName()), resources, participant.getInput().getSubscriber()); //$NON-NLS-1$
			JobStatusHandler.schedule(job, SubscriberAction.SUBSCRIBER_JOB_TYPE);
		} else { 			
			runBlocking(participant.getInput().getSubscriber(), resources);
		}
	}
		
	private static void runBlocking(final TeamSubscriber s, final IResource[] resources) {
		TeamUIPlugin.run(new IRunnableWithProgress() {
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