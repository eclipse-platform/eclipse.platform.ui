package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;

public class RemoteFileStorage extends PlatformObject implements IStorage {
	ICVSRemoteFile file;
	public RemoteFileStorage(ICVSRemoteFile file) {
		this.file = file;
	}
	public InputStream getContents() throws CoreException {
		try {
			return file.getContents(new NullProgressMonitor());
		} catch (TeamException e) {
			throw new CoreException(e.getStatus());
		}
	}
	public IPath getFullPath() {
		return new Path(file.getName());
	}
	public String getName() {
		return file.getName();
	}
	public boolean isReadOnly() {
		return true;
	}
}

