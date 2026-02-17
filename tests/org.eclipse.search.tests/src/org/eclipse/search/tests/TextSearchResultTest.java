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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.FileTextSearchScope;

import org.eclipse.search2.internal.ui.SearchView;

public class TextSearchResultTest {

	FileSearchQuery fQuery1;

	@RegisterExtension
	static JUnitSourceSetup fgJUnitSource= new JUnitSourceSetup();

	private static int fOriginalLayout= -1;

	private static boolean closeViewInTearDown;

	@BeforeAll
	public static void beforeClass() throws Exception {
		closeViewInTearDown= NewSearchUI.getSearchResultView() == null;
	}

	@BeforeEach
	public void setUp() throws Exception {
		String[] fileNamePatterns= { "*.java" };
		FileTextSearchScope scope= FileTextSearchScope.newWorkspaceScope(fileNamePatterns, false);

		fQuery1= new FileSearchQuery("Test", false, true, scope);

		NewSearchUI.runQueryInForeground(null, fQuery1);

		SearchView sV= (SearchView) NewSearchUI.getSearchResultView();
		FileSearchPage currentPage= (FileSearchPage) sV.getCurrentPage();

		if (fOriginalLayout == -1) {
			// only do it initially.. not a second time.
			fOriginalLayout= currentPage.getLayout();
		}
		currentPage.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		runEventLoopUntilEmpty();

		Object[] directElements= getDirectElements();
		if (directElements.length == 0) {
			// wait up to 5 minutes for the content provider to initialize
			long timeMillis= System.currentTimeMillis() + 1000 * 60 * 5;
			while (getDirectElements().length == 0) {
				if (System.currentTimeMillis() > timeMillis) {
					fail("Content provider did not initialize within 5 minutes");
				}
				runEventLoopUntilEmpty();
			}
		}
	}

	@AfterAll
	public static void tearDown() throws Exception {
		SearchView sV= (SearchView) NewSearchUI.getSearchResultView();
		FileSearchPage currentPage= (FileSearchPage) sV.getCurrentPage();

		currentPage.setLayout(fOriginalLayout);

		if (closeViewInTearDown) {
			sV.getSite().getPage().hideView(sV);
		}

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

		assertTrue(result.hasMatches(), "Should have matches before removal");

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

		Object[] directElements= getDirectElements();

		assertTrue(result.hasMatches(), "Should have matches before removal");
		assertEquals(1, directElements.length, "Should have only one direct element");
		assertTrue(directElements[0] instanceof IProject, "Should be a project");

		FileSearchPage currentPage= getSearchPage();
		currentPage.getViewer().setSelection(new StructuredSelection(directElements[0]));
		currentPage.internalRemoveSelected();

		assertEquals(0, result.getMatchCount(), "Correct number of matches removed");
		assertFalse(result.hasMatches(), "Should have no matches after removal");
	}

	@Test
	public void testBatchRemoveElementsByFolder() throws Exception {
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] elements= result.getElements();

		assertTrue(elements.length >= 2, "Should have at least 2 elements");
		int originalCount= result.getMatchCount();
		assertTrue(originalCount > 0, "Should have matches");

		Object[] directElements= getDirectElements();
		Object[] children= getChildren(directElements[0]);

		assertEquals(1, directElements.length, "Should have one direct element");
		assertEquals(1, children.length, "Should have one child element");
		assertTrue(children[0] instanceof IFolder, "Should be a folder");

		FileSearchPage currentPage= getSearchPage();
		currentPage.getViewer().setSelection(new StructuredSelection(children[0]));
		currentPage.internalRemoveSelected();

		assertEquals(0, result.getMatchCount(), "Correct number of matches removed");
		assertFalse(result.hasMatches(), "Should have no matches after removal");
	}

	@Test
	public void testBatchRemoveElementsByProjectWithElementLimit() throws Exception {
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] allElements= result.getElements();
		assertTrue(allElements.length >= 2, "Should have at least 2 elements in the model");
		int originalCount= result.getMatchCount();
		assertTrue(originalCount > 0, "Should have matches before applying element limit");
		FileSearchPage currentPage= getSearchPage();
		currentPage.setElementLimit(Integer.valueOf(1));
		runEventLoopUntilEmpty();
		Object[] limitedDirectElements= getDirectElements();
		assertEquals(1, limitedDirectElements.length, "Element limit should restrict visible elements to 1");
		assertTrue(allElements.length > limitedDirectElements.length, "Some elements should be hidden by the element limit");
		assertTrue(result.hasMatches(), "Model should still have matches before removal");
		assertTrue(limitedDirectElements[0] instanceof IProject, "Visible element should be a project");
		currentPage.getViewer().setSelection(new StructuredSelection(limitedDirectElements[0]));
		currentPage.internalRemoveSelected();
		assertEquals(0, result.getMatchCount(), "All matches, including hidden ones, should be removed");
		assertFalse(result.hasMatches(), "Model should have no matches after removal");
	}

	private static FileSearchPage getSearchPage() {
		return (FileSearchPage) ((SearchView) NewSearchUI.getSearchResultView()).getCurrentPage();
	}

	private static ITreeContentProvider getContentProvider() {
		StructuredViewer resViewer= getSearchPage().getViewer();
		return (ITreeContentProvider) resViewer.getContentProvider();
	}

	private static Object[] getDirectElements() {
		ITreeContentProvider treeContentProvider= getContentProvider();
		return treeContentProvider.getElements(getSearchPage().getViewer().getInput());
	}

	private static Object[] getChildren(Object parent) {
		ITreeContentProvider treeContentProvider= getContentProvider();
		return treeContentProvider.getChildren(parent);
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
