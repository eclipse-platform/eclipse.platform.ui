/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.) - extract from QuickAccessElement
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * QuickAccessMatch contains the logic to check whether a given
 * {@link QuickAccessElement} matches a input user request.
 *
 * @noreference This class is not intended to be referenced by clients.
 */
public final class QuickAccessMatcher {

	private final QuickAccessElement element;

	public QuickAccessMatcher(QuickAccessElement element) {
		this.element = element;
	}

	private static final int[][] EMPTY_INDICES = new int[0][0];

	// whitespaces filter and patterns
	private String wsFilter;
	private Pattern wsPattern;

	private Pattern getWhitespacesPattern(String filter) {
		if (wsPattern == null || !filter.equals(wsFilter)) {
			wsFilter = filter;
			wsPattern = QuickAccessMatching.whitespacesPattern(filter);
		}
		return wsPattern;
	}

	// wildcard filter and patterns
	private String wcFilter;
	private Pattern wcPattern;

	private Pattern getWildcardsPattern(String filter) {
		String squashed = filter.replaceAll("\\*+", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		if (wcPattern == null || !squashed.equals(wcFilter)) {
			wcFilter = squashed;
			wcPattern = QuickAccessMatching.wildcardsPattern(squashed);
		}
		return wcPattern;
	}

	/**
	 * If this element is a match (partial, complete, camel case, etc) to the given
	 * filter, returns a {@link QuickAccessEntry}. Otherwise returns
	 * <code>null</code>;
	 *
	 * @param filter              filter for matching
	 * @param providerForMatching the provider that will own the entry
	 * @return a quick access entry or <code>null</code>
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public QuickAccessEntry match(String filter, QuickAccessProvider providerForMatching) {
		String matchLabel = element.getMatchLabel();
		String label = element.getLabel();
		int quality = QuickAccessMatching.substringMatchQuality(matchLabel, label, filter);
		if (quality != -1) {
			if (quality == QuickAccessEntry.MATCH_PARTIAL) {
				return new QuickAccessEntry(element, providerForMatching, EMPTY_INDICES, EMPTY_INDICES, quality);
			}
			int index = label.toLowerCase().indexOf(filter);
			return new QuickAccessEntry(element, providerForMatching,
					new int[][] { { index, index + filter.length() - 1 } }, EMPTY_INDICES, quality);
		}
		//
		Pattern p;
		if (filter.contains("*") || filter.contains("?")) { //$NON-NLS-1$ //$NON-NLS-2$
			// check for wildcards
			p = getWildcardsPattern(filter);
		} else {
			// check for whitespaces
			p = getWhitespacesPattern(filter);
		}
		Matcher m = p.matcher(matchLabel);
		// if matches, return an entry
		if (m.matches()) {
			// and highlight match on the label only
			if (!matchLabel.equals(label)) {
				m = p.matcher(label);
				if (!m.matches()) {
					return new QuickAccessEntry(element, providerForMatching, EMPTY_INDICES, EMPTY_INDICES,
							QuickAccessEntry.MATCH_GOOD);
				}
			}
			int groupCount = m.groupCount();
			int[][] indices = new int[groupCount][];
			for (int i = 0; i < groupCount; i++) {
				int nGrp = i + 1;
				// capturing group
				indices[i] = new int[] { m.start(nGrp), m.end(nGrp) - 1 };
			}
			return new QuickAccessEntry(element, providerForMatching, indices, EMPTY_INDICES,
					QuickAccessEntry.MATCH_EXCELLENT);
		}
		//
		String combinedMatchLabel = providerForMatching.getName() + " " + matchLabel; //$NON-NLS-1$
		String combinedLabel = providerForMatching.getName() + " " + label; //$NON-NLS-1$
		int index = combinedMatchLabel.toLowerCase().indexOf(filter);
		if (index != -1) { // match
			index = combinedLabel.toLowerCase().indexOf(filter);
			if (index != -1) { // compute highlight on label
				int lengthOfElementMatch = index + filter.length() - providerForMatching.getName().length() - 1;
				if (lengthOfElementMatch > 0) {
					return new QuickAccessEntry(element, providerForMatching,
							new int[][] { { 0, lengthOfElementMatch - 1 } },
							new int[][] { { index, index + filter.length() - 1 } }, QuickAccessEntry.MATCH_GOOD);
				}
				return new QuickAccessEntry(element, providerForMatching, EMPTY_INDICES,
						new int[][] { { index, index + filter.length() - 1 } }, QuickAccessEntry.MATCH_GOOD);
			}
			return new QuickAccessEntry(element, providerForMatching, EMPTY_INDICES, EMPTY_INDICES,
					QuickAccessEntry.MATCH_PARTIAL);
		}
		//
		String camelCase = CamelUtil.getCamelCase(label); // use actual label for camelcase
		index = camelCase.indexOf(filter);
		if (index != -1) {
			int[][] indices = CamelUtil.getCamelCaseIndices(matchLabel, index, filter.length());
			return new QuickAccessEntry(element, providerForMatching, indices, EMPTY_INDICES,
					QuickAccessEntry.MATCH_GOOD);
		}
		String combinedCamelCase = CamelUtil.getCamelCase(combinedLabel);
		index = combinedCamelCase.indexOf(filter);
		if (index != -1) {
			String providerCamelCase = CamelUtil.getCamelCase(providerForMatching.getName());
			int lengthOfElementMatch = index + filter.length() - providerCamelCase.length();
			if (lengthOfElementMatch > 0) {
				return new QuickAccessEntry(element, providerForMatching,
						CamelUtil.getCamelCaseIndices(matchLabel, 0, lengthOfElementMatch),
						CamelUtil.getCamelCaseIndices(providerForMatching.getName(), index,
								filter.length() - lengthOfElementMatch),
						QuickAccessEntry.MATCH_GOOD);
			}
			return new QuickAccessEntry(element, providerForMatching, EMPTY_INDICES,
					CamelUtil.getCamelCaseIndices(providerForMatching.getName(), index, filter.length()),
					QuickAccessEntry.MATCH_GOOD);
		}
		return null;
	}
}
