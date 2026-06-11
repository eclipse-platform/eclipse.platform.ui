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

import java.util.List;

/**
 * A parsed stylesheet: an ordered list of {@link CssRule}s.
 */
public final class CSSStyleSheetImpl {

	private final List<CssRule> rules;

	public CSSStyleSheetImpl(List<CssRule> rules) {
		this.rules = List.copyOf(rules);
	}

	public List<CssRule> getRules() {
		return rules;
	}
}
