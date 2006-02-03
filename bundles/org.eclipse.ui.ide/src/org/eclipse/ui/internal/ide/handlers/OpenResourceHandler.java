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

package org.eclipse.ui.internal.ide.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.OpenResourceDialog;

/**
 * Implements the open resource action. Opens a dialog prompting for a file and
 * opens the selected file in an editor.
 * 
 * @since 2.1
 */
public final class OpenResourceHandler extends Action implements IHandler,
		IWorkbenchWindowActionDelegate {

	/**
	 * The identifier of the parameter storing the file path.
	 */
	private static final String PARAM_ID_FILE_PATH = "filePath"; //$NON-NLS-1$

	/**
	 * A collection of objects listening to changes to this manager. This
	 * collection is <code>null</code> if there are no listeners.
	 */
	private transient ListenerList listenerList = null;

	/**
	 * Creates a new instance of the class.
	 */
	public OpenResourceHandler() {
		super();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.OPEN_WORKSPACE_FILE_ACTION);
	}

	public final void addHandlerListener(final IHandlerListener listener) {
		if (listenerList == null) {
			listenerList = new ListenerList(ListenerList.IDENTITY);
		}

		listenerList.add(listener);
	}

	public final void dispose() {
		listenerList = null;
	}

	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		final IFile file;
		if (event.getParameter(PARAM_ID_FILE_PATH) == null) {
			// Prompt the user for the resource to open.
			file = queryFileResource();

		} else {
			// Use the given parameter.
			final IResource resource = (IResource) event
					.getObjectParameterForExecution(PARAM_ID_FILE_PATH);
			if (!(resource instanceof IFile)) {
				throw new ExecutionException(
						"filePath parameter must identify a file"); //$NON-NLS-1$
			}
			file = (IFile) resource;
		}

		// We still don't have a file for some reason, then just give up.
		if (file == null) {
			return null;
		}

		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			throw new ExecutionException("no active workbench window"); //$NON-NLS-1$
		}

		final IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			throw new ExecutionException("no active workbench page"); //$NON-NLS-1$
		}
		try {
			IDE.openEditor(page, file, true);
		} catch (final PartInitException e) {
			throw new ExecutionException("error opening file in editor", e); //$NON-NLS-1$
		}

		return null;
	}

	public final void init(final IWorkbenchWindow window) {
		// Do nothing.
	}

	/**
	 * Query the user for the resource that should be opened
	 * 
	 * @return the resource that should be opened or null if the resource
	 *         selection dialog was cancelled.
	 */
	private final IFile queryFileResource() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		final Shell parent = window.getShell();
		final IContainer input = ResourcesPlugin.getWorkspace().getRoot();

		final OpenResourceDialog dialog = new OpenResourceDialog(parent, input,
				IResource.FILE);
		final int resultCode = dialog.open();
		if (resultCode != IDialogConstants.OK_ID)
			return null;

		final Object[] result = dialog.getResult();
		if (result == null || result.length == 0
				|| result[0] instanceof IFile == false)
			return null;

		return (IFile) result[0];
	}

	public final void removeHandlerListener(final IHandlerListener listener) {
		if (listenerList != null) {
			listenerList.remove(listener);

			if (listenerList.isEmpty()) {
				listenerList = null;
			}
		}
	}

	public final void run(final IAction action) {
		try {
			execute(new ExecutionEvent());
		} catch (final ExecutionException e) {
			// TODO Do something meaningful and poignant.
		}
	}

	public final void selectionChanged(final IAction action,
			final ISelection selection) {
		// Do nothing.
	}
}
