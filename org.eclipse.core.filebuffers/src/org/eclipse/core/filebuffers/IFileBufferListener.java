/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.filebuffers;

import org.eclipse.core.resources.IFile;

/**
 * 
 */
public interface IFileBufferListener {

	void bufferContentAboutToBeReplaced(IFileBuffer buffer);
	
	void bufferContentReplaced(IFileBuffer buffer);

	void stateChanging(IFileBuffer buffer);
	
	void dirtyStateChanged(IFileBuffer buffer, boolean isDirty);
		
	void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated);

	void underlyingFileMoved(IFileBuffer buffer, IFile target);
	
	void underlyingFileDeleted(IFileBuffer buffer);

	void stateChangeFailed(IFileBuffer buffer);
}
