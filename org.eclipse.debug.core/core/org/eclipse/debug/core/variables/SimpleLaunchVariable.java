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


public class SimpleLaunchVariable implements ISimpleLaunchVariable {
	
	protected IVariableInitializer fVariableInitializer= null;
	protected String fName= null;
	protected String fValue= null;
	
	/**
	 * Creates a new launch configuration variable with the given initializer or <code>null</code>
	 * if none is available.
	 * @param initializer the initializer that should be used to calculate this variable's
	 * value if no value is set or <code>null</code> if no initializer is defined.
	 * @param initialValue the variable's initial value or <code>null</code> if no initial value
	 * should be set.
	 */
	public SimpleLaunchVariable(String name, IVariableInitializer initializer, String initialValue) {
		this(name);
		fVariableInitializer= initializer;
		fValue= initialValue;
	}
	
	/**
	 * Creates a new launch configuration varible with the given name.
	 * @param name
	 */
	public SimpleLaunchVariable(String name) {
		fName= name;
	}
	
	/**
	 * Creates a new variable with no name. Do not call.
	 */
	private SimpleLaunchVariable() {
	}

	/**
	 * @see ISimpleLaunchVariable#getInitializer()
	 */
	public IVariableInitializer getInitializer() {
		return fVariableInitializer;
	}
	
	/**
	 * @see ISimpleLaunchVariable#getName()
	 */
	public String getName() {
		return fName;
	}

	/**
	 * @see ISimpleLaunchVariable#getText()
	 */
	public String getText() {
		if (fValue == null && getInitializer() != null) {
			fValue= getInitializer().getText();
		}
		return fValue;
	}

	/**
	 * @see ISimpleLaunchVariable#setText(String)
	 */
	public void setText(String value) {
		fValue= value;
	}

}
