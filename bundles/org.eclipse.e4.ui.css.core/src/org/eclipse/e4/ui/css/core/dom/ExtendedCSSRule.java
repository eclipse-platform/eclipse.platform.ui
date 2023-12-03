/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom;

import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.css.CSSRule;

/**
 * Extend {@link CSSRule} to get selector and property list.
 */
public interface ExtendedCSSRule extends CSSRule {

	/**
	 * Return the list of {@link CSSProperty} of this {@link CSSRule}.
	 */
	public CSSPropertyList getCSSPropertyList();

	/**
	 * Return the list of {@link Selector} of this {@link CSSRule}.
	 */
	public SelectorList getSelectorList();
}
