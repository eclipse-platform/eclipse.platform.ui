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

package org.eclipse.ui.tests.quickaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.ui.internal.quickaccess.QuickAccessEntry;
import org.eclipse.ui.internal.quickaccess.QuickAccessMatching;
import org.junit.jupiter.api.Test;

/**
 * Pure unit tests for the matching helpers extracted from
 * {@link org.eclipse.ui.internal.quickaccess.QuickAccessMatcher}. These run
 * without a workbench harness, so they are fast and not flake-prone.
 */
public class QuickAccessMatchingTest {

	@Test
	public void substringQualityExactMatchIsPerfect() {
		assertEquals(QuickAccessEntry.MATCH_PERFECT,
				QuickAccessMatching.substringMatchQuality("Rename", "Rename", "rename"));
	}

	@Test
	public void substringQualityPrefixIsExcellent() {
		assertEquals(QuickAccessEntry.MATCH_EXCELLENT,
				QuickAccessMatching.substringMatchQuality("Rename Resource", "Rename Resource", "rename"));
	}

	@Test
	public void substringQualityMiddleIsGood() {
		assertEquals(QuickAccessEntry.MATCH_GOOD,
				QuickAccessMatching.substringMatchQuality("Find and Replace", "Find and Replace", "replace"));
	}

	@Test
	public void substringQualityPartialWhenOnlyMatchLabelHits() {
		// filter hits the match label but not the visible label -> partial
		assertEquals(QuickAccessEntry.MATCH_PARTIAL,
				QuickAccessMatching.substringMatchQuality("Rename Resource (keyword)", "Rename Resource", "keyword"));
	}

	@Test
	public void substringQualityReturnsMinusOneOnMiss() {
		assertEquals(-1, QuickAccessMatching.substringMatchQuality("Rename", "Rename", "xyzzy"));
	}

	@Test
	public void whitespacesPatternSplitsOnWhitespace() {
		Pattern p = QuickAccessMatching.whitespacesPattern("text white");
		assertTrue(p.matcher("Text Editors: whitespace options").matches());
		assertFalse(p.matcher("Unrelated entry").matches());
	}

	@Test
	public void whitespacesPatternIsCaseInsensitive() {
		Pattern p = QuickAccessMatching.whitespacesPattern("rename");
		assertTrue(p.matcher("RENAME RESOURCE").matches());
	}

	@Test
	public void wildcardsPatternHandlesStar() {
		Pattern p = QuickAccessMatching.wildcardsPattern("re*ce");
		assertTrue(p.matcher("Rename Resource").matches());
		assertFalse(p.matcher("Delete").matches());
	}

	@Test
	public void wildcardsPatternHandlesSingleQuestionMark() {
		Pattern p = QuickAccessMatching.wildcardsPattern("te?t");
		assertTrue(p.matcher("test").matches());
		assertTrue(p.matcher("text").matches());
	}

	@Test
	public void wildcardsPatternSquashesConsecutiveStars() {
		Pattern a = QuickAccessMatching.wildcardsPattern("re***ce");
		Pattern b = QuickAccessMatching.wildcardsPattern("re*ce");
		// both should treat the input the same way
		assertEquals(b.matcher("Rename Resource").matches(), a.matcher("Rename Resource").matches());
		assertTrue(a.matcher("Rename Resource").matches());
	}

	@Test
	public void safeCompileReturnsNonMatchingPatternForInvalidRegex() {
		// "[" is an unterminated character class -> PatternSyntaxException
		Pattern p = QuickAccessMatching.safeCompile("[");
		assertNotNull(p);
		assertFalse(p.matcher("any text").matches());
		assertFalse(p.matcher("").matches());
	}

	@Test
	public void safeCompileCompilesValidRegex() {
		Pattern p = QuickAccessMatching.safeCompile("foo.*");
		assertTrue(p.matcher("foobar").matches());
	}

	@Test
	public void scoreReturnsNoneWhenNotSubsequence() {
		assertEquals(QuickAccessMatching.SCORE_NONE, QuickAccessMatching.score("Rename", "xyz"));
	}

	@Test
	public void scoreReturnsZeroForEmptyFilter() {
		assertEquals(0, QuickAccessMatching.score("Rename", ""));
	}

	@Test
	public void scorePrefersConsecutiveOverScattered() {
		int consecutive = QuickAccessMatching.score("abcxyz", "abc");
		int scattered = QuickAccessMatching.score("axbxcx", "abc");
		assertTrue(consecutive > scattered, "consecutive " + consecutive + " should beat scattered " + scattered);
	}

	@Test
	public void scorePrefersPrefixOverMidWord() {
		int prefix = QuickAccessMatching.score("Rename", "re");
		int midWord = QuickAccessMatching.score("Score", "re");
		assertTrue(prefix > midWord, "prefix " + prefix + " should beat mid-word " + midWord);
	}

	@Test
	public void scorePrefersShorterOnOtherwiseEqualMatch() {
		int shorter = QuickAccessMatching.score("Re", "re");
		int longer = QuickAccessMatching.score("Renew", "re");
		assertTrue(shorter > longer, "shorter " + shorter + " should beat longer " + longer);
	}

	@Test
	public void scoreRewardsWordBoundaryInitials() {
		int initials = QuickAccessMatching.score("New File", "nf");
		int midWord = QuickAccessMatching.score("Confirm", "nf");
		assertTrue(initials > midWord, "word-initial " + initials + " should beat mid-word " + midWord);
	}

	@Test
	public void scoreIgnoresWildcardChars() {
		assertTrue(QuickAccessMatching.score("Rename Resource", "re*ce") > QuickAccessMatching.SCORE_NONE);
	}

	@Test
	public void scoreIsCaseInsensitive() {
		assertTrue(QuickAccessMatching.score("RENAME", "rename") > QuickAccessMatching.SCORE_NONE);
	}

	/** Groups items by their prefix up to the first digit, e.g. "file1" -> "file". */
	private static final Function<String, Object> BY_PREFIX = s -> s.replaceAll("\\d", "");

	@Test
	public void pickFairlyReturnsAllWhenEnoughSlots() {
		List<String> ranked = List.of("file1", "file2", "view1");
		assertEquals(ranked, QuickAccessMatching.pickFairly(ranked, BY_PREFIX, 5));
	}

	@Test
	public void pickFairlyCapsFloodingGroup() {
		// files dominate the top of the relevance ranking, see issue #4155
		List<String> ranked = List.of("file1", "file2", "file3", "file4", "file5", "view1", "command1");
		List<String> winners = QuickAccessMatching.pickFairly(ranked, BY_PREFIX, 4);
		assertEquals(4, winners.size());
		assertTrue(winners.contains("view1"), "flooded-out view should win a slot: " + winners);
		assertTrue(winners.contains("command1"), "flooded-out command should win a slot: " + winners);
	}

	@Test
	public void pickFairlyGivesUnusedShareToBestRemaining() {
		List<String> ranked = List.of("file1", "file2", "file3", "file4", "view1");
		// 2 groups, 4 slots -> fair share is 2 each; the view only needs 1,
		// so the leftover slot goes to the next best file
		assertEquals(List.of("file1", "file2", "view1", "file3"),
				QuickAccessMatching.pickFairly(ranked, BY_PREFIX, 4));
	}

	@Test
	public void pickFairlyKeepsRelevanceOrderWithinTheCap() {
		List<String> ranked = List.of("view1", "file1", "file2", "file3", "command1");
		List<String> winners = QuickAccessMatching.pickFairly(ranked, BY_PREFIX, 3);
		assertEquals(List.of("view1", "file1", "command1"), winners);
	}

	@Test
	public void pickFairlyHandlesZeroSlots() {
		List<String> ranked = List.of("file1", "file2");
		assertTrue(QuickAccessMatching.pickFairly(ranked, BY_PREFIX, 0).isEmpty());
	}
}
