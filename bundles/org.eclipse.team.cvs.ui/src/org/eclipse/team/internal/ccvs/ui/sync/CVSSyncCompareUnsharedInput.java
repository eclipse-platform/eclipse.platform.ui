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
package org.eclipse.team.internal.ccvs.ui.sync;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class CVSSyncCompareUnsharedInput extends CVSSyncCompareInput {
	
	private ICVSRepositoryLocation location;
	private String moduleName;
	private CVSTag tag;
	
	public CVSSyncCompareUnsharedInput(IProject project, ICVSRepositoryLocation location, String moduleName, CVSTag tag) {
		super(new IResource[] { project });
		this.location = location;
		this.moduleName = moduleName;
		this.tag = tag;
	}

	protected IRemoteSyncElement[] createSyncElements(IProgressMonitor monitor) throws TeamException {
		IResource[] resources = getResources();
		IRemoteSyncElement[] trees = new IRemoteSyncElement[resources.length];
		int work = 1000 * resources.length;
		monitor.beginTask(null, work);
		try {
			for (int i = 0; i < trees.length; i++) {
				trees[i] = CVSWorkspaceRoot.getRemoteSyncTree((IProject)resources[i], location, moduleName, tag, Policy.subMonitorFor(monitor, 1000));
			}
		} finally {
			monitor.done();
		}
		return trees;
	}
}
