/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.listeners.AnnotateListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogEntry;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.AnnotateView;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.ui.*;

public class ShowAnnotationAction extends WorkspaceAction {

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
			ResourceSyncInfo info = cvsResource.getSyncInfo();
			if (info == null) {
				handle(new CVSException(Policy.bind("ShowAnnotationAction.noSyncInfo", cvsResource.getName()))); //$NON-NLS-1$
				return;
			}
			revision = cvsResource.getSyncInfo().getRevision();
		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}

		// Run the CVS Annotate action with a progress monitor
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(null, 100);
				fetchAnnotation(listener, cvsResource, revision, Policy.subMonitorFor(monitor, 80));
				try {
					if (hasCharset(cvsResource, listener.getContents())) {
						listener.setContents(getRemoteContents(cvsResource, Policy.subMonitorFor(monitor, 20)));
					}
				} catch (CoreException e) {
					// Log and continue, using the original fetched contents
					CVSUIPlugin.log(e);
				}
				monitor.done();
			}
		}, true, PROGRESS_DIALOG);

		if (listener.hasError()) {
			throw new InvocationTargetException(new CVSException(Policy.bind("ShowAnnotationAction.1", listener.getError()))); //$NON-NLS-1$
		}

		// Open the view
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			try {
				PlatformUI.getWorkbench().showPerspective("org.eclipse.team.cvs.ui.cvsPerspective", window); //$NON-NLS-1$
			} catch (WorkbenchException e1) {
				// If this does not work we will just open the view in the
				// curren perspective.
			}
		}

		try {
			AnnotateView view = AnnotateView.openInActivePerspective();
			view.showAnnotations(cvsResource, listener.getCvsAnnotateBlocks(), listener.getContents());
		} catch (PartInitException e1) {
			handle(e1);
		}
	}

	protected boolean hasCharset(ICVSResource cvsResource, InputStream contents) {
		try {
			return TeamPlugin.getCharset(cvsResource.getName(), contents) != null;
		} catch (IOException e) {
			// Assume that the contents do have a charset
			return true;
		}
	}

	/**
	 * Send the CVS annotate command
	 * 
	 * @param listener
	 * @param cvsResource
	 * @param revision
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	private void fetchAnnotation(final AnnotateListener listener, final ICVSResource cvsResource, final String revision, IProgressMonitor monitor) throws InvocationTargetException {

		try {
			monitor = Policy.monitorFor(monitor);
			monitor.beginTask(null, 100);
			ICVSFolder folder = cvsResource.getParent();
			final FolderSyncInfo info = folder.getFolderSyncInfo();
			ICVSRepositoryLocation location = KnownRepositories.getInstance().getRepository(info.getRoot());
			Session session = new Session(location, folder, true /*
																  * output to
																  * console
																  */);
			session.open(Policy.subMonitorFor(monitor, 10), false /* read-only */);
			try {
				Command.QuietOption quietness = CVSProviderPlugin.getPlugin().getQuietness();
				try {
					CVSProviderPlugin.getPlugin().setQuietness(Command.VERBOSE);
					final Command.LocalOption[] localOption;
					if (revision == null) {
						localOption = Command.NO_LOCAL_OPTIONS;
					} else {
						localOption = new Command.LocalOption[1];
						localOption[0] = Annotate.makeRevisionOption(revision);
					}
					IStatus status = Command.ANNOTATE.execute(session, Command.NO_GLOBAL_OPTIONS, localOption, new ICVSResource[]{cvsResource}, listener, Policy.subMonitorFor(monitor, 90));
					if (status.getCode() == CVSStatus.SERVER_ERROR) {
						throw new CVSServerException(status);
					}
				} finally {
					CVSProviderPlugin.getPlugin().setQuietness(quietness);
					monitor.done();
				}
			} finally {
				session.close();
			}
		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}
	}

	private InputStream getRemoteContents(ICVSResource resource, IProgressMonitor monitor) throws CoreException {
		ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(resource);
		if (remote == null) {
			return new ByteArrayInputStream(new byte[0]);
		}
		IStorage storage = ((IResourceVariant)remote).getStorage(monitor);
		if (storage == null) {
			return new ByteArrayInputStream(new byte[0]);
		}
		return storage.getContents();
	}
	
	/**
	 * Ony enabled for single resource selection
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSResource resource = getSingleSelectedCVSResource();
		return (resource != null && ! resource.isFolder() && resource.isManaged());
	}

	/**
	 * This action is called from one of a Resource Navigator a CVS Resource
	 * Navigator or a History Log Viewer. Return the selected resource as an
	 * ICVSResource
	 * 
	 * @return ICVSResource
	 */
	protected ICVSResource getSingleSelectedCVSResource() {
		// Selected from a CVS Resource Navigator
		ICVSResource[] cvsResources = getSelectedCVSResources();
		if (cvsResources.length == 1) {
			return cvsResources[0];
		}

		// Selected from a History Viewer
		Object[] logEntries = getSelectedResources(LogEntry.class);
		if (logEntries.length == 1) {
			LogEntry aLogEntry = (LogEntry) logEntries[0];
			ICVSRemoteFile cvsRemoteFile = aLogEntry.getRemoteFile();
			return cvsRemoteFile;
		}

		// Selected from a Resource Navigator
		IResource[] resources = getSelectedResources();
		if (resources.length == 1) {
			return CVSWorkspaceRoot.getCVSResourceFor(resources[0]);
		}
		return null;
	}
}
