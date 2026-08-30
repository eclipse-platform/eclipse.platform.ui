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

import org.eclipse.e4.ui.css.core.impl.parser.CssParseException;

/**
 * A parsed stylesheet: an ordered list of {@link CssRule}s, plus the rules and
 * declarations the parser had to skip.
 */
public final class CSSStyleSheetImpl {

	private final List<CssRule> rules;
	private final List<CssParseException> problems;

	public CSSStyleSheetImpl(List<CssRule> rules) {
		this(rules, List.of());
	}

	public CSSStyleSheetImpl(List<CssRule> rules, List<CssParseException> problems) {
		this.rules = List.copyOf(rules);
		this.problems = List.copyOf(problems);
	}

	public List<CssRule> getRules() {
		return rules;
	}

	/**
	 * The malformed rules and declarations skipped while parsing, in source
	 * order. Empty for a sheet that parsed cleanly.
	 */
	public List<CssParseException> getProblems() {
		return problems;
	}
}
