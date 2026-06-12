/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.regex.Pattern;

/**
 * Pure, side-effect-free helpers used by {@link QuickAccessMatcher}. Extracted
 * so the matching/ranking rules can be unit-tested without a workbench harness.
 *
 * @noreference This class is not intended to be referenced by clients.
 */
public final class QuickAccessMatching {

	private static final String WS_WILD_START = "^\\s*(\\*|\\?)*"; //$NON-NLS-1$
	private static final String WS_WILD_END = "(\\*|\\?)*\\s*$"; //$NON-NLS-1$
	private static final String ANY_WS = "\\s+"; //$NON-NLS-1$
	private static final String EMPTY_STR = ""; //$NON-NLS-1$
	private static final String PAR_START = "\\("; //$NON-NLS-1$
	private static final String PAR_END = "\\)"; //$NON-NLS-1$
	private static final String ONE_CHAR = ".?"; //$NON-NLS-1$

	private QuickAccessMatching() {
	}

	/**
	 * Build a regex {@link Pattern} that treats every run of whitespace in the
	 * filter as a wildcard boundary. "text white" becomes {@code .*(text).*(white).*}.
	 */
	public static Pattern whitespacesPattern(String filter) {
		String sFilter = filter.replaceFirst(WS_WILD_START, EMPTY_STR).replaceFirst(WS_WILD_END, EMPTY_STR)
				.replaceAll(PAR_START, ONE_CHAR).replaceAll(PAR_END, ONE_CHAR);
		sFilter = String.format(".*(%s).*", sFilter.replaceAll(ANY_WS, ").*(")); //$NON-NLS-1$ //$NON-NLS-2$
		return safeCompile(sFilter);
	}

	/**
	 * Build a regex {@link Pattern} that honours {@code *} and {@code ?} wildcards
	 * in the filter. Consecutive {@code *} are squashed; runs of {@code ?} become a
	 * bounded length match.
	 */
	public static Pattern wildcardsPattern(String filter) {
		filter = filter.replaceAll("\\*+", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		String sFilter = filter.replaceFirst(WS_WILD_START, EMPTY_STR).replaceFirst(WS_WILD_END, EMPTY_STR)
				.replaceAll(PAR_START, ONE_CHAR).replaceAll(PAR_END, ONE_CHAR);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sFilter.length(); i++) {
			char c = sFilter.charAt(i);
			if (c == '*') {
				sb.append(").").append(c).append("("); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (c == '?') {
				int n = 1;
				for (; (i + 1) < sFilter.length(); i++) {
					if (sFilter.charAt(i + 1) != '?') {
						break;
					}
					n++;
				}
				sb.append(").").append(n == 1 ? '?' : String.format("{0,%d}", n)).append("("); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				sb.append(c);
			}
		}
		sFilter = String.format(".*(%s).*", sb.toString()); //$NON-NLS-1$
		sFilter = sFilter.replace("()", EMPTY_STR); //$NON-NLS-1$
		return safeCompile(sFilter);
	}

	/**
	 * Compile a regex or fall back to a pattern that will not match anything
	 * normal. Never throws.
	 */
	public static Pattern safeCompile(String regex) {
		try {
			return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			return Pattern.compile("\\a"); //$NON-NLS-1$
		}
	}

	/**
	 * Quality for the substring-match branch of {@link QuickAccessMatcher#match}.
	 * {@code filter} must already be lower-cased by the caller (the matcher does
	 * this once per call).
	 *
	 * @return one of {@link QuickAccessEntry#MATCH_PERFECT},
	 *         {@link QuickAccessEntry#MATCH_EXCELLENT},
	 *         {@link QuickAccessEntry#MATCH_GOOD},
	 *         {@link QuickAccessEntry#MATCH_PARTIAL}, or {@code -1} if the filter
	 *         is not a substring of {@code matchLabel}.
	 */
	public static int substringMatchQuality(String matchLabel, String label, String filter) {
		String lowerMatch = matchLabel.toLowerCase();
		if (lowerMatch.indexOf(filter) == -1) {
			return -1;
		}
		if (label.toLowerCase().indexOf(filter) == -1) {
			return QuickAccessEntry.MATCH_PARTIAL;
		}
		if (lowerMatch.equals(filter)) {
			return QuickAccessEntry.MATCH_PERFECT;
		}
		if (lowerMatch.startsWith(filter)) {
			return QuickAccessEntry.MATCH_EXCELLENT;
		}
		return QuickAccessEntry.MATCH_GOOD;
	}

	/**
	 * Sentinel returned by {@link #score} when the filter is not a subsequence of
	 * the text.
	 */
	public static final int SCORE_NONE = Integer.MIN_VALUE / 2;

	private static final int MATCH_BASE = 16;
	private static final int BOUNDARY_BONUS = 30;
	private static final int CONSECUTIVE_BONUS = 15;
	private static final int PREFIX_BONUS = 8;
	private static final int LEADING_GAP_PENALTY = 3;
	private static final int MAX_LEADING_GAP = 5;
	private static final int MAX_LENGTH_PENALTY = 20;

	/**
	 * Continuous relevance score for ranking; higher is better. Rewards matches at
	 * word boundaries, consecutive characters and a prefix, and lightly penalises
	 * leading gaps and longer text. Wildcard and whitespace characters in the
	 * filter are ignored and matching is case-insensitive.
	 *
	 * @param text   the candidate text, in its original case
	 * @param filter the user filter
	 * @return the score, or {@link #SCORE_NONE} if the filter is not a subsequence
	 *         of the text
	 */
	public static int score(String text, String filter) {
		String needle = filter.toLowerCase().replaceAll("[\\s*?]", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (needle.isEmpty()) {
			return 0;
		}
		int score = 0;
		int firstMatch = -1;
		int prevMatch = -2;
		int j = 0;
		for (int i = 0; i < text.length() && j < needle.length(); i++) {
			if (Character.toLowerCase(text.charAt(i)) != needle.charAt(j)) {
				continue;
			}
			if (firstMatch < 0) {
				firstMatch = i;
			}
			int charScore = MATCH_BASE;
			if (isBoundary(text, i)) {
				charScore += BOUNDARY_BONUS;
			}
			if (prevMatch == i - 1) {
				charScore += CONSECUTIVE_BONUS;
			}
			score += charScore;
			prevMatch = i;
			j++;
		}
		if (j < needle.length()) {
			return SCORE_NONE;
		}
		if (firstMatch == 0) {
			score += PREFIX_BONUS;
		}
		score -= Math.min(firstMatch, MAX_LEADING_GAP) * LEADING_GAP_PENALTY;
		score -= Math.min(text.length(), MAX_LENGTH_PENALTY);
		return score;
	}

	private static boolean isBoundary(String text, int i) {
		if (i == 0) {
			return true;
		}
		char prev = text.charAt(i - 1);
		char cur = text.charAt(i);
		if (!Character.isLetterOrDigit(prev)) {
			return true;
		}
		if (Character.isUpperCase(cur) && !Character.isUpperCase(prev)) {
			return true;
		}
		return Character.isDigit(cur) && !Character.isDigit(prev);
	}
}
