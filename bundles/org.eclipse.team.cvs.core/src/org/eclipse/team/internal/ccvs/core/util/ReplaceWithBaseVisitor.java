/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

public class ReplaceWithBaseVisitor implements ICVSResourceVisitor {

	private IProgressMonitor monitor;
	private int depth;
	
	/**
	 * @see ICVSResourceVisitor#visitFile(ICVSFile)
	 */
	public void visitFile(final ICVSFile file) throws CVSException {
		ResourceSyncInfo info = file.getSyncInfo();
		if (info == null) {
			// Delete unmanaged files if the user wants them deleted
			if (CVSProviderPlugin.getPlugin().isReplaceUnmanaged()) {
				file.delete();
			}
		} else if (info.isAdded()) {
			file.delete();
			file.unmanage(null);
		} else {
			CVSTag tag = info.getTag();
			boolean isModified = file.isModified();
			if (info.isDeleted()) {
				// If deleted, null the sync info so the file will be refetched
				MutableResourceSyncInfo mutable = info.cloneMutable();
				mutable.setDeleted(false);
				file.setSyncInfo(mutable);
				isModified = true;
			}
			// Fetch the file from the server
			if (isModified) {
				ICVSFolder parent = file.getParent();
				FolderSyncInfo folderInfo = parent.getFolderSyncInfo();
				Session.run(CVSProviderPlugin.getProvider().getRepository(folderInfo.getRoot()), parent, true, new ICVSRunnable() {
					public void run(IProgressMonitor monitor) throws CVSException {
						Command.UPDATE.execute(Command.NO_GLOBAL_OPTIONS, 
							new LocalOption[] {Update.makeTagOption(CVSTag.BASE), Update.IGNORE_LOCAL_CHANGES}, 
							new ICVSResource[] { file }, null, monitor);
					}
				}, Policy.subMonitorFor(monitor, 1));
	
				// Set the tag to be the original tag
				info = file.getSyncInfo();
				MutableResourceSyncInfo mutable = info.cloneMutable();
				mutable.setTag(tag);
				file.setSyncInfo(mutable);
			}
		}
		monitor.worked(1);
	}

	/**
	 * @see ICVSResourceVisitor#visitFolder(ICVSFolder)
	 */
	public void visitFolder(ICVSFolder folder) throws CVSException {
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
		monitor.worked(1);
	}

	public void replaceWithBase(IProject project, final IResource[] resources, int depth, IProgressMonitor pm) throws CVSException {
		this.depth = depth;
		final ICVSFolder root = CVSWorkspaceRoot.getCVSFolderFor(project);
		FolderSyncInfo folderInfo = root.getFolderSyncInfo();
		Session.run(CVSProviderPlugin.getProvider().getRepository(folderInfo.getRoot()), root, true, new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				root.run(new ICVSRunnable() {
					public void run(IProgressMonitor pm) throws CVSException {
						ReplaceWithBaseVisitor.this.monitor = Policy.infiniteSubMonitorFor(pm, 100);
						ReplaceWithBaseVisitor.this.monitor.beginTask(null, 512);
						for (int i = 0; i < resources.length; i++) {
							ReplaceWithBaseVisitor.this.monitor.subTask(Policy.bind("ReplaceWithBaseVisitor.replacing", resources[i].getFullPath().toString())); //$NON-NLS-1$
							CVSWorkspaceRoot.getCVSResourceFor(resources[i]).accept(ReplaceWithBaseVisitor.this);
						}
						ReplaceWithBaseVisitor.this.monitor.done();
					}
				}, monitor);
			}
		}, pm);
	}
}
