/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.tests.ResourceHelper;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;

public class ResultUpdaterTest {
	private FileSearchQuery fQuery1;

	private IProject fProject;

	private static final String PROJECT_TO_MODIFY= "ModifiableProject";

	@BeforeEach
	public void setUp() throws Exception {
		// create a own project to make modifications
		fProject= ResourceHelper.createJUnitSourceProject(PROJECT_TO_MODIFY);

		String[] fileNamePatterns= { "*.java" };
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] { fProject }, fileNamePatterns, false);

		fQuery1= new FileSearchQuery("Test", false, true, scope);
	}

	@AfterEach
	public void tearDown() throws Exception {
		ResourceHelper.deleteProject(PROJECT_TO_MODIFY);
	}

	@Test
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

	@Test
	public void testRemoveProject() throws Exception {
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		ResourceHelper.delete(fProject);
		assertEquals(0, result.getMatchCount());
	}
}
