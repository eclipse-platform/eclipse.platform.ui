package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;
import org.eclipse.ui.externaltools.variable.IVariableTextExpander;

/**
 * Represents the variable for the argument
 */
public final class ArgumentVariable extends ExternalToolVariable {
	private static final DefaultTextExpander defaultExpander = new DefaultTextExpander();
	
	private IVariableTextExpander expander = null;

	/**
	 * Creates an argument variable
	 * 
	 * @param tag the variable tag
	 * @param description a short description of what the variable will expand to
	 * @param element the configuration element
	 */
	/*package*/ ArgumentVariable(String tag, String description, IConfigurationElement element) {
		super(tag, description, element);
	}

	/**
	 * Returns the object that can expand the variable
	 * as text.
	 */
	public IVariableTextExpander getExpander() {
		if (expander == null) {
			try {
				expander = (IVariableTextExpander) createObject(ExternalToolVariableRegistry.TAG_EXPANDER_CLASS);
			} catch (ClassCastException exception) {
			}
			if (expander == null) {
				expander = defaultExpander;
			}
		}
		return expander;
	}


	/**
	 * Default variable text expander implementation which does
	 * not expand variables, but just returns <code>null</code>.
	 */	
	private static final class DefaultTextExpander implements IVariableTextExpander {
		/* (non-Javadoc)
		 * Method declared on IVariableTextExpander.
		 */
		public String getText(String varTag, String varValue, ExpandVariableContext context) {
			return null;
		}
	}
}
