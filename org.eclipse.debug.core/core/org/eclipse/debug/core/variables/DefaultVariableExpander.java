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
package org.eclipse.debug.core.variables;


import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.DebugCoreMessages;

/**
 * Default variable expander implementation. Does nothing.
 * <p>
 * Clients implementing variable expanders should extend this class.
 * </p>
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
	 * @see IVariableExpander#getResources(String, String, ExpandVariableContext)
	 */
	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		throwExpansionException(varTag, MessageFormat.format(DebugCoreMessages.getString("DefaultVariableExpander.0"), new String[] {varTag})); //$NON-NLS-1$
		return null;
	}

	/**
	 * @see IVariableExpander#getText(String, String, ExpandVariableContext)
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		throwExpansionException(varTag, MessageFormat.format(DebugCoreMessages.getString("DefaultVariableExpander.0"), new String[] {varTag})); //$NON-NLS-1$
		return null;
	}
	
	/**
	 * Utility method which throws an exception that occurred for the given reason
	 * while expanding the given variable tag.
	 */
	public static void throwExpansionException(String varTag, String reason) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), IStatus.ERROR, MessageFormat.format(DebugCoreMessages.getString("DefaultVariableExpander.2"), new String[] {varTag, reason}), null)); //$NON-NLS-1$
	}

}
