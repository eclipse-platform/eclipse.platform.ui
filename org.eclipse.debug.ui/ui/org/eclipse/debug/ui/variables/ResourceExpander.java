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
package org.eclipse.debug.ui.variables;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;

/**
 * Expands a resource type variable into the desired
 * result format.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class ResourceExpander extends DefaultVariableExpander {

	/**
	 * Expands the variable to a resource.
	 */
	/*package*/ IResource expand(String varValue, ExpandVariableContext context) {
		if (varValue != null && varValue.length() > 0) {
			return expandToMember(varValue);
		} else {
			return expandUsingContext(context);
		}
	}
	
	/**
	 * Expands using the current context information.
	 * By default, return the selected resource of the
	 * context.
	 */
	/*package*/ IResource expandUsingContext(ExpandVariableContext context) {
		return context.getSelectedResource();
	}
	
	/**
	 * Expands the variable value to a resource. The value
	 * will not be <code>null</code> nor empty. By default,
	 * lookup the member from the workspace root.
	 */
	/*package*/ IResource expandToMember(String varValue) {
		return getWorkspaceRoot().findMember(varValue);
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableLocationExpander.
	 */
	public IPath getPath(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		IResource resource = expand(varValue, context);
		if (resource != null) {
			if (isPathVariable(varTag)) {
				return resource.getFullPath();
			} else {
				return resource.getLocation();
			}
		}
		throwExpansionException(varTag, LaunchConfigurationsMessages.getString("ResourceExpander.No_resource_selected._1")); //$NON-NLS-1$
		return null;
	}
	
	/**
	 * Returns whether the given variable tag is a known path
	 * variable tag. Path variable tags represent variables that
	 * expand to paths relative to the workspace root.
	 */
	private boolean isPathVariable(String varTag) {
		return varTag.equals(IVariableConstants.VAR_CONTAINER_PATH) ||
				varTag.equals(IVariableConstants.VAR_PROJECT_PATH) ||
				varTag.equals(IVariableConstants.VAR_RESOURCE_PATH);
	}

	/* (non-Javadoc)
	 * Method declared on IVariableResourceExpander.
	 */
	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		IResource resource = expand(varValue, context);
		if (resource != null) {
			return new IResource[] {resource};
		}
		throwExpansionException(varTag, LaunchConfigurationsMessages.getString("ResourceExpander.No_resource_selected._2")); //$NON-NLS-1$
		return null;
	}
	
	/**
	 * Returns the workspace root resource.
	 */
	protected final IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/**
	 * Returns a string representation of the path to a file or directory
	 * for the given variable tag and value or <code>null</code>.
	 * 
	 * @see IVariableTextExpander#getText(String, String, ExpandVariableContext)
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		IPath path= getPath(varTag, varValue, context);
		if (path != null) {
			return path.toOSString();
		}
		throwExpansionException(varTag, LaunchConfigurationsMessages.getString("ResourceExpander.No_resource_selected._3")); //$NON-NLS-1$
		return null;
	}

}
