/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.examples.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;

/**
 * Class represents a handle to a <code>java.io.File</code> that conforms to
 * the <code>org.eclipse.team.core.IRemoteResource</code> interface.
 */
public class FileSystemRemoteResource implements IRemoteResource {
	
	// the file object in which the data is stored on the disk
	private File ioFile;

	/**
	 * The constructor.
	 * @param path the full path of the resource on disk
	 */
	public FileSystemRemoteResource(IPath path) {
		this(new File(path.toOSString()));
	}
	
	/**
	 * Create a remote resource handle from the given java.io.file
	 * 
	 * @param ioFile the file
	 */
	private FileSystemRemoteResource(File ioFile) {
		this.ioFile = ioFile;
	}
	
	/**
	 * Adapters are used to ensure that the right menus will appear in differnet views.
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * Returns an input stream containing the contents of the remote resource.
	 * The remote resource must be a file.
	 * 
	 * @see org.eclipse.team.core.sync.IRemoteResource#getContents(IProgressMonitor)
	 */
	public InputStream getContents(IProgressMonitor progress) throws TeamException {
		if (isContainer())
			throw new TeamException("This resource is a container so it cannot have data.");
		try {
			return new FileInputStream(ioFile);
		} catch (FileNotFoundException e) {
			throw FileSystemPlugin.wrapException(e);
		}
	}
		
	/**
	 * Return the modification timestamp of the remote resource.
	 * 
	 * @return long The date and time (in milliseconds) when the file was last changed on disk.
	 */
	public long getLastModified() {
		return ioFile.lastModified();
	}

	/**
	 * @see org.eclipse.team.core.sync.IRemoteResource#getName()
	 */
	public String getName() {
		return ioFile.getName();
	}
		
	/**
	 * @see org.eclipse.team.core.sync.IRemoteResource#isContainer()
	 */
	public boolean isContainer() {
		return ioFile.isDirectory();
	}

	/**
	 * Fetch the members of the remote resource. The remote resource must be a 
	 * container.
	 * 
	 * @see org.eclipse.team.core.sync.IRemoteResource#members(IProgressMonitor)
	 */
	public IRemoteResource[] members(IProgressMonitor progress) throws TeamException {
		// Make sure we have a container
		if (!isContainer())
			throw new TeamException("This resource is a file so it cannot have entries.");
		
		// convert the File children to remote resource children
		File[] members = ioFile.listFiles();
		IRemoteResource[] result = new IRemoteResource[members.length];
		for (int i = 0; i < members.length; i++) {
			result[i] = new FileSystemRemoteResource(members[i]);
		}
		return result;
	}
	
}