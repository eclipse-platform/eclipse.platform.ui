/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.variants;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.IFileState;

/**
 * Abstract implementation of {@link IFileState} that can be implemented by 
 * clients.
 * 
 * <p> <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see IFileRevision
 * @see IFileState
 * 
 * @since 3.2
 */
public abstract class FileState implements IFileState {
	
	/**
	 * Return a file state representing the current state of the
	 * local file.
	 * @param file a local file
	 * @return a file state representing the current state of the
	 * local file
	 */
	public static IFileState getFileStateFor(final IFile file) {
		return new FileState() {
			public IStorage getStorage(IProgressMonitor monitor) {
				return file;
			}
			public String getName() {
				return file.getName();
			}
			public boolean exists() {
				return file.exists();
			}
			public long getTimestamp() {
				return file.getLocalTimeStamp();
			}
			public URI getURI() {
				return file.getLocationURI();
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.history.IFileState#getURI()
	 */
	public URI getURI() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.history.IFileState#getTimestamp()
	 */
	public long getTimestamp() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.history.IFileState#exists()
	 */
	public boolean exists() {
		return true;
	}

}
