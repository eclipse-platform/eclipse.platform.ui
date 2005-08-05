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
package org.eclipse.core.internal.variables;

import java.text.MessageFormat;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.core.variables.VariablesPlugin;

/**
 * Dynamic variable
 */
public class DynamicVariable extends StringVariable implements IDynamicVariable {
	
	/**
	 * Resolver, or <code>null</code> until needed
	 */
	private IDynamicVariableResolver fResolver;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IContextVariable#getValue(java.lang.String)
	 */
	public String getValue(String argument) throws CoreException {
		if (!supportsArgument()) {
			// check for an argument - not supported
			if (argument != null && argument.length() > 0) {
				throw new CoreException(new Status(IStatus.ERROR, VariablesPlugin.getUniqueIdentifier(), VariablesPlugin.INTERNAL_ERROR, MessageFormat.format(VariablesMessages.DynamicVariable_0, new String[]{argument, getName()}), null)); 
			}
		}
		if (fResolver == null) {
			String name = getConfigurationElement().getAttribute("resolver"); //$NON-NLS-1$
			if (name == null) {
				throw new CoreException(new Status(IStatus.ERROR, VariablesPlugin.getUniqueIdentifier(), VariablesPlugin.INTERNAL_ERROR, MessageFormat.format("Contributed context variable {0} must specify a resolver.",new String[]{getName()}), null)); //$NON-NLS-1$
			}
			Object object = getConfigurationElement().createExecutableExtension("resolver"); //$NON-NLS-1$
			if (object instanceof IDynamicVariableResolver) {
				fResolver = (IDynamicVariableResolver)object;
			} else {
				throw new CoreException(new Status(IStatus.ERROR, VariablesPlugin.getUniqueIdentifier(), VariablesPlugin.INTERNAL_ERROR, MessageFormat.format("Contributed context variable resolver for {0} must be an instance of IContextVariableResolver.",new String[]{getName()}), null)); //$NON-NLS-1$
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
	public DynamicVariable(String name, String description, IConfigurationElement configurationElement) {
		super(name, description, configurationElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IDynamicVariable#supportsArgument()
	 */
	public boolean supportsArgument() {
		String arg = getConfigurationElement().getAttribute("supportsArgument"); //$NON-NLS-1$
		return arg == null || Boolean.valueOf(arg).booleanValue();
	}
	
}
