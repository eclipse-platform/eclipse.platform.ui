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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;
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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * File buffer operation action.
 *
 * @since 3.1
 */
public class FileBufferOperationAction extends Action implements IWorkbenchWindowActionDelegate {

	private Set fResources;
	private IPath fLocation;
	private IWorkbenchWindow fWindow;
	protected IFileBufferOperation fFileBufferOperation;

	protected FileBufferOperationAction(IFileBufferOperation fileBufferOperation) {
		Assert.isNotNull(fileBufferOperation);
		fFileBufferOperation= fileBufferOperation;
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fResources= null;
		fWindow= null;
		fFileBufferOperation= null;
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

		fResources= new HashSet();
		fLocation= null;

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection= (IStructuredSelection) selection;

			Iterator e= structuredSelection.iterator();
			while (e.hasNext()) {
				Object element= e.next();
				if (element instanceof IResource)
					fResources.add(element);
				else if (element instanceof IAdaptable) {
					IAdaptable adaptable= (IAdaptable) element;
					Object adapter= adaptable.getAdapter(IResource.class);
					if (adapter instanceof IResource)
						fResources.add(adapter);
				}
			}
		}

		if (selection instanceof ITextSelection) {
			IWorkbenchWindow window= getWorkbenchWindow();
			if (window != null) {
				IWorkbenchPart workbenchPart= window.getPartService().getActivePart();
				if (workbenchPart instanceof IEditorPart) {
					IEditorPart editorPart= (IEditorPart) workbenchPart;
					IEditorInput input= editorPart.getEditorInput();
					Object adapter= input.getAdapter(IResource.class);
					if (adapter instanceof IResource)
						fResources.add(adapter);
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

		action.setText(getText());
		action.setEnabled(!fResources.isEmpty() || fLocation != null);
	}

	protected final IWorkbenchWindow getWorkbenchWindow() {
		if (fWindow == null)
			fWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return fWindow;
	}

	protected final Shell getShell() {
		IWorkbenchWindow window= getWorkbenchWindow();
		return window == null ? null : window.getShell();
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fResources != null && !fResources.isEmpty()) {
			IFile[] files= collectFiles((IResource[]) fResources.toArray(new IResource[fResources.size()]));
			if (files != null && files.length > 0)
				doRun(files, null, fFileBufferOperation);
		} else if (isAcceptableLocation(fLocation))
			doRun(null, fLocation, fFileBufferOperation);
	}

	protected IFile[] collectFiles(IResource[] resources) {
		Set files= new HashSet();
		for (int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if ((IResource.FILE & resource.getType()) > 0)
				files.add(resource);
		}
		return (IFile[]) files.toArray(new IFile[files.size()]);
	}

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

	protected final IPath[] generateLocations(IFile[] files, IProgressMonitor progressMonitor) {
		progressMonitor.beginTask(TextEditorMessages.FileBufferOperationAction_collectionFiles_label, files.length);
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
	 * Tells whether this action accepts the given location.
	 *
	 * @param location the location
	 * @return <code>true</code> if the given location is acceptable
	 */
	protected boolean isAcceptableLocation(IPath location) {
		return true;
	}
}
