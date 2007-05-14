/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IUAElement;
import org.eclipse.help.internal.dynamic.FilterResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * Base class for UA model elements.
 */
public class UAElement implements IUAElement {

	private static final String ELEMENT_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

	private static DocumentBuilder builder;
	private static Document document;
	
	public Element element;
	public IUAElement src;
	public UAElement parent;
	public UAElement[] children; // cache

	public UAElement(Element element) {
		this.element = element;
	}

	public UAElement(String name) {
		this.element = getDocument().createElement(name);
	}
	
	public UAElement(String name, IUAElement src) {
		this(name);
		this.src = src;
	}
	
	public void appendChild(UAElement uaElementToAppend) {
		importElement(uaElementToAppend);
		element.appendChild(uaElementToAppend.element);
		uaElementToAppend.parent = this;
		
		// cache is now invalid
		children = null;
	}

	public void appendChildren(IUAElement[] children) {
		for (int i=0;i<children.length;i++) {
			appendChild(children[i] instanceof UAElement ? (UAElement)children[i] : UAElementFactory.newElement(children[i]));
		}
	}
	
	public String getAttribute(String name) {
		String value = element.getAttribute(name);
		if (value.length() > 0) {
			return value;
		}
		return null;
	}

	public IUAElement[] getChildren() {
		if (children == null) {
			if (element.hasChildNodes()) {
				List list = new ArrayList();
				Node node = element.getFirstChild();
				while (node != null) {
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						UAElement uaElement = UAElementFactory.newElement((Element)node);
						uaElement.parent = this;
						if (uaElement != null) {
							list.add(uaElement);
						}
					}
					node = node.getNextSibling();
				}
				children = (UAElement[])list.toArray(new UAElement[list.size()]);
			}
			else {
				children = new UAElement[0];
			}
		}
		return children;
	}
	
	public Object getChildren(Class clazz) {
		IUAElement[] children = getChildren();
		if (children.length > 0) {
			List list = new ArrayList();
			for (int i=0;i<children.length;++i) {
				IUAElement child = children[i];
				if (clazz.isAssignableFrom(child.getClass())) {
					list.add(child);
				}
			}
			return list.toArray((Object[])Array.newInstance(clazz, list.size()));
		}
		return Array.newInstance(clazz, 0);
	}
	
	public String getElementName() {
		return element.getNodeName();
	}
	
	public static Document getDocument() {
		if (document == null) {
			if (builder == null) {
				try {
					builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					builder.setEntityResolver(new EntityResolver() {
						public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
							return new InputSource(new StringReader("")); //$NON-NLS-1$
						}
					});
				}
				catch (ParserConfigurationException e) {
					String msg = "Error creating document builder"; //$NON-NLS-1$
					HelpPlugin.logError(msg, e);
				}
			}
			document = builder.newDocument();
		}
		return document;
	}

	public UAElement getParentElement() {
		return parent;
	}

	public void insertBefore(UAElement newChild, UAElement refChild) {
		importElement(newChild);
		element.insertBefore(newChild.element, refChild.element);
		newChild.parent = this;

		// cache is now invalid
		children = null;
	}
	
	public boolean isEnabled(IEvaluationContext context) {
		if (HelpSystem.isShared()) {
			return true;
		}
		if (src != null) {
			return src.isEnabled(context);
		}
		String filter = getAttribute(ATTRIBUTE_FILTER);
		if (filter != null) {
			return isEnabledByFilterAttribute(filter);
		}
		Node node = element.getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String name = node.getNodeName();
				if (ExpressionTagNames.ENABLEMENT.equals(name)) {
					return isEnabledByEnablementElement((Element)node, context);
				}
				else if (ELEMENT_FILTER.equals(name)) {
					// can be multiple filter elements; enabled if they all pass
					if (!isEnabledByFilterElement((Element)node)) {
						return false;
					}
				}
			}
			node = node.getNextSibling();
		}
		return true;
	}
	
	public void removeChild(UAElement elementToRemove) {
		element.removeChild(elementToRemove.element);
		elementToRemove.parent = null;

		// cache is now invalid
		children = null;
	}
	
	public void setAttribute(String name, String value) {
		element.setAttribute(name, value);
	}

	private void importElement(UAElement uaElementToImport) {
		Element elementToImport = uaElementToImport.element;
		Document ownerDocument = element.getOwnerDocument();
		if (!ownerDocument.equals(elementToImport.getOwnerDocument())) {
			elementToImport = (Element)ownerDocument.importNode(elementToImport, true);
		}
		uaElementToImport.element = elementToImport;
	}

	private boolean isEnabledByEnablementElement(Element enablement, IEvaluationContext context) {
		try {
			Expression expression = ExpressionConverter.getDefault().perform(enablement);
			return expression.evaluate(context) == EvaluationResult.TRUE;
		}
		catch (CoreException e) {
			/*
			 * This can happen when attempting to resolve a UI variable (e.g. "workbench")
			 * in a non-UI environment (infocenter mode). Fail silently.
			 */
			return true;
		}
	}
	
	private boolean isEnabledByFilterAttribute(String filter) {
		return !FilterResolver.getInstance().isFiltered(filter);
	}
	
	private boolean isEnabledByFilterElement(Element filter) {
		String name = filter.getAttribute(ATTRIBUTE_NAME);
		String value = filter.getAttribute(ATTRIBUTE_VALUE);
		if (name.length() > 0 && value.length() > 0) {
			boolean not = false;
			if (value.startsWith("!")) { //$NON-NLS-1$
				not = true;
				value = value.substring(1);
			}
			return !FilterResolver.getInstance().isFiltered(name, value, not);
		}
		// ignore invalid filters
		return true;
	}
}
