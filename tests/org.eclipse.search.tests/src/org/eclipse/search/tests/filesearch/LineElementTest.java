/*******************************************************************************
 * Copyright (c) 2026 Andrey Loskutov <loskutov@gmx.de> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.LineElement;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

/**
 * Tests that {@link LineElement} distinguishes between all matches of a line and
 * the matches that are not hidden by an active match filter.
 */
public class LineElementTest {

	private static final String LINE_CONTENT= "xy and xy";

	private TestResult fResult;

	private LineElement fLine;

	private FileMatch fFirstMatch;

	private FileMatch fSecondMatch;

	private LineElement fOtherLine;

	@BeforeEach
	public void setUp() {
		// a handle is sufficient, the file doesn't need to exist
		IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(IPath.fromPortableString("/p/test.txt"));
		fLine= new LineElement(file, 1, 0, LINE_CONTENT + "\n");
		fOtherLine= new LineElement(file, 2, LINE_CONTENT.length() + 1, LINE_CONTENT);
		fFirstMatch= new FileMatch(file, 0, 2, fLine);
		fSecondMatch= new FileMatch(file, LINE_CONTENT.indexOf("xy", 1), 2, fLine);
		fResult= new TestResult();
		fResult.addMatch(fFirstMatch);
		fResult.addMatch(fSecondMatch);
		fResult.addMatch(new FileMatch(file, fOtherLine.getOffset(), 2, fOtherLine));
	}

	@Test
	public void testWithoutMatchFilters() {
		assertMatches(fLine.getMatches(fResult), fFirstMatch, fSecondMatch);
		assertMatches(fLine.getDisplayedMatches(fResult), fFirstMatch, fSecondMatch);
		assertEquals(2, fLine.getNumberOfDisplayedMatches(fResult));
		assertTrue(fLine.hasDisplayedMatches(fResult));
	}

	@Test
	public void testFilteredMatchIsNotDisplayed() {
		fResult.setActiveMatchFilters(new MatchFilter[] { new OffsetFilter(fSecondMatch.getOffset()) });

		assertMatches(fLine.getMatches(fResult), fFirstMatch, fSecondMatch);
		assertMatches(fLine.getDisplayedMatches(fResult), fFirstMatch);
		assertEquals(1, fLine.getNumberOfDisplayedMatches(fResult));
		assertTrue(fLine.hasDisplayedMatches(fResult));
	}

	@Test
	public void testLineWithAllMatchesFiltered() {
		fResult.setActiveMatchFilters(new MatchFilter[] { new LineFilter(fLine) });

		assertMatches(fLine.getMatches(fResult), fFirstMatch, fSecondMatch);
		assertMatches(fLine.getDisplayedMatches(fResult));
		assertEquals(0, fLine.getNumberOfDisplayedMatches(fResult));
		assertFalse(fLine.hasDisplayedMatches(fResult));

		// the matches of other lines are not affected
		assertEquals(1, fOtherLine.getNumberOfDisplayedMatches(fResult));
		assertTrue(fOtherLine.hasDisplayedMatches(fResult));
	}

	private static void assertMatches(FileMatch[] actual, FileMatch... expected) {
		Comparator<FileMatch> byOffset= Comparator.comparingInt(FileMatch::getOffset);
		FileMatch[] sortedActual= actual.clone();
		Arrays.sort(sortedActual, byOffset);
		FileMatch[] sortedExpected= expected.clone();
		Arrays.sort(sortedExpected, byOffset);
		assertArrayEquals(sortedExpected, sortedActual);
	}

	private static final class OffsetFilter extends TestFilter {

		private final int fOffset;

		OffsetFilter(int offset) {
			fOffset= offset;
		}

		@Override
		public boolean filters(Match match) {
			return match.getOffset() == fOffset;
		}
	}

	private static final class LineFilter extends TestFilter {

		private final LineElement fFilteredLine;

		LineFilter(LineElement line) {
			fFilteredLine= line;
		}

		@Override
		public boolean filters(Match match) {
			return ((FileMatch) match).getLineElement() == fFilteredLine;
		}
	}

	private abstract static class TestFilter extends MatchFilter {

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public String getDescription() {
			return getName();
		}

		@Override
		public String getActionLabel() {
			return getName();
		}

		@Override
		public String getID() {
			return getClass().getName();
		}
	}

	private static final class TestResult extends AbstractTextSearchResult {

		@Override
		public String getLabel() {
			return "test";
		}

		@Override
		public String getTooltip() {
			return getLabel();
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public ISearchQuery getQuery() {
			return null;
		}

		@Override
		public IEditorMatchAdapter getEditorMatchAdapter() {
			return null;
		}

		@Override
		public IFileMatchAdapter getFileMatchAdapter() {
			return null;
		}
	}
}
