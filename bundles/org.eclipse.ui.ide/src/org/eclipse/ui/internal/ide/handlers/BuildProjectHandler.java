/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Default Handler for 'Build Project' command
 * 
 * @since 4.3
 * 
 */
public class BuildProjectHandler extends AbstractHandler {

	/**
	 * @throws ExecutionException
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window != null) {

			ISelection currentSelection = HandlerUtil
					.getCurrentSelection(event);

			if (currentSelection instanceof IStructuredSelection) {
				runBuildAction(window, currentSelection);
			} else {
				currentSelection = extractSelectionFromEditorInput(HandlerUtil
						.getActiveEditorInput(event));
				runBuildAction(window, currentSelection);
			}
		}
		return null;
	}

	private ISelection extractSelectionFromEditorInput(
			IEditorInput activeEditorInput) {
		if (activeEditorInput instanceof FileEditorInput) {
			IProject project = ((FileEditorInput) activeEditorInput).getFile()
					.getProject();
			return new StructuredSelection(project);
		}

		return null;
	}

	private void runBuildAction(IWorkbenchWindow window,
			ISelection currentSelection) {
		BuildAction buildAction = new BuildAction(window,
				IncrementalProjectBuilder.INCREMENTAL_BUILD);
		buildAction.selectionChanged((IStructuredSelection) currentSelection);
		buildAction.run();
	}

}