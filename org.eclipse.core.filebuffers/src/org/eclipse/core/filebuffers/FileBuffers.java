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

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;

/**
 * Facade for the file buffers plug-in. Provides access to the
 * text file buffer manager.
 * 
 * @since 3.0
 */
public final class FileBuffers {
	
	/**
	 * Cannot be instantiated.
	 */
	private FileBuffers()  {
	}

	/**
	 * Returns the text file buffer manager.
	 * 
	 * @return the text file buffer manager
	 */
	public static ITextFileBufferManager getTextFileBufferManager()  {
		return FileBuffersPlugin.getDefault().getFileBufferManager();
	}
}
