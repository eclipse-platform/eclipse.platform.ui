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
package org.eclipse.ui.editors.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.manipulation.FileBufferOperationRunner;
import org.eclipse.core.filebuffers.manipulation.IFileBufferOperation;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;

/**
 * Not yet for public use. API under construction.
 * 
 * @since 3.1
 */
public class FileBufferOperationHandler extends AbstractHandler {
	
	private IFileBufferOperation fFileBufferOperation;
	private IWorkbenchWindow fWindow;
	private IResource[] fResources;
	private IPath fLocation;
	
	public FileBufferOperationHandler(IFileBufferOperation fileBufferOperation) {
		fFileBufferOperation= fileBufferOperation;
	}
	
	public void initialize(IResource[] resources,IPath location) {
		fResources= resources;
		fLocation= location;
	}

	/*
	 * @see org.eclipse.ui.commands.IHandler#execute(java.util.Map)
	 */
	public Object execute(Map parameterValuesByName) throws ExecutionException {
		computeSelectedResources();
		try {
			
			if (fResources != null && fResources.length > 0) {
				IFile[] files= collectFiles(fResources);
				if (files != null && files.length > 0)
					doRun(files, null, fFileBufferOperation);
			} else if (fLocation != null)
				doRun(null, fLocation, fFileBufferOperation);
			
			// Standard return value. DO NOT CHANGE.
			return null;
			
		} finally {
			fResources= null;
			fLocation= null;
		}
	}
	
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
	
	protected final ISelection getSelection() {
		IWorkbenchWindow window= getWorkbenchWindow();
		if (window != null)
			return window.getSelectionService().getSelection();
		return null;
	}
	
	protected final IWorkbenchWindow getWorkbenchWindow() {
		if (fWindow == null)
			fWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return fWindow;
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
						status= new Status(IStatus.OK, EditorsUI.PLUGIN_ID, IStatus.OK, "", null);  //$NON-NLS-1$
					} finally {
						monitor.done();
					}
					
				} catch (OperationCanceledException e) {
					status= new Status(IStatus.CANCEL, EditorsUI.PLUGIN_ID, IStatus.CANCEL, "", null); //$NON-NLS-1$
				} catch (CoreException e) {
					status= new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.ERROR, "", e); //$NON-NLS-1$
				}
				return status;
			}
		};
		
		job.setUser(true);
		job.schedule();
	}
	
	protected final Shell getShell() {
		IWorkbenchWindow window= getWorkbenchWindow();
		return window == null ? null : window.getShell();
	}
	
	protected final IPath[] generateLocations(IFile[] files, IProgressMonitor progressMonitor) {
		progressMonitor.beginTask("collecting files", files.length); 
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
	
	protected boolean isAcceptableLocation(IPath location) {
		return true;
	}
}
