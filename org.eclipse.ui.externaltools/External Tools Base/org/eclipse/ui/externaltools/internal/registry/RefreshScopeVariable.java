package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;
import org.eclipse.ui.externaltools.variable.IVariableResourceExpander;

/**
 * Represents the variable for a refresh scope.
 */
public final class RefreshScopeVariable extends ExternalToolVariable {
	private static final DefaultResourceExpander defaultExpander = new DefaultResourceExpander();
	
	private IVariableResourceExpander expander = null;

	/**
	 * Creates a refresh scope variable
	 * 
	 * @param tag the variable tag
	 * @param description a short description of what the variable will expand to
	 * @param element the configuration element
	 */
	/*package*/ RefreshScopeVariable(String tag, String description, IConfigurationElement element) {
		super(tag, description, element);
	}

	/**
	 * Returns the object that can expand the variable
	 * as resources.
	 */
	public IVariableResourceExpander getExpander() {
		if (expander == null) {
			expander = (IVariableResourceExpander) createObject(ExternalToolVariableRegistry.TAG_EXPANDER_CLASS);
			if (expander == null)
				expander = defaultExpander;
		}
		return expander;
	}


	/**
	 * Default variable resource expander implementation which does
	 * not expand variables, but just returns <code>null</code>.
	 */	
	private static final class DefaultResourceExpander implements IVariableResourceExpander {
		/* (non-Javadoc)
		 * Method declared on IVariableResourceExpander.
		 */
		public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) {
			return null;
		}
	}
}
