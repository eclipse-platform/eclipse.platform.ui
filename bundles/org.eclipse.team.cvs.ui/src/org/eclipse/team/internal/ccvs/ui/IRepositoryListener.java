package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;

public interface IRepositoryListener {
	public void repositoryAdded(ICVSRepositoryLocation root);
	public void repositoryRemoved(ICVSRepositoryLocation root);
	public void branchTagAdded(BranchTag tag, ICVSRepositoryLocation root);
	public void branchTagRemoved(BranchTag tag, ICVSRepositoryLocation root);
	public void versionTagAdded(CVSTag tag, ICVSRepositoryLocation root);
	public void versionTagRemoved(CVSTag tag, ICVSRepositoryLocation root);
}

