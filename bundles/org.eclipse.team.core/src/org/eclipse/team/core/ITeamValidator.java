package org.eclipse.team.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
 
public interface ITeamValidator {
	
	public IStatus validateEdit(IFile[] files, Object data);
	
	public IStatus validateSave(IFile[] files, Object data);
}

