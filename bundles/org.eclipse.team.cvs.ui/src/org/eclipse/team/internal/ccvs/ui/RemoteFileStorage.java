package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;

public class RemoteFileStorage extends PlatformObject implements IStorage {
	ICVSRemoteFile file;
	public RemoteFileStorage(ICVSRemoteFile file) {
		this.file = file;
	}

	/**
	 * Returns an open input stream on the contents of this file.
	 * The client is responsible for closing the stream when finished.
	 *
	 * @return an input stream containing the contents of the file
	 * @exception CoreException if this method fails. 
	 */
	public InputStream getContents() throws CoreException {
		try {
			final InputStream[] holder = new InputStream[1];
			CVSUIPlugin.runWithProgress(null, true /*cancelable*/, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						holder[0] = file.getContents(monitor);
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
			return holder[0];
		} catch (InterruptedException e) {
			// operation canceled
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof TeamException) {
				throw new CoreException(((TeamException) t).getStatus());
			}
			// should not get here
		}
		return new ByteArrayInputStream(new byte[0]);
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

