/*******************************************************************************
 * Copyright (c) 2000, 2008, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation.
 *     Ian Phillips - additional expressions support ( "-N)", "(N-" ).
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.w3c.dom.Element;

import org.eclipse.core.runtime.IConfigurationElement;

@Deprecated
public class CountExpression extends org.eclipse.core.expressions.CountExpression {

	public CountExpression(IConfigurationElement configElement) {
		super(configElement);
	}

	public CountExpression(Element element) {
		super(element);
	}

	public CountExpression(String size) {
		super(size);
	}

}
