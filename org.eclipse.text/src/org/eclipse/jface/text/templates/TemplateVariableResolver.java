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
 * A <code>TemplateVariableResolver</code> resolves <code>TemplateVariables</code>
 * of a certain type inside a <code>TemplateContext</code>.
 * 
 * @see TemplateVariable
 * @since 3.0
 */
public abstract class TemplateVariableResolver {

	/** Type of this resolver. */
	private final String fType;

	/** Description of the type resolved by this resolver. */
	private final String fDescription;
	
	/**
	 * Creates an instance of <code>TemplateVariableResolver</code>.
	 * 
	 * @param type the name of the type
	 * @param description the description for the type
	 */
	protected TemplateVariableResolver(String type, String description) {
	 	fType= type;
	 	fDescription= description;   
	}
	
	/**
	 * Returns the type of this resolver.
	 * 
	 * @return the type
	 */
	public String getType() {
		return fType;
	}

	/**
	 * Returns the description for the resolver.
	 * 
	 * @return the description for the resolver
	 */
	public String getDescription() {
		return fDescription;   
	}

	/**
	 * Returns an instance of the type resolved by the receiver available in <code>context</code>.
	 * To resolve means to provide a binding to a concrete text object (a
	 * <code>String</code>) in the given context.
	 * 
	 * @param context the context in which to resolve the type
	 * @return the name of the text object of this type, or <code>null</code> if not evaluatable
	 */
	protected abstract String resolve(TemplateContext context);
	
	/**
	 * Returns all possible bindings available in <code>context</code>. The default
	 * implementation simply returns an array which contains the result of 
	 * {@link #resolve(TemplateContext)}, or an empty array if that call returns
	 * <code>null</code>.
	 * 
	 * @param context the context in which to resolve the type
	 * @return an array of possible bindings of this type in <code>context</code>
	 */
	protected String[] resolveAll(TemplateContext context) {
		String binding= resolve(context);
		if (binding == null)
			return new String[0];
		else
			return new String[] { binding };
	}
	
	/**
	 * Resolves <code>variable</code> in <code>context</code>. To resolve
	 * means to find a valid binding of the receiver's type in the given <code>TemplateContext</code>.
	 * If the variable can be successfully resolved, its value is set using
	 * {@link TemplateVariable#setValues(String[])}.
	 * 
	 * @param context the context in which variable is resolved
	 * @param variable the variable to resolve
	 */
	public void resolve(TemplateVariable variable, TemplateContext context) {
		String[] bindings= resolveAll(context);
		if (bindings.length != 0)
			variable.setValues(bindings);
		if (bindings.length > 0)
			variable.setUnambiguous(false);
		else
			variable.setUnambiguous(isUnambiguous(context));
	}

	/**
	 * Returns whether this resolver is able to resolve unambiguously. When
	 * resolving a <code>TemplateVariable</code>, its <code>isUmambiguous</code>
	 * state is set to the one of this resolver. By default, this method
	 * returns <code>false</code>. Clients can overwrite this method to give
	 * a hint about whether there should be e.g. prompting for input values for
	 * ambiguous variables.
	 * 
	 * @param context the context in which the resolved check should be
	 *        evaluated
	 * @return <code>true</code> if the receiver is unambiguously resolveable
	 *         in <code>context</code>, <code>false</code> otherwise
	 */
	protected boolean isUnambiguous(TemplateContext context) {
		return false;
	}
}
