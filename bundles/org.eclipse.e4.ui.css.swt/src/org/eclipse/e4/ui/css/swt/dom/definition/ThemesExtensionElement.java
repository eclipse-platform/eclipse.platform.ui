/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom.definition;

import org.eclipse.e4.ui.css.core.utils.ClassUtils;

import org.eclipse.e4.ui.internal.css.swt.definition.IThemesExtension;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.eclipse.e4.ui.css.core.dom.ElementAdapter;

public class ThemesExtensionElement extends ElementAdapter {
	private String localName;

	private String namespaceURI;
	
	public ThemesExtensionElement(IThemesExtension themesExtension, CSSEngine engine) {
		super(themesExtension, engine);
	}
	
	public NodeList getChildNodes() {
		return null;
	}

	public String getNamespaceURI() {
		if (namespaceURI == null) {
			namespaceURI = ClassUtils.getPackageName(getNativeWidget().getClass());
		}
		return namespaceURI;
	}

	public Node getParentNode() {
		return null;
	}

	public String getCSSId() {
		return null;
	}

	public String getCSSClass() {
		return null;
	}

	public String getCSSStyle() {
		return null;
	}

	@Override
	public String getLocalName() {
		if (localName == null) {
			localName = ClassUtils.getSimpleName(getNativeWidget().getClass());
		}
		return localName;
	}

	@Override
	public String getAttribute(String arg0) {
		return null;
	}

}
