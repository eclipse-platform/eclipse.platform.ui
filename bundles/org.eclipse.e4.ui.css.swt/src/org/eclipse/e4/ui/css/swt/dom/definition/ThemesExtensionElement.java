/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Timo Kinnunen <timo.kinnunen@gmail.com> - bug 450727
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom.definition;

import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.utils.ClassUtils;
import org.eclipse.e4.ui.internal.css.swt.definition.IThemesExtension;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ThemesExtensionElement extends ElementAdapter {
	private String localName;

	private String namespaceURI;

	public ThemesExtensionElement(IThemesExtension themesExtension, CSSEngine engine) {
		super(themesExtension, engine);
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
		return null;
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
			localName = getNativeWidget().getClass().getSimpleName();
		}
		return localName;
	}

	@Override
	public String getAttribute(String arg0) {
		return "";
	}

}
