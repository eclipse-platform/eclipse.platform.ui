package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;

public interface ICVSListener {
	public void repositoryAdded(ICVSRepositoryLocation root);
	public void repositoryRemoved(ICVSRepositoryLocation root);
}

