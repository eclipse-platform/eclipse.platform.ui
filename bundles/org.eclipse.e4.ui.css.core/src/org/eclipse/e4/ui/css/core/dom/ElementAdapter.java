/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CSSExtendedPropertiesImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * {@link Element} implementation.
 */
public abstract class ElementAdapter implements Element, CSSStylableElement {

	private static final String[] EMPTY_STRING = new String[0];

	private Object nativeWidget;

	protected CSSEngine engine;

	private Map <String, CSSStyleDeclaration> defaultStyleDeclarationMap = new HashMap<>();

	private CSSExtendedProperties style;

	private List<String> staticPseudoInstances;

	public ElementAdapter(Object nativeWidget, CSSEngine engine) {
		this.nativeWidget = nativeWidget;
		this.engine = engine;
	}

	/**
	 * Add static pseudo instance
	 *
	 * @param instance
	 */
	public void addStaticPseudoInstance(String pseudoE) {
		if (staticPseudoInstances == null) {
			staticPseudoInstances = new ArrayList<>();
		}
		staticPseudoInstances.add(pseudoE);
	}

	@Override
	public boolean isStaticPseudoInstance(String s) {
		if (staticPseudoInstances == null) {
			return false;
		}
		return staticPseudoInstances.contains(s);
	}

	@Override
	public void copyDefaultStyleDeclarations(CSSStylableElement stylableElement) {
		// Copy default style decalaration
		this.setDefaultStyleDeclaration(null, stylableElement
				.getDefaultStyleDeclaration(null));
		// Copy all static pseudo instances
		String[] staticPseudoInstances = stylableElement
				.getStaticPseudoInstances();
		if (staticPseudoInstances != null) {
			for (String pseudoE : staticPseudoInstances) {
				CSSStyleDeclaration declaration = stylableElement
						.getDefaultStyleDeclaration(pseudoE);
				this.setDefaultStyleDeclaration(pseudoE, declaration);
			}
		}
	}

	@Override
	public abstract String getLocalName();

	@Override
	public abstract String getAttribute(String arg0);

	@Override
	public String getAttributeNS(String namespace, String attr)
			throws DOMException {
		return getAttribute(attr);
	};

	@Override
	public Attr getAttributeNode(String arg0) {
		return null;
	}

	@Override
	public Attr getAttributeNodeNS(String arg0, String arg1)
			throws DOMException {
		return null;
	}

	@Override
	public NodeList getElementsByTagName(String arg0) {
		return null;
	}

	@Override
	public NodeList getElementsByTagNameNS(String arg0, String arg1)
			throws DOMException {
		return null;
	}

	@Override
	public boolean hasAttribute(String arg0) {
		return getAttribute(arg0)!=null;
	}

	@Override
	public boolean hasAttributeNS(String namespace, String attr)
			throws DOMException {
		return hasAttribute(attr);
	}

	@Override
	public void removeAttribute(String arg0) throws DOMException {
	}

	@Override
	public void removeAttributeNS(String arg0, String arg1) throws DOMException {
	}

	@Override
	public Attr removeAttributeNode(Attr arg0) throws DOMException {
		return null;
	}

	@Override
	public void setAttribute(String arg0, String arg1) throws DOMException {
	}

	@Override
	public void setAttributeNS(String arg0, String arg1, String arg2)
			throws DOMException {
	}

	@Override
	public Attr setAttributeNode(Attr arg0) throws DOMException {
		return null;
	}

	@Override
	public Attr setAttributeNodeNS(Attr arg0) throws DOMException {
		return null;
	}

	@Override
	public Node appendChild(Node newChild) throws DOMException {
		return insertBefore(newChild, null);
	}

	@Override
	public Node cloneNode(boolean arg0) {
		return null;
	}

	@Override
	public NamedNodeMap getAttributes() {
		return null;
	}

	@Override
	public Node getFirstChild() {
		return null;
	}

	@Override
	public Node getLastChild() {
		return null;
	}

	@Override
	public String getTagName() {
		return getLocalName();
	}

	@Override
	public Node getNextSibling() {
		return null;
	}

	@Override
	public String getNodeName() {
		// By default Node name is the same thing like localName
		return getLocalName();
	}

	@Override
	public short getNodeType() {
		return ELEMENT_NODE;
	}

	@Override
	public String getNodeValue() throws DOMException {
		return null;
	}

	@Override
	public Document getOwnerDocument() {
		return null;
	}

	@Override
	public String getPrefix() {
		return null;
	}

	@Override
	public Node getPreviousSibling() {
		return null;
	}

	@Override
	public boolean hasAttributes() {
		return false;
	}

	@Override
	public boolean hasChildNodes() {
		return false;
	}

	@Override
	public Node insertBefore(Node newChild, Node refChild) throws DOMException {

		return null;
	}

	@Override
	public boolean isSupported(String arg0, String arg1) {
		return false;
	}

	@Override
	public void normalize() {
	}

	@Override
	public Node removeChild(Node arg0) throws DOMException {
		return null;
	}

	@Override
	public Node replaceChild(Node arg0, Node arg1) throws DOMException {
		return null;
	}

	@Override
	public void setNodeValue(String arg0) throws DOMException {
	}

	@Override
	public void setPrefix(String arg0) throws DOMException {
	}

	@Override
	public void setIdAttribute(String name, boolean isId) throws DOMException {
	}

	@Override
	public void setIdAttributeNS(String namespaceURI, String localName,
			boolean isId) throws DOMException {
	}

	@Override
	public void setIdAttributeNode(Attr idAttr, boolean isId)
			throws DOMException {
	}

	@Override
	public short compareDocumentPosition(Node other) throws DOMException {
		return 0;
	}

	@Override
	public String getBaseURI() {
		return null;
	}

	@Override
	public Object getFeature(String feature, String version) {
		return null;
	}

	@Override
	public String getTextContent() throws DOMException {
		return null;
	}

	@Override
	public Object getUserData(String key) {
		return null;
	}

	@Override
	public boolean isDefaultNamespace(String namespaceURI) {
		return false;
	}

	@Override
	public boolean isEqualNode(Node arg) {
		return false;
	}

	@Override
	public boolean isSameNode(Node other) {
		return false;
	}

	@Override
	public String lookupNamespaceURI(String prefix) {
		return null;
	}

	@Override
	public String lookupPrefix(String namespaceURI) {
		return null;
	}

	@Override
	public void setTextContent(String textContent) throws DOMException {

	}

	@Override
	public Object getNativeWidget() {
		return nativeWidget;
	}

	@Override
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		return null;
	}

	@Override
	public TypeInfo getSchemaTypeInfo() {
		return null;
	}

	@Override
	public CSSStyleDeclaration getDefaultStyleDeclaration(String pseudoE) {
		return defaultStyleDeclarationMap.get(pseudoE);
	}

	@Override
	public void setDefaultStyleDeclaration(String pseudoE, CSSStyleDeclaration declaration) {
		this.defaultStyleDeclarationMap.put(pseudoE, declaration);
	}

	@Override
	public void onStylesApplied(NodeList nodes) {
		// Do Nothing

	}

	protected Element getElement(Object widget) {
		return engine.getElement(widget);
	}

	@Override
	public CSSExtendedProperties getStyle() {
		if (style == null) {
			style = new CSSExtendedPropertiesImpl(nativeWidget, engine);
		}
		return style;
	}

	@Override
	public String[] getStaticPseudoInstances() {
		if (staticPseudoInstances == null) {
			return EMPTY_STRING;
		}
		return staticPseudoInstances.toArray(EMPTY_STRING);
	}

	@Override
	public boolean isPseudoInstanceOf(String s) {
		if (staticPseudoInstances == null) {
			return false;
		}
		return staticPseudoInstances.contains(s);
	}

	@Override
	public void initialize() {

	}

	@Override
	public void dispose() {

	}

	/**
	 * Apply styles for the native widget.
	 */
	protected void doApplyStyles() {
		try {
			engine.applyStyles(getNativeWidget(), false, true);
		} catch (Exception ex) {
			engine.handleExceptions(ex);
		}
	}
}
