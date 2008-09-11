/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;


/**
 * Value object that represents the type of a template variable. A type is defined by its name and
 * may have parameters.
 *
 * @since 3.3
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class TemplateVariableType {

	/** The name of the type. */
	private final String fName;
	/** The parameter list. */
	private final List fParams;

	TemplateVariableType(String name) {
		this(name, new String[0]);
	}

	TemplateVariableType(String name, String[] params) {
		Assert.isLegal(name != null);
		Assert.isLegal(params != null);
		fName= name;
		fParams= Collections.unmodifiableList(new ArrayList(Arrays.asList(params)));
	}

	/**
	 * Returns the type name of this variable type.
	 *
	 * @return the type name of this variable type
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Returns the unmodifiable and possibly empty list of parameters (element type: {@link String})
	 *
	 * @return the list of parameters
	 */
	public List getParams() {
		return fParams;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof TemplateVariableType) {
			TemplateVariableType other= (TemplateVariableType) obj;
			return other.fName.equals(fName) && other.fParams.equals(fParams);
		}
		return false;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fName.hashCode() + fParams.hashCode();
	}

	/*
	 * @see java.lang.Object#toString()
	 * @since 3.3
	 */
	public String toString() {
		return fName + fParams.toString();
	}
}
