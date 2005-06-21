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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;

/**
 * Provides the context for a <code>Template</code> being resolved. Keeps track
 * of resolved variables.
 * <p>
 * Clients may extend this class.
 * </p>
 *
 * @since 3.0
 */
public abstract class TemplateContext {

	/** The context type of this context */
	private final TemplateContextType fContextType;
	/** Additional variables. */
	private final Map fVariables= new HashMap();
	/** A flag to indicate that the context should not be modified. */
	private boolean fReadOnly;

	/**
	 * Creates a template context of a particular context type.
	 *
	 * @param contextType the context type of this context
	 */
	protected TemplateContext(TemplateContextType contextType) {
		fContextType= contextType;
		fReadOnly= true;
	}

	/**
	 * Returns the context type of this context.
	 *
	 * @return the context type of this context
	 */
	public TemplateContextType getContextType() {
	 	return fContextType;
	}

	/**
	 * Sets or clears the read-only flag.
	 *
	 * @param readOnly the new read-only state
	 */
	public void setReadOnly(boolean readOnly) {
		fReadOnly= readOnly;
	}

	/**
	 * Returns <code>true</code> if the receiver is read-only, <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if the receiver is read-only, <code>false</code> otherwise
	 */
	public boolean isReadOnly() {
		return fReadOnly;
	}

	/**
	 * Defines the value of a variable.
	 *
	 * @param name the name of the variable
	 * @param value the value of the variable, <code>null</code> to un-define a variable
	 */
	public void setVariable(String name, String value) {
		fVariables.put(name, value);
	}

	/**
	 * Returns the value of a defined variable.
	 *
	 * @param name the name of the variable
	 * @return returns the value of the variable, <code>null</code> if the variable was not defined
	 */
	public String getVariable(String name) {
		return (String) fVariables.get(name);
	}

	/**
	 * Evaluates the template in this context and returns a template buffer.
	 * <p>
	 * Evaluation means translating the template into a <code>TemplateBuffer</code>,
	 * resolving the defined variables in this context and possibly formatting
	 * the resolved buffer.</p>
	 *
	 * @param template the template to evaluate
	 * @return returns the buffer with the evaluated template or <code>null</code> if the buffer could not be created
	 * @throws BadLocationException if evaluation fails due to concurrently changed documents etc.
	 * @throws TemplateException if the template specification is not valid
	 */
	public abstract TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException;

	/**
	 * Tests if the specified template can be evaluated in this context.
	 * <p>Examples are templates defined for a different context (e.g. a javadoc
	 * template cannot be evaluated in Java context).</p>
	 *
	 * @param template the <code>Template</code> to check
	 * @return <code>true</code> if <code>template</code> can be evaluated
	 *         in this context, <code>false</code> otherwise
	 */
	public abstract boolean canEvaluate(Template template);

}
