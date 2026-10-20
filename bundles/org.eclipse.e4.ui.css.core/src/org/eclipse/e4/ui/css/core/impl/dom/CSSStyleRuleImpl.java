/*******************************************************************************
 * Copyright (c) 2008, 2026 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.dom;

import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors;

/**
 * A style rule: a selector list plus its style declaration.
 */
public final class CSSStyleRuleImpl implements CssRule {

	private final Selectors.SelectorList selectors;
	private CSSStyleDeclarationImpl styleDeclaration;

	public CSSStyleRuleImpl(Selectors.SelectorList selectors) {
		this.selectors = selectors;
	}

	public String getCssText() {
		return getSelectorText() + " { " + getStyle().getCssText() + " }";
	}

	public String getSelectorText() {
		return selectors.text();
	}

	public CSSStyleDeclarationImpl getStyle() {
		return styleDeclaration;
	}

	public Selectors.SelectorList getSelectorList() {
		return selectors;
	}

	public void setStyle(CSSStyleDeclarationImpl styleDeclaration) {
		this.styleDeclaration = styleDeclaration;
	}

	@Override
	public String toString() {
		return getSelectorText();
	}
}
