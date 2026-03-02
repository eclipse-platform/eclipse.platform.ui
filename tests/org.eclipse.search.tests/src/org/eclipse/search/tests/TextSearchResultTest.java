/*******************************************************************************
 * Copyright (c) 2026 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mehmet Karaman (mehmet.karaman@advantest.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

import org.eclipse.search.internal.ui.text.FileSearchPage;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.tests.filesearch.JUnitSourceSetup;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;

import org.eclipse.search2.internal.ui.SearchView;

public class TextSearchResultTest {

	FileSearchQuery fQuery1;

	@RegisterExtension
	static JUnitSourceSetup fgJUnitSource= new JUnitSourceSetup();

	@BeforeEach
	public void setUp() throws Exception {
		String[] fileNamePatterns= { "*.java" };
		FileTextSearchScope scope= FileTextSearchScope.newWorkspaceScope(fileNamePatterns, false);

		fQuery1= new FileSearchQuery("Test", false, true, scope);
		NewSearchUI.runQueryInForeground(null, fQuery1);

		SearchView sV= (SearchView) NewSearchUI.getSearchResultView();
		FileSearchPage currentPage= (FileSearchPage) sV.getCurrentPage();
		currentPage.setLayout(org.eclipse.search.ui.text.AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		runEventLoopUntilEmpty();
	}

	@AfterAll
	public static void tearDown() throws Exception {
		SearchView sV= (SearchView) NewSearchUI.getSearchResultView();
		FileSearchPage currentPage= (FileSearchPage) sV.getCurrentPage();
		currentPage.setLayout(org.eclipse.search.ui.text.AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
		runEventLoopUntilEmpty();
	}

	@Test
	public void testBatchRemoveElements() throws Exception {
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] elements= result.getElements();

		assertTrue(elements.length >= 2, "Should have at least 2 elements");
		int originalCount= result.getMatchCount();
		assertTrue(originalCount > 0, "Should have matches");

		int matchCountElement0= result.getMatchCount(elements[0]);
		int matchCountElement1= result.getMatchCount(elements[1]);

		List<Object> toRemove= Arrays.asList(elements[0], elements[1]);
		result.removeElements(toRemove);

		int expectedCount= originalCount - matchCountElement0 - matchCountElement1;
		assertEquals(expectedCount, result.getMatchCount(), "Correct number of matches removed");

		assertFalse(result.hasMatches(elements[0]), "First element should have no matches");
		assertFalse(result.hasMatches(elements[1]), "Second element should have no matches");
	}


	@Test
	public void testBatchRemoveElementsByProject() throws Exception {
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] elements= result.getElements();

		assertTrue(elements.length >= 2, "Should have at least 2 elements");
		int originalCount= result.getMatchCount();
		assertTrue(originalCount > 0, "Should have matches");

		SearchView sV= (SearchView) NewSearchUI.getSearchResultView();
		FileSearchPage currentPage= (FileSearchPage) sV.getCurrentPage();

		StructuredViewer resViewer= currentPage.getViewer();
		ITreeContentProvider contentProvider= (ITreeContentProvider) resViewer.getContentProvider();
		Object[] directElements= contentProvider.getElements(resViewer.getInput());

		assertTrue(directElements.length == 1, "Should have only one direct element");
		assertTrue(directElements[0] instanceof IProject, "Should be a project");

		resViewer.setSelection(new StructuredSelection(directElements[0]));

		currentPage.internalRemoveSelected();

		assertEquals(0, result.getMatchCount(), "Correct number of matches removed");
	}

	@Test
	public void testBatchRemoveElementsByFolder() throws Exception {
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] elements= result.getElements();

		assertTrue(elements.length >= 2, "Should have at least 2 elements");
		int originalCount= result.getMatchCount();
		assertTrue(originalCount > 0, "Should have matches");

		SearchView sV= (SearchView) NewSearchUI.getSearchResultView();
		FileSearchPage currentPage= (FileSearchPage) sV.getCurrentPage();

		StructuredViewer resViewer= currentPage.getViewer();
		ITreeContentProvider treeContentProvider= (ITreeContentProvider) resViewer.getContentProvider();
		Object[] directElements= treeContentProvider.getElements(resViewer.getInput());
		Object[] children= treeContentProvider.getChildren(directElements[0]);

		assertTrue(directElements.length == 1, "Should have one direct element");
		assertTrue(children.length == 1, "Should have one child element");
		assertTrue(children[0] instanceof IFolder, "Should be a folder");

		resViewer.setSelection(new StructuredSelection(children[0]));

		currentPage.internalRemoveSelected();

		assertEquals(0, result.getMatchCount(), "Correct number of matches removed");
	}


	/*
	 * Process all pending UI events to ensure that the UI is updated before assertions are made.
	 */
	private static void runEventLoopUntilEmpty() {
		final Display display= Display.getDefault();
		if (display == null) {
			return;
		}
		if (Display.getCurrent() == display) {
			while (display.readAndDispatch()) {
			}
		} else {
			display.syncExec(() -> {
				while (display.readAndDispatch()) {
				}
			});
		}
	}
}
