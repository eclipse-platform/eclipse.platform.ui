package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * Interface for an visitor of the IManagedResources.
 */
public interface ICVSResourceVisitor {
	public void visitFile(ICVSFile file) throws CVSException;
	public void visitFolder(ICVSFolder folder) throws CVSException;	
}

