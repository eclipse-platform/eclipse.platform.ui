package org.eclipse.ui.externaltools.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * Represents the context the external tool is running in
 * that a variable uses to expand itself.
 */
public final class ExpandVariableContext {
	public static final ExpandVariableContext EMPTY_CONTEXT = new ExpandVariableContext(null);
	
	private IProject project = null;
	private IResource selectedResource = null;
	private String buildType = IExternalToolConstants.BUILD_TYPE_NONE;
	
	/**
	 * Create a context for an external tool running
	 * as a builder on the given project.
	 * 
	 * @param project the <code>IProject</code> being built.
	 * @param buildKind the kind of build being performed
	 * 		(see <code>IncrementalProjectBuilder</code>).
	 */
	public ExpandVariableContext(IProject project, int buildKind) {
		super();
		this.project = project;
		switch (buildKind) {
			case IncrementalProjectBuilder.INCREMENTAL_BUILD :
				this.buildType = IExternalToolConstants.BUILD_TYPE_INCREMENTAL;
				break;
			case IncrementalProjectBuilder.FULL_BUILD :
				this.buildType = IExternalToolConstants.BUILD_TYPE_FULL;
				break;
			case IncrementalProjectBuilder.AUTO_BUILD :
				this.buildType = IExternalToolConstants.BUILD_TYPE_AUTO;
				break;
			default :
				this.buildType = IExternalToolConstants.BUILD_TYPE_NONE;
				break;
		}
	}
	
	/**
	 * Create a context for an external tool running
	 * with the given resource selected.
	 * 
	 * @param selectedResource the <code>IResource</code> selected
	 * 		or <code>null</code> if none.
	 */
	public ExpandVariableContext(IResource selectedResource) {
		super();
		if (selectedResource != null) {
			this.selectedResource = selectedResource;
			this.project = selectedResource.getProject();
		}
	}
	
	/**
	 * Returns the build type being performed if the
	 * external tool is being run as a project builder.
	 * 
	 * @return one of the <code>IExternalToolConstants.BUILD_TYPE_*</code> constants.
	 */
	public String getBuildType() {
		return buildType;
	}
	
	/**
	 * Returns the project which the variable can use. This
	 * will the the project being built if the tool is being
	 * run as a builder. Otherwise, it is the project of the
	 * selected resource, or <code>null</code> if none.
	 * 
	 * @return the <code>IProject</code> or <code>null</code> if none
	 */
	public IProject getProject() {
		return project;
	}
	
	/**
	 * Returns the resource selected at the time the tool
	 * is run, or <code>null</code> if none selected.
	 * 
	 * @return the <code>IResource</code> selected, or <code>null</code> if none
	 */
	public IResource getSelectedResource() {
		return selectedResource;
	}
}
