package org.eclipse.ui.wizards.datatransfer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.jface.operation.*;
import org.eclipse.swt.*;
import java.io.*;
import java.util.*;

/**
 *	Operation for exporting the contents of a resource to the local file system.
 */
/*package*/ class FileSystemExportOperation implements IRunnableWithProgress {
	private IPath				path;
	private IProgressMonitor	monitor;
	private FileSystemExporter	exporter = new FileSystemExporter();
	private List				resourcesToExport;
	private IOverwriteQuery		overwriteCallback;
	private IResource			resource;
	private List				errorTable = new ArrayList(1);
	
	private boolean				overwriteFiles = false;
	private boolean				createLeadupStructure = true;
	private boolean				createContainerDirectories = true;
/**
 *  Create an instance of this class.  Use this constructor if you wish to
 *  export specific resources without a common parent resource
 */
public FileSystemExportOperation(List resources,String destinationPath,IOverwriteQuery overwriteImplementor) {
	super();

	// Eliminate redundancies in list of resources being exported
	Iterator elementsEnum = resources.iterator();
	while (elementsEnum.hasNext()) {
		IResource currentResource = (IResource) elementsEnum.next();
		if (isDescendent(resources, currentResource))
			elementsEnum.remove(); //Remove currentResource
	}

	resourcesToExport = resources;
	path = new Path(destinationPath);
	overwriteCallback = overwriteImplementor;
}
/**
 *  Create an instance of this class.  Use this constructor if you wish to
 *  recursively export a single resource
 */
public FileSystemExportOperation(IResource res,String destinationPath,IOverwriteQuery overwriteImplementor) {
	super();
	resource = res;
	path = new Path(destinationPath);
	overwriteCallback = overwriteImplementor;
}
/**
 *  Create an instance of this class.  Use this constructor if you wish to
 *  export specific resources with a common parent resource (affects container
 *  directory creation)
 */
public FileSystemExportOperation(IResource res, List resources, String destinationPath, IOverwriteQuery overwriteImplementor) {
	this(res,destinationPath,overwriteImplementor);
	resourcesToExport = resources;
}
/**
 * Add a new entry to the error table with the passed information
 */
protected void addError(String message,Throwable e) {
	errorTable.add(
		new Status(
			IStatus.ERROR,
			PlatformUI.PLUGIN_ID,
			0,
			message,
			e));
}
/**
 *  Answer the total number of file resources that exist at or below self in the
 *  resources hierarchy.
 *
 *  @return int
 *  @param resource org.eclipse.core.resources.IResource
 */
protected int countChildrenOf(IResource resource) throws CoreException {
	if (resource.getType() == IResource.FILE)
		return 1;

	int count = 0;
	if (resource.isAccessible()) {
		IResource[] children = ((IContainer) resource).members();
		for (int i = 0; i<children.length; i++)
			count += countChildrenOf(children[i]);
	}

	return count;
}
/**
 *	Answer a boolean indicating the number of file resources that were
 *	specified for export
 *
 *	@return int
 */
protected int countSelectedResources() throws CoreException {
	int result = 0;
	Iterator resources = resourcesToExport.iterator();
	
	while (resources.hasNext())
		result += countChildrenOf((IResource)resources.next());
		
	return result;
}
/**
 *  Create the directories required for exporting the passed resource,
 *  based upon its container hierarchy
 *
 *  @param resource org.eclipse.core.resources.IResource
 */
protected void createLeadupDirectoriesFor(IResource resource) {
	IPath resourcePath = resource.getFullPath().removeLastSegments(1);

	for (int i = 0; i < resourcePath.segmentCount(); i++) {
		path = path.append(resourcePath.segment(i));
		exporter.createFolder(path);
	}
}
/**
 *	Recursively export the previously-specified resource
 */
protected void exportAllResources() throws InterruptedException {
	if (resource.getType() == IResource.FILE)
		exportFile((IFile)resource,path);
	else {
		try {
			exportChildren(((IContainer)resource).members(),path);
		} catch (CoreException e) {
			// not safe to show a dialog
			// should never happen because the file system export wizard ensures that the
			// single resource chosen for export is both existent and accessible
			errorTable.add(e);
		}
	}
}
/**
 *	Export all of the resources contained in the passed collection
 *
 *	@param children java.util.Enumeration
 *	@param currentPath IPath
 */
protected void exportChildren(IResource[] children,IPath currentPath) throws InterruptedException {
	for (int i = 0; i<children.length; i++) {
		IResource child = children[i];
		if (!child.isAccessible())
			continue;
		
		if (child.getType() == IResource.FILE)
			exportFile((IFile)child,currentPath);
		else {
			IPath destination = currentPath.append(child.getName());
			exporter.createFolder(destination);
			try {
				exportChildren(((IContainer)child).members(),destination);
			} catch (CoreException e) {
				// not safe to show a dialog
				// should never happen because:
				// i. this method is called recursively iterating over the result of #members,
				//		which only answers existing children
				// ii. there is an #isAccessible check done before #members is invoked
				errorTable.add(e.getStatus());
			}
		}
	}
}
/**
 *  Export the passed file to the specified location
 *
 *  @param file org.eclipse.core.resources.IFile
 *  @param location org.eclipse.core.runtime.IPath
 */
protected void exportFile(IFile file, IPath location)
	throws InterruptedException {
	IPath fullPath = location.append(file.getName());
	monitor.subTask(file.getFullPath().toString());
	String properPathString = fullPath.toOSString();
	File targetFile = new File(properPathString);

	if (targetFile.exists()) {
		if (!targetFile.canWrite()) {
			errorTable
				.add(new Status(
					IStatus.ERROR,
					PlatformUI.PLUGIN_ID,
					0,
					DataTransferMessages.format("DataTransfer.cannotOverwrite", //$NON-NLS-1$
			new Object[] { targetFile.getAbsolutePath()}), null));
			monitor.worked(1);
			return;
		}

		if (!overwriteFiles) {
			String overwriteAnswer = overwriteCallback.queryOverwrite(properPathString);

			if (overwriteAnswer.equals(IOverwriteQuery.CANCEL))
				throw new InterruptedException();

			if (overwriteAnswer.equals(IOverwriteQuery.NO)) {
				monitor.worked(1);
				return;
			}

			if (overwriteAnswer.equals(IOverwriteQuery.ALL))
				overwriteFiles = true;
		}
	}

	try {
		exporter.write(file, fullPath);
	} catch (IOException e) {
		errorTable
			.add(new Status(
				IStatus.ERROR,
				PlatformUI.PLUGIN_ID,
				0,
				DataTransferMessages.format(
					"DataTransfer.errorExporting", //$NON-NLS-1$
					new Object[] { fullPath, e.getMessage() }),
		e));
	} catch (CoreException e) {
		errorTable
			.add(new Status(
				IStatus.ERROR,
				PlatformUI.PLUGIN_ID,
				0,
				DataTransferMessages.format(
					"DataTransfer.errorExporting", //$NON-NLS-1$
					new Object[] { fullPath, e.getMessage() }),
		e));
	}

	monitor.worked(1);
	ModalContext.checkCanceled(monitor);
}
/**
 *	Export the resources contained in the previously-defined
 *	resourcesToExport collection
 */
protected void exportSpecifiedResources() throws InterruptedException {
	Iterator resources = resourcesToExport.iterator();
	IPath initPath = (IPath)path.clone();
	
	while (resources.hasNext()) {
		IResource currentResource = (IResource)resources.next();
		if (!currentResource.isAccessible())
			continue;
			
		path = initPath;

		if (resource == null) {
			// No root resource specified and creation of containment directories
			// is required.  Create containers from depth 2 onwards (ie.- project's
			// child inclusive) for each resource being exported.
			if (createLeadupStructure)
				createLeadupDirectoriesFor(currentResource);

		} else {
			// Root resource specified.  Must create containment directories
			// from this point onwards for each resource being exported
			IPath containersToCreate =
				currentResource.getFullPath().removeFirstSegments(
					resource.getFullPath().segmentCount()).removeLastSegments(1);

			for (int i = 0; i < containersToCreate.segmentCount(); i++) {
				path = path.append(containersToCreate.segment(i));
				exporter.createFolder(path);
			}
		}

		if (currentResource.getType() == IResource.FILE)
			exportFile((IFile)currentResource,path);
		else {
			if (createContainerDirectories) {
				path = path.append(currentResource.getName());
				exporter.createFolder(path);
			}

			try {	
				exportChildren(((IContainer)currentResource).members(),path);
			} catch (CoreException e) {
				// should never happen because #isAccessible is called before #members is invoked,
				// which implicitly does an existence check
				errorTable.add(e.getStatus());
			}
		}
	}
}
/**
 * Returns the status of the export operation.
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
		DataTransferMessages.getString("FileSystemExportOperation.problemsExporting"),  //$NON-NLS-1$
		null);
}
/**
 *  Answer a boolean indicating whether the passed child is a descendent
 *  of one or more members of the passed resources collection
 *
 *  @return boolean
 *  @param resources java.util.List
 *  @param child org.eclipse.core.resources.IResource
 */
protected boolean isDescendent(List resources, IResource child) {
	if (child.getType() == IResource.PROJECT)
		return false;

	IResource parent = child.getParent();
	if (resources.contains(parent))
		return true;
		
	return isDescendent(resources,parent);
}
/**
 *	Export the resources that were previously specified for export
 *	(or if a single resource was specified then export it recursively)
 */
public void run(IProgressMonitor monitor) throws InterruptedException {
	this.monitor = monitor;

	if (resource != null) {
		if (createLeadupStructure)
			createLeadupDirectoriesFor(resource);

		if (createContainerDirectories &&
			resource.getType() != IResource.FILE) {	// ensure it's a container
				path = path.append(resource.getName());
				exporter.createFolder(path);
		}
	}

	try {
		int totalWork = IProgressMonitor.UNKNOWN;
		try {
			if (resourcesToExport == null)
				totalWork = countChildrenOf(resource);
			else
				totalWork = countSelectedResources();
		}
		catch (CoreException e) {
			// Should not happen
			errorTable.add(e.getStatus());
		}
		monitor.beginTask(DataTransferMessages.getString("DataTransfer.exportingTitle"), totalWork); //$NON-NLS-1$
		if (resourcesToExport == null) {
			exportAllResources();
		} else {
			exportSpecifiedResources();
		}
	} finally {
		monitor.done();
	}
}
/**
 *	Set this boolean indicating whether a directory should be created for
 *	Folder resources that are explicitly passed for export
 *
 *	@param value boolean
 */
public void setCreateContainerDirectories(boolean value) {
	createContainerDirectories = value;
}
/**
 *	Set this boolean indicating whether each exported resource's complete path should
 *	include containment hierarchies as dictated by its parents
 *
 *	@param value boolean
 */
public void setCreateLeadupStructure(boolean value) {
	createLeadupStructure = value;
}
/**
 *	Set this boolean indicating whether exported resources should automatically
 *	overwrite existing files when a conflict occurs
 *
 *	@param value boolean
 */
public void setOverwriteFiles(boolean value) {
	overwriteFiles = value;
}
}
