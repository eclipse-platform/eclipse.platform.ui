/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.ui.PartInitException;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.tests.SearchTestUtil;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search2.internal.ui.InternalSearchUI;

public class PositionTrackerTest {
	FileSearchQuery fQuery1;

	@RegisterExtension
	static JUnitSourceSetup fgJUnitSource= new JUnitSourceSetup();

	public static JUnitSourceSetup junitSource= new JUnitSourceSetup();

	private IFile fManyMatchesFile;

	@BeforeEach
	public void setUp() throws Exception {
		String[] fileNamePatterns= { "*.java" };
		FileTextSearchScope scope= FileTextSearchScope.newWorkspaceScope(fileNamePatterns, false);

		fQuery1= new FileSearchQuery("Test", false, true, scope);
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("JUnitTest");
		if (!project.exists()) {
			project.create(null);
		}
		if (!project.isOpen()) {
			project.open(null);
		}
		fManyMatchesFile = project.getFile("ManyMatches.txt");
		if (!fManyMatchesFile.exists()) {
			List<String> lines = new ArrayList<>();
			for (int i = 0; i < 100; i++) {
				lines.add("Test Test Test Test Test Test Test Test Test Test");
			}
			Files.write(fManyMatchesFile.getLocation().toFile().toPath(), lines);
			fManyMatchesFile.refreshLocal(IResource.DEPTH_ZERO, null);
		}
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (fManyMatchesFile != null && fManyMatchesFile.exists()) {
			fManyMatchesFile.delete(true, null);
		}
	}


	@Test
	public void testInsertAt0() throws Exception {
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] elements= result.getElements();
		try {
			for (Object element : elements) {
				if (element instanceof IFile)
					checkInsertAtZero(result, (IFile) element);
			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

	@Test
	public void testInsertInsideMatch() throws Exception {
		assumeFalse(Util.isMac(), "test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/882");
		NewSearchUI.runQueryInForeground(null, fQuery1);
		FileSearchResult result= (FileSearchResult) fQuery1.getSearchResult();
		Object[] elements= result.getElements();
		try {
			for (Object element : elements) {
				if (element instanceof IFile)
					checkInsertInsideMatch(result, (IFile) element);
			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}


	private void checkInsertInsideMatch(FileSearchResult result, IFile file) throws PartInitException, BadLocationException {
		Match[] matches= result.getMatches(file);
		try {
			SearchTestUtil.openTextEditor(SearchPlugin.getActivePage(), file);
			ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
			Job.getJobManager().beginRule(file, null);
			IDocument doc= fb.getDocument();

			for (Match matche : matches) {
				assertNotNull(matche, "null match for file: " + file);
				Position currentPosition = InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(matche);
				assertNotNull(currentPosition, "null position for match: " + matche);
				doc.replace(currentPosition.offset + 1, 0, "Test");
			}
			for (Match matche : matches) {
				Position currentPosition = InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(matche);
				assertNotNull(currentPosition, "null position for match: " + matche);
				String text= doc.get(currentPosition.offset, currentPosition.length);
				StringBuilder buf= new StringBuilder();
				buf.append(text.charAt(0));
				buf.append(text.substring(5));
				assertEquals(buf.toString(), ((FileSearchQuery) result.getQuery()).getSearchString());
			}
		} finally {
			Job.getJobManager().endRule(file);
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
}

	private void checkInsertAtZero(AbstractTextSearchResult result, IFile file) throws PartInitException, BadLocationException {
		Match[] matches= result.getMatches(file);
		int[] originalStarts= new int[matches.length];
		for (int i = 0; i < originalStarts.length; i++) {
			originalStarts[i]= matches[i].getOffset();
		}
		try {
			SearchTestUtil.openTextEditor(SearchPlugin.getActivePage(), file);
			ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
			Job.getJobManager().beginRule(file, null);
			IDocument doc= fb.getDocument();
			doc.replace(0, 0, "Test");

			for (int i= 0; i < originalStarts.length; i++) {
				Position currentPosition= InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(matches[i]);
				assertNotNull(currentPosition, "null position for match: " + matches[i]);
				assertEquals(originalStarts[i] + "Test".length(), currentPosition.getOffset());

			}
		} finally {
			Job.getJobManager().endRule(file);
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

	@Test
	public void testSearchResultConsistency() throws Exception {
		String[] txtPatterns = { "*.txt" };
		FileTextSearchScope txtScope = FileTextSearchScope.newWorkspaceScope(txtPatterns, false);
		FileSearchQuery txtQuery = new FileSearchQuery("Test", false, true, txtScope);

		NewSearchUI.runQueryInForeground(null, txtQuery);
		AbstractTextSearchResult result = (AbstractTextSearchResult) txtQuery.getSearchResult();
		Match[] matches = result.getMatches(fManyMatchesFile);
		assertTrue(matches.length >= 10, "Expected many matches in ManyMatches.txt, got " + matches.length);

		try {
			SearchTestUtil.openTextEditor(SearchPlugin.getActivePage(), fManyMatchesFile);
			ITextFileBuffer fb = FileBuffers.getTextFileBufferManager().getTextFileBuffer(fManyMatchesFile.getFullPath(), LocationKind.IFILE);
			Job.getJobManager().beginRule(fManyMatchesFile, null);
			try {
				IDocument doc = fb.getDocument();

				// Shift all matches
				doc.replace(0, 0, "Shift all matches");

				// Trigger dirtyStateChanged to update matches
				InternalSearchUI.getInstance().getPositionTracker().dirtyStateChanged(fb, false);

				// Verify we can still remove all matches.
				for (Match match : matches) {
					int countBefore = result.getMatchCount(fManyMatchesFile);
					result.removeMatch(match);
					assertEquals(countBefore - 1, result.getMatchCount(fManyMatchesFile), "Match " + match + " was NOT removed! ConcurrentSkipListSet might be corrupted.");
				}
				assertEquals(0, result.getMatchCount(fManyMatchesFile));
			} finally {
				Job.getJobManager().endRule(fManyMatchesFile);
			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

}
