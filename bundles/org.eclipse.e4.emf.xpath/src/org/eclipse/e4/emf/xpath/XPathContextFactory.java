/*******************************************************************************
 * Copyright (c) 2010, 2015 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.emf.xpath;

import org.eclipse.e4.emf.internal.xpath.JXPathContextFactoryImpl;

/**
 * Factory responsible to create an XPath-Context
 *
 * @param <Type>
 *            the object type the XPath is created for
 */
public abstract class XPathContextFactory<Type extends Object> {

	/**
	 * Creates a new XPathContext with the specified object as the root node.
	 *
	 * @param contextBean
	 *            Object
	 * @return XPathContext
	 */
	public abstract XPathContext newContext(Type contextBean);

	/**
	 * Creates a new XPathContext with the specified bean as the root node and
	 * the specified parent context. Variables defined in a parent context can
	 * be referenced in XPaths passed to the child context.
	 *
	 * @param parentContext
	 *            parent context
	 * @param contextBean
	 *            Object
	 * @return XPathContext
	 */
	public abstract XPathContext newContext(XPathContext parentContext, Type contextBean);

	/**
	 * @param <Type>
	 *            the object type the xpath is created for
	 * @return Create a new XPath-Factory
	 */
	public static <Type> XPathContextFactory<Type> newInstance() {
		return new JXPathContextFactoryImpl<>();
	}
}
