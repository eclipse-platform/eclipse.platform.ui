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
package org.eclipse.team.internal.ccvs.core.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * This class is used to prepare a local CVS workspace for replacement by
 * the corresponding remote resources. More specifically, this class will
 * unmanage added and deleted resources so that, after the operation, the
 * resources in the local workspace will either correspond to the remote
 * counterparts or be unmanaged.
 */
public class PrepareForReplaceVisitor implements ICVSResourceVisitor {

	private IProgressMonitor monitor;
	private int depth;

	/**
	 * @see ICVSResourceVisitor#visitFile(ICVSFile)
	 */
	public void visitFile(ICVSFile file) throws CVSException {
		byte[] syncBytes = file.getSyncBytes();
		if (syncBytes == null) {
			// Delete unmanaged files if the user wants them deleted
			if (CVSProviderPlugin.getPlugin().isReplaceUnmanaged()) {
				file.delete();
			}
		} else if (ResourceSyncInfo.isAddition(syncBytes)) {
			file.delete();
			file.unmanage(null);
		} else if (ResourceSyncInfo.isDeletion(syncBytes)) {
			// If deleted, null the sync info so the file will be refetched
			file.unmanage(null);
		} else if (file.isModified(null)) {
			// If the file is modified, delete and unmanage it and allow the 
			// replace operaton to fetch it again. This is required because "update -C" 
			// will fail for locally modified resources that have been deleted remotely.
			file.delete();
			// Only unmanage if the delete was succesful (bug 76029)
			file.unmanage(null);
		}
		monitor.worked(1);
	}

	/**
	 * @see ICVSResourceVisitor#visitFolder(ICVSFolder)
	 */
	public void visitFolder(ICVSFolder folder) throws CVSException {
		// Delete unmanaged folders if the user wants them deleted
		if (!folder.isCVSFolder() && CVSProviderPlugin.getPlugin().isReplaceUnmanaged()) {
			folder.delete();
		} else {
			// Visit the children of the folder as appropriate
			if (depth == IResource.DEPTH_INFINITE) {
				folder.acceptChildren(this);
			} else if (depth == IResource.DEPTH_ONE) {
				ICVSResource[] files = folder.members(ICVSFolder.FILE_MEMBERS);
				for (int i = 0; i < files.length; i++) {
					files[i].accept(this);
				}
			}
			// Also delete ignored child files that start with .#
			ICVSResource[] ignoredFiles = folder.members(ICVSFolder.FILE_MEMBERS | ICVSFolder.IGNORED_MEMBERS);
			for (int i = 0; i < ignoredFiles.length; i++) {
				ICVSResource cvsResource = ignoredFiles[i];
				if (cvsResource.getName().startsWith(".#")) { //$NON-NLS-1$
					cvsResource.delete();
				}
			}
		}
		monitor.worked(1);
	}
	
	public void visitResources(IProject project, final ICVSResource[] resources, final String key, int depth, IProgressMonitor pm) throws CVSException {
		this.depth = depth;
		CVSWorkspaceRoot.getCVSFolderFor(project).run(new ICVSRunnable() {
			public void run(IProgressMonitor pm) throws CVSException {
				monitor = Policy.infiniteSubMonitorFor(pm, 100);
				monitor.beginTask(null, 512);
				for (int i = 0; i < resources.length; i++) {
					if (key != null) {
						monitor.subTask(Policy.bind(key, resources[i].getIResource().getFullPath().toString())); //$NON-NLS-1$
					}
					resources[i].accept(PrepareForReplaceVisitor.this);
				}
				monitor.done();
			}
		}, pm);
	}
}
