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
package org.eclipse.debug.internal.ui.stringsubstitution;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Common function of variable resolvers.
 * 
 * @since 3.0
 */
public class ResourceResolver implements IDynamicVariableResolver {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IContextVariableResolver#resolveValue(org.eclipse.debug.internal.core.stringsubstitution.IContextVariable, java.lang.String)
	 */
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		IResource resource = null;
		if (argument == null) {
			resource = getSelectedResource(variable);
		} else {
			resource = getWorkspaceRoot().findMember(new Path(argument));
		}
		if (resource != null && resource.exists()) {
			resource = translateSelectedResource(resource);
			if (resource != null && resource.exists()) {
				return translateToValue(resource, variable);
			}
		}
		abort(MessageFormat.format(StringSubstitutionMessages.ResourceResolver_6, new String[]{getReferenceExpression(variable, argument)}), null);				 
		return null;
	}
	
	/**
	 * Returns the resource applicable to this resolver, relative to the selected
	 * resource. This method is called when no argument is present in a variable
	 * expression. For, example, this method might return the project for the
	 * selected resource.
	 * 
	 * @param resource selected resource
	 * @return resource applicable to this variable resolver
	 */
	protected IResource translateSelectedResource(IResource resource) {
		return resource;
	}
	
	/**
	 * Returns the workspace root
	 * 
	 * @return workspace root
	 */
	protected IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Returns an expression used to reference the given variable and optional argument.
	 * For example, <code>${var_name:arg}</code>.
	 * 
	 * @param variable referenced variable
	 * @param argument referenced argument or <code>null</code>
	 * @return vraiable reference expression
	 */
	protected String getReferenceExpression(IDynamicVariable variable, String argument) {
		StringBuffer reference = new StringBuffer();
		reference.append("${"); //$NON-NLS-1$
		reference.append(variable.getName());
		if (argument != null) {
			reference.append(":"); //$NON-NLS-1$
			reference.append(argument);
		}
		reference.append("}"); //$NON-NLS-1$
		return reference.toString();
	}
	
	/**
	 * Throws an exception with the given message and underlying exception.
	 *  
	 * @param message exception message
	 * @param exception underlying exception or <code>null</code> 
	 * @throws CoreException
	 */
	protected void abort(String message, Throwable exception) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, message, exception));
	}
	
	/**
	 * Returns the selected resource.
	 * 
	 * @param variable variable referencing a resource
	 * @return selected resource
	 * @throws CoreException if there is no selection
	 */
	protected IResource getSelectedResource(IDynamicVariable variable) throws CoreException {
		IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
		if (resource == null) {
			abort(MessageFormat.format(StringSubstitutionMessages.ResourceResolver_7, new String[]{getReferenceExpression(variable, null)}), null); 
		}
		return resource;	
	}

	/**
	 * Translates the given resource into a value for this variable resolver.
	 * 
	 * @param resource the resource applicable to this resolver's variable
	 * @param variable the variable being resolved
	 * @return variable value
	 * @throws CoreException if the variable name is not recognized
	 */
	protected String translateToValue(IResource resource, IDynamicVariable variable) throws CoreException {
		String name = variable.getName();
		if (name.endsWith("_loc")) { //$NON-NLS-1$
			return resource.getLocation().toOSString();
		} else if (name.endsWith("_path")) { //$NON-NLS-1$
			return resource.getFullPath().toOSString();
		} else if (name.endsWith("_name")) { //$NON-NLS-1$
			return resource.getName();
		}
		abort(MessageFormat.format(StringSubstitutionMessages.ResourceResolver_8, new String[]{getReferenceExpression(variable, null)}), null); 
		return null;
	}

}
