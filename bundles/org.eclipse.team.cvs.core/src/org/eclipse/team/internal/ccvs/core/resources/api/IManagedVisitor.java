package org.eclipse.team.internal.ccvs.core.resources.api;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * Interface for an visitor of the IManagedResources.
 */
public interface IManagedVisitor {
	
	public void visitFile(IManagedFile file) throws CVSException;
	public void visitFolder(IManagedFolder folder) throws CVSException;
	
}

