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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search.internal.core.text.FileNamePatternSearchScope;
import org.eclipse.search.internal.ui.text.FileSearchPage;
import org.eclipse.search.internal.ui.text.FileSearchQuery;

public class SearchResultPageTest extends TestCase {
	FileSearchQuery fQuery1;

	public SearchResultPageTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return setUpTest(new TestSuite(SearchResultPageTest.class));
	}
	
	
	public static Test setUpTest(Test test) {
		return new JUnitSourceSetup(test);
	}
	
	public static Test suite() {
		return allTests();
	}

	protected void setUp() throws Exception {
		super.setUp();
		FileNamePatternSearchScope scope= FileNamePatternSearchScope.newWorkspaceScope();
		scope.addFileNamePattern("*.java");
		scope.setIncludeDerived(false);
		fQuery1= new FileSearchQuery(scope, "", "Test");
	}

	public void atestBasicDisplay() throws Exception {
		ISearchResultViewPart view= NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
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
		for (int i= 0; i < elements.length; i++) {
			// make sure all elements in the test result are present in the viewer and have the proper count displayed
			checkElementDisplay(viewer, result, elements[i]);
		}
	}

	public void atestRemoveTreeMatches() throws Exception {
		ISearchResultViewPart view= NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		FileSearchPage page= (FileSearchPage) view.getActivePage();
		page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		AbstractTreeViewer viewer= (AbstractTreeViewer) page.getViewer();
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		// make sure all elements have items.
		viewer.expandAll();
		Object[] elements= result.getElements();
		//page.setUpdateTracing(true);
		for (int i= 0; i < elements.length; i++) {
			Match[] matches= result.getMatches(elements[i]);
			viewer.reveal(elements[i]);
			for (int j= 0; j < matches.length; j++) {
				checkElementDisplay(viewer, result, elements[i]);
				result.removeMatch(matches[j]);
				consumeEvents(page);
			}
		}
		//page.setUpdateTracing(false);
	}

	private void consumeEvents(FileSearchPage page) {
		IJobManager manager= Platform.getJobManager();
		while (manager.find(page).length > 0) {
			Display.getDefault().readAndDispatch();
		}
	}
	
	private void consumeEvents() {
		while (Display.getDefault().readAndDispatch()) {
		}
	}

	private void checkElementDisplay(StructuredViewer viewer, AbstractTextSearchResult result, Object element) {
		Widget widget= viewer.testFindItem(element);
		assertTrue(widget instanceof Item);
		Item item= (Item) widget;
		int itemCount= result.getMatchCount(element);
		assertTrue(itemCount > 0);
		if (itemCount > 1)
			assertTrue(item.getText().indexOf(String.valueOf(itemCount)) >= 0);
	}
	
	public void testTableNavigation() {
		ISearchResultViewPart view= NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		FileSearchPage page= (FileSearchPage) view.getActivePage();
		page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
		Table table= ((TableViewer) page.getViewer()).getTable();
		
		consumeEvents();
		
		// select the first element.
		table.setSelection(0);
		table.showSelection();
		
		consumeEvents();
		// back from first match, goto last
		page.gotoPreviousMatch();
		
		consumeEvents();
		
		assertEquals(1, table.getSelectionCount());
		assertEquals(table.getItemCount()-1, table.getSelectionIndex());

		// and forward again, to the first match.
		page.gotoNextMatch();
		
		consumeEvents();
		assertEquals(1, table.getSelectionCount());
		assertEquals(0, table.getSelectionIndex());
}
}
