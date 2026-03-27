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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.search.internal.ui.text.FileSearchPage;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.tests.SearchTestUtil;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;

public class SearchResultPageTest {
	FileSearchQuery fQuery1;

	@RegisterExtension
	static JUnitSourceSetup fgJUnitSource= new JUnitSourceSetup();

	@BeforeEach
	public void setUp() throws Exception {
		SearchTestUtil.ensureWelcomePageClosed();
		String[] fileNamePatterns= { "*.java" };
		FileTextSearchScope scope= FileTextSearchScope.newWorkspaceScope(fileNamePatterns, false);

		fQuery1= new FileSearchQuery("Test", false, true, scope);
	}

	@Test
	@Disabled
	public void testBasicDisplay() throws Exception {
		NewSearchUI.runQueryInForeground(null, fQuery1);
		ISearchResultViewPart view= NewSearchUI.getSearchResultView();
		FileSearchPage page= (FileSearchPage) view.getActivePage();
		page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		checkViewerDisplay(page);
		page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
		checkViewerDisplay(page);
	}

	private void checkViewerDisplay(FileSearchPage page) {
		StructuredViewer viewer= page.getViewer();
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		// make sure all elements have items.
		if (viewer instanceof AbstractTreeViewer)
			((AbstractTreeViewer)viewer).expandAll();
		Object[] elements= result.getElements();
		for (Object element : elements) {
			// make sure all elements in the test result are present in the viewer and have the proper count displayed
			checkElementDisplay(viewer, result, element);
		}
	}


	@Test
	@Disabled // checkElementDisplay(..) misses cases where one line contains multiple matches
	public void testRemoveTreeMatches() throws Exception {
		NewSearchUI.runQueryInForeground(null, fQuery1);
		ISearchResultViewPart view= NewSearchUI.getSearchResultView();
		FileSearchPage page= (FileSearchPage) view.getActivePage();
		page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		AbstractTreeViewer viewer= (AbstractTreeViewer) page.getViewer();
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		// make sure all elements have items.
		viewer.expandAll();
		Object[] elements= result.getElements();
		//page.setUpdateTracing(true);
		for (Object element : elements) {
			Match[] matches = result.getMatches(element);
			viewer.reveal(element);
			for (Match matche : matches) {
				checkElementDisplay(viewer, result, element);
				result.removeMatch(matche);
				consumeEvents(page);
			}
		}
		//page.setUpdateTracing(false);
	}

	private void consumeEvents(FileSearchPage page) {
		IJobManager manager= Job.getJobManager();
		while (manager.find(page).length > 0) {
			consumeEvents();
		}
		consumeEvents();
	}

	private void consumeEvents() {
		while (Display.getDefault().readAndDispatch()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
		}
	}

	private void checkElementDisplay(StructuredViewer viewer, AbstractTextSearchResult result, Object element) {
		Widget widget= viewer.testFindItem(element);
		assertTrue(widget instanceof Item);
		Item item= (Item) widget;
		int itemCount= result.getMatchCount(element);
		assertTrue(itemCount > 0);
		if (itemCount > 1)
			assertTrue(item.getText().contains(String.valueOf(itemCount)));
	}

	@Test
	public void testTableNavigation() {
		NewSearchUI.runQueryInForeground(null, fQuery1);
		ISearchResultViewPart view= NewSearchUI.getSearchResultView();
		FileSearchPage page= (FileSearchPage) view.getActivePage();
		page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
		Table table= ((TableViewer) page.getViewer()).getTable();

		consumeEvents(page);

		// select the first element.
		table.setSelection(0);
		consumeEvents(page);
		table.showSelection();

		consumeEvents(page);
		// back from first match, goto last
		page.gotoPreviousMatch();

		consumeEvents(page);

		assertEquals(1, table.getSelectionCount(), "Selection count is wrong");
		assertEquals(table.getItemCount() - 1, table.getSelectionIndex(), "Selection index is wrong");

		// and forward again, to the first match.
		page.gotoNextMatch();

		consumeEvents(page);
		assertEquals(1, table.getSelectionCount(), "Selection count is wrong");
		assertEquals(0, table.getSelectionIndex(), "Selection index is wrong");
}
}
