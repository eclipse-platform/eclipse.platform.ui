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

public class RemoteAnnotationStorage extends RemoteFileStorage implements IStorage {

	InputStream contents;
	
	/**
	 * @param file
	 */
	public RemoteAnnotationStorage(ICVSRemoteFile file, InputStream contents) {
		super(file);
		this.contents = contents;
	}

	public InputStream getContents() throws CoreException {
		return contents;
	}

}
