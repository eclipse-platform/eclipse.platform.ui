package org.eclipse.ui.wizards.datatransfer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import java.io.*;
import java.util.*;

/**
 * An operation which does the actual work of copying objects from the local file
 * system into the workspace.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class ImportOperation extends WorkspaceModifyOperation {
	private static final int POLICY_DEFAULT = 0;
	private static final int POLICY_SKIP_CHILDREN = 1;
	private static final int POLICY_FORCE_OVERWRITE = 2;
	
	private Object source;
	private IPath destinationPath;
	private IContainer destinationContainer;
	private List selectedFiles;
	private IImportStructureProvider provider;
	private IProgressMonitor	monitor;
	protected IOverwriteQuery overwriteCallback;
	private List errorTable = new ArrayList();
	private boolean overwriteResources = false;
	private boolean createContainerStructure = true;
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
public ImportOperation(IPath containerPath, Object source, IImportStructureProvider provider, IOverwriteQuery overwriteImplementor) {
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
public ImportOperation(IPath containerPath, Object source, IImportStructureProvider provider, IOverwriteQuery overwriteImplementor, List filesToImport) {
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
public ImportOperation(IPath containerPath, IImportStructureProvider provider, IOverwriteQuery overwriteImplementor, List filesToImport) {
	this(containerPath, null, provider, overwriteImplementor);
	setFilesToImport(filesToImport);
}
/**
 * Creates the folders that appear in the specified resource path.
 * These folders are created relative to the destination container.
 *
 * @param path the relative path of the resource
 * @return the container resource coresponding to the given path
 * @exception CoreException if this method failed
 */
IContainer createContainersFor(IPath path) throws CoreException {
	// 1FV0B3Y: ITPUI:ALL - sub progress monitors granularity issues

	IContainer currentFolder = (IContainer) destinationContainer;
	int segmentCount = path.segmentCount();

	
	for (int i = 0; i < segmentCount; i++) {
		currentFolder = currentFolder.getFolder(new Path(path.segment(i)));
		if (!currentFolder.exists()) 
			((IFolder) currentFolder).create(false,true,null);
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
/**
 * Attempts to ensure that the given resource does not already exist in the
 * workspace. The resource will be deleted if required, perhaps after asking
 * the user's permission.
 *
 * @param targetResource the resource that should not exist
 * @param policy determines how the resource is imported
 * @return <code>true</code> if the resource does not exist, and 
 *    <code>false</code> if it does exist
 */
boolean ensureTargetDoesNotExist(IResource targetResource, int policy) {
	if (targetResource.exists()) {
		if (policy != POLICY_FORCE_OVERWRITE && !overwriteResources && !queryOverwrite(targetResource.getFullPath()))
			return false;

		deleteResource(targetResource);
	}

	return true;
}
/* (non-Javadoc)
 * Method declared on WorkbenchModifyOperation.
 * Imports the specified file system objects from the file system.
 */
protected void execute(IProgressMonitor progressMonitor) {
	
	monitor = progressMonitor;

	try {
		if (selectedFiles == null) {
			//Set the amount to 1000 as we have no idea of how long this will take
			monitor.beginTask(DataTransferMessages.getString("DataTransfer.importTask"),1000); //$NON-NLS-1$
			ContainerGenerator generator = new ContainerGenerator(destinationPath);
			monitor.worked(50);
			destinationContainer = generator.generateContainer(new SubProgressMonitor(monitor, 50));
			importRecursivelyFrom(source, POLICY_DEFAULT);
			//Be sure it finishes
			monitor.worked(90);
		} else {
			// Choose twice the selected files size to take folders into account
			int creationCount = selectedFiles.size();
			monitor.beginTask(DataTransferMessages.getString("DataTransfer.importTask"),creationCount + 100); //$NON-NLS-1$
			ContainerGenerator generator = new ContainerGenerator(destinationPath);
			monitor.worked(50);
			destinationContainer = generator.generateContainer(new SubProgressMonitor(monitor, 50));
			importFileSystemObjects(selectedFiles);
			monitor.done();
		}
	}
	catch (CoreException e) {
		errorTable.add(e.getStatus());
	}
	finally {
		monitor.done();
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
IContainer getDestinationContainerFor(Object fileSystemObject) throws CoreException {
	IPath pathname = new Path(provider.getFullPath(fileSystemObject));
	
	if (createContainerStructure)
		return createContainersFor(pathname.removeLastSegments(1));
	else {
		if (source == fileSystemObject)
			return null;
		IPath sourcePath = new Path(provider.getFullPath(source));
		IPath destContainerPath = pathname.removeLastSegments(1);
		IPath relativePath = destContainerPath.removeFirstSegments(sourcePath.segmentCount()).setDevice(null);
		return createContainersFor(relativePath);
	}
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
	return new MultiStatus(
		PlatformUI.PLUGIN_ID, 
		IStatus.OK, 
		errors,
		DataTransferMessages.getString("ImportOperation.importProblems"),  //$NON-NLS-1$
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
void importFile(Object fileObject, int policy) {
	IContainer containerResource;
	try {
		containerResource = getDestinationContainerFor(fileObject);
	} catch (CoreException e) {
		IStatus coreStatus = e.getStatus();		
		String newMessage = DataTransferMessages.format("ImportOperation.coreImportError", new Object [] {fileObject, coreStatus.getMessage()}); //$NON-NLS-1$
		IStatus status = new Status(coreStatus.getSeverity(), coreStatus.getPlugin(), coreStatus.getCode(), newMessage, null);
		errorTable.add(status);
		return;
	}
		
	String fileObjectPath = provider.getFullPath(fileObject);
	monitor.subTask(fileObjectPath);
	IFile targetResource = containerResource.getFile(new Path(provider.getLabel(fileObject)));
	monitor.worked(1);
	
	// ensure that the source and target are not the same
	IPath targetPath = targetResource.getLocation();
	// Use Files for comparison to avoid platform specific case issues
	if (targetPath != null && 
		(targetPath.toFile().equals(new File(fileObjectPath)))) {
		errorTable.add(
			new Status(
				IStatus.ERROR,
				PlatformUI.PLUGIN_ID,
				0,
				DataTransferMessages.format(
					"ImportOperation.targetSameAsSourceError", //$NON-NLS-1$
					new Object [] {fileObjectPath}), 
				null));
		return;		
	}

	if (!ensureTargetDoesNotExist(targetResource, policy)) {
		// Do not add an error status because the user
		// has explicitely said no overwrite. Do not
		// update the monitor as it was done in queryOverwrite.
		return;
	}
	
	InputStream contentStream = provider.getContents(fileObject);
	if (contentStream == null) {
		errorTable.add(
			new Status(
				IStatus.ERROR,
				PlatformUI.PLUGIN_ID,
				0,
				DataTransferMessages.format("ImportOperation.openStreamError", new Object [] {fileObjectPath}), //$NON-NLS-1$
				null));
		return;
	}
	
	try {
		targetResource.create(
			contentStream,
			false,
			null);
	} catch (CoreException e) {
		errorTable.add(e.getStatus());
	} finally {
		try {
			contentStream.close();
		} catch (IOException e) {
			errorTable.add(
				new Status(
					IStatus.ERROR,
					PlatformUI.PLUGIN_ID,
					0,
					DataTransferMessages.format("ImportOperation.closeStreamError", new Object [] {fileObjectPath}),  //$NON-NLS-1$
					e));
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
void importFileSystemObjects(List filesToImport) {
	Iterator filesEnum = filesToImport.iterator();
	while (filesEnum.hasNext()) {
		Object fileSystemObject = filesEnum.next();
		if (source == null) {
			// We just import what we are given into the destination
			IPath sourcePath = new Path(provider.getFullPath(fileSystemObject)).removeLastSegments(1);
			if (provider.isFolder(fileSystemObject) && sourcePath.isEmpty()) {
				// If we don't have a parent then we have selected the
				// file systems root. Roots can't copied (at least not
				// under windows).
				errorTable.add(
					new Status(
						IStatus.INFO,
						PlatformUI.PLUGIN_ID,
						0,
						DataTransferMessages.getString("ImportOperation.cannotCopy"), //$NON-NLS-1$
						null));
				continue;
			}
			source = sourcePath.toFile();
		}
		importRecursivelyFrom(fileSystemObject, POLICY_DEFAULT);
	}
}
/**
 * Imports the specified file system container object into the workspace.
 * If the import fails, adds a status object to the list to be returned by
 * <code>getResult</code>.
 *
 * @param fileObject the file system container object to be imported
 * @param policy determines how the folder object and children are imported
 * @return the policy to use to import the folder's children
 */
int importFolder(Object folderObject, int policy) {
	IContainer containerResource;
	try {
		containerResource = getDestinationContainerFor(folderObject);
	} catch (CoreException e) {
		errorTable.add(e.getStatus());
		return policy;
	}

	if (containerResource == null)
		return policy;

	monitor.subTask(provider.getFullPath(folderObject));
	IWorkspace workspace = destinationContainer.getWorkspace();
	IPath containerPath = containerResource.getFullPath();
	IPath resourcePath = containerPath.append(provider.getLabel(folderObject));

	// Do not attempt the import if the resource path is unchanged. This may happen
	// when importing from a zip file.
	if (resourcePath.equals(containerPath))
		return policy;

	if (workspace.getRoot().exists(resourcePath)) {
		if (policy != POLICY_FORCE_OVERWRITE
			&& !overwriteResources
			&& !queryOverwrite(resourcePath)) {
			// Do not add an error status because the user
			// has explicitely said no overwrite. Do not
			// update the monitor as it was done in queryOverwrite.
			return POLICY_SKIP_CHILDREN;
		}
		return POLICY_FORCE_OVERWRITE;
	}

	try {
		workspace.getRoot().getFolder(resourcePath).create(
			false,
			true,
			null);
	} catch (CoreException e) {
		errorTable.add(e.getStatus());
	}

	return policy;
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
void importRecursivelyFrom(Object fileSystemObject, int policy) {
	if (monitor.isCanceled())
		throw new OperationCanceledException();
	
	if (!provider.isFolder(fileSystemObject)) {
		importFile(fileSystemObject, policy);
		return;
	}

	int childPolicy = importFolder(fileSystemObject, policy);
	if (childPolicy != POLICY_SKIP_CHILDREN) {
		Iterator children = provider.getChildren(fileSystemObject).iterator();
		while (children.hasNext())
			importRecursivelyFrom(children.next(), childPolicy);
	}
}
/**
 * Queries the user whether the resource with the specified path should be
 * overwritten by a file system object that is being imported.
 * 
 * @param path the workspace path of the resource that needs to be overwritten
 * @return <code>true</code> to overwrite, <code>false</code> to not overwrite
 * @exception OperationCanceledException if canceled
 */
boolean queryOverwrite(IPath resourcePath) throws OperationCanceledException {
	String overwriteAnswer = overwriteCallback.queryOverwrite(resourcePath.makeRelative().toString());

	if (overwriteAnswer.equals(IOverwriteQuery.CANCEL))
		throw new OperationCanceledException(DataTransferMessages.getString("DataTransfer.emptyString")); //$NON-NLS-1$
				
	if (overwriteAnswer.equals(IOverwriteQuery.NO)) {
		return false;
	}
			
	if (overwriteAnswer.equals(IOverwriteQuery.ALL))
		overwriteResources = true;

	return true;
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
	overwriteResources = value;
}
}
