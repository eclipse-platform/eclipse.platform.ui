package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.model.Tag;

public interface IRepositoryListener {
	public void repositoryAdded(ICVSRepositoryLocation root);
	public void repositoryRemoved(ICVSRepositoryLocation root);
	public void tagAdded(Tag tag, ICVSRepositoryLocation root);
	public void tagRemoved(Tag tag, ICVSRepositoryLocation root);
}

