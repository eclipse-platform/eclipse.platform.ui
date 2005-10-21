/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Abstract operation for caching the contents for any files
 * in a particular remote tree that differ from the local contents.*
 */
public abstract class CacheTreeContentsOperation extends SingleCommandOperation {

	private final SyncInfoTree tree;

	public CacheTreeContentsOperation(IWorkbenchPart part, ResourceMapping[] mappers, SyncInfoTree tree) {
		super(part, mappers, Command.NO_LOCAL_OPTIONS);
		this.tree = tree;
	}
	
	protected void execute(CVSTeamProvider provider, IResource[] resources, boolean recurse, IProgressMonitor monitor) throws CVSException, InterruptedException {
		IResource[] files = getFilesWithUncachedContents(resources, recurse);
		if (files.length > 0)
			super.execute(provider, files, recurse, monitor);
	}
	
	private IResource[] getFilesWithUncachedContents(IResource[] resources, boolean recurse) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			SyncInfo[] infos = tree.getSyncInfos(resource, recurse ? IResource.DEPTH_INFINITE: IResource.DEPTH_ONE);
			for (int j = 0; j < infos.length; j++) {
				SyncInfo info = infos[j];
				if (needsContents(info)) {
					result.add(info.getLocal());
				}
			}
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}

	protected abstract boolean needsContents(SyncInfo info);
	
	/* (non-Javadoc)
	 * 
	 * Use a local root that is really the base tree so we can cache
	 * the base contents without affecting the local contents.
	 * 
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getLocalRoot(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
	 */
	protected ICVSFolder getLocalRoot(CVSTeamProvider provider)
			throws CVSException {
		try {
			ICVSRemoteResource tree = buildTree(provider);
			return (ICVSFolder)tree;
		} catch (TeamException e) {
			throw CVSException.wrapException(e);
		}
	}

	protected abstract ICVSRemoteResource buildTree(CVSTeamProvider provider) throws TeamException;
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getCVSArguments(org.eclipse.core.resources.IResource[])
	 */
	protected ICVSResource[] getCVSArguments(Session session, IResource[] resources) {
        List result = new ArrayList();
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            try {
				ICVSResource file = session.getLocalRoot().getChild(resource.getProjectRelativePath().toString());
				result.add(file);
			} catch (CVSException e) {
				// Log and continue
				CVSUIPlugin.log(e);
			}
        }

        return (ICVSResource[]) result.toArray(new ICVSResource[result.size()]);
	}
	
	protected IStatus executeCommand(Session session, CVSTeamProvider provider, ICVSResource[] resources, boolean recurse, IProgressMonitor monitor) throws CVSException, InterruptedException {
		return Command.UPDATE.execute(
                session,
                Command.NO_GLOBAL_OPTIONS,
                getLocalOptions(true),
                resources,
                null,
                monitor);
	}

	protected LocalOption[] getLocalOptions(boolean recurse) {
		return Update.IGNORE_LOCAL_CHANGES.addTo(super.getLocalOptions(recurse));
	}
	
	protected String getTaskName(CVSTeamProvider provider) {
		return NLS.bind("Fetching contents for changed files in {0}", new String[] {provider.getProject().getName()});
	}

	protected String getTaskName() {
		return "Fetching contents for changed files";
	}

}
