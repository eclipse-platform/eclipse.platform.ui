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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFile;

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

	@ClassRule
	public static JUnitSourceSetup junitSource= new JUnitSourceSetup();

	@Before
	public void setUp() throws Exception {
		String[] fileNamePatterns= { "*.java" };
		FileTextSearchScope scope= FileTextSearchScope.newWorkspaceScope(fileNamePatterns, false);

		fQuery1= new FileSearchQuery("Test", false, true, scope);
	}


	@Test
	public void testInsertAt0() throws Exception {
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] elements= result.getElements();
		try {
			for (Object element : elements) {
				checkInsertAtZero(result, (IFile) element);
			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

	@Test
	public void testInsertInsideMatch() throws Exception {
		assumeFalse("test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/882", Util.isMac());
		NewSearchUI.runQueryInForeground(null, fQuery1);
		FileSearchResult result= (FileSearchResult) fQuery1.getSearchResult();
		Object[] elements= result.getElements();
		try {
			for (Object element : elements) {
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
				assertNotNull("null match for file: " + file, matche);
				Position currentPosition = InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(matche);
				assertNotNull("null position for match: " + matche, currentPosition);
				doc.replace(currentPosition.offset + 1, 0, "Test");
			}
			for (Match matche : matches) {
				Position currentPosition = InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(matche);
				assertNotNull("null position for match: " + matche, currentPosition);
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
				assertNotNull("null position for match: " + matches[i], currentPosition);
				assertEquals(originalStarts[i] + "Test".length(), currentPosition.getOffset());

			}
		} finally {
			Job.getJobManager().endRule(file);
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

}
