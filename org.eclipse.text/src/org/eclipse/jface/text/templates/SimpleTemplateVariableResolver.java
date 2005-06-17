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
package org.eclipse.jface.text.templates;


/**
 * A simple template variable resolver, which always evaluates to a defined string.
 * <p>
 * Clients may instantiate and extend this class.
 * </p>
 *
 * @since 3.0
 */
public class SimpleTemplateVariableResolver extends TemplateVariableResolver {

	/** The string to which this variable evaluates. */
	private String fEvaluationString;

	/*
	 * @see TemplateVariableResolver#TemplateVariableResolver(String, String)
	 */
	protected SimpleTemplateVariableResolver(String type, String description) {
		super(type, description);
	}

	/**
	 * Sets the string to which this variable evaluates.
	 *
	 * @param evaluationString the evaluation string, may be <code>null</code>.
	 */
	public final void setEvaluationString(String evaluationString) {
		fEvaluationString= evaluationString;
	}

	/*
	 * @see TemplateVariableResolver#evaluate(TemplateContext)
	 */
	protected String resolve(TemplateContext context) {
		return fEvaluationString;
	}

	/**
	 * Returns always <code>true</code>, since simple variables are normally
	 * unambiguous.
	 *
	 * @param context {@inheritDoc}
	 * @return <code>true</code>
	 */
	protected boolean isUnambiguous(TemplateContext context) {
		return true;
	}
}
