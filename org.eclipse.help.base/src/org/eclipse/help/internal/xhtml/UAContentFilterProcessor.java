/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.xhtml;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.FilterableUAElement;
import org.eclipse.help.internal.util.StringUtil;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles content manipulation. Filters filter content in as opposed to filtering out.
 */
public class UAContentFilterProcessor {

	public Document applyFilters(Document dom) {
		Element body = DOMUtil.getBodyElement(dom);
		NodeList allChildElements = body.getChildNodes();
		for (int i = 0; i < allChildElements.getLength(); i++) {
			Node node = (Node) allChildElements.item(i);
			if (!(node instanceof Element))
				continue;
			applyFilters((Element) node);

		}
		return dom;
	}

	public void applyFilters(Element element) {
		boolean filteredIn = false;
		if (hasFilterAttribute(element)) {
			filteredIn = processFilterAttribute(element);
			if (!filteredIn)
				return;
		} else if (hasFiltersAsChildren(element)) {
			Element[] filters = DOMUtil.getElementsByTagName(element, "filter"); //$NON-NLS-1$
			filteredIn = processFilterChildren(element, filters);
			if (!filteredIn)
				return;
		}

		NodeList allChildElements = element.getChildNodes();
		for (int i = 0; i < allChildElements.getLength(); i++) {
			Node node = (Node) allChildElements.item(i);
			if (!(node instanceof Element))
				continue;
			Element childElement = (Element) node;
			applyFilters(childElement);
		}
	}

	/**
	 * Returns the current value for the given single-value filterable property
	 * by the given name (e.g. "os"). For multi-value properties, returns null. 
	 * 
	 * @param filterName the filter name (e.g. "os")
	 * @return the current value (e.g. "win32") or null
	 */
	public String getCurrentValue(String filterName) {
		if (filterName.equals("ws")) { //$NON-NLS-1$
			return Platform.getWS();
		}
		else if (filterName.equals("os")) { //$NON-NLS-1$
			return Platform.getOS();
		}
		else if (filterName.equals("arch")) { //$NON-NLS-1$
			return Platform.getOSArch();
		}
		else if (filterName.equals("product")) { //$NON-NLS-1$ 
			IProduct product = Platform.getProduct();
			if (product != null) {
				return product.getId();
			}
		}
		return null;
	}
	
	private static boolean hasFilterAttribute(Element element) {
		if (element.getAttribute(DOMUtil.ATT_FILTER).equals("")) //$NON-NLS-1$
			return false;
		return true;
	}

	private static boolean hasFiltersAsChildren(Element element) {
		Element[] filters = DOMUtil.getElementsByTagName(element, "filter"); //$NON-NLS-1$
		if (filters != null && filters.length > 0)
			return true;
		return false;
	}

	/**
	 * Returns true is filter passes and Element is to be included.
	 * 
	 * @param element the element whose filter attribute to check
	 * @return whether or not the element should be filtered in
	 */
	private boolean processFilterAttribute(Element element) {
		String filterString = element.getAttribute(DOMUtil.ATT_FILTER);
		boolean filtered_in = isFilteredIn(filterString);

		if (!filtered_in) {
			element.getParentNode().removeChild(element);
		}
		return filtered_in;
	}

	private boolean processFilterChildren(Element parent, Element[] filters) {
		boolean filtered_in = false;
		for (int i = 0; i < filters.length; i++) {
			String filter = filters[i].getAttribute("name"); //$NON-NLS-1$
			String value = filters[i].getAttribute("value"); //$NON-NLS-1$
			boolean isPositive = (value.length() == 0 || value.charAt(0) != '!');
			if (!isPositive) {
				// strip the NOT symbol (!)
				value = value.substring(1);
			}
			filtered_in = isFilteredIn(filter, value, isPositive);
			if (!filtered_in) {
				parent.getParentNode().removeChild(parent);
				break;
			}
		}
		return filtered_in;
	}

	/**
	 * Returns whether or not the object with the given filter attribute should be
	 * filtered in.
	 * 
	 * @param filterAttribute the attribute to check, e.g. "os=win32"
	 * @return whether or not the element should be filtered in
	 */
	public boolean isFilteredIn(String filterAttribute) {
		String[] parsedFilterString = null;
		boolean isPositive = (filterAttribute.indexOf("!=") == -1); //$NON-NLS-1$
		// split at "=" or "!="
		parsedFilterString = StringUtil.split(filterAttribute, "!?="); //$NON-NLS-1$
		String filter = parsedFilterString[0];
		String value = parsedFilterString[1];
		return isFilteredIn(filter, value, isPositive);
	}

	/**
	 * Returns whether or not the object with the given filter should be filtered in.
	 * Can be overriden to provide additional filtering.
	 * 
	 * @param filter the filter name (e.g. "os")
	 * @param value the filter value (e.g. "win32")
	 * @param isPositive whether the filter is a positive filter (as opposed to a NOT)
	 * @return whether or not to filter the element
	 */
	public boolean isFilteredIn(String filter, String value, boolean isPositive) {
		boolean filtered_in = false;
		if (filter.equals("ws")) { //$NON-NLS-1$
			filtered_in = filterByWS(value);
		} else if (filter.equals("os")) { //$NON-NLS-1$
			filtered_in = filterByOS(value);
		} else if (filter.equals("arch")) { //$NON-NLS-1$
			filtered_in = filterByARCH(value);
		} else if (filter.equals("product")) { //$NON-NLS-1$ 
			filtered_in = filterByProduct(value);
		} else if (filter.equals("plugin")) { //$NON-NLS-1$
			filtered_in = filterByPlugin(value);
		} else
			filtered_in = filterBySystemProperty(filter, value);

		return isPositive ? filtered_in : !filtered_in;
	}

	/**
	 * Returns whether or not the given object should be filtered out.
	 * Can be overriden to provide additional filtering.
	 * 
	 * @param element the element to check
	 * @return whether or not to filter the element
	 */
	public boolean isFilteredIn(FilterableUAElement element) {
		if (element != null) {
			Map filters = ((FilterableUAElement)element).getFilters();
			if (filters != null) {
				Iterator iter = filters.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry)iter.next();
					String key = (String)entry.getKey();
					String value = (String)entry.getValue();
					boolean isPositive = (value.length() == 0 || value.charAt(0) != '!');
					if (!isPositive) {
						value = value.substring(1);
					}
					if (!isFilteredIn(key, value, isPositive)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Returns whether or not the given filter property allows multiple instances
	 * to be specified. For example, for os there can only ever be one os running,
	 * but for plugins there can be many.
	 * 
	 * @param filterName the name of the filter (without value)
	 * @return whether or not the filter allows multiple values
	 */
	public boolean isMultiValue(String filterName) {
		return !(filterName.equals("os") //$NON-NLS-1$
				|| filterName.equals("ws") //$NON-NLS-1$
				|| filterName.equals("arch") //$NON-NLS-1$
				|| filterName.equals("product")); //$NON-NLS-1$
	}
	
	/**
	 * evaluates WS filter.
	 */
	private static boolean filterByWS(String ws) {
		String currentWS = Platform.getWS();
		if (currentWS.equals(ws))
			return true;
		return false;
	}

	/**
	 * evaluates OS filter.
	 */
	private static boolean filterByOS(String os) {
		String currentOS = Platform.getOS();
		if (currentOS.equals(os))
			return true;
		return false;
	}

	/**
	 * evaluates ARCH filter.
	 */
	private static boolean filterByARCH(String arch) {
		String currentArch = Platform.getOSArch();
		if (currentArch.equals(arch))
			return true;
		return false;
	}

	/**
	 * evaluates product filter.
	 */
	private static boolean filterByProduct(String productId) {
		IProduct product = Platform.getProduct();
		if (product == null)
			return false;

		String currentProductId = product.getId();
		if (currentProductId.equals(productId))
			return true;
		return false;
	}

	/**
	 * evaluates plugin filter.
	 */
	private static boolean filterByPlugin(String bundleId) {
		Bundle bundle = Platform.getBundle(bundleId);
		boolean bundleIsOK = checkBundleState(bundle);
		if (bundleIsOK)
			return true;

		return false;
	}

	public static boolean checkBundleState(Bundle bundle) {
		if (bundle == null || bundle.getState() == Bundle.UNINSTALLED
				|| bundle.getState() == Bundle.INSTALLED)
			return false;

		return true;
	}

	/**
	 * evaluates system property filter.
	 */
	private static boolean filterBySystemProperty(String property, String value) {
		String currentValue = System.getProperty(property);
		if (currentValue != null && currentValue.equals(value))
			return true;
		return false;
	}
}
