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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.client.Annotate;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.listeners.AnnotateListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogEntry;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.AnnotateView;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class ShowAnnotationAction extends CVSAction {

/**
 * Action to open a CVS Annotate View
 */

	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		// Get the selected resource.
		final ICVSResource cvsResource = getSingleSelectedCVSResource();
		execute(cvsResource);
	}

	public void execute(final ICVSResource cvsResource) throws InvocationTargetException, InterruptedException {

		final AnnotateListener listener = new AnnotateListener();
		if (cvsResource == null) {
			return;		
		}
		// Get the selected revision
		final String revision;
		try {
			revision = cvsResource.getSyncInfo().getRevision();
		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}
		
		// Run the CVS Annotate action with a progress monitor
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				fetchAnnotation(listener, cvsResource, revision, monitor);
			} 
		}, true, PROGRESS_DIALOG);

		// Open the view
		AnnotateView view = AnnotateView.openInActivePerspective();
		if (view == null) {
			return;
		}

		// Populate the view.
		
		if (listener.hasError()) {
			throw new InvocationTargetException(new CVSException("Unexpected response from CVS Server: " + listener.getError()));
		} else {		
			view.showAnnotations(cvsResource, listener.getCvsAnnotateBlocks(), listener.getContents());
		}
	}

	/**
	 * Send the CVS annotate command
	 * @param listener
	 * @param cvsResource
	 * @param revision
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	private void fetchAnnotation(final AnnotateListener listener, final ICVSResource cvsResource, final String revision, IProgressMonitor monitor) throws InvocationTargetException {
		
		try {
			ICVSFolder folder = cvsResource.getParent();
			final FolderSyncInfo info = folder.getFolderSyncInfo();
			ICVSRepositoryLocation location = CVSProviderPlugin.getPlugin().getRepository(info.getRoot());
			Session.run(location, folder, false, new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					monitor = Policy.monitorFor(monitor);
					monitor.beginTask(null, 100);
					Command.QuietOption quietness = CVSProviderPlugin.getPlugin().getQuietness();
					try {
						CVSProviderPlugin.getPlugin().setQuietness(Command.VERBOSE);
						final Command.LocalOption[] localOption;
						if (revision == null) {
							localOption = Command.NO_LOCAL_OPTIONS;	
						} else {
							localOption  = new Command.LocalOption[1];
							localOption[0] = Annotate.makeRevisionOption(revision);
						}
						IStatus status = Command.ANNOTATE.execute(Command.NO_GLOBAL_OPTIONS, 
							localOption, new ICVSResource[] { cvsResource }, listener,
							Policy.subMonitorFor(monitor, 100));
						if (status.getCode() == CVSStatus.SERVER_ERROR) {
							throw new CVSServerException(status);
						}
					} finally {
						CVSProviderPlugin.getPlugin().setQuietness(quietness);
						monitor.done();
					}
				}
			}, monitor);
		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}
	}

	/**
	 * Ony enabled for single resource selection
	 */
	protected boolean isEnabled() throws TeamException {
		return (selection.size() == 1);
	}

	/**
	 * This action is called from one of a Resource Navigator a
	 * CVS Resource Navigator or a History Log Viewer.  Return
	 * the selected resource as an ICVSResource
	 * 
	 * @return ICVSResource
	 */
	protected ICVSResource getSingleSelectedCVSResource() {
		
		// Selected from a Resource Navigator
		IResource[] resources = getSelectedResources();
		if (resources.length == 1) {
			IContainer parent = resources[0].getParent();
			ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(parent);
			return CVSWorkspaceRoot.getCVSResourceFor(resources[0]);
		}
		
		// Selected from a CVS Resource Navigator
		ICVSResource[] cvsResources = getSelectedCVSResources();
		if (cvsResources.length == 1) {
			return cvsResources[0];
		}
		
		// Selected from a History Viewer
		Object[] logEntries =  getSelectedResources(LogEntry.class);
		if (logEntries.length == 1) {
			LogEntry aLogEntry = (LogEntry) logEntries[0];
			ICVSRemoteFile cvsRemoteFile = aLogEntry.getRemoteFile();
			return cvsRemoteFile;
		}
		return null;
	}
}
