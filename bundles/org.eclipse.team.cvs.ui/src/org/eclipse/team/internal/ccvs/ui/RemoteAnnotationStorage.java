/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;

public class RemoteAnnotationStorage extends PlatformObject implements IEncodedStorage {

	private InputStream contents;
	private ICVSRemoteFile file;
	
	public RemoteAnnotationStorage(ICVSRemoteFile file, InputStream contents) {
		this.file = file;
		this.contents = contents;
	}

	public InputStream getContents() throws CoreException {
		try {
			// Contents are a ByteArrayInputStream which can be reset to the beginning
			contents.reset();
		} catch (IOException e) {
			CVSUIPlugin.log(CVSException.wrapException(e));
		}
		return contents;
	}

	public String getCharset() throws CoreException {
		InputStream contents = getContents();
		try {
			String charSet = TeamPlugin.getCharset(getName(), contents);
			return charSet;
		} catch (IOException e) {
			throw new CVSException(new Status(IStatus.ERROR, CVSUIPlugin.ID, IResourceStatus.FAILED_DESCRIBING_CONTENTS, Policy.bind("RemoteAnnotationStorage.1", getFullPath().toString()), e)); //$NON-NLS-1$
		} finally {
			try {
				contents.close();
			} catch (IOException e1) {
				// Ignore
			}
		}
	}

	public IPath getFullPath() {
		ICVSRepositoryLocation location = file.getRepository();
		IPath path = new Path(null, location.getRootDirectory());
		path = path.setDevice(location.getHost() + Path.DEVICE_SEPARATOR);
		path = path.append(file.getRepositoryRelativePath());
		return path;
	}
	public String getName() {
		return file.getName();
	}
	public boolean isReadOnly() {
		return true;
	}
}
