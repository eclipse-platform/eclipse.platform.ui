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
package org.eclipse.jface.text.templates;

/**
 * A <code>TemplateVariable</code> represents a symbol in a template which can be
 * resolved in a given <code>TemplateContext</code>.
 * 
 * @since 3.0
 */
public abstract class TemplateVariable {

	/** name of the variable */
	private final String fName;

	/** description of the variable */
	private final String fDescription;
	
	/**
	 * Creates an instance of <code>TemplateVariable</code>.
	 * 
	 * @param name the name of the variable
	 * @param description the description for the variable
	 */
	protected TemplateVariable(String name, String description) {
	 	fName= name;
	 	fDescription= description;   
	}
	
	/**
	 * Returns the name of the variable.
	 * 
	 * @return the name of the variable 
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Returns the description for the variable.
	 * 
	 * @return the description for the variable
	 */
	public String getDescription() {
		return fDescription;   
	}

	/**
	 * Resolves this variable. To resolve means to provide a binding of this
	 * variable to a concrete text object (a<code>String</code>) in the
	 * given context.
	 * 
	 * @param context the context in which to resolve the receiver
	 * @return the evaluated string, or <code>null</code> if not evaluatable
	 */
	public abstract String resolve(TemplateContext context);

	/**
	 * Returns whether this variable is resolved. By default, the variable is
	 * not resolved. Clients can overwrite this method to force resolution of
	 * the variable.
	 * 
	 * @param context the context in which the resolved check should be
	 *        evaluated
	 * @return <code>true</code> if the receiver is resolved in <code>context</code>,
	 *         <code>false</code> otherwise
	 */
	public boolean isResolved(TemplateContext context) {
		return false;
	}

}
