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

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.history.IFileState;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Operation that ensures that the contents for base
 * of each local resource is cached.
 */
public class CacheBaseContentsOperation extends CacheTreeContentsOperation {

	private final boolean includeOutgoing;

	public CacheBaseContentsOperation(IWorkbenchPart part, ResourceMapping[] mappers, IResourceDiffTree tree, boolean includeOutgoing) {
		super(part, mappers, tree);
		this.includeOutgoing = includeOutgoing;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CacheTreeContentsOperation#getRemoteFileState(org.eclipse.team.core.diff.IThreeWayDiff)
	 */
	protected IFileState getRemoteFileState(IThreeWayDiff twd) {
		IResourceDiff diff = (IResourceDiff)twd.getRemoteChange();
		if (diff == null)
			diff = (IResourceDiff)twd.getLocalChange();
		IFileState base = diff.getBeforeState();
		return base;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CacheTreeContentsOperation#isEnabledForDirection(int)
	 */
	protected boolean isEnabledForDirection(int direction) {
		return direction == IThreeWayDiff.CONFLICTING || 
		(includeOutgoing && direction == IThreeWayDiff.OUTGOING);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CacheTreeContentsOperation#buildTree(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
	 */
	protected ICVSRemoteResource buildTree(CVSTeamProvider provider) throws TeamException {
		return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().buildBaseTree(provider.getProject(), true, new NullProgressMonitor());
	}

}
