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
package org.eclipse.debug.internal.core.stringsubstitution;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;

/**
 * Context variable
 */
public class ContextVariable extends StringVariable implements IContextVariable {
	
	/**
	 * Resolver, or <code>null</code> until needed
	 */
	private IContextVariableResolver fResolver;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IContextVariable#getValue(java.lang.String)
	 */
	public String getValue(String argument) throws CoreException {
		if (fResolver == null) {
			String name = getConfigurationElement().getAttribute("resolver"); //$NON-NLS-1$
			if (name == null) {
				throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format("Contributed context variable {0} must specify a resolver.",new String[]{getName()}), null)); //$NON-NLS-1$
			}
			Object object = getConfigurationElement().createExecutableExtension("resolver"); //$NON-NLS-1$
			if (object instanceof IContextVariableResolver) {
				fResolver = (IContextVariableResolver)object;
			} else {
				throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format("Contributed context variable resolver for {0} must be an instance of IContextVariableResolver.",new String[]{getName()}), null)); //$NON-NLS-1$
			}
		}
		return fResolver.resolveValue(this, argument);
	}

	/**
	 * Constructs a new context variable.
	 * 
	 * @param name variable name
	 * @param description variable description or <code>null</code>
	 * @param configurationElement configuration element
	 */
	public ContextVariable(String name, String description, IConfigurationElement configurationElement) {
		super(name, description, configurationElement);
	}

}
