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

import org.eclipse.search.internal.core.text.FileNamePatternSearchScope;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.tests.ResourceHelper;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class ResultUpdaterTest extends TestCase {
	private FileSearchQuery fQuery1;
	
	private IProject fProject;
	
	private static final String PROJECT_TO_MODIFY= "ModifiableProject";

	public ResultUpdaterTest(String name) {
		super(name);
	}
		
	public static Test allTests() {
		return setUpTest(new TestSuite(ResultUpdaterTest.class));
	}
	
	public static Test setUpTest(Test test) {
		return test;
	}
	
	public static Test suite() {
		return allTests();
	}

	protected void setUp() throws Exception {
		super.setUp();
		// create a own project to make modifications
		fProject= ResourceHelper.createJUnitSourceProject(PROJECT_TO_MODIFY);
		
		FileNamePatternSearchScope scope= FileNamePatternSearchScope.newSearchScope("xx", new IResource[] { fProject }, false);
		scope.addFileNamePattern("*.java");
		fQuery1= new FileSearchQuery(scope,  "", "Test");
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		ResourceHelper.deleteProject(PROJECT_TO_MODIFY);
	}
	
	public void testRemoveFile() throws Exception {
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] elements= result.getElements();
		int fileCount= result.getMatchCount(elements[0]);
		int totalCount= result.getMatchCount();
		ResourceHelper.delete((IFile)elements[0]);
		assertEquals(totalCount-fileCount, result.getMatchCount());
		assertEquals(0, result.getMatchCount(elements[0]));
	}
	
	public void testRemoveProject() throws Exception {
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		ResourceHelper.delete(fProject);
		assertEquals(0, result.getMatchCount());
	}
}
