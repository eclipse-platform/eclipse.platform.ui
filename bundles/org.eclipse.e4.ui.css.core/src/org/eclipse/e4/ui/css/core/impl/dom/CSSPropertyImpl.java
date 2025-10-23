/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.eclipse.e4.ui.css.core.dom.CSSProperty;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyImpl implements CSSProperty {

	/*
	 * The string used to signify a property's priority as "important"
	 * e.g. @see org.w3c.dom.css.CSSStyleDeclaration#getPropertyPriority(String)
	 */
	protected static String IMPORTANT_IDENTIFIER = "important";

	private final String name;
	private CSSValue value;
	private boolean important;

	public static boolean sameName(CSSProperty property, String testName) {
		return property.getName().equalsIgnoreCase(testName);
	}

	/** Creates new Property */
	public CSSPropertyImpl(String name, CSSValue value, boolean important) {
		this.name = name;
		this.value = value;
		this.important = important;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CSSValue getValue() {
		return value;
	}

	@Override
	public boolean isImportant() {
		return important;
	}

	@Override
	public void setImportant(boolean important) {
		this.important = important;
	}

	@Override
	public void setValue(CSSValue value) {
		this.value = value;
	}

}
