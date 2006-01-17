/***************************************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Util class for model. Has methods for resolving model attributes, and methods for manipulating
 * XHTML DOM.
 */
public class DOMUtil {

	public static String TAG_BODY = "body"; //$NON-NLS-1$
	private static String TAG_HEAD = "head"; //$NON-NLS-1$
	public static String TAG_BASE = "base"; //$NON-NLS-1$
	public static String TAG_DIV = "div"; //$NON-NLS-1$
	public static String TAG_HEAD_LINK = "link"; //$NON-NLS-1$
	public static String TAG_FILTER = "filter"; //$NON-NLS-1$
	public static String ATT_HREF = "href"; //$NON-NLS-1$
	public static String ATT_REL = "rel"; //$NON-NLS-1$
	public static String ATT_TYPE = "type"; //$NON-NLS-1$
	public static String ATT_FILTER = "filter"; //$NON-NLS-1$




	public static void insertBase(Document dom, String baseURL) {
		// there should only be one head and one base element dom.
		NodeList headList = dom.getElementsByTagName(TAG_HEAD);
		Element head = (Element) headList.item(0);
		NodeList baseList = head.getElementsByTagName(TAG_BASE);
		if (baseList.getLength() == 0) {
			// insert a base element, since one is not defined already.
			Element base = dom.createElement(TAG_BASE);
			base.setAttribute(ATT_HREF, baseURL);
			head.insertBefore(base, head.getFirstChild());
		}
	}


	public static Element getBase(Document dom) {
		// there should only be one head and one base element dom.
		NodeList headList = dom.getElementsByTagName(TAG_HEAD);
		Element head = (Element) headList.item(0);
		NodeList baseList = head.getElementsByTagName(TAG_BASE);
		if (baseList.getLength() == 0)
			// no base defined, signal failure.
			return null;

		return (Element) baseList.item(baseList.getLength() - 1);

	}

	public static void insertStyle(Document dom, String cssUrl) {
		// there should only be one head and one base element dom.
		NodeList headList = dom.getElementsByTagName(TAG_HEAD);
		Element head = null;
		// Element base = getBase(dom);
		NodeList styleList = null;
		// there can be more than one style. DO not add style if it exists.
		if (headList.getLength() >= 1) {
			head = (Element) headList.item(0);
			styleList = head.getElementsByTagName(TAG_HEAD_LINK);
			for (int i = 0; i < styleList.getLength(); i++) {
				Element style = (Element) styleList.item(0);
				String styleString = style.getAttribute(ATT_HREF);
				if (styleString.equals(cssUrl))
					return;
			}
		}

		// insert the style, since it is not defined.
		Element styleToAdd = dom.createElement(TAG_HEAD_LINK);
		styleToAdd.setAttribute(ATT_HREF, cssUrl);
		styleToAdd.setAttribute(ATT_REL, "stylesheet"); //$NON-NLS-1$
		styleToAdd.setAttribute(ATT_TYPE, "text/css"); //$NON-NLS-1$
		if (styleList != null && styleList.getLength() >= 1)
			styleList.item(0).getParentNode().insertBefore(styleToAdd, styleList.item(0));
		else
			head.appendChild(styleToAdd);

	}

	/**
	 * Returns a reference to the body of the DOM.
	 * 
	 * @param dom
	 * @return
	 */
	public static Element getBodyElement(Document dom) {
		// there should only be one body element dom.
		NodeList bodyList = dom.getElementsByTagName(TAG_BODY);
		Element body = (Element) bodyList.item(0);
		return body;
	}



	public static Element createElement(Document dom, String elementName, Properties attributes) {

		// make sure to create element with any namespace uri to enable finding
		// it again using Dom.getElementsByTagNameNS()
		Element element = dom.createElementNS("", elementName); //$NON-NLS-1$
		if (attributes != null) {
			Enumeration e = attributes.keys();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				element.setAttribute(key, attributes.getProperty(key));
			}
		}
		return element;
	}

	public static Element createAndAppendChild(Element parentElement, String elementName,
			Properties attributes) {

		Element element = createElement(parentElement.getOwnerDocument(), elementName, attributes);
		parentElement.appendChild(element);
		return element;
	}



	/**
	 * Returns an Element array of all first level descendant Elements with a given tag name, in the
	 * order in which they are encountered in the DOM. Unlike the JAXP apis, which returns preorder
	 * traversal of this Element tree, this method filters out children deeper than first level
	 * child nodes.
	 */
	public static Element[] getElementsByTagName(Element parent, String tagName) {
		NodeList allChildElements = parent.getElementsByTagName(tagName);
		Vector vector = new Vector();
		for (int i = 0; i < allChildElements.getLength(); i++) {
			// we know that the nodelist is of elements.
			Element aElement = (Element) allChildElements.item(i);
			if (aElement.getParentNode().equals(parent))
				// first level child element. add it.
				vector.add(aElement);
		}
		Element[] filteredElements = new Element[vector.size()];
		vector.copyInto(filteredElements);
		return filteredElements;
	}

	/**
	 * Same as getElementsByTagName(Element parent, String tagName) but the parent element is
	 * assumed to be the root of the document.
	 * 
	 * @see getElementsByTagName(Element parent, String tagName)
	 */
	public static Element[] getElementsByTagName(Document dom, String tagName) {
		NodeList allChildElements = dom.getElementsByTagName(tagName);
		Vector vector = new Vector();
		for (int i = 0; i < allChildElements.getLength(); i++) {
			// we know that the nodelist is of elements.
			Element aElement = (Element) allChildElements.item(i);
			if (aElement.getParentNode().equals(dom.getDocumentElement()))
				// first level child element. add it. Cant use getParent
				// here.
				vector.add(aElement);
		}
		Element[] filteredElements = new Element[vector.size()];
		vector.copyInto(filteredElements);
		return filteredElements;
	}


	/*
	 * Util method similar to DOM getElementById() method, but it works without an id attribute
	 * being specified. Deep searches all children in this container's DOM for the first child with
	 * the given id. The element retrieved must have the passed local name. Note that in an XHTML
	 * file (aka DOM) elements should have a unique id within the scope of a document. We use local
	 * name because this allows for finding anchors and includes regardless of whether or not an
	 * xmlns was used in the xml.
	 */
	public static Element getElementById(Document dom, String id, String localElementName) {

		NodeList children = dom.getElementsByTagNameNS("*", localElementName); //$NON-NLS-1$
		for (int i = 0; i < children.getLength(); i++) {
			Element element = (Element) children.item(i);
			if (element.getAttribute("id").equals(id)) //$NON-NLS-1$
				return element;
		}
		// non found.
		return null;

	}




	/**
	 * Returns an array version of the passed NodeList. Used to work around DOM design issues.
	 */
	public static Node[] getArray(NodeList nodeList) {
		Node[] nodes = new Node[nodeList.getLength()];
		for (int i = 0; i < nodeList.getLength(); i++)
			nodes[i] = nodeList.item(i);
		return nodes;
	}


	/**
	 * Remove all instances of the element from the DOM.
	 * 
	 */
	public static void removeAllElements(Document dom, String elementLocalName) {
		// get all elements in DOM and remove them.
		NodeList elements = dom.getElementsByTagNameNS("*", //$NON-NLS-1$
				elementLocalName);
		// get the array version of the nodelist to work around DOM api design.
		Node[] elementsArray = DOMUtil.getArray(elements);
		for (int i = 0; i < elementsArray.length; i++) {
			Node element = elementsArray[i];
			element.getParentNode().removeChild(element);
		}

	}



}
