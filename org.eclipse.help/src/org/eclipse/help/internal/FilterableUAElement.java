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
package org.eclipse.help.internal;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

/**
 * Any model element or node in any user assistance component that is filterable.
 * Filters are generally specified in the XML as a filter attribute or child element,
 * where the filter has a name and value, for example:
 * 
 * <myUAElement filter="os=win32" myattribute="..." />
 * <myUAElement filter="os!=linux" myattribute="..." />
 * 
 * or
 * 
 * <myUAElement myattribute="...">
 *    <filter name="os" value="linux" />
 *    <filter name="ws" value="!gtk" />
 * </myUAElement>
 * 
 * Filters must be stored in the model because they should only be processed when
 * the content is about to be shown, because some of the filtering properties can change
 * during a session (e.g. activities).
 */
public abstract class FilterableUAElement {
	
	private Map filters;
	
	/**
	 * Adds the filters specified in the given attributes. This looks for
	 * the "filter" attribute and parses it.
	 * 
	 * @param attrs the XML attributes for the element
	 */
	public void addFilters(Attributes attrs) {
		// add filter attribute if it exists
		if (attrs != null) {
			String filterAttribute = attrs.getValue("filter"); //$NON-NLS-1$
			if (filterAttribute != null) {
				addFilter(filterAttribute);
			}
		}
	}
	
    /**
     * Adds all filters associated with the given element in the DOM. These can be defined
     * as either a filter attribute on the element, or filter elements as children of this
     * element.
     * 
     * @param element the element whose filters to find
     * @return a filter name to value mapping for all the filters on this element
     */
    public void addFilters(Element element) {
    	// check for filter attribute
    	String filterAttribute = element.getAttribute("filter"); //$NON-NLS-1$
    	if (filterAttribute.length() > 0) {
    		addFilter(filterAttribute);
    	}
        // check for child filter elements
        NodeList list = element.getChildNodes();
        for (int i=0;i<list.getLength();++i) {
        	Node node = list.item(i);
        	if (node.getNodeType() == Node.ELEMENT_NODE && "filter".equals(node.getNodeName())) { //$NON-NLS-1$
        		Element elementNode = (Element)node;
        		addFilter(elementNode.getAttribute("name"), elementNode.getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$
        	}
        }
    }

	/**
	 * Adds the filter specified by the given string containing both the
	 * filter name and value (e.g. "os=win32").
	 * 
	 * @param nameAndValue the filter name and value
	 */
	public void addFilter(String nameAndValue) {
		boolean isPositive = (nameAndValue.indexOf("!=") == -1); //$NON-NLS-1$
		// split at "=" or "!="
		String[] tokens = nameAndValue.split("!?="); //$NON-NLS-1$
    	String name = tokens[0];
    	String value = tokens[1];
    	if (!isPositive) {
    		value = '!' + value;
    	}
    	addFilter(name, value);
	}

	/**
	 * Adds the specified filter to this element, e.g. name="os", value="win32".
	 * 
	 * @param name the filter name, e.g. "os"
	 * @param name the filter value, e.g. "win32"
	 */
	public void addFilter(String name, String value) {
		if (filters == null) {
			filters = new HashMap();
		}
		filters.put(name, value);
	}
	
	/**
	 * Returns all the filters on this element. This is a mapping of
	 * filter names (e.g. "os") to filter values (e.g. "win32").
	 * 
	 * @return this element's filters
	 */
	public Map getFilters() {
		return filters;
	}
}
