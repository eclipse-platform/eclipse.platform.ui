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
package org.eclipse.team.internal.ccvs.ui.operations;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTree;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Create a folder and any missing parents in the repository
 */
public class ShareProjectOperation extends CVSOperation {

	private ICVSRepositoryLocation location;
	private IProject project;
	private String moduleName;

	public ShareProjectOperation(Shell shell, ICVSRepositoryLocation location, IProject project, String moduleName) {
		super(shell);
		this.moduleName = moduleName;
		this.project = project;
		this.location = location;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		
		// Determine if the repository is known
		boolean alreadyExists = CVSProviderPlugin.getPlugin().isKnownRepository(location.getLocation());
			
		try {
			createRemoteFolder(monitor);
			mapProjectToRemoteFolder(monitor);
		} catch (CVSException e) {
			// The checkout may have triggered password caching
			// Therefore, if this is a newly created location, we want to clear its cache
			if ( ! alreadyExists)
				CVSProviderPlugin.getPlugin().disposeRepository(location);
			throw e;
		}
		// Add the repository if it didn't exist already
		if ( ! alreadyExists) {
			CVSProviderPlugin.getPlugin().addRepository(location);
		}
	}

	private void mapProjectToRemoteFolder(IProgressMonitor monitor) throws CVSException {
		// perform the workspace modifications in a runnable
		try {
			// Set the folder sync info of the project to point to the remote module
			final ICVSFolder folder = (ICVSFolder)CVSWorkspaceRoot.getCVSResourceFor(project);
			final TeamException[] exception = new TeamException[] {null};
			final String modName = moduleName;
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						// Link the project to the newly created module
						folder.setFolderSyncInfo(new FolderSyncInfo(modName, location.getLocation(), null, false));
						//Register it with Team.  If it already is, no harm done.
						RepositoryProvider.map(project, CVSProviderPlugin.getTypeId());
					} catch (TeamException e) {
						exception[0] = e;
					}
				}
			}, project, 0, monitor);
			if (exception[0] != null)
				throw exception[0];
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	/*
	 * Create the remote folder (and any ancestors).
	 */
	private void createRemoteFolder(IProgressMonitor monitor) throws CVSException {
		String projectName = project.getName();
		if (moduleName == null)
			moduleName = projectName;

		RemoteFolderTree root = new RemoteFolderTree(null, location, Path.EMPTY.toString(), null);
		Path path = new Path(moduleName);
		
		try {
			monitor.beginTask(getTaskName(), 100 * path.segmentCount());
			ensureTreeExists(root, path, monitor);
		} catch (TeamException e) {
			throw CVSException.wrapException(e);
		} finally {
			monitor.done();
		}
	}

	/*
	 * Create handles for all the children in the moduleName path
	 */
	private RemoteFolderTree createChild(RemoteFolderTree parent, String name, IProgressMonitor monitor) throws CVSException, TeamException {
		RemoteFolderTree child = new RemoteFolderTree(parent, name, location, new Path(parent.getRepositoryRelativePath()).append(name).toString(), null);
		parent.setChildren(new ICVSRemoteResource[] { child });
		if (child.exists(Policy.subMonitorFor(monitor, 50))) {
			// The child exists so get the handle that was received from the server
			return (RemoteFolderTree)parent.getFolder(name);
		} else {
			// Create the folder remotely
			createFolder(child, Policy.subMonitorFor(monitor, 50));
			return child;
		}
	}

	/*
	 * Ensure that all the folders in the tree exist
	 */
	private void ensureTreeExists(RemoteFolderTree folder, IPath path, IProgressMonitor monitor) throws TeamException {
		if (path.isEmpty()) return;
		String name = path.segment(0);
		RemoteFolderTree child = createChild(folder, name, monitor);
		ensureTreeExists(child, path.removeFirstSegments(1), monitor);
	}
	
	private void createFolder(RemoteFolderTree folder, IProgressMonitor monitor) throws TeamException {
		Session s = new Session(location, folder.getParent());
		s.open(monitor, true /* open for modification */);
		try {
			IStatus status = Command.ADD.execute(s,
					Command.NO_GLOBAL_OPTIONS,
					Command.NO_LOCAL_OPTIONS,
					new String[] { folder.getName() },
					null,
					monitor);
			// If we get a warning, the operation most likely failed so check that the status is OK
			if (status.getCode() == CVSStatus.SERVER_ERROR  || ! status.isOK()) {
				throw new CVSServerException(status);
			}
		} finally {
			s.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		// TODO Auto-generated method stub
		return null;
	}

}
