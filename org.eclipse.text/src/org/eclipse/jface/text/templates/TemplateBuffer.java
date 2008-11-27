/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

		// XXX: assert non-overlapping variable properties

		fString= string;
		fVariables= copy(variables);
	}

	/**
	 * Returns a copy of the given array.
	 *
	 * @param array the array to be copied
	 * @return a copy of the given array or <code>null</code> when <code>array</code> is <code>null</code>
	 * @since 3.1
	 */
	private static TemplateVariable[] copy(TemplateVariable[] array) {
		if (array != null) {
			TemplateVariable[] copy= new TemplateVariable[array.length];
			System.arraycopy(array, 0, copy, 0, array.length);
			return copy;
		}
		return null;
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
	 * Returns the variable positions of the template buffer. The returned array is
	 * owned by this variable and must not be modified.
	 *
	 * @return the variable positions of the template buffer
	 */
	public final TemplateVariable[] getVariables() {
		return fVariables;
	}

}
