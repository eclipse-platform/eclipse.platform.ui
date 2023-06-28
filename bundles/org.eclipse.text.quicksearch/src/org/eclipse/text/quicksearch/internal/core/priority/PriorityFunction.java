/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.core.priority;

import org.eclipse.core.resources.IResource;

/**
 * An instance implementing this interface can optionally be provided to influence
 * the searching order. If it is not provided then the default will be used.
 */
public abstract class PriorityFunction {

	/**
	 * The highest priority. Any elements in the queue with this priority will be visited before
	 * any others in the queue. Be warned that assigning this priority to a deeply nested
	 * element in the tree alone doesn't guarantee it will be visited early on because in
	 * order to reach the element the parents have to be visited first. If the parent
	 * has a low priority...
	 */
	public static final double PRIORITY_HIGHEST = Double.POSITIVE_INFINITY;

	/**
	 * Priority indicating something that is moderately more interesting than the default.
	 * So it should be processed before default stuff but not before "VISIT_FIRST" priority.
	 */
	public static final double PRIORITY_INTERESTING = 100;

	/**
	 * A default priority value. Meant to be used for elements that are neither particularly
	 * interesting or particularly non-interesting. Use larger numbers to emphasize elements
	 * and lower numbers to de-emphasise them. Note that in order to emphasise an element
	 * globally it also necessary to raise the priority of their parents because children
	 * can't be reached without passing through their parent.
	 */
	public static final double PRIORITY_DEFAULT = 0;


	/**
	 * A special priority that causes elements (and their children) to be completely ignored.
	 */
	public static final double PRIORITY_IGNORE = Double.NEGATIVE_INFINITY;




	public abstract double priority(IResource r);
}
