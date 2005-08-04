/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.synchronize.SyncInfoFilter.ContentComparisonSyncInfoFilter;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.*;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Resets the dirty state of files whose contents match their base.
 */
public class RefreshDirtyStateOperation extends CVSSubscriberOperation {
	
	protected RefreshDirtyStateOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		super(configuration, elements);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberOperation#run(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void run(SyncInfoSet set, IProgressMonitor monitor) throws TeamException {
		final ContentComparisonSyncInfoFilter comparator = new SyncInfoFilter.ContentComparisonSyncInfoFilter(false);
		final SyncInfo[] infos = set.getSyncInfos();
		if (infos.length == 0) return;
        monitor.beginTask(null, 200);
		IProject project = infos[0].getLocal().getProject();
		ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(project);
        ensureBaseContentsCached(folder, infos, Policy.subMonitorFor(monitor, 100));
		folder.run(new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				monitor.beginTask(null, infos.length * 100);
				for (int i = 0; i < infos.length; i++) {
					SyncInfo info = infos[i];
					IResource resource = info.getLocal();
					if (resource.getType() == IResource.FILE) {
						if (comparator.compareContents((IFile)resource, info.getBase(), Policy.subMonitorFor(monitor, 100))) {
							ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
							cvsFile.checkedIn(null, false /* not a commit */);
						}
					}
				}
				monitor.done();
			}
		}, Policy.subMonitorFor(monitor, 100));
        monitor.done();
	}
	
	private void ensureBaseContentsCached(ICVSFolder project, SyncInfo[] infos, IProgressMonitor monitor) throws CVSException {
        ICVSRepositoryLocation location = getRemoteLocation(project);
        if (location == null) return;
        monitor.beginTask(null, 100);
        SyncInfo[] needContents = getBaseFilesWithUncachedContents(infos, Policy.subMonitorFor(monitor, 10));
        if (needContents.length == 0) return;
        RemoteFolderTree tree = RemoteFolderTreeBuilder.buildBaseTree((CVSRepositoryLocation)location , project, null, Policy.subMonitorFor(monitor, 20));
        ICVSFile[] files = getFilesToUpdate(tree, infos);
        replaceContents(location, tree, files, Policy.subMonitorFor(monitor, 70));
        monitor.done();
    }

    private ICVSFile[] getFilesToUpdate(RemoteFolderTree tree, SyncInfo[] infos) throws CVSException {
        List newFiles = new ArrayList();
        for (int i = 0; i < infos.length; i++) {
            SyncInfo info = infos[i];
            ICVSFile file = tree.getFile(info.getLocal().getProjectRelativePath().toString());
            newFiles.add(file);
        }

        return (ICVSFile[]) newFiles.toArray(new ICVSFile[newFiles.size()]);
    }

    private void replaceContents(ICVSRepositoryLocation location, ICVSFolder project, ICVSFile[] files, IProgressMonitor monitor) throws CVSException {
        monitor.beginTask(null, 100);
        Session session = new Session(location, project, false);
        try {
            session.open(Policy.subMonitorFor(monitor, 10));
            IStatus execute = Command.UPDATE.execute(
                    session,
                    Command.NO_GLOBAL_OPTIONS, 
                    new LocalOption[] { Update.IGNORE_LOCAL_CHANGES }, 
                    files,
                    null,
                    Policy.subMonitorFor(monitor, 90));
            if (execute.getCode() == CVSStatus.SERVER_ERROR) {
                throw new CVSServerException(execute);
            }
        } finally {
            session.close();
        }
        
    }

    private SyncInfo[] getBaseFilesWithUncachedContents(SyncInfo[] infos, IProgressMonitor monitor) {
        List files = new ArrayList();
        for (int i = 0; i < infos.length; i++) {
            SyncInfo info = infos[i];
            IResourceVariant base = info.getBase();
            if (base instanceof RemoteFile) {
                RemoteFile remote = (RemoteFile) base;
                if (!remote.isContentsCached()) {
                    files.add(info);
                }
            }
        }
        return (SyncInfo[]) files.toArray(new SyncInfo[files.size()]);
    }

    private ICVSRepositoryLocation getRemoteLocation(ICVSFolder project) throws CVSException {
        FolderSyncInfo info = project.getFolderSyncInfo();
        if (info == null) {
            return null;
        }
        return KnownRepositories.getInstance().getRepository(info.getRoot());
    }
    
    protected String getErrorTitle() {
		return CVSUIMessages.RefreshDirtyStateOperation_0; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberAction#getJobName(org.eclipse.team.ui.sync.SyncInfoSet)
	 */
	protected String getJobName() {
		return CVSUIMessages.RefreshDirtyStateOperation_1; 
	}
}
