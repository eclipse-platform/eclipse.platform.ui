/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;

/**
 * Default handler for 'Build All' command.
 * 
 * @since 3.6
 */
public class BuildAllProjectsHandler extends AbstractHandler {
	
	/**
	 * @throws ExecutionException  
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
    	if (isEnabled()) {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			if (window != null) {
				GlobalBuildAction globalBuildAction = new GlobalBuildAction(window, IncrementalProjectBuilder.INCREMENTAL_BUILD);
				try {
					globalBuildAction.run();
				} finally {
					globalBuildAction.dispose();
				}
			}
    	}
		return null;
	}
	
	/*
	 * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object)
	 */
	public void setEnabled(Object evaluationContext) {
    	IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
    	boolean enabled = BuildUtilities.isEnabled(projects, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		setBaseEnabled(enabled);
	}
}
