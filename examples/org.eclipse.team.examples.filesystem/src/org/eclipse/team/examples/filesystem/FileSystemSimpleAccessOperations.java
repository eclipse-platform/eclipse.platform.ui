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
import org.eclipse.core.resources.IFolder;
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
		progress.beginTask(Policy.bind("GetAction.working"), resources.length);
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
							source = remote.getContents(progress);
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
				//If the resource is a container, copy its children over.
				IRemoteResource[] estranged = remote.members(progress);
				IResource[] children = new IResource[estranged.length];

				if (resources[i].getType() == IResource.PROJECT) {
					for (int j = 0; j < estranged.length; j++) {
						if (estranged[j].isContainer())
							children[j] = provider.getProject().getFolder(estranged[j].getName());
						else
							children[j] = provider.getProject().getFile(estranged[j].getName());
					}
				} else if (resources[i].getType() == IResource.FOLDER) {
					//Make sure that the folder exists before trying to put anything into it:
					IFolder localFolder = (IFolder) resources[i];
					if (!localFolder.exists()) {
						try {
							localFolder.create(false, true, progress);
						} catch (CoreException e) {
							throw FileSystemPlugin.wrapException(e);
						}
					}

					//Create placeholder local resources to place data into:
					for (int j = 0; j < estranged.length; j++) {
						if (estranged[j].isContainer())
							children[j] = provider.getProject().getFolder(resources[i].getProjectRelativePath().append(estranged[j].getName()));
						else
							children[j] = provider.getProject().getFile(resources[i].getProjectRelativePath().append(estranged[j].getName()));
					}
				}

				//Recurse into children:
				if (children.length > 0)
					get(children, depth - 1, null);
			}
			progress.worked(1);
		}
		progress.done();
	}

	/**
	 * Simply make sure that the local resource is not read only.
	 * 
	 * @see SimpleAccessOperations#checkout(IResource[], int, IProgressMonitor)
	 */
	public void checkout(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
		progress = Policy.monitorFor(progress);
		progress.beginTask("Checking resources out...", resources.length);
		IPath rootdir = provider.getRoot();
		for (int i = 0; i < resources.length; i++) {
			Policy.checkCanceled(progress);

			//Do the actual file locking:
			FileSystemRemoteResource remote = getRemoteResourceFor(resources[i]);
			File diskFile = new File(rootdir.append(resources[i].getProjectRelativePath()).toOSString());
			if (resources[i].getType() == IResource.FILE) {
				//TODO: lock the file on the 'server'.
				resources[i].setReadOnly(false);
			} else if (depth > 0) {
				diskFile.mkdirs();
				//Recursively checkout children too:
				try {
					IResource[] children;
					if (resources[i].getType() == IResource.PROJECT)
						children = provider.getProject().members();
					else
						children = provider.getProject().getFolder(resources[i].getName()).members();
					if (children.length > 0)
						checkout(children, depth - 1, null);
				} catch (CoreException e) {
					throw FileSystemPlugin.wrapException(e);
				}
			}
			progress.worked(1);
		}
		progress.done();
	}

	/**
	 * Checkin the resources to the given depth. Mark all checked in resources as read only.
	 * 
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#checkin(IResource[], int, IProgressMonitor)
	 */
	public void checkin(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
		// ensure the progress monitor is not null
		progress = Policy.monitorFor(progress);
		progress.beginTask(Policy.bind("PutAction.working"), resources.length);
		for (int i = 0; i < resources.length; i++) {
			Policy.checkCanceled(progress);
			IPath rootdir = provider.getRoot();
			// Verify that the resources are checked out:
			if (!isCheckedOut(resources[i]))
				return;

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
		uncheckout(resources, depth, progress);
		progress.done();
	}

	/**
	 * Mark all checked in resources as read only.
	 * 
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#uncheckout(IResource[], int, IProgressMonitor)
	 */
	public void uncheckout(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
		progress = Policy.monitorFor(progress);
		progress.beginTask("Re-locking resources...", resources.length);
		IPath rootdir = provider.getRoot();
		for (int i = 0; i < resources.length; i++) {
			Policy.checkCanceled(progress);

			//Do the actual file unlocking:
			FileSystemRemoteResource remote = getRemoteResourceFor(resources[i]);
			File diskFile = new File(rootdir.append(resources[i].getProjectRelativePath()).toOSString());
			if (resources[i].getType() == IResource.FILE) {
				//TODO: unlock the file on the 'server'.
				resources[i].setReadOnly(true);
			} else if (depth > 0) {
				diskFile.mkdirs();
				//Recursively uncheckout children too:
				try {
					IResource[] children;
					if (resources[i].getType() == IResource.PROJECT)
						children = provider.getProject().members();
					else
						children = provider.getProject().getFolder(resources[i].getName()).members();
					if (children.length > 0)
						uncheckout(children, depth - 1, null);
				} catch (CoreException e) {
					throw FileSystemPlugin.wrapException(e);
				}
			}
			progress.worked(1);
		}
		progress.done();
	}

	/**
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#delete(IResource[], IProgressMonitor)
	 */
	public void delete(IResource[] resources, IProgressMonitor progress) throws TeamException {}

	/**
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#moved(IPath, IResource, IProgressMonitor)
	 */
	public void moved(IPath source, IResource target, IProgressMonitor progress) throws TeamException {}

	/**
	 * A resource is checked out if it is not read only.
	 * 
	 * @see org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations#isCheckedOut(IResource)
	 */
	public boolean isCheckedOut(IResource resource) {
		return !resource.isReadOnly();
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
