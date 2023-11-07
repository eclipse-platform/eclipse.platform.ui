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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.tests.SearchTestPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search2.internal.ui.InternalSearchUI;

public class PositionTrackerTest {
	FileSearchQuery fQuery1;

	@ClassRule
	public static JUnitSourceSetup junitSource= new JUnitSourceSetup();

	@Rule
	public final TestWatcher temporaryPreferenceDisablement= new TestWatcher() {
		private boolean oldLineNumberRulerValue= true;
		private boolean oldQuickDiffAlwaysOnValue= true;

		@Override
		protected void starting(Description description) {
			IPreferenceStore store= EditorsPlugin.getDefault().getPreferenceStore();
			oldLineNumberRulerValue= store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER);
			oldQuickDiffAlwaysOnValue= store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON);
			store.setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, false);
			store.setValue(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON, false);
		}

		@Override
		protected void finished(Description description) {
			IPreferenceStore store= EditorsPlugin.getDefault().getPreferenceStore();
			store.setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, oldLineNumberRulerValue);
			store.setValue(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON, oldQuickDiffAlwaysOnValue);
		}

	};

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
			SearchTestPlugin.openTextEditor(SearchPlugin.getActivePage(), file);
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
			SearchTestPlugin.openTextEditor(SearchPlugin.getActivePage(), file);
			ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
			Job.getJobManager().beginRule(file, null);
			IDocument doc= fb.getDocument();
			doc.replace(0, 0, "Test");

			for (int i= 0; i < originalStarts.length; i++) {
				Position currentPosition= InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(matches[i]);
				assertThat("No position for match found: " + matches[i], currentPosition, not(is(nullValue())));
				assertEquals(originalStarts[i] + "Test".length(),currentPosition.getOffset());

			}
		} finally {
			Job.getJobManager().endRule(file);
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

}
