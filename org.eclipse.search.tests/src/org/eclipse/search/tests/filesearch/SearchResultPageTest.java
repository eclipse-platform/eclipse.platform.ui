/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.search.tests.filesearch;

import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;

import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;

import org.eclipse.search.tests.SearchTestPlugin;

import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.text.FileSearchPage;
import org.eclipse.search.internal.ui.text.FileSearchQuery;

public class SearchResultPageTest extends TestCase {
	FileSearchQuery fQuery1;

	public SearchResultPageTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		ZipFile zip= new ZipFile(SearchTestPlugin.getDefault().getFileInPlugin(new Path("testresources/junit37-noUI-src.zip"))); //$NON-NLS-1$
		SearchTestPlugin.importFilesFromZip(zip, new Path("Test"), null); //$NON-NLS-1$
		TextSearchScope scope= TextSearchScope.newWorkspaceScope();
		scope.addExtension("*.java");
		fQuery1= new FileSearchQuery(scope, "", "Test");
	}

	public void testBasicDisplay() throws Exception {
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

	public void testRemoveTreeMatches() throws Exception {
		ISearchResultViewPart view= NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		FileSearchPage page= (FileSearchPage) view.getActivePage();
		page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		AbstractTreeViewer viewer= (AbstractTreeViewer) page.getViewer();
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		// make sure all elements have items.
		viewer.expandAll();
		Object[] elements= result.getElements();
		for (int i= 0; i < elements.length; i++) {
			Match[] matches= result.getMatches(elements[i]);
			viewer.reveal(elements[i]);
			for (int j= 0; j < matches.length; j++) {
				checkElementDisplay(viewer, result, elements[i]);
				result.removeMatch(matches[j]);
			}
		}
	}

	private void checkElementDisplay(StructuredViewer viewer, AbstractTextSearchResult result, Object element) {
		Widget widget= viewer.testFindItem(element);
		assertTrue(widget instanceof Item);
		Item item= (Item) widget;
		int itemCount= result.getMatchCount(element);
		assertTrue(itemCount > 0);
		assertTrue(item.getText().indexOf(String.valueOf(itemCount)) >= 0);
	}
}