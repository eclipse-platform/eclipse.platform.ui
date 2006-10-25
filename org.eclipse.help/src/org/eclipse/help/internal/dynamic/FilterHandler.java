/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.dynamic;

import org.eclipse.help.Node;

/*
 * The handler responsible for filtering elements. Filters can either be
 * an attribute of the element to filter, or any number of child filter
 * elements.
 */
public class FilterHandler extends DocumentProcessorHandler {

	private static final String ELEMENT_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

	private FilterResolver resolver;
	
	public short handle(Node node, String id) {
		if (node.getAttribute(ATTRIBUTE_FILTER) != null) {
			return handleFilterAttribute(node);
		}
		Node[] children = node.getChildren();
		for (int i=0;i<children.length;++i) {
			if (ELEMENT_FILTER.equals(children[i].getName())) {
				return handleFilterNodes(node);
			}
		}
		return UNHANDLED;
	}
	
	/*
	 * Handle the filter attribute case.
	 */
	private short handleFilterAttribute(Node node) {
		String expression = node.getAttribute(ATTRIBUTE_FILTER);
		if (resolver == null) {
			resolver = new FilterResolver();
		}
		boolean isFiltered = resolver.isFiltered(expression);
		if (isFiltered) {
			node.getParent().removeChild(node);
			return HANDLED_SKIP;
		}
		node.removeAttribute(ATTRIBUTE_FILTER);
		return HANDLED_CONTINUE;
	}
	
	/*
	 * Handle the child filter node case.
	 */
	private short handleFilterNodes(Node node) {
		boolean hasFilterFailedYet = false;
		Node[] children = node.getChildren();
		for (int i=0;i<children.length;++i) {
			Node child = children[i];
			// is it a filter node?
			if (ELEMENT_FILTER.equals(child.getName())) {
				// if it already failed, don't bother evaluating the rest
				if (!hasFilterFailedYet) {
					String name = child.getAttribute(ATTRIBUTE_NAME);
					String value = child.getAttribute(ATTRIBUTE_VALUE);
					if (name != null && value != null && name.length() > 0 && value.length() > 0) {
						boolean not = (value.charAt(0) == '!');
						if (not) {
							value = value.substring(1);
						}
						hasFilterFailedYet = resolver.isFiltered(name, value, not);
					}
				}
				// remove all filter elements
				node.removeChild(child);
			}
		}
		if (hasFilterFailedYet) {
			// at least one filter didn't pass
			node.getParent().removeChild(node);
			return HANDLED_SKIP;
		}
		// all filters passed; continue with children
		return HANDLED_CONTINUE;
	}
}
