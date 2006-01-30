/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * An Intro Config component that has an id attribute. It is used as a base
 * class for all config elements that can take an id, and hence are valid
 * targets for includes and finds.
 * 
 */
public abstract class AbstractIntroIdElement extends AbstractIntroElement {

    public static final String ATT_ID = "id"; //$NON-NLS-1$
    public static final String ATT_FILTER = "filter"; //$NON-NLS-1$
    public static final String ATT_NAME = "name"; //$NON-NLS-1$
    public static final String ATT_VALUE = "value"; //$NON-NLS-1$
    public static final String ELEMENT_FILTER = "filter"; //$NON-NLS-1$

    protected String id;
    private Map filters;

    AbstractIntroIdElement(IConfigurationElement element) {
        super(element);
        id = element.getAttribute(ATT_ID);
    }

    AbstractIntroIdElement(Element element, Bundle bundle) {
        super(element, bundle);
        id = getAttribute(element, ATT_ID);
        filters = findFilters(element);
    }

    AbstractIntroIdElement(Element element, Bundle bundle, String base) {
        super(element, bundle, base);
        id = getAttribute(element, ATT_ID);
        filters = findFilters(element);
    }

    /**
     * Finds all filters associated with the given element. These can be defined as either
     * a filter attribute on the element, or filter elements as children of this element.
     * The result is a mapping of all the filter names to values. For example, "os" -> "win32".
     * 
     * @param element the element whose filters to find
     * @return a filter name to value mapping for all the filters on this element
     */
    private static Map findFilters(Element element) {
    	Map map = null;
    	// check for filter attribute
        String filterAttribute = element.getAttribute(ATT_FILTER);
        if (filterAttribute.length() > 0) {
        	map = new HashMap();
        	int equalsIndex = filterAttribute.indexOf('=');
        	String name = filterAttribute.substring(0, equalsIndex);
        	String value = filterAttribute.substring(equalsIndex + 1);
        	map.put(name, value);
        }
        // check for child filter elements
        NodeList list = element.getChildNodes();
        for (int i=0;i<list.getLength();++i) {
        	Node node = list.item(i);
        	if (node.getNodeType() == Node.ELEMENT_NODE && ELEMENT_FILTER.equals(node.getNodeName())) {
        		Element elementNode = (Element)node;
        		if (map == null) {
        			map = new HashMap();
        		}
        		map.put(elementNode.getAttribute(ATT_NAME), elementNode.getAttribute(ATT_VALUE));
        	}
        }
        return map;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    public Map getFilters() {
    	return filters;
    }
}
