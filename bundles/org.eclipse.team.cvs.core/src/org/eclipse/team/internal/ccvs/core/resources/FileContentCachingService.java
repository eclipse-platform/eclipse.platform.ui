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
package org.eclipse.team.internal.ccvs.core.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;

/**
 * This class can be used to fetch and cache file contents for remote files.
 */
public class FileContentCachingService {

	String[] fileDiffs;
	private CVSRepositoryLocation repository;
	private ICVSFolder remoteRoot;

	public static RemoteFolderTree buildRemoteTree(CVSRepositoryLocation repository, ICVSFolder root, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		monitor.beginTask(null, 100);
		try {
			RemoteFolderTreeBuilder builder = new RemoteFolderTreeBuilder(repository, root, tag);
			RemoteFolderTree tree =  builder.buildTree(new ICVSResource[] { root }, Policy.subMonitorFor(monitor, 50));
			FileContentCachingService service = new FileContentCachingService(repository, tree, builder.getFileDiffs());
			service.cacheFileContents(Policy.subMonitorFor(monitor, 50));
			return tree;
		} finally {
			monitor.done();
		}
	}
	
	public static RemoteFile buildRemoteTree(CVSRepositoryLocation repository, ICVSFile file, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		monitor.beginTask(null, 100);
		try {
			RemoteFolderTreeBuilder builder = new RemoteFolderTreeBuilder(repository, file.getParent(), tag);
			RemoteFile remote =  builder.buildTree(file, monitor);
			if (builder.getFileDiffs().length > 0 && !remote.isContentsCached()) {
				remote.fetchContents(Policy.subMonitorFor(monitor, 50));
			}
			return remote;
		} finally {
			monitor.done();
		}
	}
	
	public FileContentCachingService(CVSRepositoryLocation repository, RemoteFolderTree tree, String[] fileDiffs) {
		this.repository = repository;
		this.remoteRoot = tree;
		this.fileDiffs = fileDiffs;
	}
	
	private void cacheFileContents(IProgressMonitor monitor) throws CVSException {
		String[] files = getUncachedFiles();
		if (files.length == 0) return;
		// Fetch the file contents for all out-of-sync files by running an update
		// on the remote tree passing the known changed files as arguments
		monitor.beginTask(null, 10 + files.length * 100);
		Policy.checkCanceled(monitor);
		Session session = new Session(repository, remoteRoot, false);
		session.open(Policy.subMonitorFor(monitor, 10));
		try {
			Policy.checkCanceled(monitor);
			IStatus status = Command.UPDATE.execute(session,
				Command.NO_GLOBAL_OPTIONS,
				new LocalOption[] { Update.IGNORE_LOCAL_CHANGES },
				files,
				null,
				Policy.subMonitorFor(monitor, files.length * 100));
		} finally {
			session.close();
			monitor.done();
		}
	}

	/*
	 * Only return those file in the diff list that exist remotely and whose contents are not already cached
	 */
	private String[] getUncachedFiles() {
		if (fileDiffs.length == 0) return fileDiffs;
		List existing = new ArrayList();
		for (int i = 0; i < fileDiffs.length; i++) {
			String filePath = fileDiffs[i];
			try {
				ICVSFile file = remoteRoot.getFile(filePath);
				if (file instanceof RemoteFile) {
					if (!((RemoteFile)file).isContentsCached()) {
						existing.add(filePath);
					}
				}
			} catch (CVSException e) {
				// The child does not exists so exclude it
			}
		}
		return (String[]) existing.toArray(new String[existing.size()]);
	}
}
