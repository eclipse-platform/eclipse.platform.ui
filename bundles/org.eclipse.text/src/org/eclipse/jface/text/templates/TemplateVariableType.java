/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	private final List<String> fParams;

	TemplateVariableType(String name) {
		this(name, new String[0]);
	}

	TemplateVariableType(String name, String[] params) {
		Assert.isLegal(name != null);
		Assert.isLegal(params != null);
		fName= name;
		fParams= Collections.unmodifiableList(new ArrayList<>(Arrays.asList(params)));
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
	 * Returns the unmodifiable and possibly empty list of parameters
	 *
	 * @return the list of parameters
	 */
	public List<String> getParams() {
		return fParams;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TemplateVariableType) {
			TemplateVariableType other= (TemplateVariableType) obj;
			return other.fName.equals(fName) && other.fParams.equals(fParams);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return fName.hashCode() + fParams.hashCode();
	}

	@Override
	public String toString() {
		return fName + fParams.toString();
	}
}
