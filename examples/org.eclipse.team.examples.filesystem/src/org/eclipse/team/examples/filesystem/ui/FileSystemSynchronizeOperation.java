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
package org.eclipse.team.examples.filesystem.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.examples.filesystem.FileSystemPlugin;
import org.eclipse.team.examples.filesystem.FileSystemProvider;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

/**
 * Override SynchronizeModelOperation in order to delegate the operation to each file system 
 * provider instance (i.e. each project). Also, prompt to prune conflicts from the set of
 * selected resources.
 */
public abstract class FileSystemSynchronizeOperation extends SynchronizeModelOperation {

	protected FileSystemSynchronizeOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		super(configuration, elements);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		// First, ask the user if they want to include conflicts
		SyncInfoSet syncSet = getSyncInfoSet();
		if (!promptForConflictHandling(getShell(), syncSet)) return;
		// Divide the sync info by project
		final Map projectSyncInfos = getProjectSyncInfoSetMap(syncSet);
		monitor.beginTask(null, projectSyncInfos.size() * 100);
		for (Iterator iter = projectSyncInfos.keySet().iterator(); iter.hasNext(); ) {
			final IProject project = (IProject) iter.next();
			try {
				// Pass the scheduling rule to the synchronizer so that sync change events
				// and cache commits to disk are batched
				FileSystemProvider provider = (FileSystemProvider)RepositoryProvider.getProvider(project, FileSystemPlugin.PROVIDER_ID);
				if (provider != null) {
					run(provider, (SyncInfoSet)projectSyncInfos.get(project), monitor);
				}
			} catch (TeamException e) {
				throw new InvocationTargetException(e);
			}
		}
		monitor.done();
	}

	/**
	 * Prompt the user to include conflicts. If the user choses not to include
	 * conflicts, they will be removed from the passed set. If the user cancels,
	 * <code>false</code> is returned.
	 * @param shell a shell
	 * @param syncSet the set of selected resources
	 * @return whether the operation should proceed.
	 */
	protected abstract boolean promptForConflictHandling(Shell shell, SyncInfoSet syncSet);

	/*
	 * Divide the sync info for the operation by project
	 */
	private Map getProjectSyncInfoSetMap(SyncInfoSet syncSet) {
		Map map = new HashMap();
		SyncInfo[] infos = syncSet.getSyncInfos();
		for (int i = 0; i < infos.length; i++) {
			SyncInfo info = infos[i];
			IProject project = info.getLocal().getProject();
			SyncInfoSet set = (SyncInfoSet)map.get(project);
			if (set == null) {
				set = new SyncInfoSet();
				map.put(project, set);
			}
			set.add(info);
		}
		return map;
	}
	
	/**
	 * Run the operation on the sync info in the given set. The sync info will be all
	 * from the same project.
	 * @param provider
	 * @param set the sync info set
	 * @param monitor a progress monitor
	 */
	protected abstract void run(FileSystemProvider provider, SyncInfoSet set, IProgressMonitor monitor) throws TeamException;
}
