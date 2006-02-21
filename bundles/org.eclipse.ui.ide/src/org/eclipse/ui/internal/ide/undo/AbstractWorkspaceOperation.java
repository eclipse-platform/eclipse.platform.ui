/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.undo;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.2
 * 
 */
public abstract class AbstractWorkspaceOperation extends AbstractOperation {

	protected IResource[] resources;

	private IResourceChangeListener listener;

	private boolean isValid = true;

	private String errorTitle;

	AbstractWorkspaceOperation(String name, String errorTitle) {
		super(name);
		this.addContext(PlatformUI.getWorkbench().getOperationSupport()
				.getUndoContext());
		this.errorTitle = errorTitle;
	}

	protected void setTargetResources(IResource[] resources) {
		this.resources = resources;
		if (listener == null && resources != null) {
			addWorkspaceListener();
		}
	}

	protected IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	private void addWorkspaceListener() {
		listener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					event.getDelta().accept(getDeltaVisitor());
				} catch (CoreException e) {
					markInvalid();
				}
			}

		};
		getWorkspace().addResourceChangeListener(listener,
				IResourceChangeEvent.POST_CHANGE);
	}

	public void dispose() {
		getWorkspace().removeResourceChangeListener(listener);
		super.dispose();
	}

	protected void markInvalid() {
		isValid = false;
	}

	public boolean canUndo() {
		return isValid;
	}

	public boolean canRedo() {
		return isValid;
	}

	public IStatus execute(IProgressMonitor monitor, final IAdaptable info) {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doExecute(monitor, info);
				}
			}, null);
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(info),
					errorTitle, null,
					e.getStatus());
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#redo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */

	public IStatus redo(IProgressMonitor monitor, IAdaptable info) {
		return execute(monitor, info);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#undo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus undo(IProgressMonitor monitor, final IAdaptable info) {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doUndo(monitor, info);
				}
			}, null);
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(info),
					errorTitle, null,
					e.getStatus());
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	protected abstract void doUndo(IProgressMonitor monitor, IAdaptable info)
			throws CoreException;

	protected abstract void doExecute(IProgressMonitor monitor, IAdaptable info)
			throws CoreException;

	protected IResourceDeltaVisitor getDeltaVisitor() {
		return new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) {
				if (resources == null)
					return false;
				for (int i = 0; i < resources.length; i++) {
					if (resources[i].equals(delta.getResource())) {
						if (isResourceInvalid(resources[i], delta)) {
							markInvalid();
							return false;
						}
					}
				}
				return true;
			}
		};
	}

	/**
	 * One of our resources is affected. Does this make this operation invalid?
	 */
	protected boolean isResourceInvalid(IResource resource, IResourceDelta delta) {
		return true;
	}
	
	protected Shell getShell(IAdaptable info) {
		if (info != null) {
			Shell shell = (Shell)info.getAdapter(Shell.class);
			if (shell != null) {
				return shell;
			}
		}
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
}
