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
	public void branchTagsAdded(CVSTag[] tags, ICVSRepositoryLocation root);
	public void branchTagsRemoved(CVSTag[] tags, ICVSRepositoryLocation root);
	public void versionTagsAdded(CVSTag[] tags, ICVSRepositoryLocation root);
	public void versionTagsRemoved(CVSTag[] tags, ICVSRepositoryLocation root);
}

