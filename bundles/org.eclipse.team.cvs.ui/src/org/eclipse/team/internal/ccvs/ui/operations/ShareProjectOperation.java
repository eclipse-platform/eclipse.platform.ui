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
package org.eclipse.team.internal.ccvs.ui.operations;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTree;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Create a folder and any missing parents in the repository
 */
public class ShareProjectOperation extends CVSOperation {

	private ICVSRepositoryLocation location;
	private IProject project;
	private String moduleName;
	private Shell shell;

	public ShareProjectOperation(Shell shell, ICVSRepositoryLocation location, IProject project, String moduleName) {
		super(null);
		this.shell = shell;
		this.moduleName = moduleName;
		this.project = project;
		this.location = location;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		try {
			monitor.beginTask(getTaskName(), 100);
			// Create the remote module
			final ICVSRemoteFolder remote = createRemoteFolder(Policy.subMonitorFor(monitor, 50));
			// Map the project to the module in a workspace runnable
			final TeamException[] exception = new TeamException[] {null};
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						mapProjectToRemoteFolder(remote, monitor);
					} catch (TeamException e) {
						exception[0] = e;
					}
				}
			}, ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(project), 0, Policy.subMonitorFor(monitor, 100));
			if (exception[0] != null)
				throw exception[0];
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Create the remote folder to which the project is being mapped 
	 * (as well as any ancestors) and return it. If the remote folder does not
	 * exist remotely, this method will create it.
	 * @param monitor a progress monitor
	 * @return the existing remote folder to which the project is being mapped
	 * @throws CVSException
	 */
	protected ICVSRemoteFolder createRemoteFolder(IProgressMonitor monitor) throws CVSException {
		String projectName = project.getName();
		if (moduleName == null)
			moduleName = projectName;

		RemoteFolderTree root = new RemoteFolderTree(null, location, Path.EMPTY.toString(), null);
		Path path = new Path(null, moduleName);
		
		try {
			monitor.beginTask(getTaskName(), 100 * path.segmentCount());
			return ensureTreeExists(root, path, monitor);
		} catch (TeamException e) {
			throw CVSException.wrapException(e);
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Map the project to the remote folder by associating the CVS
	 * Repository Provider with the project and, at the very least,
	 * assigning the folder sync info for the remote folder as the
	 * folder sync info for the project.
	 * @param remote the remote folder to which the projetc is being mapped
	 * @param monitor a progress monitor
	 * @throws CVSException
	 */
	protected void mapProjectToRemoteFolder(final ICVSRemoteFolder remote, IProgressMonitor monitor) throws TeamException {
		monitor.beginTask(null, 100);
		purgeAnyCVSFolders();
		// Link the project to the newly created module
		ICVSFolder folder = (ICVSFolder)CVSWorkspaceRoot.getCVSResourceFor(project);
		folder.setFolderSyncInfo(remote.getFolderSyncInfo());
		//Register it with Team.  If it already is, no harm done.
		RepositoryProvider.map(project, CVSProviderPlugin.getTypeId());
		monitor.done();
	}

	/*
	 * Create handles for all the children in the moduleName path
	 */
	private RemoteFolderTree createChild(RemoteFolderTree parent, String name, IProgressMonitor monitor) throws CVSException, TeamException {
		RemoteFolderTree child = new RemoteFolderTree(parent, name, location, new Path(null, parent.getRepositoryRelativePath()).append(name).toString(), null);
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
	private ICVSRemoteFolder ensureTreeExists(RemoteFolderTree folder, IPath path, IProgressMonitor monitor) throws TeamException {
		if (path.isEmpty()) return folder;
		String name = path.segment(0);
		RemoteFolderTree child = createChild(folder, name, monitor);
		return ensureTreeExists(child, path.removeFirstSegments(1), monitor);
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
		return Policy.bind("ShareProjectOperation.0", project.getName(), moduleName); //$NON-NLS-1$
	}

	/**
	 * @return Returns the project.
	 */
	public IProject getProject() {
		return project;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getShell()
	 */
	protected Shell getShell() {
		return shell;
	}
	
	/**
	 * Method findCommonRootInSubfolders.
	 * @return String
	 */
	private void purgeAnyCVSFolders() {
		try {
			ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(project);
			folder.accept(new ICVSResourceVisitor() {
				public void visitFile(ICVSFile file) throws CVSException {
					// nothing to do for files
				}
				public void visitFolder(ICVSFolder folder) throws CVSException {
					if (folder.isCVSFolder()) {
						// for now, just unmanage
						folder.unmanage(null);
					}
				}
			}, true /* recurse */);
		} catch (CVSException e) {
			// log the exception and return null
			CVSUIPlugin.log(e);
		}
	}
}
