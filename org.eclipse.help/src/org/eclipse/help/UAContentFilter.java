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
package org.eclipse.help;

import org.eclipse.help.internal.dynamic.FilterResolver;

/**
 * <p>
 * This class provides the ability to filter out user assistance model elements
 * that support filtering (e.g. <code>IToc</code>, <code>ITopic</code>, ...).
 * Implementations that display such elements should consult this class before
 * attempting to display them.
 * </p>
 * 
 * @since 3.2
 */
public class UAContentFilter {
	
	private static final String ELEMENT_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

	private static FilterResolver resolver;
	
	/**
	 * <p>
	 * Returns whether or not the given object should be filtered out. This
	 * applies to any user assistance component's elements where filters apply
	 * (e.g. help tocs, topics, intro elements, context help topics). If the
	 * element is <code>null</code> or is not filterable, this method returns
	 * <code>false</code>.
	 * </p>
	 * 
	 * @param element the element to check
	 * @return whether or not the element should be filtered out
	 */
	public static boolean isFiltered(Object element) {
		if (element instanceof Node) {
			Node node = (Node)element;
			if (node.getAttribute(ATTRIBUTE_FILTER) != null) {
				return handleFilterAttribute(node);
			}
			Node[] children = node.getChildNodes();
			for (int i=0;i<children.length;++i) {
				if (ELEMENT_FILTER.equals(children[i].getNodeName())) {
					return handleFilterNodes(node);
				}
			}
		}
		return false;
	}
	
	private static boolean handleFilterAttribute(Node node) {
		String expression = node.getAttribute(ATTRIBUTE_FILTER);
		if (resolver == null) {
			resolver = new FilterResolver();
		}
		return resolver.isFiltered(expression);
	}
	
	/*
	 * Handle the child filter node case.
	 */
	private static boolean handleFilterNodes(Node node) {
		boolean hasFilteredYet = false;
		Node[] children = node.getChildNodes();
		for (int i=0;i<children.length;++i) {
			Node child = children[i];
			// is it a filter node?
			if (ELEMENT_FILTER.equals(child.getNodeName())) {
				// if it already filtered, don't bother evaluating the rest
				if (!hasFilteredYet) {
					String name = child.getAttribute(ATTRIBUTE_NAME);
					String value = child.getAttribute(ATTRIBUTE_VALUE);
					if (name != null && value != null && name.length() > 0 && value.length() > 0) {
						boolean not = (value.charAt(0) == '!');
						if (not) {
							value = value.substring(1);
						}
						hasFilteredYet = resolver.isFiltered(name, value, not);
					}
				}
			}
		}
		return hasFilteredYet;
	}
}
