/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import org.eclipse.jface.text.Assert;

/**
 * A template buffer is a container for a string and variables.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * 
 * @since 3.0
 */
public final class TemplateBuffer {
	
	/** The string of the template buffer */
	private String fString;
	/** The variable positions of the template buffer */
	private TemplateVariable[] fVariables;
	
	/**
	 * Creates a template buffer.
	 * 
	 * @param string the string
	 * @param variables the variable positions
	 */
    public TemplateBuffer(String string, TemplateVariable[] variables) {
		setContent(string, variables);
    }

	/**
	 * Sets the content of the template buffer.
	 * 
	 * @param string the string
	 * @param variables the variable positions
	 */
	public final void setContent(String string, TemplateVariable[] variables) {
		Assert.isNotNull(string);
		Assert.isNotNull(variables);

		// XXX assert non-overlapping variable properties

		fString= string;
		fVariables= variables;
	}

	/**
	 * Returns the string of the template buffer.
	 * 
	 * @return the string representation of the template buffer
	 */
	public final String getString() {
		return fString;
	}
	
	/**
	 * Returns the variable positions of the template buffer.
	 * 
	 * @return the variable positions of the template buffer
	 */
	public final TemplateVariable[] getVariables() {
		return fVariables;
	}

}
