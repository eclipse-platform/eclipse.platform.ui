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

import java.util.Iterator;

/**
 * Context in which the xpath is executed
 *
 * @since 1.0
 */
public interface XPathContext {

	/**
	 * Evaluates the xpath and returns the resulting object. Primitive types are
	 * wrapped into objects.
	 *
	 * @param xpath
	 *            to evaluate
	 * @return Object found
	 */
	Object getValue(String xpath);

	/**
	 * Evaluates the xpath, converts the result to the specified class and
	 * returns the resulting object.
	 *
	 * @param xpath
	 *            to evaluate
	 * @param requiredType
	 *            required type
	 * @return Object found
	 */
	Object getValue(String xpath, Class<?> requiredType);

	/**
	 * Traverses the xpath and returns an Iterator of all results found for the
	 * path. If the xpath matches no properties in the graph, the Iterator will
	 * be empty, but not null.
	 *
	 * @param <O>
	 *            the expected object type
	 *
	 * @param xpath
	 *            to iterate
	 * @return Iterator&lt;Object&gt;
	 */
	<O> Iterator<O> iterate(String xpath);
}
