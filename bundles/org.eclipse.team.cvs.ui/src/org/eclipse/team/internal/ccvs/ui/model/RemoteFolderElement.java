/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui.model;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.FetchMembersOperation;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryRoot;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public class RemoteFolderElement extends RemoteResourceElement implements IDeferredWorkbenchAdapter {

    /**
     * Overridden to append the version name to remote folders which
     * have version tags and are top-level folders.
     */
    public String getLabel(Object o) {
        if (!(o instanceof ICVSRemoteFolder))
            return null;
        ICVSRemoteFolder folder = (ICVSRemoteFolder) o;
        CVSTag tag = folder.getTag();
        if (tag != null && tag.getType() != CVSTag.HEAD) {
            if (folder.getRemoteParent() == null) {
                return NLS.bind(CVSUIMessages.RemoteFolderElement_nameAndTag, new String[] { folder.getName(), tag.getName() }); 
            }
        }
        return folder.getName();
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        if (!(object instanceof ICVSRemoteFolder))
            return null;
        ICVSRemoteFolder folder = (ICVSRemoteFolder) object;
        if (folder.isDefinedModule()) {
            return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_MODULE);
        }
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
    }

    public Object[] fetchChildren(Object o, IProgressMonitor monitor) throws TeamException {
        if (!(o instanceof ICVSRemoteFolder))
            return new Object[0];
		ICVSRemoteFolder remoteFolder = (ICVSRemoteFolder) o;
		if (remoteFolder.getTag() != null
				&& remoteFolder.getTag().getType() == CVSTag.BRANCH) {
			monitor = Policy.monitorFor(monitor);
			monitor.beginTask(NLS.bind(
					CVSUIMessages.RemoteFolderElement_fetchingRemoteChildren,
					new String[] { getLabel(remoteFolder) }), 100);
			try {
				ICVSRemoteResource[] cachedFolders = CVSUIPlugin
						.getPlugin()
						.getRepositoryManager()
						.getFoldersForTag(remoteFolder.getRepository(),
								remoteFolder.getTag(),
								Policy.subMonitorFor(monitor, 50));
				if (!hasCachedParent(cachedFolders, remoteFolder)) {
					return getCachedChildren(cachedFolders, remoteFolder,
							Policy.subMonitorFor(monitor, 50));
				}
				return remoteFolder.members(Policy.subMonitorFor(monitor, 50));
			} finally {
				monitor.done();
			}
		}
		return remoteFolder.members(monitor);
    }

    public void fetchDeferredChildren(Object o, IElementCollector collector, IProgressMonitor monitor) {
    	// If it's not a folder, return an empty array
		if (!(o instanceof ICVSRemoteFolder)) {
			collector.add(new Object[0], monitor);
		}
        try {
            monitor = Policy.monitorFor(monitor);
			ICVSRemoteFolder remoteFolder = (ICVSRemoteFolder) o;
			if (remoteFolder.getTag() != null
					&& remoteFolder.getTag().getType() == CVSTag.BRANCH) {
				monitor.beginTask(
						NLS.bind(
								CVSUIMessages.RemoteFolderElement_fetchingRemoteChildren,
								new String[] { getLabel(o) }), 200);
				ICVSRemoteResource[] cachedFolders = CVSUIPlugin
						.getPlugin()
						.getRepositoryManager()
						.getFoldersForTag(remoteFolder.getRepository(),
								remoteFolder.getTag(),
								Policy.subMonitorFor(monitor, 100));
				if (!hasCachedParent(cachedFolders, remoteFolder)) {
					collector.add(
							getCachedChildren(cachedFolders, remoteFolder,
									Policy.subMonitorFor(monitor, 90)), Policy
									.subMonitorFor(monitor, 10));
					return;
				}
			} else {
				monitor.beginTask(
						NLS.bind(
								CVSUIMessages.RemoteFolderElement_fetchingRemoteChildren,
								new String[] { getLabel(o) }), 100);
			}
			FetchMembersOperation operation = new FetchMembersOperation(null, (ICVSRemoteFolder)o, collector);
			operation.run(Policy.subMonitorFor(monitor, 100));
        } catch (InvocationTargetException e) {
            handle(collector, e);
		} catch (InterruptedException e) {
			// Cancelled by the user;
		} catch (CVSException e) {
			handle(collector, e);
		} finally {
            monitor.done();
        }
    }

	private boolean hasCachedParent(ICVSRemoteResource[] cachedFolders,
			ICVSRemoteFolder remoteFolder) {
		for (int i = 0; i < cachedFolders.length; i++) {
			if (remoteFolder.getRepositoryRelativePath().startsWith(
					cachedFolders[i].getRepositoryRelativePath())) {
				return true;
			}
		}
		return false;
	}

	private ICVSRemoteResource[] getCachedChildren(
			ICVSRemoteResource[] cachedFolders, ICVSRemoteFolder remoteFolder,
			IProgressMonitor monitor) {
		Set remoteResources = new HashSet();
		monitor = Policy.monitorFor(monitor);
		monitor.beginTask(NLS.bind(
				CVSUIMessages.RemoteFolderElement_fetchingRemoteChildren,
				new String[] { getLabel(remoteFolder) }), cachedFolders.length);
		try {
			RepositoryRoot repositoryRoot = CVSUIPlugin.getPlugin()
					.getRepositoryManager()
					.getRepositoryRootFor(remoteFolder.getRepository());
			for (int i = 0; i < cachedFolders.length; i++) {
				if (cachedFolders[i].getRepositoryRelativePath().startsWith(
						remoteFolder.getRepositoryRelativePath())) {
					String path = new Path(
							cachedFolders[i].getRepositoryRelativePath())
							.uptoSegment(
									new Path(remoteFolder
											.getRepositoryRelativePath())
											.segmentCount() + 1).toString();
					remoteResources.add(repositoryRoot.getRemoteFolder(path,
							remoteFolder.getTag(),
							Policy.subMonitorFor(monitor, 1)));
				}
			}
			return (ICVSRemoteResource[]) remoteResources
					.toArray(new ICVSRemoteResource[remoteResources.size()]);
		} finally {
			monitor.done();
		}
	}

    public ISchedulingRule getRule(Object element) {
    	ICVSRepositoryLocation location = getRepositoryLocation(element);
        return new RepositoryLocationSchedulingRule(location); 
    }

	private ICVSRepositoryLocation getRepositoryLocation(Object o) {
		if (!(o instanceof ICVSRemoteFolder))
			return null;
		return ((ICVSRemoteFolder)o).getRepository();
	}

	public boolean isContainer() {
        return true;
    }
}
