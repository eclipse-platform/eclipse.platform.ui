/*******************************************************************************
 * Copyright (c) 2013, 2017 IBM Corporation and others.
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
 *     Timo Kinnunen <timo.kinnunen@gmail.com> - Bug 450727
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 466075
 *     Dawid Pakuła <zulus@w3des.net> - Bug 466075
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom.preference;

import static org.eclipse.e4.ui.css.swt.helpers.ThemeElementDefinitionHelper.escapeId;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.utils.ClassUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EclipsePreferencesElement extends ElementAdapter {
	private String localName;

	private String namespaceURI;

	private String id;

	public EclipsePreferencesElement(IEclipsePreferences preferences, CSSEngine engine) {
		super(preferences, engine);
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
			id = escapeId(((IEclipsePreferences) getNativeWidget()).name());
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
			localName = ClassUtils.getSimpleName(IEclipsePreferences.class);
		}
		return localName;
	}

	@Override
	public String getAttribute(String arg0) {
		return "";
	}

	@Override
	public boolean isPseudoInstanceOf(String s) {
		if (!super.isStaticPseudoInstance(s)) {
			this.addStaticPseudoInstance(s);
		}
		return true;
	}
}
