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
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;

import com.ibm.jvm.io.FileOutputStream;

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
	FileSystemRemoteResource(File ioFile) {
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
			throw new TeamException(Policy.bind("RemoteResource.mustBeFolder", ioFile.getName()));

		// convert the File children to remote resource children
		File[] members = ioFile.listFiles();
		IRemoteResource[] result = new IRemoteResource[members.length];
		for (int i = 0; i < members.length; i++) {
			result[i] = new FileSystemRemoteResource(members[i]);
		}
		return result;
	}

	/**
	 * copies a single specified file to a specified location on the filesystem.
	 * @param dest The location on the filesystem to which the file is to be copied
	 * @param src The source file
	 */
	static void copyFile(IPath dest, File src) {
		File target = new File(dest.append(src.getName()).toOSString());
		try {
			InputStream in = ((IFile) src).getContents();
			java.io.FileOutputStream out = new java.io.FileOutputStream(target);
			StreamUtil.pipe(in, out, target.length(), null, target.getName());
		} catch (FileNotFoundException e) {} catch (IOException e) {} catch (CoreException e) {}
	}
	/**
	 * Recursively copies an entire directory structure to a specified location on the filesystem
	 * @param dest The location on the filssystem to which the directory structure is to be written
	 * @param src The directory structure that is to be duplicated
	 */
	static void copyFolder(IPath dest, File src) {
		String children[] = src.list();
		File current;
		for (int i = 0; i < children.length; i++) {
			current = new File(children[i]);
			if (current.isFile())
				copyFile(dest.append(src.getName()), current);
			else if (current.isDirectory())
				copyFolder(dest.append(src.getName()), current);
		}
	}

	/**
	 * Creates a copy of the remote resource in the location specified
	 * @param location The destination for the copy of the remote resource
	 */
	public void copyOver(IPath location) {
		copyFolder(location, ioFile);
	}

}