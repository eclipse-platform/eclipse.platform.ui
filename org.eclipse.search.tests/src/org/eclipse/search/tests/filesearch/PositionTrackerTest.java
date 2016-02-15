/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.ui.PartInitException;

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
			for (int i = 0; i < elements.length; i++) {
				checkInsertAtZero(result, (IFile) elements[i]);
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
			for (int i = 0; i < elements.length; i++) {
				checkInsertInsideMatch(result, (IFile) elements[i]);
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
			IDocument doc= fb.getDocument();

			for (int i= 0; i < matches.length; i++) {
				Position currentPosition= InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(matches[i]);
				assertNotNull(currentPosition);
				doc.replace(currentPosition.offset + 1, 0, "Test");
			}

			for (int i= 0; i < matches.length; i++) {
				Position currentPosition= InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(matches[i]);
				assertNotNull(currentPosition);
				String text= doc.get(currentPosition.offset, currentPosition.length);
				StringBuffer buf= new StringBuffer();
				buf.append(text.charAt(0));
				buf.append(text.substring(5));
				assertEquals(buf.toString(), ((FileSearchQuery) result.getQuery()).getSearchString());
			}
		} finally {
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
			IDocument doc= fb.getDocument();
			doc.replace(0, 0, "Test");

			for (int i= 0; i < originalStarts.length; i++) {
				Position currentPosition= InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(matches[i]);
				assertNotNull(currentPosition);
				assertEquals(originalStarts[i] + "Test".length(), currentPosition.getOffset());

			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}
	
}
