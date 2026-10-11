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
		if (wcPattern == null || !filter.equals(wcFilter)) {
			wcFilter = filter;
			wcPattern = QuickAccessMatching.wildcardsPattern(filter);
		}
		return wcPattern;
	}

	// needle for scoring, derived from the filter
	private String needleFilter;
	private String needle;

	private String getNeedle(String filter) {
		if (needle == null || !filter.equals(needleFilter)) {
			needleFilter = filter;
			needle = QuickAccessMatching.needle(filter);
		}
		return needle;
	}

	/**
	 * Returns a {@link QuickAccessEntry} carrying highlight regions and a relevance
	 * score if {@code element} matches the filter, or <code>null</code> otherwise.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public QuickAccessEntry match(QuickAccessElement element, String filter, QuickAccessProvider providerForMatching) {
		QuickAccessEntry entry = doMatch(element, filter, providerForMatching);
		if (entry != null) {
			entry.matchScore = computeScore(element, getNeedle(filter), providerForMatching);
		}
		return entry;
	}

	private static int computeScore(QuickAccessElement element, String needle, QuickAccessProvider provider) {
		int score = QuickAccessMatching.scoreNeedle(element.getMatchLabel(), needle);
		if (score == QuickAccessMatching.SCORE_NONE) {
			score = QuickAccessMatching.scoreNeedle(provider.getName() + ' ' + element.getMatchLabel(), needle);
		}
		return score == QuickAccessMatching.SCORE_NONE ? 0 : score;
	}

	private QuickAccessEntry doMatch(QuickAccessElement element, String filter, QuickAccessProvider providerForMatching) {
		String matchLabel = element.getMatchLabel();
		String label = element.getLabel();
		boolean sameLabel = label.equals(matchLabel);
		String lowerMatchLabel = matchLabel.toLowerCase();
		String lowerLabel = sameLabel ? lowerMatchLabel : label.toLowerCase();
		int quality = QuickAccessMatching.substringMatchQualityLowered(lowerMatchLabel, lowerLabel, filter);
		if (quality != -1) {
			if (quality == QuickAccessEntry.MATCH_PARTIAL) {
				return new QuickAccessEntry(element, providerForMatching, EMPTY_INDICES, EMPTY_INDICES, quality);
			}
			int index = lowerLabel.indexOf(filter);
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
		String combinedLabel = sameLabel ? combinedMatchLabel : providerForMatching.getName() + " " + label; //$NON-NLS-1$
		String lowerCombinedMatchLabel = combinedMatchLabel.toLowerCase();
		int index = lowerCombinedMatchLabel.indexOf(filter);
		if (index != -1) { // match
			index = (sameLabel ? lowerCombinedMatchLabel : combinedLabel.toLowerCase()).indexOf(filter);
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
