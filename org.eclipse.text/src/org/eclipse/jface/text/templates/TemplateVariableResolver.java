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
package org.eclipse.jface.text.templates;

import org.eclipse.core.runtime.Assert;

/**
 * A <code>TemplateVariableResolver</code> resolves <code>TemplateVariables</code>
 * of a certain type inside a <code>TemplateContext</code>.
 * <p>
 * Clients may instantiate and extend this class.
 * </p>
 *
 * @see TemplateVariable
 * @since 3.0
 */
public class TemplateVariableResolver {

	/** Type of this resolver. */
	private String fType= null;

	/** Description of the type resolved by this resolver. */
	private String fDescription= null;

	/**
	 * Creates an instance of <code>TemplateVariableResolver</code>.
	 *
	 * @param type the name of the type
	 * @param description the description for the type
	 */
	protected TemplateVariableResolver(String type, String description) {
	 	setType(type);
	 	setDescription(description);
	}

	/**
	 * Creates an empty instance.
	 * <p>
	 * This is a framework-only constructor that exists only so that resolvers
	 * can be contributed via an extension point and that should not be called
	 * in client code except for subclass constructors; use
	 * {@link #TemplateVariableResolver(String, String)} instead.
	 * </p>
	 */
	public TemplateVariableResolver() {
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
	 * <p>
	 * The default implementation looks up the type in the context.</p>
	 *
	 * @param context the context in which to resolve the type
	 * @return the name of the text object of this type, or <code>null</code> if it cannot be determined
	 */
	protected String resolve(TemplateContext context) {
		return context.getVariable(getType());
	}

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
		if (bindings.length > 1)
			variable.setUnambiguous(false);
		else
			variable.setUnambiguous(isUnambiguous(context));
		variable.setResolved(true);
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
	 * @return <code>true</code> if the receiver is unambiguously resolvable
	 *         in <code>context</code>, <code>false</code> otherwise
	 */
	protected boolean isUnambiguous(TemplateContext context) {
		return false;
	}

	/**
	 * Sets the description.
	 * <p>
	 * This is a framework-only method that exists only so that resolvers
	 * can be contributed via an extension point and that should not be called
	 * in client code; use {@link #TemplateVariableResolver(String, String)} instead.
	 * </p>
	 *
	 * @param description the description of this resolver
	 */
	public final void setDescription(String description) {
		Assert.isNotNull(description);
		Assert.isTrue(fDescription == null); // may only be called once when initialized
		fDescription= description;
	}

	/**
	 * Sets the type name.
	 * <p>
	 * This is a framework-only method that exists only so that resolvers
	 * can be contributed via an extension point and that should not be called
	 * in client code; use {@link #TemplateVariableResolver(String, String)} instead.
	 * </p>
	 *
	 * @param type the type name of this resolver
	 */
	public final void setType(String type) {
		Assert.isNotNull(type);
		Assert.isTrue(fType == null); // may only be called once when initialized
		fType= type;
	}
}
