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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * The handler responsible for filtering elements. Filters can either be
 * an attribute of the element to filter, or any number of child filter
 * elements.
 */
public class FilterHandler extends DOMProcessorHandler {

	private static final String ELEMENT_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

	private FilterResolver resolver;
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.dynamic.DOMProcessorHandler#handle(org.w3c.dom.Element, java.lang.String)
	 */
	public short handle(Element elem, String id) {
		if (elem.hasAttribute(ATTRIBUTE_FILTER)) {
			return handleFilterAttribute(elem);
		}
		Node node = elem.getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE && ELEMENT_FILTER.equals(node.getNodeName())) {
				return handleFilterElements(elem);
			}
			node = node.getNextSibling();
		}
		return UNHANDLED;
	}
	
	/*
	 * Handle the filter attribute case.
	 */
	private short handleFilterAttribute(Element elem) {
		String expression = elem.getAttribute(ATTRIBUTE_FILTER);
		if (resolver == null) {
			resolver = new FilterResolver();
		}
		boolean isFiltered = resolver.isFiltered(expression);
		if (isFiltered) {
			elem.getParentNode().removeChild(elem);
			return HANDLED_SKIP;
		}
		elem.removeAttribute(ATTRIBUTE_FILTER);
		return HANDLED_CONTINUE;
	}
	
	/*
	 * Handle the child filter element case.
	 */
	private short handleFilterElements(Element elem) {
		Node node = elem.getFirstChild();
		Node nextNode = node;
		boolean hasFilterFailedYet = false;
		while (node != null) {
			// have to look ahead in case we remove node
			nextNode = node.getNextSibling();
			// is it a filter element?
			if (node.getNodeType() == Node.ELEMENT_NODE && ELEMENT_FILTER.equals(node.getNodeName())) {
				Element filterElem = (Element)node;
				// if it already failed, don't bother evaluating the rest
				if (!hasFilterFailedYet) {
					String name = filterElem.getAttribute(ATTRIBUTE_NAME);
					String value = filterElem.getAttribute(ATTRIBUTE_VALUE);
					if (name.length() > 0 && value.length() > 0) {
						boolean not = (value.charAt(0) == '!');
						if (not) {
							value = value.substring(1);
						}
						hasFilterFailedYet = resolver.isFiltered(name, value, not);
					}
				}
				// remove all filter elements
				elem.removeChild(filterElem);
			}
			node = nextNode;
		}
		if (hasFilterFailedYet) {
			// at least one filter didn't pass
			elem.getParentNode().removeChild(elem);
			return HANDLED_SKIP;
		}
		// all filters passed; continue with children
		return HANDLED_CONTINUE;
	}
}
