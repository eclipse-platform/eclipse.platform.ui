/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search.internal.core.text.FileNamePatternSearchScope;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;

import org.eclipse.search2.internal.ui.InternalSearchUI;

public class PositionTrackerTest extends TestCase {
	FileSearchQuery fQuery1;

	public PositionTrackerTest(String name) {
		super(name);
	}
		
	public static Test allTests() {
		return setUpTest(new TestSuite(PositionTrackerTest.class));
	}
	
	public static Test suite() {
		return allTests();
	}

	public static Test setUpTest(Test test) {
		return new JUnitSourceSetup(test);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		FileNamePatternSearchScope scope= FileNamePatternSearchScope.newWorkspaceScope(false);
		scope.addFileNamePattern("*.java");
		fQuery1= new FileSearchQuery(scope,  "", "Test");
	}
	
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
			IDE.openEditor(SearchPlugin.getActivePage(), file);
			ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath());
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
			IDE.openEditor(SearchPlugin.getActivePage(), file);
			ITextFileBuffer fb= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getLocation());
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
