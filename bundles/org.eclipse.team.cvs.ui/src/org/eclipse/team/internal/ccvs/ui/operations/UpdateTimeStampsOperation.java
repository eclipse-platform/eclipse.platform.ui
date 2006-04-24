/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.diff.provider.ThreeWayDiff;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.listeners.ILogEntryListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogListener;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.core.mapping.SyncInfoToDiffConverter;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IWorkbenchPart;

public class UpdateTimeStampsOperation extends SingleCommandOperation {
	
	private final class LogEntryListener implements ILogEntryListener {

		private Map entries = new HashMap();
		
		public void handleLogEntryReceived(ILogEntry entry) {
			String repoPath = entry.getRemoteFile().getRepositoryRelativePath();
			if (repoPath != null){
				String revision = entry.getRevision();
				Map fileEntries = (Map) entries.get(repoPath);
				if (fileEntries == null) {
					fileEntries = new HashMap();
					entries.put(repoPath, fileEntries);
				}
				fileEntries.put(revision, entry);
			}
		
			
		}

		public ILogEntry[] getLogEntries(String repoRelativePath) {
			Map map = (Map) entries.get(repoRelativePath);
			return (ILogEntry[]) map.values().toArray(new ILogEntry[map.values().size()]);
		}

		public ILogEntry getLogEntry(String repositoryRelativePath, String revision) {
			Map fileEntries = (Map) entries.get(repositoryRelativePath);
			if (fileEntries != null) {
				return (ILogEntry) fileEntries.get(revision);
			}
			return null;
		}
		
	}
	
	private final IResourceDiffTree tree;

	public UpdateTimeStampsOperation(IWorkbenchPart part, ResourceMapping[] mappings,IResourceDiffTree tree) {
		super(part, mappings, Command.NO_LOCAL_OPTIONS);
		this.tree = tree;
	}

	protected void execute(CVSTeamProvider provider, IResource[] resources, boolean recurse, IProgressMonitor monitor) throws CVSException, InterruptedException {
		IResource[] files = getFilesWithNoTimestamps(resources, recurse);
		if (files.length > 0)
			super.execute(provider, files, recurse, monitor);
	}
	
	private IResource[] getFilesWithNoTimestamps(IResource[] resources, boolean recurse) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			IDiff[] nodes = tree.getDiffs(resource, recurse ? IResource.DEPTH_INFINITE: IResource.DEPTH_ONE);
			for (int j = 0; j < nodes.length; j++) {
				IDiff node = nodes[j];
				if (node instanceof ThreeWayDiff && 
					((ThreeWayDiff)node).getRemoteChange() == null)
					continue;
				
				if (!hasTimeStamp(node)) {
					result.add(tree.getResource(node));
				}
			}
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}

	private boolean hasTimeStamp(IDiff node) {
		if (node instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) node;
			IFileRevision remote = getRemoteFileState(twd);

			if (remote != null) {
				IResourceVariant variant = (IResourceVariant) Utils.getAdapter(remote, IResourceVariant.class);
				 if (variant instanceof RemoteFile) {
				 RemoteFile var = (RemoteFile) variant;
				 Date ti = var.getTimeStamp();
				 if (ti != null)
					 return true;
				 }
			}
		}
		return false;

	}

	private IFileRevision getRemoteFileState(IThreeWayDiff twd) {
		IResourceDiff diff = (IResourceDiff)twd.getRemoteChange();
		if (diff == null)
			return null;
		return diff.getAfterState();
	}

	protected IStatus executeCommand(Session session, CVSTeamProvider provider, ICVSResource[] resources, boolean recurse, IProgressMonitor monitor) throws CVSException, InterruptedException {
		 LogEntryListener listener = new LogEntryListener();
		 IStatus status = Command.LOG.execute(
                session,
                Command.NO_GLOBAL_OPTIONS,
                getLocalOptions(true),
                resources,
                new LogListener(listener),
                monitor);
		 
		 for (int i = 0; i < resources.length; i++) {
			ICVSResource resource = getCachedResourceFor(resources[i]);
			if (resource != null){
				//Find out what revision we need the timestamp for
				ILogEntry entry = listener.getLogEntry(resource.getRepositoryRelativePath(),resource.getSyncInfo().getRevision());
				if (entry != null){
					Date timeStamp = entry.getDate();
					MutableResourceSyncInfo syncInfo = resource.getSyncInfo().cloneMutable();
					syncInfo.setTimeStamp(timeStamp);
					if (resource instanceof ICVSFile){
						((ICVSFile) resource).setSyncInfo(syncInfo, ICVSFile.CLEAN);
					}
				}
			}
		 }
		 
		 return status; 
	}

	private ICVSResource getCachedResourceFor(ICVSResource resource) {
		IDiff diff = tree.getDiff(resource.getIResource());
		IResourceVariant remote = SyncInfoToDiffConverter.getRemoteVariant((IThreeWayDiff)diff);
		if (remote != null)
			return (ICVSResource)((RemoteFile)remote).getCachedHandle();
		
		return null;
	}

	protected String getTaskName(CVSTeamProvider provider) {
		return CVSUIMessages.CacheTreeContentsOperation_1;
	}

	protected String getTaskName() {
		return CVSUIMessages.CacheTreeContentsOperation_1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#consultModelsForMappings()
	 */
	public boolean consultModelsForMappings() {
		return false;
	}

}
