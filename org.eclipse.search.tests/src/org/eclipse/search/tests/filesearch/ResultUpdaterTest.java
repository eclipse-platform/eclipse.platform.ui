/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;

public class ResultUpdaterTest extends TestCase {
	FileSearchQuery fQuery1;
	FileSearchQuery fQuery2;

	private AnnotationTypeLookup fAnnotationTypeLookup= EditorsUI.getAnnotationTypeLookup();

	public ResultUpdaterTest(String name) {
		super(name);
	}
		
	public static Test allTests() {
		return new JUnitSetup(new TestSuite(ResultUpdaterTest.class));
	}
	
	public static Test suite() {
		return allTests();
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		TextSearchScope scope= TextSearchScope.newWorkspaceScope();
		scope.addExtension("*.java");
		fQuery1= new FileSearchQuery(scope,  "", "Test");
		fQuery2= new FileSearchQuery(scope, "", "TestCase");
	}
	
	public void testRemoveFile() throws Exception {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] elements= result.getElements();
		int fileCount= result.getMatchCount(elements[0]);
		int totalCount= result.getMatchCount();
		((IFile)elements[0]).delete(true, true, null);
		assertEquals(totalCount-fileCount, result.getMatchCount());
		assertEquals(0, result.getMatchCount(elements[0]));
	}
	
	public void testRemoveProject() throws Exception {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		JUnitSetup.getProject().delete(true, true, null);
		assertEquals(0, result.getMatchCount());
		// must recreate the project.
		ResourcesPlugin.getWorkspace().getRoot().getProject(JUnitSetup.PROJECT_NAME).create(null);
	}
}
