/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.wizards.datatransfer;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.operation.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 *	Operation for exporting a resource and its children to a new .zip file
 */
/*package*/
class ZipFileExportOperation implements IRunnableWithProgress {
	private ZipFileExporter exporter;
	private String destinationFilename;
	private IProgressMonitor monitor;

	private List resourcesToExport;
	private IResource resource;
	private List errorTable = new ArrayList(1); //IStatus

	private boolean useCompression = true;
	private boolean createLeadupStructure = true;
	private boolean generateManifestFile = false;
	/**
	 *	Create an instance of this class.  Use this constructor if you wish to
	 *	export specific resources without a common parent resource
	 *
	 *	@param resources java.util.Vector
	 *	@param filename java.lang.String
	 */
	public ZipFileExportOperation(List resources, String filename) {
		super();

		// Eliminate redundancies in list of resources being exported
		Iterator elementsEnum = resources.iterator();
		while (elementsEnum.hasNext()) {
			IResource currentResource = (IResource) elementsEnum.next();
			if (isDescendent(resources, currentResource))
				elementsEnum.remove(); //Removes currentResource;
		}

		resourcesToExport = resources;
		destinationFilename = filename;
	}
	/**
	 *  Create an instance of this class.  Use this constructor if you wish
	 *  to recursively export a single resource.
	 *
	 *  @param res org.eclipse.core.resources.IResource;
	 *  @param filename java.lang.String
	 */
	public ZipFileExportOperation(IResource res, String filename) {
		super();
		resource = res;
		destinationFilename = filename;
	}
	/**
	 *  Create an instance of this class.  Use this constructor if you wish to
	 *  export specific resources with a common parent resource (affects container
	 *  directory creation)
	 *
	 *  @param res org.eclipse.core.resources.IResource
	 *  @param resources java.util.Vector
	 *  @param filename java.lang.String
	 */
	public ZipFileExportOperation(
		IResource res,
		List resources,
		String filename) {
		this(res, filename);
		resourcesToExport = resources;
	}
	/**
	 * Add a new entry to the error table with the passed information
	 */
	protected void addError(String message, Throwable e) {
		errorTable.add(
			new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, e));
	}
	/**
	 *  Answer the total number of file resources that exist at or below self
	 *  in the resources hierarchy.
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
			for (int i = 0; i < children.length; i++)
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
			result += countChildrenOf((IResource) resources.next());

		return result;
	}

	/**
	 *  Export the passed resource to the destination .zip. Export with
	 * no path leadup
	 *
	 *  @param resource org.eclipse.core.resources.IResource
	 */
	protected void exportResource(IResource resource)
		throws InterruptedException {
		exportResource(resource, 1);
	}

	/**
	 *  Export the passed resource to the destination .zip
	 *
	 *  @param resource org.eclipse.core.resources.IResource
	 *  @param depth - the number of resource levels to be included in
	 *   				the path including the resourse itself.
	 */
	protected void exportResource(IResource resource, int leadupDepth)
		throws InterruptedException {
		if (!resource.isAccessible())
			return;

		if (resource.getType() == IResource.FILE) {
			String destinationName;
			IPath fullPath = resource.getFullPath();
			if (createLeadupStructure)
				destinationName = fullPath.makeRelative().toString();
			else
				destinationName =
					fullPath
						.removeFirstSegments(
							fullPath.segmentCount() - leadupDepth)
						.toString();
			monitor.subTask(destinationName);

			try {
				exporter.write((IFile) resource, destinationName);
			} catch (IOException e) {
				addError(DataTransferMessages.format("DataTransfer.errorExporting", //$NON-NLS-1$
				new Object[] {
					resource.getFullPath().makeRelative(),
					e.getMessage()}),
					e);
			} catch (CoreException e) {
				addError(DataTransferMessages.format("DataTransfer.errorExporting", //$NON-NLS-1$
				new Object[] {
					resource.getFullPath().makeRelative(),
					e.getMessage()}),
					e);
			}

			monitor.worked(1);
			ModalContext.checkCanceled(monitor);
		} else {
			IResource[] children = null;

			try {
				children = ((IContainer) resource).members();
			} catch (CoreException e) {
				// this should never happen because an #isAccessible check is done before #members is invoked
				addError(DataTransferMessages.format("DataTransfer.errorExporting", new Object[] { resource.getFullPath()}), e); //$NON-NLS-1$
			}

			for (int i = 0; i < children.length; i++)
				exportResource(children[i], leadupDepth + 1);

		}
	}
	/**
	 *	Export the resources contained in the previously-defined
	 *	resourcesToExport collection
	 */
	protected void exportSpecifiedResources() throws InterruptedException {
		Iterator resources = resourcesToExport.iterator();

		while (resources.hasNext()) {
			IResource currentResource = (IResource) resources.next();
			exportResource(currentResource);
		}
	}
	/**
	 *	Answer the error table
	 *
	 *	@return Vector of IStatus
	 */
	public List getResult() {
		return errorTable;
	}
	/**
	 * Returns the status of the operation.
	 * If there were any errors, the result is a status object containing
	 * individual status objects for each error.
	 * If there were no errors, the result is a status object with error code <code>OK</code>.
	 *
	 * @return the status
	 */
	public IStatus getStatus() {
		IStatus[] errors = new IStatus[errorTable.size()];
		errorTable.toArray(errors);
		return new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, errors, DataTransferMessages.getString("FileSystemExportOperation.problemsExporting"), //$NON-NLS-1$
		null);
	}
	/**
	 *	Initialize this operation
	 *
	 *	@exception java.io.IOException
	 */
	protected void initialize() throws IOException {
		exporter =
			new ZipFileExporter(
				destinationFilename,
				useCompression,
				generateManifestFile);

	}
	/**
	 *  Answer a boolean indicating whether the passed child is a descendent
	 *  of one or more members of the passed resources collection
	 *
	 *  @return boolean
	 *  @param resources java.util.Vector
	 *  @param child org.eclipse.core.resources.IResource
	 */
	protected boolean isDescendent(List resources, IResource child) {
		if (child.getType() == IResource.PROJECT)
			return false;

		IResource parent = child.getParent();
		if (resources.contains(parent))
			return true;

		return isDescendent(resources, parent);
	}
	/**
	 *	Export the resources that were previously specified for export
	 *	(or if a single resource was specified then export it recursively)
	 */
	public void run(IProgressMonitor monitor)
		throws InvocationTargetException, InterruptedException {
		this.monitor = monitor;

		try {
			initialize();
		} catch (IOException e) {
			throw new InvocationTargetException(e, DataTransferMessages.format("ZipExport.cannotOpen", new Object[] { e.getMessage()})); //$NON-NLS-1$
		}

		try {
			// ie.- a single resource for recursive export was specified
			int totalWork = IProgressMonitor.UNKNOWN;
			try {
				if (resourcesToExport == null)
					totalWork = countChildrenOf(resource);
				else
					totalWork = countSelectedResources();
			} catch (CoreException e) {
				// Should not happen
			}
			monitor.beginTask(DataTransferMessages.getString("DataTransfer.exportingTitle"), totalWork); //$NON-NLS-1$
			if (resourcesToExport == null) {
				exportResource(resource);
			} else {
				// ie.- a list of specific resources to export was specified
				exportSpecifiedResources();
			}

			try {
				exporter.finished();
			} catch (IOException e) {
				throw new InvocationTargetException(e, DataTransferMessages.format("ZipExport.cannotClose", new Object[] { e.getMessage()})); //$NON-NLS-1$
			}
		} finally {
			monitor.done();
		}
	}
	/**
	 *	Set this boolean indicating whether each exported resource's path should
	 *	include containment hierarchies as dictated by its parents
	 *
	 *	@param value boolean
	 */
	public void setCreateLeadupStructure(boolean value) {
		createLeadupStructure = value;
	}
	/**
	 *	Set this boolean indicating whether a manifest.mf file based upon
	 *	the exported contents should be created and included in the final
	 *	archive
	 *
	 *	@param value boolean
	 */
	public void setGenerateManifestFile(boolean value) {
		generateManifestFile = value;
	}
	/**
	 *	Set this boolean indicating whether exported resources should
	 *	be compressed (as opposed to simply being stored)
	 *
	 *	@param value boolean
	 */
	public void setUseCompression(boolean value) {
		useCompression = value;
	}
}
