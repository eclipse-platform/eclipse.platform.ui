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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.PruneFolderVisitor;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.*;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.actions.SubscriberAction;
import org.eclipse.team.ui.synchronize.actions.SyncInfoSet;

public abstract class CVSSubscriberAction extends SubscriberAction {
	
	protected boolean isOutOfSync(SyncInfo resource) {
		if (resource == null) return false;
		return (!(resource.getKind() == 0) || ! resource.getLocal().exists());
	}
	
	protected void makeInSync(SyncInfo[] folders) throws TeamException {
		// If a node has a parent that is an incoming folder creation, we have to 
		// create that folder locally and set its sync info before we can get the
		// node itself. We must do this for all incoming folder creations (recursively)
		// in the case where there are multiple levels of incoming folder creations.
		for (int i = 0; i < folders.length; i++) {
			SyncInfo resource = folders[i];
			makeInSync(resource);
		}
	}
	
	protected void makeInSync(SyncInfo info) throws TeamException {
		if (isOutOfSync(info)) {
			SyncInfo parent = getParent(info);
			if (parent != null) {
				makeInSync(parent);
			}
			if (info == null) return;
			if (info instanceof CVSSyncInfo) {
				CVSSyncInfo cvsInfo= (CVSSyncInfo) info;
				cvsInfo.makeInSync();
			}
		}
	}
	
	protected void makeOutgoing(SyncInfo[] folders, IProgressMonitor monitor) throws TeamException {
		// If a node has a parent that is an incoming folder creation, we have to 
		// create that folder locally and set its sync info before we can get the
		// node itself. We must do this for all incoming folder creations (recursively)
		// in the case where there are multiple levels of incoming folder creations.
		monitor.beginTask(null, 100 * folders.length);
		for (int i = 0; i < folders.length; i++) {
			SyncInfo info = folders[i];
			makeOutgoing(info, Policy.subMonitorFor(monitor, 100));
		}
		monitor.done();
	}
	
	private void makeOutgoing(SyncInfo info, IProgressMonitor monitor) throws TeamException {
		if (info == null) return;
		if (info instanceof CVSSyncInfo) {
			CVSSyncInfo cvsInfo= (CVSSyncInfo) info;
			cvsInfo.makeOutgoing(monitor);
		}
	}

	/**
	 * Handle the exception by showing an error dialog to the user.
	 * Sync actions seem to need to be sync-execed to work
	 * @param t
	 */
	protected void handle(Throwable t) {
		CVSUIPlugin.openError(getShell(), getErrorTitle(), null, t, CVSUIPlugin.PERFORM_SYNC_EXEC | CVSUIPlugin.LOG_NONTEAM_EXCEPTIONS);
	}

	/**
	 * Return the error title that will appear in any error dialogs shown to the user
	 * @return
	 */
	protected String getErrorTitle() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
//		TODO: Saving can change the sync state! How should this be handled?
//			 boolean result = saveIfNecessary();
//			 if (!result) return null;

		SyncInfoSet syncSet = getFilteredSyncInfoSet(getFilteredSyncInfos());
		if (syncSet == null || syncSet.isEmpty()) return;
		try {
			getCVSRunnableContext().run(getJobName(syncSet), getSchedulingRule(syncSet), true, getRunnable(syncSet));
		} catch (InvocationTargetException e) {
			handle(e);
		} catch (InterruptedException e) {
			// nothing to do;
		}
	}

	/**
	 * Return an IRunnableWithProgress that will operate on the given sync set.
	 * This method is invoked by <code>run(IAction)</code> when the action is
	 * executed from a menu. The default implementation invokes the method
	 * <code>run(SyncInfoSet, IProgressMonitor)</code>.
	 * @param syncSet
	 * @return
	 */
	public IRunnableWithProgress getRunnable(final SyncInfoSet syncSet) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					// Pass the scheduling rule to the synchronizer so that sync change events
					// and cache commits to disk are batched
					EclipseSynchronizer.getInstance().run(
						getSchedulingRule(syncSet),
						new ICVSRunnable() {
							public void run(IProgressMonitor monitor) throws CVSException {
								try {
									CVSSubscriberAction.this.run(syncSet, monitor);
								} catch (TeamException e) {
									throw CVSException.wrapException(e);
								}
							}
						}, monitor);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
	}

	protected abstract void run(SyncInfoSet syncSet, IProgressMonitor monitor) throws TeamException;

	/*
	 * Return the ICVSRunnableContext which will be used to run the operation.
	 */
	private ICVSRunnableContext getCVSRunnableContext() {
		if (canRunAsJob() && areJobsEnabled()) {
			return new CVSSubscriberNonblockingContext();
		} else {
			return new CVSBlockingRunnableContext(shell);
		}
	}
	
	protected boolean areJobsEnabled() {
		return true;
	}
	
	/**
	 * Return the job name to be used if the action can run as a job.
	 * 
	 * @param syncSet
	 * @return
	 */
	protected abstract String getJobName(SyncInfoSet syncSet);

	/**
	 * Return a scheduling rule that includes all resources that will be operated 
	 * on by the subscriber action. The default behavior is to include all projects
	 * effected by the operation. Subclasses may override.
	 * 
	 * @param syncSet
	 * @return
	 */
	protected ISchedulingRule getSchedulingRule(SyncInfoSet syncSet) {
		IResource[] resources = syncSet.getResources();
		Set set = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			set.add(resource.getProject());
		}
		IProject[] projects = (IProject[]) set.toArray(new IProject[set.size()]);
		if (projects.length == 1) {
			return projects[0];
		} else {
			return new MultiRule(projects);
		}
	}

	protected boolean canRunAsJob() {
		return true;
	}

	/**
	 * Filter the sync resource set using action specific criteria or input from the user.
	 */
	protected SyncInfoSet getFilteredSyncInfoSet(SyncInfo[] selectedResources) {
		// If there are conflicts or outgoing changes in the syncSet, we need to warn the user.
		return new SyncInfoSet(selectedResources);
	}
	
	protected void pruneEmptyParents(SyncInfo[] nodes) throws CVSException {
		// TODO: A more explicit tie in to the pruning mechanism would be prefereable.
		// i.e. I don't like referencing the option and visitor directly
		if (!CVSProviderPlugin.getPlugin().getPruneEmptyDirectories()) return;
		ICVSResource[] cvsResources = new ICVSResource[nodes.length];
		for (int i = 0; i < cvsResources.length; i++) {
			cvsResources[i] = CVSWorkspaceRoot.getCVSResourceFor(nodes[i].getLocal());
		}
		new PruneFolderVisitor().visit(
			CVSWorkspaceRoot.getCVSFolderFor(ResourcesPlugin.getWorkspace().getRoot()),
			cvsResources);
	}
	
	public CVSSyncInfo getCVSSyncInfo(SyncInfo info) {
		if (info instanceof CVSSyncInfo) {
			return (CVSSyncInfo)info;
		}
		return null;
	}
	
	protected SyncInfo getParent(SyncInfo info) throws TeamException {
		return info.getSubscriber().getSyncInfo(info.getLocal().getParent(), new NullProgressMonitor());
	}

	protected IResource[] getIResourcesFrom(SyncInfo[] nodes) {
		List resources = new ArrayList(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			resources.add(nodes[i].getLocal());
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}

	/**
	 * Prompt to overwrite those resources that could not be safely updated
	 * Note: This method is designed to be overridden by test cases.
	 * 
	 * @return whether to perform the overwrite
	 */
	protected boolean promptForOverwrite(final SyncInfoSet syncSet) {
		final int[] result = new int[] {Dialog.CANCEL};
		TeamUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			public void run() {
				UpdateDialog dialog = new UpdateDialog(getShell(), syncSet);
				result[0] = dialog.open();
			}
		});
		return (result[0] == UpdateDialog.YES);
	}
}
