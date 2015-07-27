/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Timo Kinnunen <timo.kinnunen@gmail.com> - bug 450727
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom.definition;

import static org.eclipse.e4.ui.css.swt.helpers.ThemeElementDefinitionHelper.escapeId;

import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.utils.ClassUtils;
import org.eclipse.e4.ui.internal.css.swt.definition.IThemeElementDefinitionOverridable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ThemeDefinitionElement<T extends IThemeElementDefinitionOverridable<?>> extends ElementAdapter {
	private String localName;

	private String namespaceURI;

	private String id;

	public ThemeDefinitionElement(T definition, CSSEngine engine) {
		super(definition, engine);
	}

	@Override
	public NodeList getChildNodes() {
		return null;
	}

	@Override
	public String getNamespaceURI() {
		if (namespaceURI == null) {
			namespaceURI = ClassUtils.getPackageName(getNativeWidget().getClass());
		}
		return namespaceURI;
	}

	@Override
	public Node getParentNode() {
		return null;
	}

	@Override
	public String getCSSId() {
		if (id == null) {
			id = escapeId(((IThemeElementDefinitionOverridable<?>) getNativeWidget()).getId());
		}
		return id;
	}

	@Override
	public String getCSSClass() {
		return null;
	}

	@Override
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
	public String getAttribute(String attr) {
		return "";
	}
}
