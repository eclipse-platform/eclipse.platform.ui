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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations;

/**
 * SimpleAccessOperations is not part of the Team API. We use it here because it provides
 * a reasonable set of operation commonly implemented by repository providers.
 * Note: This class is not to be interpreted as an example of how a repository
 * provider is to do its work. It is only here because we needed to have some operations
 * to perform. In the future, we may update this class to illustrate the use of the workspace 
 * synchronizer (<code>ISynchronizer</code>).
 */
public class FileSystemSimpleAccessOperations implements SimpleAccessOperations {
	
	// A reference to the provider
	private FileSystemProvider provider;

	/**
	 * Constructor
	 * @param provider
	 */
	FileSystemSimpleAccessOperations(FileSystemProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * Given a local resource, finds the remote counterpart.
	 * @param resource The local resource to lookup
	 * @return FileSystemRemoteResource The remote counterpart to the given local resource
	 */
	public FileSystemRemoteResource getRemoteResourceFor(IResource resource) {
		return new FileSystemRemoteResource(provider.getRoot().append(resource.getProjectRelativePath()));
	}

	/**
	 * @see SimpleAccessOperations#get(IResource[], int, IProgressMonitor)
	 */
	public void get(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
		// ensure the progress monitor is not null
		progress = Policy.monitorFor(progress);
		progress.beginTask("Checking resources in...", resources.length);
		for (int i = 0; i < resources.length; i++) {
			Policy.checkCanceled(progress);
			IPath rootdir = provider.getRoot();

			FileSystemRemoteResource remote = getRemoteResourceFor(resources[i]);
			if (resources[i].getType() == IResource.FILE) {
				//Copy the resource over to the other side:
				IFile localFile = (IFile) resources[i]; //since we know the local resource is a file.
				if (localFile.getModificationStamp() != remote.getLastModified()) {
					//Only do this if the timestamps are different
					try {
						//Copy from the local file to the remote file:
						InputStream source = null;
						try {
							// Get the remote file content.
							source = remote.getContents(progress); //new FileInputStream(diskFile);
							// Set the local file content to be the same as the remote file.
							if (localFile.exists())
								localFile.setContents(source, false, false, progress);
							else
								localFile.create(source, false, progress);
						} finally {
							if (source != null)
								source.close();
						}
					} catch (IOException e) {
						throw FileSystemPlugin.wrapException(e);
					} catch (CoreException e) {
						throw FileSystemPlugin.wrapException(e);
					}
				}
			} else if (depth > 0) { //Assume that resources are either files or containers.
				//Recursively copy children, if any, over as well:
				try {
					IResource[] children;
					if (resources[i].getType() == IResource.PROJECT) {
						children = provider.getProject().members();
					} else {
						IRemoteResource[] estranged = remote.members(progress);
						children = new IResource[estranged.length];
						for (int j = 0; j < estranged.length; j++) {
							children[j] = provider.getProject().getFile(estranged[j].getName());
						}
					}
					if (children.length > 0)
						get(children, depth - 1, null);
				} catch (CoreException e) {
					throw FileSystemPlugin.wrapException(e);
				}
			}
			progress.worked(1);
		}
		//TODO: release lock (i.e. diskFile should no longer be locked).
		progress.done();
	}

	/**
	 * @see SimpleAccessOperations#checkout(IResource[], int, IProgressMonitor)
	 */
	public void checkout(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {}

	/**
	 * Checkin the resources to the given depth.
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#checkin(IResource[], int, IProgressMonitor)
	 */
	public void checkin(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
		// ensure the progress monitor is not null
		progress = Policy.monitorFor(progress);
		progress.beginTask("Checking resources in...", resources.length);
		for (int i = 0; i < resources.length; i++) {
			Policy.checkCanceled(progress);
			//TODO: verify that the resources are checked out.
			IPath rootdir = provider.getRoot();
			File diskFile = new File(rootdir.append(resources[i].getProjectRelativePath()).toOSString());
			if (resources[i].getType() == IResource.FILE) {
				//Copy the resource over to the other side:
				IFile localFile = (IFile) resources[i]; //since we know the local resource is a file.
				if (localFile.getModificationStamp() != diskFile.lastModified()) {
					//Only do this if the timestamps are different
					try {
						diskFile.getParentFile().mkdirs();
						//Copy from the local file to the remote file:
						InputStream in = null;
						FileOutputStream out = null;
						try {
							in = localFile.getContents();
							out = new FileOutputStream(diskFile);
							//Copy the contents of the local file to the remote file:
							StreamUtil.pipe(in, out, diskFile.length(), progress, diskFile.getName());
						} finally {
							if (in != null)
								in.close();
							if (out != null)
								out.close();
						}
					} catch (IOException e) {
						throw FileSystemPlugin.wrapException(e);
					} catch (CoreException e) {
						throw FileSystemPlugin.wrapException(e);
					}
				}
			} else if (depth > 0) { //Assume that resources are either files or containers.
				diskFile.mkdirs();
				//Recursively copy children, if any, over as well:
				try {
					IResource[] children;
					if (resources[i].getType() == IResource.PROJECT)
						children = provider.getProject().members();
					else
						children = provider.getProject().getFolder(resources[i].getName()).members();
					if (children.length > 0)
						checkin(children, depth - 1, null);
				} catch (CoreException e) {
					throw FileSystemPlugin.wrapException(e);
				}
			}
			progress.worked(1);
		}
		//TODO: release lock (i.e. diskFile should no longer be locked).
		progress.done();
	}

	/**
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#uncheckout(IResource[], int, IProgressMonitor)
	 */
	public void uncheckout(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {}

	/**
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#delete(IResource[], IProgressMonitor)
	 */
	public void delete(IResource[] resources, IProgressMonitor progress) throws TeamException {}

	/**
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#moved(IPath, IResource, IProgressMonitor)
	 */
	public void moved(IPath source, IResource target, IProgressMonitor progress) throws TeamException {}

	/**
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#isCheckedOut(IResource)
	 */
	public boolean isCheckedOut(IResource resource) {
		return false;
	}

	/**
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#hasRemote(IResource)
	 */
	public boolean hasRemote(IResource resource) {
		return false;
	}

	/**
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#isDirty(IResource)
	 */
	public boolean isDirty(IResource resource) {
		return false;
	}

}
