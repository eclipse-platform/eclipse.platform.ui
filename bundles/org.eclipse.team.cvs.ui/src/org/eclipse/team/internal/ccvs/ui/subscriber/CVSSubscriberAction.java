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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSSyncInfo;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.client.PruneFolderVisitor;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.sync.SubscriberAction;
import org.eclipse.team.ui.sync.SyncInfoSet;
import org.eclipse.ui.PlatformUI;

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
			getRunnableContext().run(true /* fork */, true /* cancelable */, getRunnable(syncSet));
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
	protected IRunnableWithProgress getRunnable(final SyncInfoSet syncSet) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					CVSWorkspaceRoot.getCVSFolderFor(ResourcesPlugin.getWorkspace().getRoot()).run(
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

	protected IRunnableContext getRunnableContext() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
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
		return getSubscriber().getSyncInfo(info.getLocal().getParent(), new NullProgressMonitor());
	}
}
