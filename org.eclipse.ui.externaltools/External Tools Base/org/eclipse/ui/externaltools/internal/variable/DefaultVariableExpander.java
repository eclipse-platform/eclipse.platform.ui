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
package org.eclipse.ui.externaltools.internal.variable;


import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

public class DefaultVariableExpander implements IVariableExpander {

	private static DefaultVariableExpander instance;

	public static DefaultVariableExpander getDefault() {
		if (instance == null) {
			instance= new DefaultVariableExpander();
		}
		return instance;
	}

	public IPath getPath(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		throwExpansionException(varTag, MessageFormat.format(ExternalToolsVariableMessages.getString("DefaultVariableExpander.No_expander"), new String[] {varTag})); //$NON-NLS-1$
		return null;
	}

	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		throwExpansionException(varTag, MessageFormat.format(ExternalToolsVariableMessages.getString("DefaultVariableExpander.No_expander"), new String[] {varTag})); //$NON-NLS-1$
		return null;
	}

	public String getText(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		throwExpansionException(varTag, MessageFormat.format(ExternalToolsVariableMessages.getString("DefaultVariableExpander.No_expander"), new String[] {varTag})); //$NON-NLS-1$
		return null;
	}
	
	/**
	 * Utility method which throws an exception that occurred for the given reason
	 * while expanding the given variable tag.
	 */
	public static void throwExpansionException(String varTag, String reason) throws CoreException {
		throw new CoreException(ExternalToolsPlugin.newErrorStatus(MessageFormat.format(ExternalToolsVariableMessages.getString("DefaultVariableExpander.Error"), new String[] {varTag, reason}), null)); //$NON-NLS-1$
	}

}
