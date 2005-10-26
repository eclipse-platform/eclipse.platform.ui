/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.io.*;
import java.net.URI;
import java.util.*;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.InputSource;

/**
 * Manages the synchronization between the workspace's view and the file system.  
 */
public class FileSystemResourceManager implements ICoreConstants, IManager {

	/**
	 * The history store is initialized lazily - always use the accessor method
	 */
	protected IHistoryStore _historyStore;
	protected Workspace workspace;

	public FileSystemResourceManager(Workspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * Returns the workspace paths of all resources that may correspond to
	 * the given file system location.  Returns an empty ArrayList if there are no 
	 * such paths.  This method does not consider whether resources actually 
	 * exist at the given locations.
	 */
	protected ArrayList allPathsForLocation(IPath location) {
		IProject[] projects = getWorkspace().getRoot().getProjects();
		final ArrayList results = new ArrayList();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			//check the project location
			IPath testLocation = project.getLocation();
			IPath suffix;
			if (testLocation != null && testLocation.isPrefixOf(location)) {
				suffix = location.removeFirstSegments(testLocation.segmentCount());
				results.add(project.getFullPath().append(suffix));
			}
			if (!project.isAccessible())
				continue;
			IResource[] children = null;
			try {
				children = project.members();
			} catch (CoreException e) {
				//ignore projects that cannot be accessed
			}
			if (children == null)
				continue;
			for (int j = 0; j < children.length; j++) {
				IResource child = children[j];
				if (child.isLinked()) {
					testLocation = child.getLocation();
					if (testLocation != null && testLocation.isPrefixOf(location)) {
						//add the full workspace path of the corresponding child of the linked resource
						suffix = location.removeFirstSegments(testLocation.segmentCount());
						results.add(child.getFullPath().append(suffix));
					}
				}
			}
		}
		return results;
	}

	/**
	 * Returns all resources that correspond to the given file system location,
	 * including resources under linked resources.  Returns an empty array
	 * if there are no corresponding resources.
	 * @param location the file system location
	 * @param files resources that may exist below the project level can
	 * be either files or folders.  If this parameter is true, files will be returned,
	 * otherwise containers will be returned.
	 */
	public IResource[] allResourcesFor(IPath location, boolean files) {
		ArrayList result = allPathsForLocation(location);
		int count = 0;
		for (int i = 0, imax = result.size(); i < imax; i++) {
			//replace the path in the list with the appropriate resource type
			IResource resource = resourceFor((IPath) result.get(i), files);
			result.set(i, resource);
			//count actual resources - some paths won't have a corresponding resource
			if (resource != null)
				count++;
		}
		//convert to array and remove null elements
		IResource[] toReturn = files ? (IResource[]) new IFile[count] : (IResource[]) new IContainer[count];
		count = 0;
		for (Iterator it = result.iterator(); it.hasNext();) {
			IResource resource = (IResource) it.next();
			if (resource != null)
				toReturn[count++] = resource;
		}
		return toReturn;
	}

	/* (non-javadoc)
	 * @see IResource.getResourceAttributes
	 */
	public ResourceAttributes attributes(IResource resource) {
		IFileStore store = getStore(resource);
		IFileInfo fileInfo = store.fetchInfo();
		if (!fileInfo.exists())
			return null;
		return FileUtil.fileInfoToAttributes(fileInfo);
	}

	/**
	 * Returns a container for the given file system location or null if there
	 * is no mapping for this path. If the path has only one segment, then an 
	 * <code>IProject</code> is returned.  Otherwise, the returned object
	 * is a <code>IFolder</code>.  This method does NOT check the existence
	 * of a folder in the given location. Location cannot be null.
	 */
	public IContainer containerForLocation(IPath location) {
		IPath path = pathForLocation(location);
		return path == null ? null : (IContainer) resourceFor(path, false);
	}

	public void copy(IResource target, IResource destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			int totalWork = ((Resource) target).countResources(IResource.DEPTH_INFINITE, false);
			String title = NLS.bind(Messages.localstore_copying, target.getFullPath());
			monitor.beginTask(title, totalWork);
			IFileStore destinationStore = getStore(destination);
			if (destinationStore.fetchInfo().exists()) {
				String message = NLS.bind(Messages.localstore_resourceExists, destination.getFullPath());
				throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, destination.getFullPath(), message, null);
			}
			getHistoryStore().copyHistory(target, destination, false);
			CopyVisitor visitor = new CopyVisitor(target, destination, updateFlags, monitor);
			UnifiedTree tree = new UnifiedTree(target);
			tree.accept(visitor, IResource.DEPTH_INFINITE);
			IStatus status = visitor.getStatus();
			if (!status.isOK())
				throw new ResourceException(status);
		} finally {
			monitor.done();
		}
	}

	public void delete(IResource target, int flags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			Resource resource = (Resource) target;
			int totalWork = resource.countResources(IResource.DEPTH_INFINITE, false);
			boolean force = (flags & IResource.FORCE) != 0;
			if (!force)
				totalWork += 100;
			String title = NLS.bind(Messages.localstore_deleting, resource.getFullPath());
			monitor.beginTask(title, totalWork);
			monitor.subTask(""); //$NON-NLS-1$
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_DELETE_LOCAL, Messages.localstore_deleteProblem, null);
			List skipList = null;
			UnifiedTree tree = new UnifiedTree(target);
			if (!force) {
				IProgressMonitor sub = Policy.subMonitorFor(monitor, 100);
				sub.beginTask("", 1000); //$NON-NLS-1$
				try {
					CollectSyncStatusVisitor refreshVisitor = new CollectSyncStatusVisitor(Messages.localstore_deleteProblem, sub);
					refreshVisitor.setIgnoreLocalDeletions(true);
					tree.accept(refreshVisitor, IResource.DEPTH_INFINITE);
					status.merge(refreshVisitor.getSyncStatus());
					skipList = refreshVisitor.getAffectedResources();
				} finally {
					sub.done();
				}
			}
			DeleteVisitor deleteVisitor = new DeleteVisitor(skipList, flags, monitor, totalWork / 2);
			tree.accept(deleteVisitor, IResource.DEPTH_INFINITE);
			status.merge(deleteVisitor.getStatus());
			if (!status.isOK())
				throw new ResourceException(status);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns true if the description on disk is different from the given byte array,
	 * and false otherwise.
	 */
	private boolean descriptionChanged(IFile descriptionFile, byte[] newContents) {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(descriptionFile.getContents(true));
			int newLength = newContents.length;
			byte[] oldContents = new byte[newLength];
			int read = stream.read(oldContents);
			if (read != newLength)
				return true;
			//if the stream still has bytes available, then the description is changed
			if (stream.read() >= 0)
				return true;
			return !Arrays.equals(newContents, oldContents);
		} catch (Exception e) {
			System.out.println(e);
			//if we failed to compare, just write the new contents
		} finally {
			FileUtil.safeClose(stream);
		}
		return true;
	}

	/**
	 * @deprecated
	 */
	public int doGetEncoding(IFileStore store) throws CoreException {
		InputStream input = null;
		try {
			input = store.openInputStream(EFS.NONE, null);
			int first = input.read();
			int second = input.read();
			if (first == -1 || second == -1)
				return IFile.ENCODING_UNKNOWN;
			first &= 0xFF;//converts unsigned byte to int
			second &= 0xFF;
			//look for the UTF-16 Byte Order Mark (BOM)
			if (first == 0xFE && second == 0xFF)
				return IFile.ENCODING_UTF_16BE;
			if (first == 0xFF && second == 0xFE)
				return IFile.ENCODING_UTF_16LE;
			int third = (input.read() & 0xFF);
			if (third == -1)
				return IFile.ENCODING_UNKNOWN;
			//look for the UTF-8 BOM
			if (first == 0xEF && second == 0xBB && third == 0xBF)
				return IFile.ENCODING_UTF_8;
			return IFile.ENCODING_UNKNOWN;
		} catch (IOException e) {
			String message = NLS.bind(Messages.localstore_couldNotRead, store.toString());
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, null, message, e);
		} finally {
			FileUtil.safeClose(input);
		}
	}

	/**
	 * Optimized sync check for files.  Returns true if the file exists and is in sync, and false
	 * otherwise.  The intent is to let the default implementation handle the complex
	 * cases like gender change, case variants, etc.
	 */
	public boolean fastIsSynchronized(File target) {
		ResourceInfo info = target.getResourceInfo(false, false);
		if (target.exists(target.getFlags(info), true)) {
			IFileInfo fileInfo= getStore(target).fetchInfo();
			if (!fileInfo.isDirectory() && info.getLocalSyncInfo() == fileInfo.getLastModified())
				return true;
		}
		return false;
	}

	/**
	 * Returns an IFile for the given file system location or null if there
	 * is no mapping for this path. This method does NOT check the existence
	 * of a file in the given location. Location cannot be null.
	 */
	public IFile fileForLocation(IPath location) {
		IPath path = pathForLocation(location);
		return path == null ? null : (IFile) resourceFor(path, true);
	}

	/**
	 * @deprecated
	 */
	public int getEncoding(File target) throws CoreException {
		// thread safety: (the location can be null if the project for this file does not exist)
		IFileStore store = getStore(target);
		if (!store.fetchInfo().exists()) {
			String message = NLS.bind(Messages.localstore_fileNotFound, store.toString());
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
		}
		return doGetEncoding(store);
	}

	public IHistoryStore getHistoryStore() {
		if (_historyStore == null) {
			IPath location = getWorkspace().getMetaArea().getHistoryStoreLocation();
			location.toFile().mkdirs();
			_historyStore = ResourcesCompatibilityHelper.createHistoryStore(location, 256);
		}
		return _historyStore;
	}

	/** 
	 * Returns the real name of the resource on disk. Returns null if no local
	 * file exists by that name.  This is useful when dealing with
	 * case insensitive file systems.
	 */
	public String getLocalName(IFileStore target) {
		IFileStore parent = target.getParent();
		try {
			String[] list = parent.childNames(EFS.NONE, null);
			String targetName = target.getName();
			for (int i = 0; i < list.length; i++)
				if (targetName.equalsIgnoreCase(list[i]))
					return list[i];
		} catch (CoreException e) {
			//fall through and treat non-accessible directory as non-existent directory
		}
		return null;
	}

	protected IPath getProjectDefaultLocation(IProject project) {
		return Platform.getLocation().append(project.getFullPath());
	}

	/**
	 * Never returns null
	 * @param target
	 * @return The file store for this resource
	 */
	public IFileStore getStore(IResource target) {
		return getStoreRoot(target).createStore(target.getFullPath());
	}

	/**
	 * Returns the file store root for the provided resource. Never returns null.
	 */
	private FileStoreRoot getStoreRoot(IResource target) {
		ResourceInfo info = workspace.getResourceInfo(target.getFullPath(), true, false);
		FileStoreRoot root;
		if (info != null) {
			root = info.getFileStoreRoot();
			if (root != null && root.isValid())
				return root;
		}
		final IContainer parent = target.getParent();
		if (parent == null) {
			//this is the root, so we know where this must be located
			//initialize root location
			info = workspace.getResourceInfo(Path.ROOT, false, true);
			setLocation(workspace.getRoot(), info, FileUtil.toURI(Platform.getLocation()));
			return info.getFileStoreRoot();
		}
		root = getStoreRoot(parent);
		if (info != null)
			info.setFileStoreRoot(root);
		return root;
	}

	protected Workspace getWorkspace() {
		return workspace;
	}

	/**
	 * Returns whether the project has any local content on disk.
	 */
	public boolean hasSavedContent(IProject project) {
		return getStore(project).fetchInfo().exists();
	}

	/**
	 * Returns whether the project has a project description file on disk.
	 */
	public boolean hasSavedDescription(IProject project) {
		return getStore(project).getChild(IProjectDescription.DESCRIPTION_FILE_NAME).fetchInfo().exists();
	}

	/**
	 * Initializes the file store for a resource.
	 * 
	 * @param target The resource to initialize the file store for.
	 * @param location the File system location of this resource on disk
	 * @return The file store for the provided resource
	 */
	private IFileStore initializeStore(IResource target, URI location) {
		FileStoreRoot root = new FileStoreRoot(location, target.getFullPath());
		IFileStore store = root.createStore(target.getFullPath());
		ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
		info.setFileStoreRoot(root);
		return store;
	}

	/**
	 * The target must exist in the workspace.  This method must only ever
	 * be called from Project.writeDescription(), because that method ensures
	 * that the description isn't then immediately discovered as a new change.
	 * @return true if a new description was written, and false if it wasn't written
	 * because it was unchanged
	 */
	public boolean internalWrite(IProject target, IProjectDescription description, int updateFlags, boolean hasPublicChanges, boolean hasPrivateChanges) throws CoreException {
		//write the project's private description to the metadata area
		if (hasPrivateChanges)
			getWorkspace().getMetaArea().writePrivateDescription(target);
		if (!hasPublicChanges)
			return false;
		//can't do anything if there's no description
		if (description == null)
			return false;

		//write the model to a byte array
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new ModelObjectWriter().write(description, out);
		} catch (IOException e) {
			String msg = NLS.bind(Messages.resources_writeMeta, target.getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, target.getFullPath(), msg, e);
		}
		byte[] newContents = out.toByteArray();

		//write the contents to the IFile that represents the description
		IFile descriptionFile = target.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		if (!descriptionFile.exists())
			workspace.createResource(descriptionFile, false);
		else {
			//if the description has not changed, don't write anything
			if (!descriptionChanged(descriptionFile, newContents))
				return false;
		}
		ByteArrayInputStream in = new ByteArrayInputStream(newContents);
		if (descriptionFile.isReadOnly()) {
			IStatus result = getWorkspace().validateEdit(new IFile[] {descriptionFile}, null);
			if (!result.isOK())
				throw new ResourceException(result);
		}
		descriptionFile.setContents(in, updateFlags, null);

		//update the timestamp on the project as well so we know when it has
		//been changed from the outside
		long lastModified = ((Resource) descriptionFile).getResourceInfo(false, false).getLocalSyncInfo();
		ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
		updateLocalSync(info, lastModified);

		//for backwards compatibility, ensure the old .prj file is deleted
		getWorkspace().getMetaArea().clearOldDescription(target);
		return true;
	}

	/**
	 * Returns true if the given project's description is synchronized with
	 * the project description file on disk, and false otherwise.
	 */
	public boolean isDescriptionSynchronized(IProject target) {
		//sync info is stored on the description file, and on project info.
		//when the file is changed by someone else, the project info modification
		//stamp will be out of date
		IFile descriptionFile = target.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		ResourceInfo projectInfo = ((Resource) target).getResourceInfo(false, false);
		if (projectInfo == null)
			return false;
		return projectInfo.getLocalSyncInfo() == getStore(descriptionFile).fetchInfo().getLastModified();
	}

	/* (non-Javadoc)
	 * Returns true if the given resource is synchronized with the file system
	 * to the given depth.  Returns false otherwise.
	 * 
	 * @see IResource#isSynchronized(int)
	 */
	public boolean isSynchronized(IResource target, int depth) {
		switch (target.getType()) {
			case IResource.ROOT :
				if (depth == IResource.DEPTH_ZERO)
					return true;
				//check sync on child projects.
				depth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : depth;
				IProject[] projects = ((IWorkspaceRoot) target).getProjects();
				for (int i = 0; i < projects.length; i++) {
					if (!isSynchronized(projects[i], depth))
						return false;
				}
				return true;
			case IResource.PROJECT :
				if (!target.isAccessible())
					return true;
				break;
			case IResource.FILE :
				if (fastIsSynchronized((File) target))
					return true;
				break;
		}
		IsSynchronizedVisitor visitor = new IsSynchronizedVisitor(Policy.monitorFor(null));
		UnifiedTree tree = new UnifiedTree(target);
		try {
			tree.accept(visitor, depth);
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
			return false;
		} catch (IsSynchronizedVisitor.ResourceChangedException e) {
			//visitor throws an exception if out of sync
			return false;
		}
		return true;
	}

	public void link(Resource target, URI location) {
		IFileStore store = initializeStore(target, location);
		ResourceInfo info = target.getResourceInfo(false, true);
		long lastModified = store.fetchInfo().getLastModified();
		if (lastModified == 0)
			info.clearModificationStamp();
		updateLocalSync(info, lastModified);
	}

	/**
	 * Returns the resolved, absolute file system location of the given resource.
	 * Returns null if the location could not be resolved.
	 */
	public IPath locationFor(IResource target) {
		//note: this method is a critical performance path,
		//code may be in-lined to prevent method calls
		switch (target.getType()) {
			case IResource.ROOT :
				return Platform.getLocation();
			case IResource.PROJECT :
				Project project = (Project) target;
				ProjectDescription description = project.internalGetDescription();
				if (description != null && description.getLocation() != null) {
					IPath resolved = workspace.getPathVariableManager().resolvePath(description.getLocation());
					//if path is still relative then path variable could not be resolved
					return resolved != null && resolved.isAbsolute() ? resolved : null;
				}
				return getProjectDefaultLocation(project);
			default :
				//check if the resource is a linked resource
				IPath targetPath = target.getFullPath();
				int numSegments = targetPath.segmentCount();
				IResource linked = target;
				if (numSegments > 2) {
					//parent could be a linked resource
					linked = workspace.getRoot().getFolder(targetPath.removeLastSegments(numSegments - 2));
				}
				description = ((Project) target.getProject()).internalGetDescription();
				if (linked.isLinked()) {
					IPath location = description.getLinkLocation(linked.getName());
					//location may have been deleted from the project description between sessions
					if (location != null) {
						location = workspace.getPathVariableManager().resolvePath(location);
						//if path is still relative then path variable could not be resolved
						if (!location.isAbsolute())
							return null;
						return location.append(targetPath.removeFirstSegments(2));
					}
				}
				//not a linked resource -- get location of project
				if (description != null && description.getLocation() != null) {
					IPath resolved = workspace.getPathVariableManager().resolvePath(description.getLocation());
					//if path is still relative then path variable could not be resolved
					if (!resolved.isAbsolute())
						return null;
					return resolved.append(target.getProjectRelativePath());
				}
				return Platform.getLocation().append(target.getFullPath());
		}
	}

	public void move(IResource source, IFileStore destination, int flags, IProgressMonitor monitor) throws CoreException {
		IFileStore sourceStore = getStore(source);
		int storeFlags = 0;
		if ((flags & IResource.FORCE) != 0)
			storeFlags &= EFS.OVERWRITE;
		sourceStore.move(destination, storeFlags, monitor);
	}

	public void move(IResource source, IResource destination, int flags, IProgressMonitor monitor) throws CoreException {
		move(source, getStore(destination), flags, monitor);
	}

	/**
	 * Returns a resource path to the given local location. Returns null if
	 * it is not under a project's location.
	 */
	protected IPath pathForLocation(IPath location) {
		if (Platform.getLocation().equals(location))
			return Path.ROOT;
		IProject[] projects = getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			IPath projectLocation = project.getLocation();
			if (projectLocation != null && projectLocation.isPrefixOf(location)) {
				int segmentsToRemove = projectLocation.segmentCount();
				return project.getFullPath().append(location.removeFirstSegments(segmentsToRemove));
			}
		}
		return null;
	}

	public InputStream read(IFile target, boolean force, IProgressMonitor monitor) throws CoreException {
		IFileStore store = getStore(target);
		final IFileInfo fileInfo = store.fetchInfo();
		if (!fileInfo.exists()) {
			// thread safety: (the location can be null if the project for this file does not exist)
			((Project) target.getProject()).checkExists(NULL_FLAG, true);
			String message = NLS.bind(Messages.localstore_fileNotFound, store.toString());
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
		}
		if (!force) {
			ResourceInfo info = ((Resource) target).getResourceInfo(true, false);
			int flags = ((Resource) target).getFlags(info);
			((Resource) target).checkExists(flags, true);
			if (fileInfo.getLastModified() != info.getLocalSyncInfo()) {
				String message = NLS.bind(Messages.localstore_resourceIsOutOfSync, target.getFullPath());
				throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message, null);
			}
		}
		return store.openInputStream(EFS.NONE, monitor);
	}

	/**
	 * Reads and returns the project description for the given project.
	 * Never returns null.
	 * @param target the project whose description should be read.
	 * @param creation true if this project is just being created, in which
	 * case the private project information (including the location) needs to be read 
	 * from disk as well.
	 * @exception CoreException if there was any failure to read the project
	 * description, or if the description was missing.
	 */
	public ProjectDescription read(IProject target, boolean creation) throws CoreException {
		//read the project location if this project is being created
		URI projectLocation = null;
		ProjectDescription privateDescription = null;
		if (creation) {
			privateDescription = new ProjectDescription();
			getWorkspace().getMetaArea().readPrivateDescription(target, privateDescription);
			projectLocation = privateDescription.getLocationURI();
		} else {
			IProjectDescription description = ((Project) target).internalGetDescription();
			if (description != null && description.getLocationURI() != null) {
				projectLocation = description.getLocationURI();
			}
		}
		final boolean isDefaultLocation = projectLocation == null;
		if (isDefaultLocation) {
			projectLocation = FileUtil.toURI(getProjectDefaultLocation(target));
		}
		IFileStore projectStore = initializeStore(target, projectLocation);
		IFileStore descriptionStore = projectStore.getChild(IProjectDescription.DESCRIPTION_FILE_NAME);
		ProjectDescription description = null;
		if (!descriptionStore.fetchInfo().exists()) {
			//try the legacy location in the meta area
			description = getWorkspace().getMetaArea().readOldDescription(target);
			if (description == null) {
				String msg = NLS.bind(Messages.resources_missingProjectMeta, target.getName());
				throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, target.getFullPath(), msg, null);
			}
			return description;
		}
		//hold onto any exceptions until after sync info is updated, then throw it
		ResourceException error = null;
		InputStream in = null;
		try {
			in = new BufferedInputStream(descriptionStore.openInputStream(EFS.NONE, null));
			description = new ProjectDescriptionReader().read(new InputSource(in));
		} catch (CoreException e) {
			String msg = NLS.bind(Messages.resources_readProjectMeta, target.getName());
			error = new ResourceException(IResourceStatus.FAILED_READ_METADATA, target.getFullPath(), msg, e);
		} finally {
			FileUtil.safeClose(in);
		}
		if (error == null && description == null) {
			String msg = NLS.bind(Messages.resources_readProjectMeta, target.getName());
			error = new ResourceException(IResourceStatus.FAILED_READ_METADATA, target.getFullPath(), msg, null);
		}
		if (description != null) {
			//don't trust the project name in the description file
			description.setName(target.getName());
			if (!isDefaultLocation)
				description.setLocationURI(projectLocation);
			if (creation && privateDescription != null)
				description.setDynamicReferences(privateDescription.getDynamicReferences(false));
		}
		long lastModified = descriptionStore.fetchInfo().getLastModified();
		IFile descriptionFile = target.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		//don't get a mutable copy because we might be in restore which isn't an operation
		//it doesn't matter anyway because local sync info is not included in deltas
		ResourceInfo info = ((Resource) descriptionFile).getResourceInfo(false, false);
		if (info == null) {
			//create a new resource on the sly -- don't want to start an operation
			info = getWorkspace().createResource(descriptionFile, false);
			updateLocalSync(info, lastModified);
		}
		//if the project description has changed between sessions, let it remain
		//out of sync -- that way link changes will be reconciled on next refresh
		if (!creation)
			updateLocalSync(info, lastModified);

		//update the timestamp on the project as well so we know when it has
		//been changed from the outside
		info = ((Resource) target).getResourceInfo(false, true);
		updateLocalSync(info, lastModified);

		if (error != null)
			throw error;
		return description;
	}

	public boolean refresh(IResource target, int depth, boolean updateAliases, IProgressMonitor monitor) throws CoreException {
		switch (target.getType()) {
			case IResource.ROOT :
				return refreshRoot((IWorkspaceRoot) target, depth, updateAliases, monitor);
			case IResource.PROJECT :
				if (!target.isAccessible())
					return false;
			//fall through
			case IResource.FOLDER :
			case IResource.FILE :
				return refreshResource(target, depth, updateAliases, monitor);
		}
		return false;
	}

	protected boolean refreshResource(IResource target, int depth, boolean updateAliases, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		int totalWork = RefreshLocalVisitor.TOTAL_WORK;
		String title = NLS.bind(Messages.localstore_refreshing, target.getFullPath());
		try {
			monitor.beginTask(title, totalWork);
			RefreshLocalVisitor visitor = updateAliases ? new RefreshLocalAliasVisitor(monitor) : new RefreshLocalVisitor(monitor);
			UnifiedTree tree = new UnifiedTree(target);
			tree.accept(visitor, depth);
			IStatus result = visitor.getErrorStatus();
			if (!result.isOK())
				throw new ResourceException(result);
			return visitor.resourcesChanged();
		} finally {
			monitor.done();
		}
	}

	/**
	 * Synchronizes the entire workspace with the local file system.
	 * The current implementation does this by synchronizing each of the
	 * projects currently in the workspace.  A better implementation may
	 * be possible.
	 */
	protected boolean refreshRoot(IWorkspaceRoot target, int depth, boolean updateAliases, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		IProject[] projects = target.getProjects();
		int totalWork = projects.length;
		String title = Messages.localstore_refreshingRoot;
		try {
			monitor.beginTask(title, totalWork);
			// if doing depth zero, there is nothing to do (can't refresh the root).  
			// Note that we still need to do the beginTask, done pair.
			if (depth == IResource.DEPTH_ZERO)
				return false;
			boolean changed = false;
			// drop the depth by one level since processing the root counts as one level.
			depth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : depth;
			for (int i = 0; i < projects.length; i++)
				changed |= refresh(projects[i], depth, updateAliases, Policy.subMonitorFor(monitor, 1));
			return changed;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns the resource corresponding to the given workspace path.  The 
	 * "files" parameter is used for paths of two or more segments.  If true,
	 * a file is returned, otherwise a folder is returned.  Returns null if files is true
	 * and the path is not of sufficient length.
	 */
	protected IResource resourceFor(IPath path, boolean files) {
		int numSegments = path.segmentCount();
		if (files && numSegments < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH)
			return null;
		IWorkspaceRoot root = getWorkspace().getRoot();
		if (path.isRoot())
			return root;
		if (numSegments == 1)
			return root.getProject(path.segment(0));
		return files ? (IResource) root.getFile(path) : (IResource) root.getFolder(path);
	}

	/* (non-javadoc)
	 * @see IResouce.setLocalTimeStamp
	 */
	public long setLocalTimeStamp(IResource target, ResourceInfo info, long value) throws CoreException {
		IFileStore store = getStore(target);
		IFileInfo fileInfo = store.fetchInfo();
		fileInfo.setLastModified(value);
		store.putInfo(fileInfo, EFS.SET_LAST_MODIFIED, null);
		//actual value may be different depending on file system granularity
		fileInfo = store.fetchInfo();
		long actualValue = fileInfo.getLastModified();
		updateLocalSync(info, actualValue);
		return actualValue;
	}

	/**
	 * The storage location for a resource has changed; update the location.
	 * @param target
	 * @param info
	 * @param location
	 */
	public void setLocation(IResource target, ResourceInfo info, URI location) {
		FileStoreRoot oldRoot = info.getFileStoreRoot();
		if (location != null) {
			info.setFileStoreRoot(new FileStoreRoot(location, target.getFullPath()));
		} else {
			//project is in default location so clear the store root
			info.setFileStoreRoot(null);
		}
		if (oldRoot != null)
			oldRoot.setValid(false);
	}

	/* (non-javadoc)
	 * @see IResource.setResourceAttributes
	 */
	public void setResourceAttributes(IResource resource, ResourceAttributes attributes) throws CoreException {
		IFileStore store = getStore(resource);
		//when the executable bit is changed on a folder a refresh is required
		boolean refresh = false;
		if (resource instanceof IContainer && ((store.getFileSystem().attributes() & EFS.ATTRIBUTE_EXECUTABLE) != 0))
			refresh = store.fetchInfo().getAttribute(EFS.ATTRIBUTE_EXECUTABLE) != attributes.isExecutable();
		store.putInfo(FileUtil.attributesToFileInfo(attributes), EFS.SET_ATTRIBUTES, null);
		//must refresh in the background because we are not inside an operation
		if (refresh)
			workspace.getRefreshManager().refresh(resource);
	}

	public void shutdown(IProgressMonitor monitor) throws CoreException {
		if (_historyStore != null)
			_historyStore.shutdown(monitor);
	}

	public void startup(IProgressMonitor monitor) throws CoreException {
		//nothing to do
	}

	/**
	 * The ResourceInfo must be mutable.
	 */
	public void updateLocalSync(ResourceInfo info, long localSyncInfo) {
		info.setLocalSyncInfo(localSyncInfo);
		if (localSyncInfo == I_NULL_SYNC_INFO)
			info.clear(M_LOCAL_EXISTS);
		else
			info.set(M_LOCAL_EXISTS);
	}

	/**
	 * The target must exist in the workspace. The content InputStream is
	 * closed even if the method fails. If the force flag is false we only write
	 * the file if it does not exist or if it is already local and the timestamp
	 * has NOT changed since last synchronization, otherwise a CoreException
	 * is thrown.
	 */
	public void write(IFile target, InputStream content, boolean force, boolean keepHistory, boolean append, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(null);
		try {
			IFileStore store = getStore(target);
			IFileInfo fileInfo = store.fetchInfo();
			if (fileInfo.getAttribute(EFS.ATTRIBUTE_READ_ONLY)) {
				String message = NLS.bind(Messages.localstore_couldNotWriteReadOnly, target.getFullPath());
				throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, target.getFullPath(), message, null);
			}
			long lastModified = fileInfo.getLastModified();
			if (force) {
				if (append && !target.isLocal(IResource.DEPTH_ZERO) && !fileInfo.exists()) {
					// force=true, local=false, existsInFileSystem=false
					String message = NLS.bind(Messages.resources_mustBeLocal, target.getFullPath());
					throw new ResourceException(IResourceStatus.RESOURCE_NOT_LOCAL, target.getFullPath(), message, null);
				}
			} else {
				if (target.isLocal(IResource.DEPTH_ZERO)) {
					// test if timestamp is the same since last synchronization
					ResourceInfo info = ((Resource) target).getResourceInfo(true, false);
					if (lastModified != info.getLocalSyncInfo()) {
						String message = NLS.bind(Messages.localstore_resourceIsOutOfSync, target.getFullPath());
						throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message, null);
					}
				} else {
					if (fileInfo.exists()) {
						String message = NLS.bind(Messages.localstore_resourceExists, target.getFullPath());
						throw new ResourceException(IResourceStatus.EXISTS_LOCAL, target.getFullPath(), message, null);
					}
					if (append) {
						String message = NLS.bind(Messages.resources_mustBeLocal, target.getFullPath());
						throw new ResourceException(IResourceStatus.RESOURCE_NOT_LOCAL, target.getFullPath(), message, null);
					}
				}
			}
			// add entry to History Store.
			if (keepHistory && fileInfo.exists())
				//never move to the history store, because then the file is missing if write fails
				getHistoryStore().addState(target.getFullPath(), store, lastModified, false);
			if (!fileInfo.exists())
				store.getParent().mkdir(EFS.NONE, null);
			int options = append ? EFS.APPEND : EFS.NONE;
			OutputStream out = store.openOutputStream(options, Policy.subMonitorFor(monitor, 0));
			FileUtil.transferStreams(content, out, store.toString(), monitor);
			// get the new last modified time and stash in the info
			lastModified = store.fetchInfo().getLastModified();
			ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
			updateLocalSync(info, lastModified);
		} finally {
			FileUtil.safeClose(content);
		}
	}

	/**
	 * If force is false, this method fails if there is already a resource in
	 * target's location.
	 */
	public void write(IFolder target, boolean force, IProgressMonitor monitor) throws CoreException {
		IFileStore store = getStore(target);
		if (!force) {
			IFileInfo fileInfo = store.fetchInfo();
			if (fileInfo.isDirectory()) {
				String message = NLS.bind(Messages.localstore_resourceExists, target.getFullPath());
				throw new ResourceException(IResourceStatus.EXISTS_LOCAL, target.getFullPath(), message, null);
			}
			if (fileInfo.exists()) {
				String message = NLS.bind(Messages.localstore_fileExists, target.getFullPath());
				throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message, null);
			}
		}
		store.mkdir(EFS.NONE, null);
		ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
		updateLocalSync(info, store.fetchInfo().getLastModified());
	}

	/**
	 * Write the .project file without modifying the resource tree.  This is called
	 * during save when it is discovered that the .project file is missing.  The tree
	 * cannot be modified during save.
	 */
	public void writeSilently(IProject target) throws CoreException {
		IPath location = locationFor(target);
		//if the project location cannot be resolved, we don't know if a description file exists or not
		if (location == null)
			return;
		IFileStore projectStore = getStore(target);
		projectStore.mkdir(EFS.NONE, null);
		//can't do anything if there's no description
		IProjectDescription desc = ((Project) target).internalGetDescription();
		if (desc == null)
			return;
		//write the project's private description to the meta-data area
		getWorkspace().getMetaArea().writePrivateDescription(target);

		//write the file that represents the project description
		IFileStore fileStore = projectStore.getChild(IProjectDescription.DESCRIPTION_FILE_NAME);
		OutputStream out = null;
		try {
			out = fileStore.openOutputStream(EFS.NONE, null);
			new ModelObjectWriter().write(desc, out);
		} catch (IOException e) {
			String msg = NLS.bind(Messages.resources_writeMeta, target.getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, target.getFullPath(), msg, e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore failure to close stream
				}
			}
		}
		//for backwards compatibility, ensure the old .prj file is deleted
		getWorkspace().getMetaArea().clearOldDescription(target);
	}
}
