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
import org.eclipse.debug.core.DebugPlugin;

/**
 * Implementation of a value variable.
 */
public class ValueVariable extends StringVariable implements IValueVariable {
	
	/**
	 * Variable value or <code>null</code> if none 
	 */
	private String fValue;
	
	/**
	 * Whether this variable's value has been initialized
	 */
	private boolean fInitialized = false;
	
	/**
	 * Constructs a new value variable with the given name, description, and
	 * associated configuration element.
	 * 
	 * @param name variable name
	 * @param description variable description, or <code>null</code>
	 * @param configurationElement configuration element or <code>null</code>
	 */
	public ValueVariable(String name, String description, IConfigurationElement configurationElement) {
		super(name, description, configurationElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IValueVariable#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		fValue = value;
		setInitialized(true);
		StringVariableManager.getDefault().notifyChanged(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IValueVariable#getValue()
	 */
	public String getValue() {
		if (!isInitialized()) {
			initialize();
		}
		return fValue;
	}

	/**
	 * Initialize this variable's value.
	 */
	private void initialize() {
		if (getConfigurationElement() != null) {
			// check for a explicit value specified in plug-in XML
			String value = getConfigurationElement().getAttribute("initialValue"); //$NON-NLS-1$
			if (value == null) {
				// check for initializer
				String className = getConfigurationElement().getAttribute("initializerClass"); //$NON-NLS-1$
				if (className != null) {
					try {
						Object object = getConfigurationElement().createExecutableExtension("initializerClass"); //$NON-NLS-1$
						if (object instanceof IValueVariableInitializer) {
							IValueVariableInitializer initializer = (IValueVariableInitializer)object;
							initializer.initialize(this);
						} else {
							DebugPlugin.logMessage(MessageFormat.format("Unable to initialize variable {0} - initializer must be an instance of IValueVariableInitializer.", new String[]{getName()}), null); //$NON-NLS-1$
						}
					} catch (CoreException e) {
						DebugPlugin.logMessage(MessageFormat.format("Unable to initialize variable {0}",new String[]{getName()}), e); //$NON-NLS-1$
					}
				}
			} else {
				setValue(value);
			}
		}
		setInitialized(true);
	}

	/**
	 * Returns whether this variable has been initialized with a value by one of:
	 * <ul>
	 * <li><code>setValue(String)</code></li>
	 * <li>its configuration element's <code>initialValue</code> attribute</li>
	 * <li>its configuration element's initializer</li>
	 * </ul>
	 * @return whether this variable has been initialized with a value
	 */	
	protected boolean isInitialized() {
		return fInitialized;
	} 
	
	/**
	 * Sets whether this variable has been initialized with a value.
	 *  
	 * @param initialized whether this variable has been initialized
	 */
	protected void setInitialized(boolean initialized) {
		fInitialized = initialized;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IValueVariable#isContributed()
	 */
	public boolean isContributed() {
		return getConfigurationElement() != null;
	}

}
