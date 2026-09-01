/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An {@code @media} rule: a query list plus the style rules it guards. The
 * rules join the cascade at this rule's position while the query matches.
 */
public final class CSSMediaRuleImpl implements CssRule {

	private final List<Media.Query> queries;
	private final List<CSSStyleRuleImpl> rules;

	public CSSMediaRuleImpl(List<Media.Query> queries, List<CSSStyleRuleImpl> rules) {
		this.queries = List.copyOf(queries);
		this.rules = List.copyOf(rules);
	}

	public List<Media.Query> getQueries() {
		return queries;
	}

	public List<CSSStyleRuleImpl> getRules() {
		return rules;
	}

	public boolean matches(Media.Context context) {
		return Media.matches(queries, context);
	}

	public String getMediaText() {
		return queries.stream().map(Media.Query::text).collect(Collectors.joining(", ")); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return "@media " + getMediaText(); //$NON-NLS-1$
	}
}
