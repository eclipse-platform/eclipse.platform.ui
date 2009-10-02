/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.manipulation.FileBufferOperationRunner;
import org.eclipse.core.filebuffers.manipulation.IFileBufferOperation;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Operation handler for a file buffer.
 * <p>
 * This class may be instantiated or be subclassed.</p>
 *
 * @since 3.1
 */
public class FileBufferOperationHandler extends AbstractHandler {

	private IFileBufferOperation fFileBufferOperation;
	private IWorkbenchWindow fWindow;
	private IResource[] fResources;
	private IPath fLocation;

	/**
	 * Creates a new file buffer operation handler.
	 *
	 * @param fileBufferOperation the file buffer operation
	 */
	public FileBufferOperationHandler(IFileBufferOperation fileBufferOperation) {
		fFileBufferOperation= fileBufferOperation;
	}

	/**
	 * Initializes this file buffer operation handler with the given resources and the given location. The array of resources
	 * is adopted by this handler and may not be modified by clients after that method has been called.
	 *
	 * @param resources the resources to be adopted
	 * @param location the location
	 */
	public void initialize(IResource[] resources, IPath location) {
		if (resources != null) {
			fResources= new IResource[resources.length];
			System.arraycopy(resources, 0, fResources, 0, resources.length);
		} else {
			fResources= null;
		}
		fLocation= location;
	}

	/**
	 * Computes the selected resources.
	 */
	protected final void computeSelectedResources() {

		if (fResources != null || fLocation != null)
			return;

		ISelection selection= getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection= (IStructuredSelection) selection;
			ArrayList resources= new ArrayList(structuredSelection.size());

			Iterator e= structuredSelection.iterator();
			while (e.hasNext()) {
				Object element= e.next();
				if (element instanceof IResource)
					resources.add(element);
				else if (element instanceof IAdaptable) {
					IAdaptable adaptable= (IAdaptable) element;
					Object adapter= adaptable.getAdapter(IResource.class);
					if (adapter instanceof IResource)
						resources.add(adapter);
				}
			}

			if (!resources.isEmpty())
				fResources= (IResource[]) resources.toArray(new IResource[resources.size()]);

		} else if (selection instanceof ITextSelection) {
			IWorkbenchWindow window= getWorkbenchWindow();
			if (window != null) {
				IWorkbenchPart workbenchPart= window.getPartService().getActivePart();
				if (workbenchPart instanceof IEditorPart) {
					IEditorPart editorPart= (IEditorPart) workbenchPart;
					IEditorInput input= editorPart.getEditorInput();
					Object adapter= input.getAdapter(IResource.class);
					if (adapter instanceof IResource)
						fResources= new IResource[] { (IResource) adapter };
					else {
						adapter= input.getAdapter(ILocationProvider.class);
						if (adapter instanceof ILocationProvider) {
							ILocationProvider provider= (ILocationProvider) adapter;
							fLocation= provider.getPath(input);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the selection of the active workbench window.
	 *
	 * @return the current selection in the active workbench window or <code>null</code>
	 */
	protected final ISelection getSelection() {
		IWorkbenchWindow window= getWorkbenchWindow();
		if (window != null)
			return window.getSelectionService().getSelection();
		return null;
	}

	/**
	 * Returns the active workbench window.
	 *
	 * @return the active workbench window or <code>null</code> if not available
	 */
	protected final IWorkbenchWindow getWorkbenchWindow() {
		if (fWindow == null)
			fWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return fWindow;
	}

	/**
	 * Collects the files out of the given resources.
	 *
	 * @param resources the resources from which to get the files
	 * @return an array of files
	 */
	protected IFile[] collectFiles(IResource[] resources) {
		Set files= new HashSet();
		for (int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if ((IResource.FILE & resource.getType()) > 0)
				files.add(resource);
		}
		return (IFile[]) files.toArray(new IFile[files.size()]);
	}

	/**
	 * Runs the given operation.
	 *
	 * @param files the file on which to run this operation
	 * @param location the file buffer location
	 * @param fileBufferOperation the operation to run
	 */
	protected final void doRun(final IFile[] files, final IPath location, final IFileBufferOperation fileBufferOperation) {
		Job job= new Job(fileBufferOperation.getOperationName()) {
			protected IStatus run(IProgressMonitor monitor) {
				IStatus status;

				try {

					int ticks= 100;
					monitor.beginTask(fFileBufferOperation.getOperationName(), ticks);
					try {
						IPath[] locations;
						if (files != null) {
							ticks -= 30;
							locations= generateLocations(files, new SubProgressMonitor(monitor, 30));
						} else
							locations= new IPath[] { location };

						if (locations != null && locations.length > 0) {
							FileBufferOperationRunner runner= new FileBufferOperationRunner(FileBuffers.getTextFileBufferManager(), getShell());
							runner.execute(locations, fileBufferOperation, new SubProgressMonitor(monitor, ticks));
						}
						status= Status.OK_STATUS;
					} finally {
						monitor.done();
					}

				} catch (OperationCanceledException e) {
					status= new Status(IStatus.CANCEL, EditorsUI.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
				} catch (CoreException e) {
					status= new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, "", e); //$NON-NLS-1$
				}
				return status;
			}
		};

		job.setUser(true);
		job.schedule();
	}

	/**
	 * Returns the shell of the active workbench window.
	 *
	 * @return the shell
	 */
	protected final Shell getShell() {
		IWorkbenchWindow window= getWorkbenchWindow();
		return window == null ? null : window.getShell();
	}

	/**
	 * Generates the file buffer locations out of the given files.
	 *
	 * @param files an array of files
	 * @param progressMonitor the progress monitor
	 * @return an array with the generated locations
	 */
	protected final IPath[] generateLocations(IFile[] files, IProgressMonitor progressMonitor) {
		progressMonitor.beginTask(TextEditorMessages.FileBufferOperationHandler_collectionFiles_label, files.length);
		try {
			Set locations= new HashSet();
			for (int i= 0; i < files.length; i++) {
				IPath fullPath= files[i].getFullPath();
				if (isAcceptableLocation(fullPath))
					locations.add(fullPath);
				progressMonitor.worked(1);
			}
			return (IPath[]) locations.toArray(new IPath[locations.size()]);

		} finally {
			progressMonitor.done();
		}
	}

	/**
	 * Tells whether the given location is accepted by this handler.
	 *
	 * @param location a file buffer location
	 * @return <code>true</code> if the given location is acceptable
	 */
	protected boolean isAcceptableLocation(IPath location) {
		return true;
	}

	/*
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 * @since 3.1
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		computeSelectedResources();
		try {

			if (fResources != null && fResources.length > 0) {
				IFile[] files= collectFiles(fResources);
				if (files != null && files.length > 0)
					doRun(files, null, fFileBufferOperation);
			} else if (isAcceptableLocation(fLocation))
				doRun(null, fLocation, fFileBufferOperation);

			// Standard return value. DO NOT CHANGE.
			return null;

		} finally {
			fResources= null;
			fLocation= null;
		}
	}
}
