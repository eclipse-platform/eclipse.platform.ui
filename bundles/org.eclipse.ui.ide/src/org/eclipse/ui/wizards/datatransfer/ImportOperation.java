/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat, Inc - changed TarFileStructureProvider to TarLeveledStructureProvider
 *******************************************************************************/
package org.eclipse.ui.wizards.datatransfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.filesystem.ZipFileUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.TarEntry;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;

/**
 * An operation which does the actual work of copying objects from the local file
 * system into the workspace.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ImportOperation extends WorkspaceModifyOperation {
	private static final int POLICY_DEFAULT = 0;

	private static final int POLICY_SKIP_CHILDREN = 1;

	private static final int POLICY_FORCE_OVERWRITE = 2;

	private Object source;

	private IPath destinationPath;

	private IContainer destinationContainer;

	private List<?> selectedFiles;

	private List<IPath> rejectedFiles;

	private IImportStructureProvider provider;

	protected IOverwriteQuery overwriteCallback;

	private Shell context;

	private List<IStatus> errorTable = new ArrayList<>();

	private boolean createVirtualFolder = false;

	private boolean createLinks = false;

	private boolean createLinkFilesOnly = false;

	private String relativeVariable = null;

	private boolean createContainerStructure = true;

	//The constants for the overwrite 3 state
	private static final int OVERWRITE_NOT_SET = 0;

	private static final int OVERWRITE_NONE = 1;

	private static final int OVERWRITE_ALL = 2;

	private int overwriteState = OVERWRITE_NOT_SET;

	private static final String ABSOLUTE_PATH = "<Absolute Path>"; //$NON-NLS-1$

	/**
	 * Creates a new operation that recursively imports the entire contents of the
	 * specified root file system object.
	 * <p>
	 * The <code>source</code> parameter represents the root file system object to
	 * import. All contents of this object are imported. Valid types for this parameter
	 * are determined by the supplied <code>IImportStructureProvider</code>.
	 * </p>
	 * <p>
	 * The <code>provider</code> parameter allows this operation to deal with the
	 * source object in an abstract way. This operation calls methods on the provider
	 * and the provider in turn calls specific methods on the source object.
	 * </p>
	 *  <p>
	 * The default import behavior is to recreate the complete container structure
	 * for the contents of the root file system object in their destination.
	 * If <code>setCreateContainerStructure</code> is set to false then the container
	 * structure created is relative to the root file system object.
	 * </p>
	 *
	 * @param containerPath the full path of the destination container within the
	 *   workspace
	 * @param source the root file system object to import
	 * @param provider the file system structure provider to use
	 * @param overwriteImplementor the overwrite strategy to use
	 */
	public ImportOperation(IPath containerPath, Object source,
			IImportStructureProvider provider,
			IOverwriteQuery overwriteImplementor) {
		super();
		this.destinationPath = containerPath;
		this.source = source;
		this.provider = provider;
		overwriteCallback = overwriteImplementor;
	}

	/**
	 * Creates a new operation that imports specific file system objects.
	 * In this usage context, the specified source file system object is used by the
	 * operation solely to determine the destination container structure of the file system
	 * objects being imported.
	 * <p>
	 * The <code>source</code> parameter represents the root file system object to
	 * import. Valid types for this parameter are determined by the supplied
	 * <code>IImportStructureProvider</code>. The contents of the source which
	 * are to be imported are specified in the <code>filesToImport</code>
	 * parameter.
	 * </p>
	 * <p>
	 * The <code>provider</code> parameter allows this operation to deal with the
	 * source object in an abstract way. This operation calls methods on the provider
	 * and the provider in turn calls specific methods on the source object.
	 * </p>
	 * <p>
	 * The <code>filesToImport</code> parameter specifies what contents of the root
	 * file system object are to be imported.
	 * </p>
	 * <p>
	 * The default import behavior is to recreate the complete container structure
	 * for the file system objects in their destination. If <code>setCreateContainerStructure</code>
	 * is set to <code>false</code>, then the container structure created for each of
	 * the file system objects is relative to the supplied root file system object.
	 * </p>
	 *
	 * @param containerPath the full path of the destination container within the
	 *   workspace
	 * @param source the root file system object to import from
	 * @param provider the file system structure provider to use
	 * @param overwriteImplementor the overwrite strategy to use
	 * @param filesToImport the list of file system objects to be imported
	 *  (element type: <code>Object</code>)
	 */
	public ImportOperation(IPath containerPath, Object source,
			IImportStructureProvider provider,
			IOverwriteQuery overwriteImplementor, List filesToImport) {
		this(containerPath, source, provider, overwriteImplementor);
		setFilesToImport(filesToImport);
	}

	/**
	 * Creates a new operation that imports specific file system objects.
	 * <p>
	 * The <code>provider</code> parameter allows this operation to deal with the
	 * source object in an abstract way. This operation calls methods on the provider
	 * and the provider in turn calls specific methods on the source object.
	 * </p>
	 * <p>
	 * The <code>filesToImport</code> parameter specifies what file system objects
	 * are to be imported.
	 * </p>
	 * <p>
	 * The default import behavior is to recreate the complete container structure
	 * for the file system objects in their destination. If <code>setCreateContainerStructure</code>
	 * is set to <code>false</code>, then no container structure is created for each of
	 * the file system objects.
	 * </p>
	 *
	 * @param containerPath the full path of the destination container within the
	 *   workspace
	 * @param provider the file system structure provider to use
	 * @param overwriteImplementor the overwrite strategy to use
	 * @param filesToImport the list of file system objects to be imported
	 *  (element type: <code>Object</code>)
	 */
	public ImportOperation(IPath containerPath,
			IImportStructureProvider provider,
			IOverwriteQuery overwriteImplementor, List filesToImport) {
		this(containerPath, null, provider, overwriteImplementor);
		setFilesToImport(filesToImport);
	}

	/**
	 * Prompts if existing resources should be overwritten. Recursively collects
	 * existing read-only files to overwrite and resources that should not be
	 * overwritten.
	 *
	 * @param sourceStart destination path to check for existing files
	 * @param sources file system objects that may exist in the destination
	 * @param noOverwrite files that were selected to be skipped (don't overwrite).
	 * 	object type IPath
	 * @param overwriteReadonly the collected existing read-only files to overwrite.
	 * 	object type IPath
	 * @param policy on of the POLICY constants defined in the
	 * class.
	 */
	void collectExistingReadonlyFiles(IPath sourceStart, List<?> sources, ArrayList<IPath> noOverwrite,
			ArrayList<IPath> overwriteReadonly, int policy, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		Iterator<?> sourceIter = sources.iterator();
		IPath sourceRootPath = null;

		if (this.source != null) {
			sourceRootPath = IPath.fromOSString(provider.getFullPath(this.source));
		}
		while (sourceIter.hasNext()) {
			Object nextSource = sourceIter.next();
			IPath sourcePath = IPath.fromOSString(provider.getFullPath(nextSource));
			IPath newDestinationPath;
			IResource newDestination;

			if (sourceRootPath == null) {
				newDestinationPath = sourceStart.append(provider
						.getLabel(nextSource));
			} else {
				int prefixLength = sourcePath
						.matchingFirstSegments(sourceRootPath);
				IPath relativeSourcePath = sourcePath
						.removeFirstSegments(prefixLength);
				newDestinationPath = this.destinationPath
						.append(relativeSourcePath);
			}
			newDestination = workspaceRoot.findMember(newDestinationPath);
			if (newDestination == null) {
				continue;
			}

			IFolder folder = getFolder(newDestination);
			if (folder != null) {
				if (provider.isFolder(nextSource) && this.overwriteState == OVERWRITE_ALL) {
					collectExistingReadonlyFiles(newDestinationPath, provider.getChildren(nextSource), noOverwrite,
							overwriteReadonly, POLICY_FORCE_OVERWRITE, subMonitor.split(100));
				}
				else if (policy != POLICY_FORCE_OVERWRITE) {
					if (this.overwriteState == OVERWRITE_NONE
							|| !queryOverwrite(newDestinationPath)) {
						noOverwrite.add(folder.getFullPath());
						continue;
					}
				}
			} else {
				IFile file = getFile(newDestination);

				if (file != null) {
					if (!queryOverwriteFile(file, policy)) {
						noOverwrite.add(file.getFullPath());
					} else if (file.isReadOnly()) {
						overwriteReadonly.add(file.getFullPath());
					}
				}
			}
		}
	}

	/**
	 * Creates the folders that appear in the specified resource path. These folders
	 * are created relative to the destination container.
	 *
	 * @param path the relative path of the resource
	 * @return the container resource corresponding to the given path
	 * @exception CoreException if this method failed
	 */
	IContainer createContainersFor(IPath path) throws CoreException {

		IContainer currentFolder = destinationContainer;

		int segmentCount = path.segmentCount();

		//No containers to create
		if (segmentCount == 0) {
			return currentFolder;
		}

		//Needs to be handles differently at the root
		if (currentFolder.getType() == IResource.ROOT) {
			return createFromRoot(path);
		}

		for (int i = 0; i < segmentCount; i++) {
			currentFolder = currentFolder.getFolder(IPath.fromOSString(path.segment(i)));
			if (!currentFolder.exists()) {
				if (createVirtualFolder)
					((IFolder) currentFolder).create(IResource.VIRTUAL, true,
							null);
				else if (createLinks)
					((IFolder) currentFolder).createLink(createRelativePath(
							path, currentFolder), 0, null);
				else
					((IFolder) currentFolder).create(false, true, null);
			}
		}

		return currentFolder;
	}

	/**
	 * Creates the folders that appear in the specified resource path
	 * assuming that the destinationContainer begins at the root. Do not create projects.
	 *
	 * @param path the relative path of the resource
	 * @return the container resource coresponding to the given path
	 * @exception CoreException if this method failed
	 */
	private IContainer createFromRoot(IPath path) throws CoreException {

		int segmentCount = path.segmentCount();

		//Assume the project exists
		IContainer currentFolder = ((IWorkspaceRoot) destinationContainer)
				.getProject(path.segment(0));

		for (int i = 1; i < segmentCount; i++) {
			currentFolder = currentFolder.getFolder(IPath.fromOSString(path.segment(i)));
			if (!currentFolder.exists()) {
				((IFolder) currentFolder).create(false, true, null);
			}
		}

		return currentFolder;
	}

	/**
	 * Deletes the given resource. If the resource fails to be deleted, adds a
	 * status object to the list to be returned by <code>getResult</code>.
	 *
	 * @param resource the resource
	 */
	void deleteResource(IResource resource) {
		try {
			resource.delete(IResource.KEEP_HISTORY, null);
		} catch (CoreException e) {
			errorTable.add(e.getStatus());
		}
	}

	@Override
	protected void execute(IProgressMonitor progressMonitor) {
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor, DataTransferMessages.DataTransfer_importTask, 100);
		try {
			if (selectedFiles == null) {
				ContainerGenerator generator = new ContainerGenerator(destinationPath);
				subMonitor.worked(3);
				validateFiles(Arrays.asList(source), subMonitor.split(3));
				destinationContainer = generator.generateContainer(subMonitor.split(4));
				importRecursivelyFrom(source, POLICY_DEFAULT, subMonitor.split(90));
			} else {
				// Choose twice the selected files size to take folders into account
				ContainerGenerator generator = new ContainerGenerator(destinationPath);
				subMonitor.worked(3);
				validateFiles(selectedFiles, subMonitor.split(3));
				destinationContainer = generator.generateContainer(subMonitor.split(4));
				importFileSystemObjects(selectedFiles, subMonitor.split(90));
			}
		} catch (CoreException e) {
			errorTable.add(e.getStatus());
		}
	}

	/**
	 * Returns the container resource that the passed file system object should be
	 * imported into.
	 *
	 * @param fileSystemObject the file system object being imported
	 * @return the container resource that the passed file system object should be
	 *     imported into
	 * @exception CoreException if this method failed
	 */
	IContainer getDestinationContainerFor(Object fileSystemObject)
			throws CoreException {
		IPath pathname = IPath.fromOSString(provider.getFullPath(fileSystemObject));

		if (createContainerStructure) {
			return createContainersFor(pathname.removeLastSegments(1));
		}
		if (source == fileSystemObject) {
			return null;
		}
		IPath sourcePath = IPath.fromOSString(provider.getFullPath(source));
		IPath destContainerPath = pathname.removeLastSegments(1);
		IPath relativePath = destContainerPath.removeFirstSegments(
				sourcePath.segmentCount()).setDevice(null);
		return createContainersFor(relativePath);

	}

	/**
	 * Returns the resource either casted to or adapted to an IFile.
	 *
	 * @param resource resource to cast/adapt
	 * @return the resource either casted to or adapted to an IFile.
	 * 	<code>null</code> if the resource does not adapt to IFile
	 */
	IFile getFile(IResource resource) {
		// TODO: Check if this is necessary. No IResource should
		// adapt to IFile unless it *is* an IFile. An instanceof check
		// might be better
		return Adapters.adapt(resource, IFile.class);
	}

	/**
	 * Returns the resource either casted to or adapted to an IFolder.
	 *
	 * @param resource resource to cast/adapt
	 * @return the resource either casted to or adapted to an IFolder.
	 * 	<code>null</code> if the resource does not adapt to IFolder
	 */
	IFolder getFolder(IResource resource) {
		// TODO: Check if this is necessary. No IResource should
		// adapt to IFolder unless it *is* an IFolder. An instanceof
		// check might be better
		return Adapters.adapt(resource, IFolder.class);
	}

	/**
	 * Returns the rejected files based on the given multi status.
	 *
	 * @param multiStatus multi status to use to determine file rejection
	 * @param files source files
	 * @return list of rejected files as absolute paths. Object type IPath.
	 */
	ArrayList<IPath> getRejectedFiles(IStatus multiStatus, IFile[] files) {
		ArrayList<IPath> filteredFiles = new ArrayList<>();

		IStatus[] status = multiStatus.getChildren();
		for (int i = 0; i < status.length; i++) {
			if (!status[i].isOK()) {
				errorTable.add(status[i]);
				filteredFiles.add(files[i].getFullPath());
			}
		}
		return filteredFiles;
	}

	/**
	 * Returns the status of the import operation.
	 * If there were any errors, the result is a status object containing
	 * individual status objects for each error.
	 * If there were no errors, the result is a status object with error code <code>OK</code>.
	 *
	 * @return the status
	 */
	public IStatus getStatus() {
		IStatus[] errors = new IStatus[errorTable.size()];
		errorTable.toArray(errors);
		return new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, errors,
				DataTransferMessages.ImportOperation_importProblems,
				null);
	}

	/**
	 * Imports the specified file system object into the workspace.
	 * If the import fails, adds a status object to the list to be returned by
	 * <code>getResult</code>.
	 *
	 * @param fileObject the file system object to be imported
	 * @param policy determines how the file object is imported
	 */
	void importFile(Object fileObject, int policy, IProgressMonitor mon) {
		SubMonitor subMonitor = SubMonitor.convert(mon, 100);
		IContainer containerResource;
		try {
			containerResource = getDestinationContainerFor(fileObject);
		} catch (CoreException e) {
			IStatus coreStatus = e.getStatus();
			String newMessage = NLS.bind(DataTransferMessages.ImportOperation_coreImportError, fileObject, coreStatus.getMessage());
			IStatus status = new Status(coreStatus.getSeverity(), coreStatus
					.getPlugin(), coreStatus.getCode(), newMessage, null);
			errorTable.add(status);
			return;
		}

		String fileObjectPath = provider.getFullPath(fileObject);
		subMonitor.subTask(fileObjectPath);
		IFile targetResource = containerResource.getFile(IPath.fromOSString(provider.getLabel(fileObject)));

		if (rejectedFiles.contains(targetResource.getFullPath())) {
			return;
		}

		// ensure that the source and target are not the same
		IPath targetPath = targetResource.getLocation();
		// Use Files for comparison to avoid platform specific case issues
		if (targetPath != null
				&& (targetPath.toFile().equals(new File(fileObjectPath)))) {
			errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
					NLS.bind(DataTransferMessages.ImportOperation_targetSameAsSourceError, fileObjectPath), null));
			return;
		}

		try (InputStream contentStream = provider.getContents(fileObject)) {
			if (contentStream == null) {
				if (isNotReadableFile(fileObject)) {
					errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
							NLS.bind(DataTransferMessages.ImportOperation_cannotReadError, fileObjectPath), null));
				} else {
					errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
							NLS.bind(DataTransferMessages.ImportOperation_openStreamError, fileObjectPath), null));
				}
				return;
			}

			if (createVirtualFolder || createLinks || createLinkFilesOnly) {
				if (targetResource.exists())
					targetResource.delete(true, subMonitor.split(50));
				targetResource.createLink(
						createRelativePath(IPath.fromOSString(provider.getFullPath(fileObject)), targetResource), 0,
						subMonitor.split(50));
			} else if (ZipFileUtil.isOpenZipFile(targetResource.getLocationURI())) {// $NON-NLS-1$
				// When overwriting an opened Zip File with a new Zip File that has the same
				// name, the opened zip file needs to be deleted first.
				IFolder openedZipFile = targetResource.getProject().getFolder(targetResource.getName());
				if (openedZipFile.exists()) {
					openedZipFile.delete(true, subMonitor.split(50));
				}
				targetResource.create(contentStream, false, subMonitor.split(50));
			} else if (targetResource.exists()) {
				if (targetResource.isLinked()) {
					targetResource.delete(true, subMonitor.split(50));
					targetResource.create(contentStream, false, subMonitor.split(50));
				} else
					targetResource.setContents(contentStream, IResource.KEEP_HISTORY, subMonitor.split(100));
			} else
				targetResource.create(contentStream, false, subMonitor.split(100));
			setResourceAttributes(targetResource, fileObject);

			if (provider instanceof TarLeveledStructureProvider) {
				try {
					targetResource.setResourceAttributes(
							((TarLeveledStructureProvider) provider).getResourceAttributes(fileObject));
				} catch (CoreException e) {
					errorTable.add(e.getStatus());
				}
			}
		} catch (CoreException e) {
			errorTable.add(e.getStatus());
		} catch (IOException e) {
				errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
						NLS.bind(DataTransferMessages.ImportOperation_closeStreamError, fileObjectPath), e));
		}
	}

	/**
	 * Test if a file exists but has no read permissions.
	 *
	 * @param file the File object
	 * @return <code>true</code> if the file exists but is not readable,
	 *         <code>false</code> otherwise; <code>false</code> if other objects are
	 *         passed or when the File object is a directory.
	 */
	private boolean isNotReadableFile(Object file) {
		if (!(file instanceof File)) {
			return false;
		}
		// File.canRead does not properly work on Windows 7
		// see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6203387
		// java.nio works properly on all OSes
		java.nio.file.Path path = ((File) file).toPath();
		boolean readable = Files.isReadable(path);
		boolean regular = Files.isRegularFile(path);
		return regular && !readable;
	}

	/**
	 * Reuse the file attributes set in the import.
	 */
	private void setResourceAttributes(IFile targetResource, Object fileObject) {

		long timeStamp = 0;
		if(fileObject instanceof File) {
			try {
				targetResource.setResourceAttributes(ResourceAttributes.fromFile((File) fileObject));
				timeStamp = ((File)fileObject).lastModified();
			} catch (CoreException e) {
				errorTable.add(e.getStatus());
			}
		}else if (fileObject instanceof TarEntry) {
			try {
				targetResource.setResourceAttributes(((TarLeveledStructureProvider) provider).getResourceAttributes(fileObject));
				timeStamp = ((TarEntry)fileObject).getTime()*1000; // TarEntry time is in secs. Convert to msecs
			} catch (CoreException e) {
				errorTable.add(e.getStatus());
			}
		}else if (fileObject instanceof ZipEntry) {
			long zipTimeStamp = ((ZipEntry)fileObject).getTime();
			if(zipTimeStamp != -1)
				timeStamp = zipTimeStamp;
		}

		if(timeStamp!= 0) {
			try {
				targetResource.setLocalTimeStamp(timeStamp);
			} catch (CoreException e) {
				errorTable.add(e.getStatus());
			}
		}
	}

	/**
	 * Imports the specified file system objects into the workspace.
	 * If the import fails, adds a status object to the list to be returned by
	 * <code>getStatus</code>.
	 *
	 * @param filesToImport the list of file system objects to import
	 *   (element type: <code>Object</code>)
	 * @exception OperationCanceledException if canceled
	 */
	void importFileSystemObjects(List<?> filesToImport, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, filesToImport.size());
		Iterator<?> filesEnum = filesToImport.iterator();
		while (filesEnum.hasNext()) {
			SubMonitor iterationMonitor = subMonitor.split(1);
			Object fileSystemObject = filesEnum.next();
			if (source == null) {
				// We just import what we are given into the destination
				IPath sourcePath = IPath.fromOSString(provider.getFullPath(fileSystemObject)).removeLastSegments(1);
				if (provider.isFolder(fileSystemObject) && sourcePath.isEmpty()) {
					// If we don't have a parent then we have selected the
					// file systems root. Roots can't copied (at least not
					// under windows).
					errorTable.add(new Status(IStatus.INFO,
							PlatformUI.PLUGIN_ID, 0, DataTransferMessages.ImportOperation_cannotCopy,
							null));
					continue;
				}
				source = sourcePath.toFile();
			}
			importRecursivelyFrom(fileSystemObject, POLICY_DEFAULT, iterationMonitor);
		}
	}

	/**
	 * Imports the specified file system container object into the workspace.
	 * If the import fails, adds a status object to the list to be returned by
	 * <code>getResult</code>.
	 *
	 * @param folderObject the file system container object to be imported
	 * @param policy determines how the folder object and children are imported
	 * @return the policy to use to import the folder's children
	 */
	int importFolder(Object folderObject, int policy, IProgressMonitor monitor) throws CoreException {
		IContainer containerResource;
		try {
			containerResource = getDestinationContainerFor(folderObject);
		} catch (CoreException e) {
			errorTable.add(e.getStatus());
			return policy;
		}

		if (containerResource == null) {
			return policy;
		}

		monitor.subTask(provider.getFullPath(folderObject));
		IWorkspace workspace = destinationContainer.getWorkspace();
		IPath containerPath = containerResource.getFullPath();
		IPath resourcePath = containerPath.append(provider
				.getLabel(folderObject));

		// Do not attempt the import if the resource path is unchanged. This may happen
		// when importing from a zip file.
		if (resourcePath.equals(containerPath)) {
			return policy;
		}

		if (workspace.getRoot().exists(resourcePath)) {
			if (rejectedFiles.contains(resourcePath)) {
				return POLICY_SKIP_CHILDREN;
			}

			IFolder folder = workspace.getRoot().getFolder(resourcePath);
			if (createVirtualFolder || createLinks || folder.isVirtual() || folder.isLinked()) {
				folder.delete(true, null);
			} else
				return POLICY_FORCE_OVERWRITE;
		}

		try {
			if (createVirtualFolder)
				workspace.getRoot().getFolder(resourcePath).create(
						IResource.VIRTUAL, true, null);
			else if (createLinks) {
				IFolder newFolder = workspace.getRoot().getFolder(resourcePath);
				newFolder.createLink(
						createRelativePath(IPath.fromOSString(provider.getFullPath(folderObject)), newFolder),
						0, null);
				policy = POLICY_SKIP_CHILDREN;
			} else
				workspace.getRoot().getFolder(resourcePath).create(false, true, null);
		} catch (CoreException e) {
			errorTable.add(e.getStatus());
		}

		return policy;
	}

	/**
	 * Transform an absolute path URI to a relative path one (i.e. from
	 * "C:\foo\bar\file.txt" to "VAR\file.txt" granted that the relativeVariable
	 * is "VAR" and points to "C:\foo\bar\").
	 *
	 * @return an URI that was made relative to a variable
	 */
	private IPath createRelativePath(IPath location, IResource resource) {
		if (relativeVariable == null)
			return location;
		if (relativeVariable.equals(ABSOLUTE_PATH))
			return location;
		IPathVariableManager pathVariableManager = resource.getPathVariableManager();
		try {
			return URIUtil.toPath(pathVariableManager.convertToRelative(URIUtil.toURI(location), true, relativeVariable));
		} catch (CoreException e) {
			return location;
		}
	}

	/**
	 * Imports the specified file system object recursively into the workspace.
	 * If the import fails, adds a status object to the list to be returned by
	 * <code>getStatus</code>.
	 *
	 * @param fileSystemObject the file system object to be imported
	 * @param policy determines how the file system object and children are imported
	 * @exception OperationCanceledException if canceled
	 */
	void importRecursivelyFrom(Object fileSystemObject, int policy, IProgressMonitor mon) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(mon, 100);
		if (!provider.isFolder(fileSystemObject)) {
			importFile(fileSystemObject, policy, subMonitor.split(100));
			return;
		}

		int childPolicy = importFolder(fileSystemObject, policy, subMonitor.split(10));
		if (childPolicy != POLICY_SKIP_CHILDREN) {
			List<?> children = provider.getChildren(fileSystemObject);
			SubMonitor loopMonitor = subMonitor.split(90).setWorkRemaining(children.size());
			for (Object child : children) {
				importRecursivelyFrom(child, childPolicy, loopMonitor.split(1));
			}
		}
	}

	/**
	 * Queries the user whether the resource with the specified path should be
	 * overwritten by a file system object that is being imported.
	 *
	 * @param resourcePath the workspace path of the resource that needs to be overwritten
	 * @return <code>true</code> to overwrite, <code>false</code> to not overwrite
	 * @exception OperationCanceledException if canceled
	 */
	boolean queryOverwrite(IPath resourcePath)
			throws OperationCanceledException {
		String overwriteAnswer = overwriteCallback.queryOverwrite(resourcePath
				.makeRelative().toString());

		if (overwriteAnswer.equals(IOverwriteQuery.CANCEL)) {
			throw new OperationCanceledException(DataTransferMessages.DataTransfer_emptyString);
		}

		if (overwriteAnswer.equals(IOverwriteQuery.NO)) {
			return false;
		}

		if (overwriteAnswer.equals(IOverwriteQuery.NO_ALL)) {
			this.overwriteState = OVERWRITE_NONE;
			return false;
		}

		if (overwriteAnswer.equals(IOverwriteQuery.ALL)) {
			this.overwriteState = OVERWRITE_ALL;
		}

		return true;
	}

	/**
	 * Returns whether the given file should be overwritten.
	 *
	 * @param targetFile the file to ask to overwrite
	 * @param policy determines if the user is queried for overwrite
	 * @return <code>true</code> if the file should be overwritten, and
	 * 	<code>false</code> if not.
	 */
	boolean queryOverwriteFile(IFile targetFile, int policy) {
		//If force overwrite is on don't bother
		if (policy != POLICY_FORCE_OVERWRITE) {
			if (this.overwriteState == OVERWRITE_NOT_SET
					&& !queryOverwrite(targetFile.getFullPath())) {
				return false;
			}
			if (this.overwriteState == OVERWRITE_NONE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Sets the context for use by the VCM provider to prompt the user
	 * for check-out of files.
	 *
	 * @param shell context for use by the VCM provider to prompt user
	 * 	for check-out. The user will not be prompted if set to <code>null</code>.
	 * @see IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
	 * @since 2.1
	 */
	public void setContext(Shell shell) {
		context = shell;
	}

	/**
	 * Sets whether the containment structures that are implied from the full paths
	 * of file system objects being imported should be duplicated in the workbench.
	 *
	 * @param value <code>true</code> if containers should be created, and
	 *  <code>false</code> otherwise
	 */
	public void setCreateContainerStructure(boolean value) {
		createContainerStructure = value;
	}

	/**
	 * Sets the file system objects to import.
	 *
	 * @param filesToImport the list of file system objects to be imported
	 *   (element type: <code>Object</code>)
	 */
	public void setFilesToImport(List filesToImport) {
		this.selectedFiles = filesToImport;
	}

	/**
	 * Sets whether imported file system objects should automatically overwrite
	 * existing workbench resources when a conflict occurs.
	 *
	 * @param value <code>true</code> to automatically overwrite, and
	 *   <code>false</code> otherwise
	 */
	public void setOverwriteResources(boolean value) {
		if (value) {
			this.overwriteState = OVERWRITE_ALL;
		}
	}

	/**
	 * Validates that the given source resources can be copied to the
	 * destination as decided by the VCM provider.
	 *
	 * @param existingFiles existing files to validate
	 * @return list of rejected files as absolute paths. Object type IPath.
	 */
	ArrayList<IPath> validateEdit(List<IPath> existingFiles) {

		if (existingFiles.size() > 0) {
			IFile[] files = existingFiles
					.toArray(new IFile[existingFiles.size()]);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateEdit(files, context);

			//If there was a mix return the bad ones
			if (status.isMultiStatus()) {
				return getRejectedFiles(status, files);
			}

			if(!status.isOK()){
				//If just a single status reject them all
				errorTable.add(status);
				ArrayList<IPath> filteredFiles = new ArrayList<>();

				for (IFile file : files) {
					filteredFiles.add(file.getFullPath());
				}
				return filteredFiles;
			}

		}
		return new ArrayList<>();
	}

	/**
	 * Validates the given file system objects.
	 * The user is prompted to overwrite existing files.
	 * Existing read-only files are validated with the VCM provider.
	 *
	 * @param sourceFiles files to validate
	 */
	void validateFiles(List<?> sourceFiles, IProgressMonitor monitor) {
		ArrayList<IPath> noOverwrite = new ArrayList<>();
		ArrayList<IPath> overwriteReadonly = new ArrayList<>();

		collectExistingReadonlyFiles(destinationPath, sourceFiles, noOverwrite, overwriteReadonly, POLICY_DEFAULT,
				monitor);
		rejectedFiles = validateEdit(overwriteReadonly);
		rejectedFiles.addAll(noOverwrite);
	}

	/**
	 * Set Whether groups and links will be created instead of files and folders
	 *
	 * @param virtualFolders the desired behaviour
	 * @since 3.6
	 */
	public void setVirtualFolders(boolean virtualFolders) {
		createVirtualFolder = virtualFolders;
	}

	/**
	 * Set Whether links will be created instead of files and folders
	 *
	 * @param links the desired behaviour
	 * @since 3.6
	 */
	public void setCreateLinks(boolean links) {
		createLinks = links;
	}

	/**
	 * Set a variable relative to which the links are created
	 *
	 * @param variable base for relative links
	 * @since 3.6
	 */
	public void setRelativeVariable(String variable) {
		relativeVariable = variable;
	}
}
