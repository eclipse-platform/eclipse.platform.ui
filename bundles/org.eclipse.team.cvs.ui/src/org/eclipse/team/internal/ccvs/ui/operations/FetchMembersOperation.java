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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.progress.IElementCollector;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderMemberFetcher;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Fetch the members of a remote folder in the background, passing incremental
 * results through an IElementCollector.
 */
public class FetchMembersOperation extends RemoteOperation {

	private IElementCollector collector;
	private RemoteFolderFilter filter = new RemoteFolderFilter();
	
	public static class RemoteFolderFilter {
		public ICVSRemoteResource[] filter(ICVSRemoteResource[] resource) {
			return resource;
		}
	}
	
	public class InternalRemoteFolderMemberFetcher extends RemoteFolderMemberFetcher {
		int sendIncrement = 100;
		int foldersSent = 0;
		List unsent = new ArrayList();
		protected InternalRemoteFolderMemberFetcher(RemoteFolder parentFolder, CVSTag tag) {
			super(parentFolder, tag);
		}
		protected void parentDoesNotExist() {
			super.parentDoesNotExist();
			// Indicate that there are no children
			collector.add(new Object[0], getProgressMonitor());
		}
		protected RemoteFolder recordFolder(String name) {
			RemoteFolder folder = super.recordFolder(name);
			unsent.add(folder);
			if (unsent.size() == sendIncrement) {
				sendFolders();
			}
			return folder;
		}
		protected IStatus performUpdate(IProgressMonitor progress) throws CVSException {
			IStatus status = super.performUpdate(progress);
			sendFolders();
			return status;
		}
		protected void updateFileRevisions(ICVSFile[] files, IProgressMonitor monitor) throws CVSException {
			super.updateFileRevisions(files, monitor);
			sendFiles();
		}
		private void sendFolders() {
			updateParentFolderChildren();
			collector.add(filter.filter((ICVSRemoteFolder[]) unsent.toArray(new ICVSRemoteFolder[unsent.size()])), getProgressMonitor());
			unsent.clear();
		}
		private void sendFiles() {
			collector.add(getFiles(), getProgressMonitor());
			unsent.clear();
		}
		private IProgressMonitor getProgressMonitor() {
			return null;
		}
	}

	public FetchMembersOperation(Shell shell, ICVSRemoteFolder folder, IElementCollector collector) {
		super(shell, new ICVSRemoteResource[] { folder });
		this.collector = collector;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		ICVSRemoteFolder remote = getRemoteFolder();
		if (remote.getClass().equals(RemoteFolder.class)) {
			monitor = Policy.monitorFor(monitor);
			boolean isRoot = remote.getName().equals(ICVSRemoteFolder.REPOSITORY_ROOT_FOLDER_NAME);
			monitor.beginTask(null, 100 + (isRoot ? 30 : 0));
			RemoteFolderMemberFetcher fetcher = new InternalRemoteFolderMemberFetcher((RemoteFolder)remote, remote.getTag());
			fetcher.fetchMembers(Policy.subMonitorFor(monitor, 100));
			if (isRoot) {
				ICVSRemoteResource[] modules = CVSUIPlugin.getPlugin()
					.getRepositoryManager()
					.getRepositoryRootFor(remote.getRepository())
					.getDefinedModules(remote.getTag(), Policy.subMonitorFor(monitor, 25));
				collector.add(filter.filter(modules), Policy.subMonitorFor(monitor, 5));
			}
		} else {
			monitor = Policy.monitorFor(monitor);
			try {
				monitor.beginTask(null, 100);
				IRemoteResource[] children = remote.members(Policy.subMonitorFor(monitor, 95));
				collector.add(children, Policy.subMonitorFor(monitor, 5));
			} catch (TeamException e) {
				throw CVSException.wrapException(e);
			} finally {
				monitor.done();
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("FetchMembersOperation.0", getRemoteFolder().getName()); //$NON-NLS-1$
	}

	private ICVSRemoteFolder getRemoteFolder() {
		return (ICVSRemoteFolder)getRemoteResources()[0];
	}

	public RemoteFolderFilter getFilter() {
		return filter;
	}

	public void setFilter(RemoteFolderFilter filter) {
		this.filter = filter;
	}

}
