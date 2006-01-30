/***************************************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles content manipulation. Filters filter content in as opposed to filtering out.
 * 
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
	 * @param element
	 * @return
	 */
	private boolean processFilterAttribute(Element element) {
		String filterString = element.getAttribute(DOMUtil.ATT_FILTER);
		String[] parsedFilterString = filterString.split("="); //$NON-NLS-1$
		String filter = parsedFilterString[0];
		String value = parsedFilterString[1];

		boolean filtered_in = isFilteredIn(filter, value);

		if (!filtered_in)
			element.getParentNode().removeChild(element);
		return filtered_in;
	}


	private boolean processFilterChildren(Element parent, Element[] filters) {
		boolean filtered_in = false;
		for (int i = 0; i < filters.length; i++) {
			String filter = filters[i].getAttribute("name"); //$NON-NLS-1$
			String value = filters[i].getAttribute("value"); //$NON-NLS-1$
			filtered_in = isFilteredIn(filter, value);
			if (!filtered_in) {
				parent.getParentNode().removeChild(parent);
				break;
			}
		}
		return filtered_in;
	}


	/**
	 * FIltering capabilities. Can be overiden by subclasses to add more filtering capabilities.
	 * 
	 * @param filter
	 * @param value
	 * @return
	 */
	public boolean isFilteredIn(String filter, String value) {
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

		return filtered_in;
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
