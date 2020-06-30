/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.core.internal.expressions;

import org.w3c.dom.Element;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

@Deprecated
public class EqualsExpression extends org.eclipse.core.expressions.EqualsExpression {

	public EqualsExpression(Object expectedValue) {
		super(expectedValue);
	}

	public EqualsExpression(IConfigurationElement element) throws CoreException {
		super(element);
	}

	public EqualsExpression(Element element) throws CoreException {
		super(element);
	}

}
