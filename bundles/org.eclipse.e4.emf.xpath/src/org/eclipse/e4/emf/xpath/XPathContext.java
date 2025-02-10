/*******************************************************************************
 * Copyright (c) 2010, 2025 BestSolution.at and others.
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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Context in which the xpath is executed
 *
 * @since 1.0
 * @deprecated To query an E4-model use
 *             {@code org.eclipse.e4.ui.workbench.modeling.EModelService#findMatchingElements(org.eclipse.e4.ui.model.application.MApplicationElement, String, Class)}
 *             instead.
 */
@Deprecated(forRemoval = true, since = "2025-03 (removal in 2027-03 or later)")
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
	<T> T getValue(String xpath, Class<T> requiredType);

	/**
	 * Traverses the xpath and returns an Iterator of all results found for the
	 * path. If the xpath matches no properties in the graph, the Iterator will be
	 * empty, but not null.
	 *
	 * @param <T>   the expected object type
	 *
	 * @param xpath to iterate
	 * @return Iterator&lt;Object&gt;
	 */
	<T> Iterator<T> iterate(String xpath);

	/**
	 * Traverses the xpath and returns an {@link Stream} of all results found for
	 * the path. If the xpath matches no properties in the graph, the stream will be
	 * empty.
	 *
	 * @param <T>   the expected object type
	 * @param xpath the xpath expression to iterate
	 * @param type  the type of elements in the returned stream
	 * @return a stream of elements matching the specified xpath and of the given
	 *         type
	 * @since 0.5
	 */
	default <T> Stream<T> stream(String xpath, Class<T> type) {
		Iterator<?> iterator = iterate(xpath);
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
				.filter(type::isInstance).map(type::cast);
	}
}
