/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * An editor input for a cvs annotation response
 */
public class RemoteAnnotationEditorInput extends RemoteFileEditorInput implements IWorkbenchAdapter, IStorageEditorInput {

	InputStream contents;
	
	/**
	 * @param contents
	 */
	public RemoteAnnotationEditorInput(ICVSRemoteFile file, InputStream contents) {
		super(file);
		this.contents = contents;
	}
	
	/**
	 * Returns the underlying IStorage object.
	 *
	 * @return an IStorage object.
	 * @exception CoreException if this method fails
	 */
	public IStorage getStorage() throws CoreException {
		if (storage == null) {
			storage = new RemoteAnnotationStorage(file, contents);
		}
		return storage;
	}
}
