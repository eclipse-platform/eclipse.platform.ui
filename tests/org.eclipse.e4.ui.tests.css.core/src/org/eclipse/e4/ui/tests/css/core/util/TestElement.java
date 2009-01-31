/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Dummy DOM Element implementation for testing.
 */
public class TestElement extends ElementAdapter {

	private final String typeName;
	private String className;
	private String id;
	private Map attrs = new HashMap();

	public TestElement(String type, CSSEngine engine) {
		super(null, engine);
		this.typeName = type;
	}

	public void setClass(String className) {
		this.className = className;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setAttribute(String name, String value) {
		attrs.put(name, value);
	}

	public String getAttribute(String name) {
		String value = (String) attrs.get(name);
		return value == null ? "" : value;
	}

	public String getLocalName() {
		return typeName;
	}

	public NodeList getChildNodes() {
		return null;
	}

	public String getNamespaceURI() {
		return null;
	}

	public Node getParentNode() {
		return null;
	}

	public String getCSSClass() {
		return className;
	}

	public String getCSSId() {
		return id;
	}

	public String getCSSStyle() {
		return null;
	}
}