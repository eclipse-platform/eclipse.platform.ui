/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Stefan Winkler <stefan@winklerweb.net> - Bug 419482, 419377
 *   Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Dummy DOM Element implementation for testing.
 */
public class TestElement extends ElementAdapter {

	private final String typeName;
	private String className;
	private String id;
	private Map<String, String> attrs = new HashMap<String, String>();
	private Node parentNode = null;
	private List<Node> children = null;

	public TestElement(String type, CSSEngine engine) {
		super(null, engine);
		this.typeName = type;
	}

	public TestElement(String type, TestElement parent, CSSEngine engine) {
		this(type, engine);
		this.parentNode = parent;
		parent.appendChild(this);
	}

	public void setClass(String className) {
		this.className = className;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setAttribute(String name, String value) {
		attrs.put(name, value);
	}

	@Override
	public String getAttribute(String name) {
		String value = attrs.get(name);
		return value == null ? "" : value;
	}

	@Override
	public boolean hasAttribute(String name) {
		return attrs.containsKey(name);
	}

	@Override
	public String getLocalName() {
		return typeName;
	}

	@Override
	public NodeList getChildNodes() {
		if (children == null) {
			return null;
		}

		return new NodeList() {
			@Override
			public int getLength() {
				return children.size();
			}

			@Override
			public Node item(int index) {
				return children.get(index);
			}
		};
	}

	@Override
	public String getNamespaceURI() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return parentNode;
	}

	@Override
	public String getCSSClass() {
		return className;
	}

	@Override
	public String getCSSId() {
		return id;
	}

	@Override
	public String getCSSStyle() {
		return null;
	}

	@Override
	public Node appendChild(Node newChild) throws DOMException {
		if (children == null) {
			children = new ArrayList<Node>();
		}
		children.add(newChild);
		return newChild;
	}
}