/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * An editor input for a cvs annotation response
 */
public class RemoteAnnotationEditorInput extends RemoteFileEditorInput implements IWorkbenchAdapter, IStorageEditorInput {

	InputStream contents;
	
	public RemoteAnnotationEditorInput(ICVSRemoteFile file, InputStream contents) {
		super(file, new NullProgressMonitor());
		this.contents = contents;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.RemoteFileEditorInput#initializeStorage(org.eclipse.team.internal.ccvs.core.ICVSRemoteFile, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void initializeStorage(ICVSRemoteFile file, IProgressMonitor monitor) throws TeamException {
		if (contents != null) {
			storage = new RemoteAnnotationStorage(file, contents);
		}
	}
}
