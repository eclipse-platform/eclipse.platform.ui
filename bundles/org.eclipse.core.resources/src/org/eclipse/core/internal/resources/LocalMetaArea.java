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
package org.eclipse.core.internal.resources;

import java.io.*;
import org.eclipse.core.internal.localstore.SafeChunkyInputStream;
import org.eclipse.core.internal.localstore.SafeChunkyOutputStream;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class LocalMetaArea implements ICoreConstants {
	protected IPath metaAreaLocation;
	/* package */static final String F_BACKUP_FILE_EXTENSION = ".bak"; //$NON-NLS-1$
	/* package */static final String F_DESCRIPTION = ".workspace"; //$NON-NLS-1$
	/* package */static final String F_HISTORY_STORE = ".history"; //$NON-NLS-1$
	/* package */static final String F_MARKERS = ".markers"; //$NON-NLS-1$
	/* package */static final String F_OLD_PROJECT = ".prj"; //$NON-NLS-1$
	/* package */static final String F_PROJECT_LOCATION = ".location"; //$NON-NLS-1$
	/* package */static final String F_PROJECTS = ".projects"; //$NON-NLS-1$
	/* package */static final String F_PROPERTIES = ".properties"; //$NON-NLS-1$
	/* package */static final String F_ROOT = ".root"; //$NON-NLS-1$
	/* package */static final String F_SAFE_TABLE = ".safetable"; //$NON-NLS-1$
	/* package */static final String F_SNAP = ".snap"; //$NON-NLS-1$
	/* package */static final String F_SNAP_EXTENSION = "snap"; //$NON-NLS-1$
	/* package */static final String F_SYNCINFO = ".syncinfo"; //$NON-NLS-1$
	/* package */static final String F_TREE = ".tree"; //$NON-NLS-1$

	public LocalMetaArea() {
		super();
	}

	/**
	 * For backwards compatibility, if there is a project at the old project
	 * description location, delete it.
	 */
	public void clearOldDescription(IProject target) {
		Workspace.clear(getOldDescriptionLocationFor(target).toFile());
	}

	public void create(IProject target) {
		java.io.File file = locationFor(target).toFile();
		//make sure area is empty
		Workspace.clear(file);
		file.mkdirs();
	}

	/**
	 * Creates the meta area root directory.
	 */
	public synchronized void createMetaArea() throws CoreException {
		java.io.File workspaceLocation = getLocation().toFile();
		Workspace.clear(workspaceLocation);
		if (!workspaceLocation.mkdirs()) {
			String message = Policy.bind("resources.writeWorkspaceMeta", workspaceLocation.toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, null, message, null);
		}
	}

	/**
	 * The project is being deleted. Delete all meta-data associated with the
	 * project.
	 */
	public void delete(IProject target) throws CoreException {
		IPath path = locationFor(target);
		if (!Workspace.clear(path.toFile()) && path.toFile().exists()) {
			String message = Policy.bind("resources.deleteMeta", target.getFullPath().toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_DELETE_METADATA, target.getFullPath(), message, null);
		}
	}

	public IPath getBackupLocationFor(IPath file) {
		return file.removeLastSegments(1).append(file.lastSegment() + F_BACKUP_FILE_EXTENSION);
	}

	/**
	 * The project description file is the only metadata file stored outside
	 * the metadata area. It is stored as a file directly under the project
	 * location. For backwards compatibility, we also have to check for a
	 * project file at the old location in the metadata area.
	 */
	public IPath getOldDescriptionLocationFor(IProject target) {
		return locationFor(target).append(F_OLD_PROJECT);
	}

	public IPath getHistoryStoreLocation() {
		return getLocation().append(F_HISTORY_STORE);
	}

	/**
	 * Returns the local filesystem location which contains the META data for
	 * the resources plugin (i.e., the entire workspace).
	 */
	public IPath getLocation() {
		if (metaAreaLocation == null)
			metaAreaLocation = ResourcesPlugin.getPlugin().getStateLocation();
		return metaAreaLocation;
	}

	/**
	 * Returns the path of the file in which to save markers for the given
	 * resource. Should only be called for the workspace root and projects.
	 */
	public IPath getMarkersLocationFor(IResource resource) {
		Assert.isNotNull(resource);
		Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
		return locationFor(resource).append(F_MARKERS);
	}

	/**
	 * Returns the path of the file in which to snapshot markers for the given
	 * resource. Should only be called for the workspace root and projects.
	 */
	public IPath getMarkersSnapshotLocationFor(IResource resource) {
		return getMarkersLocationFor(resource).addFileExtension(F_SNAP_EXTENSION);
	}

	public IPath getPropertyStoreLocation(IResource resource) {
		int type = resource.getType();
		Assert.isTrue(type != IResource.FILE && type != IResource.FOLDER);
		return locationFor(resource).append(F_PROPERTIES);
	}

	public IPath getSafeTableLocationFor(String pluginId) {
		IPath prefix = getLocation().append(F_SAFE_TABLE);
		// if the plugin is the resources plugin, we return the master table
		// location
		if (pluginId.equals(ResourcesPlugin.PI_RESOURCES))
			return prefix.append(pluginId); // master table
		int saveNumber = getWorkspace().getSaveManager().getSaveNumber(pluginId);
		return prefix.append(pluginId + "." + saveNumber); //$NON-NLS-1$
	}

	public IPath getSnapshotLocationFor(IResource resource) {
		return getLocation().append(F_SNAP);
	}

	/**
	 * Returns the path of the file in which to save the sync information for
	 * the given resource. Should only be called for the workspace root and
	 * projects.
	 */
	public IPath getSyncInfoLocationFor(IResource resource) {
		Assert.isNotNull(resource);
		Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
		return locationFor(resource).append(F_SYNCINFO);
	}

	/**
	 * Returns the path of the file in which to snapshot the sync information
	 * for the given resource. Should only be called for the workspace root and
	 * projects.
	 */
	public IPath getSyncInfoSnapshotLocationFor(IResource resource) {
		return getSyncInfoLocationFor(resource).addFileExtension(F_SNAP_EXTENSION);
	}

	/**
	 * Returns the local file system location of the tree file for the given
	 * resource. This file does not follow the same save number as its plug-in.
	 * So, the number here is called "sequence number" and not "save number" to
	 * avoid confusion.
	 */
	public IPath getTreeLocationFor(IResource target, boolean updateSequenceNumber) {
		IPath key = target.getFullPath().append(F_TREE);
		String sequenceNumber = getWorkspace().getSaveManager().getMasterTable().getProperty(key.toString());
		if (sequenceNumber == null)
			sequenceNumber = "0"; //$NON-NLS-1$
		if (updateSequenceNumber) {
			int n = new Integer(sequenceNumber).intValue() + 1;
			n = n < 0 ? 1 : n;
			sequenceNumber = new Integer(n).toString();
			getWorkspace().getSaveManager().getMasterTable().setProperty(key.toString(), new Integer(sequenceNumber).toString());
		}
		return locationFor(target).append(sequenceNumber + F_TREE);
	}

	public IPath getWorkingLocation(IResource resource, String id) {
		return locationFor(resource).append(id);
	}

	protected Workspace getWorkspace() {
		return (Workspace) ResourcesPlugin.getWorkspace();
	}

	public IPath getOldWorkspaceDescriptionLocation() {
		return getLocation().append(F_DESCRIPTION);
	}

	public boolean hasSavedProject(IProject project) {
		//if there is a location file, then the project exists
		return getOldDescriptionLocationFor(project).toFile().exists() || locationFor(project).append(F_PROJECT_LOCATION).toFile().exists();
	}

	public boolean hasSavedWorkspace() {
		return getLocation().toFile().exists() || getBackupLocationFor(getLocation()).toFile().exists();
	}

	/**
	 * Returns the local filesystem location in which the meta data for the
	 * given resource is stored.
	 */
	public IPath locationFor(IResource resource) {
		if (resource.getType() == IResource.ROOT)
			return getLocation().append(F_ROOT);
		return getLocation().append(F_PROJECTS).append(resource.getProject().getName());
	}

	/**
	 * Returns the portions of the project description that are private, and
	 * adds them to the supplied project description. In particular, the
	 * project location and the project's dynamic references are stored here.
	 * The project location will be set to <code>null</code> if the default
	 * location should be used. In the case of failure, log the exception and
	 * return silently, thus reverting to using the default location and no
	 * dynamic references. The format of the location file for 3.0 is:
	 *    UTF - project location
	 *    int - number of dynamic project references
	 *    UTF - project reference 1
	 *    ... repeat for remaining references
	 */
	public void readPrivateDescription(IProject target, IProjectDescription description) {
		IPath locationFile = locationFor(target).append(F_PROJECT_LOCATION);
		java.io.File file = locationFile.toFile();
		if (!file.exists()) {
			locationFile = getBackupLocationFor(locationFile);
			file = locationFile.toFile();
			if (!file.exists())
				return;
		}
		try {
			SafeChunkyInputStream input = new SafeChunkyInputStream(file, 500);
			DataInputStream dataIn = new DataInputStream(input);
			try {
				try {
					String location = dataIn.readUTF();
					if (location.length() > 0)
						description.setLocation(Path.fromOSString(location));
				} catch (IOException e) {
					String msg = Policy.bind("resources.exReadProjectLocation", target.getName()); //$NON-NLS-1$
					ResourcesPlugin.getPlugin().getLog().log(new ResourceStatus(IStatus.ERROR, IResourceStatus.FAILED_READ_METADATA, target.getFullPath(), msg, e));
				}
				//try to read the dynamic references - will fail for old location files
				int numRefs = dataIn.readInt();
				IProject[] references = new IProject[numRefs];
				IWorkspaceRoot root = getWorkspace().getRoot();
				for (int i = 0; i < numRefs; i++)
					references[i] = root.getProject(dataIn.readUTF());
				description.setDynamicReferences(references);
			} finally {
				dataIn.close();
			}
		} catch (IOException e) {
			//ignore - this is an old location file or an exception occurred
			// closing the stream
		}
	}

	/**
	 * Reads and returns the project description for the given project. Returns
	 * null if there was no project description file on disk. Throws an
	 * exception if there was any failure to read the project.
	 */
	public ProjectDescription readOldDescription(IProject project) throws CoreException {
		IPath path = getOldDescriptionLocationFor(project);
		if (!path.toFile().exists())
			return null;
		IPath tempPath = getBackupLocationFor(path);
		ProjectDescription description = null;
		try {
			description = new ProjectDescriptionReader().read(path, tempPath);
		} catch (IOException e) {
			String msg = Policy.bind("resources.readMeta", project.getName()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, project.getFullPath(), msg, e);
		}
		if (description == null) {
			String msg = Policy.bind("resources.readMeta", project.getName()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, project.getFullPath(), msg, null);
		}
		return description;
	}

	/**
	 * Provides backward compatibility with existing workspaces based on
	 * descriptions.
	 */
	public WorkspaceDescription readOldWorkspace() {
		IPath path = getOldWorkspaceDescriptionLocation();
		IPath tempPath = getBackupLocationFor(path);
		try {
			WorkspaceDescription oldDescription = (WorkspaceDescription) new WorkspaceDescriptionReader().read(path, tempPath);
			// if one of those files exist, get rid of them
			Workspace.clear(path.toFile());
			Workspace.clear(tempPath.toFile());
			return oldDescription;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Write the private project description information, including the location
	 * and the dynamic project references.  See <tt>readPrivateDescription</tt>
	 * for details on the file format.
	 */
	public void writePrivateDescription(IProject target) throws CoreException {
		IPath location = locationFor(target).append(F_PROJECT_LOCATION);
		java.io.File file = location.toFile();
		//delete any old location file
		Workspace.clear(file);
		//don't write anything if there is no interesting private metadata
		ProjectDescription desc = ((Project) target).internalGetDescription();
		if (desc == null)
			return;
		final IPath projectLocation = desc.getLocation();
		final IProject[] references = desc.getDynamicReferences(false);
		final int numRefs = references.length;
		if (projectLocation == null && numRefs == 0)
			return;
		//write the private metadata file
		try {
			SafeChunkyOutputStream output = new SafeChunkyOutputStream(file);
			DataOutputStream dataOut = new DataOutputStream(output);
			try {
				String locationString = projectLocation == null ? "" : projectLocation.toOSString(); //$NON-NLS-1$
				dataOut.writeUTF(locationString);
				dataOut.writeInt(numRefs);
				for (int i = 0; i < numRefs; i++)
					dataOut.writeUTF(references[i].getName());
				output.succeed();
			} finally {
				dataOut.close();
			}
		} catch (IOException e) {
			String message = Policy.bind("resources.exSaveProjectLocation", target.getName()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, e);
		}
	}

	/**
	 * Writes the workspace description to the local meta area. This method is
	 * synchronized to prevent multiple current write attempts.
	 * 
	 * @deprecated should not be called any more - workspace preferences are
	 *                     now maintained in the plug-in's preferences
	 */
	public synchronized void write(WorkspaceDescription description) throws CoreException {
		IPath path = getOldWorkspaceDescriptionLocation();
		path.toFile().getParentFile().mkdirs();
		IPath tempPath = getBackupLocationFor(path);
		try {
			new ModelObjectWriter().write(description, path, tempPath);
		} catch (IOException e) {
			String message = Policy.bind("resources.writeWorkspaceMeta", path.toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, null, message, e);
		}
	}
}