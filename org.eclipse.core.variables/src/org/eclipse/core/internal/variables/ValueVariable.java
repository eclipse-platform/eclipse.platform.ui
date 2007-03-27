/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.variables;

import org.eclipse.core.variables.IValueVariable;

/**
 * Implementation of a value variable.
 */
public class ValueVariable extends StringVariable implements IValueVariable {
	
	/**
	 * Variable value or <code>null</code> if none 
	 */
	private String fValue;
	
	/**
	 * Whether this variable is read only.  If true, users cannot change the value.
	 */
	private boolean fReadOnly;
	
	/**
	 * Constructs a new value variable with the given name, description, read only
	 * property and string value.  Value can be null.
	 * 
	 * @param name variable name
	 * @param description variable description or <code>null</code>
	 * @param readOnly whether the variable should be a read only variable
	 * @param value the initial value of the variable or <code>null</code>
	 */
	public ValueVariable(String name, String description, boolean readOnly, String value) {
		super(name, description, null);
		fReadOnly = readOnly;
		fValue = value;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IValueVariable#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		if (!isReadOnly()){
			fValue = value;
			StringVariableManager.getDefault().notifyChanged(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IValueVariable#getValue()
	 */
	public String getValue() {
		return fValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IValueVariable#isReadOnly()
	 */
	public boolean isReadOnly() {
		return fReadOnly;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IValueVariable#isContributed()
	 */
	public boolean isContributed() {
		return false;
	}

}
