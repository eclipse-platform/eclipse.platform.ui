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


import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;

/**
 * Default variable expander implementation. Does nothing.
 * 
 * Clients are intended to extend this class.
 * @since 3.0
 */
public class DefaultVariableExpander implements IVariableExpander {

	private static DefaultVariableExpander instance;

	public static DefaultVariableExpander getDefault() {
		if (instance == null) {
			instance= new DefaultVariableExpander();
		}
		return instance;
	}

	/**
	 * @see IVariableExpander#getPath(String, String, ExpandVariableContext)
	 */
	public IPath getPath(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		throwExpansionException(varTag, MessageFormat.format(LaunchConfigurationsMessages.getString("DefaultVariableExpander.No_expander_class_defined_for_the_variable_{0}_1"), new String[] {varTag})); //$NON-NLS-1$
		return null;
	}

	/**
	 * @see IVariableExpander#getResources(String, String, ExpandVariableContext)
	 */
	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		throwExpansionException(varTag, MessageFormat.format(LaunchConfigurationsMessages.getString("DefaultVariableExpander.No_expander_class_defined_for_the_variable_{0}_2"), new String[] {varTag})); //$NON-NLS-1$
		return null;
	}

	/**
	 * @see IVariableExpander#getText(String, String, ExpandVariableContext)
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		throwExpansionException(varTag, MessageFormat.format(LaunchConfigurationsMessages.getString("DefaultVariableExpander.No_expander_class_defined_for_the_variable_{0}_3"), new String[] {varTag})); //$NON-NLS-1$
		return null;
	}
	
	/**
	 * Utility method which throws an exception that occurred for the given reason
	 * while expanding the given variable tag.
	 */
	public static void throwExpansionException(String varTag, String reason) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, MessageFormat.format(LaunchConfigurationsMessages.getString("DefaultVariableExpander.An_error_occurred_attempting_to_expand_the_variable_{0}._{1}_4"), new String[] {varTag, reason}), null)); //$NON-NLS-1$
	}

}
