package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;

public interface ICVSListener {
	public void repositoryAdded(ICVSRepositoryLocation root);
	public void repositoryRemoved(ICVSRepositoryLocation root);
}

