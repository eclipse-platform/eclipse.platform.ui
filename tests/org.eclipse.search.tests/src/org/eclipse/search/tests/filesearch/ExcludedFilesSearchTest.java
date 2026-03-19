/*******************************************************************************
 * Copyright (c) 2026 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.internal.core.SearchCorePlugin;
import org.eclipse.search.internal.core.text.PatternConstructor;
import org.eclipse.search.tests.ResourceHelper;
import org.eclipse.search.ui.text.FileTextSearchScope;

/**
 * Test for excluding files from the file search based on a preference. Sets the preference
 * {@code org.eclipse.search/search_exclusion_property=search_excluded_file}, adds the session
 * property {@code search_excluded_file} on some test files and performs a search. Search matches
 * are not expected for the files with the session property.
 */
public class ExcludedFilesSearchTest {

	private static final String EXCLUSION_PREFERENCE_NAME= "search_exclusion_property";

	private static final QualifiedName SESSION_PROPERTY_QN= new QualifiedName(null, "search_excluded_file");

	private static final String SESSION_PROPERTY= "search_excluded_file";

	private static final String EXCLUDED_FILE_PREFIX= "excluded_";

	private static final String PROJECT_NAME= "excluded-search-test-project";

	@AfterEach
	public void cleanUp() throws Exception {
		ResourceHelper.deleteProject(PROJECT_NAME);
		setExcludedSearchEnabled(null);
	}

	@Test
	public void testFileLinks1() throws Exception {
		// test setting session property on first of three links
		doLinkTest(3, 0);
	}

	@Test
	public void testFileLinks2() throws Exception {
		// test setting session property on second of three links
		doLinkTest(3, 1);
	}

	/**
	 * Creates a test project with {@code n} links to the same temporary file on the file system.
	 * Excludes one of the links from the search.
	 *
	 * @param n The number of link files.
	 * @param index The index of the link file which will be excluded from the search.
	 */
	private void doLinkTest(int n, int index) throws Exception {
		String searchString= "hello";
		IProject project= ResourceHelper.createProject(PROJECT_NAME);
		IFolder folder= project.getFolder("tst");
		folder.create(true, false, null);
		File file= File.createTempFile("test", "file");
		try {
			Files.write(file.toPath(), searchString.getBytes());
			for (int i= 0; i < n; ++i) {
				IFile link= ResourceHelper.createLinkedFile(project, new Path("link_file_" + i), file);
				if (i == index) {
					setExcludedSearchSessionProperty(link);
				}
			}
			setExcludedSearchEnabled(SESSION_PROPERTY);
			TestResultCollector collector= new TestResultCollector(true);
			doSearch(project, collector, searchString);
			TestResult[] results= collector.getResults();
			assertMatches(results, n - 1, searchString);
		} finally {
			assertTrue(file.delete(), "Failed to delete temporary file");
		}
	}

	@Test
	public void testSimpleFilesParallel() throws Exception {
		doSearchTest(10, 5, true, true);
	}

	@Test
	public void testSimpleFilesSerial() throws Exception {
		doSearchTest(10, 5, false, true);
	}

	/**
	 * Creates the following project structure:
	 *
	 * <pre>
	 * tst/
	 * |- match_file_1
	 * |- excluded_1_1
	 * |- excluded_1_2
	 * | ...
	 * |- excluded_1_n
	 * |- tst_1/
	 *    |- match_file_1
	 *    |- excluded_1_1
	 *    |- excluded_1_2
	 *    | ...
	 *    |- excluded_1_n
	 *    |- tst_2/
	 *    |  |- match_file_2
	 *    |  |- excluded_2_1
	 *    |  |- ...
	 *       ...
	 *           |- tst_m/
	 *           |- match_file_m
	 *           |- excluded_m_1
	 *           |- ...
	 * </pre>
	 *
	 * Then performs a search in the test project and validates search results. If
	 * {@code sessionProperty} is set, files with name prefix {@code excluded_} are not expected in
	 * the search matches.
	 *
	 * @param n The number of files per folder.
	 * @param m The number of nested folders.
	 * @param parallel Whether to use parallel search.
	 * @param sessionProperty Whether to set the excluded resource session property.
	 */
	private static void doSearchTest(int n, int m, boolean parallel, boolean sessionProperty) throws Exception {
		String searchString= "hello";
		IProject project= prepareProject(n, m, PROJECT_NAME, searchString);
		if (sessionProperty) {
			setExcludedSearchEnabled(SESSION_PROPERTY);
			setSessionProperty(project);
		}
		TestResultCollector collector= new TestResultCollector(parallel);
		performSearch(n, m, sessionProperty, project, collector, searchString);

	}

	private static void setSessionProperty(IProject project) throws CoreException {
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource.getName().startsWith(EXCLUDED_FILE_PREFIX)) {
					setExcludedSearchSessionProperty(resource);
				}
				return true;
			}
		});
	}

	private static void performSearch(int n, int m, boolean sessionProperty, IProject project, TestResultCollector collector, String searchString) throws Exception {
		doSearch(project, collector, searchString);

		int expectedMatches= m;
		if (!sessionProperty) {
			expectedMatches+= n * m;
		}
		TestResult[] results= collector.getResults();
		assertEquals(expectedMatches, results.length, "Wrong number of total results");

		assertMatches(results, expectedMatches, searchString);
	}

	private static void doSearch(IProject project, TestResultCollector collector, String searchString) {
		Pattern searchPattern= PatternConstructor.createPattern(searchString, false, true);
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] { project }, (String[]) null, false);
		TextSearchEngine.create().search(scope, collector, searchPattern, null);
	}

	private static IProject prepareProject(int n, int m, String projectName, String searchString) throws Exception {
		IProject project= ResourceHelper.createProject(projectName);
		IPath path= new Path("tst");
		for (int i= 1; i <= m; ++i) {
			IFolder folder= project.getFolder(path);
			folder.create(true, false, null);
			path= path.append("tst_" + i);
			for (int j= 1; j <= n; ++j) {
				createFile(folder, EXCLUDED_FILE_PREFIX + "_file_" + i + "_" + j + ".txt", "test content " + searchString + " " + i + " " + j);
			}
			createFile(folder, "match_file_" + i + ".txt", "prefix " + searchString + " suffix");
		}
		return project;
	}

	private static IFile createFile(IFolder folder, String fileName, String contents) throws Exception {
		IFile file= folder.getFile(fileName);
		file.create(contents.getBytes(), IResource.FORCE, null);
		return file;
	}
	private static void assertMatches(TestResult[] results, int expectedCount, String string) throws Exception {
		int k= 0;
		for (TestResult curr : results) {
			String content= curr.file.readString();
			k++;
			assertEquals(string, content.substring(curr.offset, curr.offset + curr.length), "Wrong positions");
		}
		assertEquals(expectedCount, k, "Wrong number of results in file");
	}

	private static void setExcludedSearchSessionProperty(IResource resource) throws CoreException {
		resource.setSessionProperty(SESSION_PROPERTY_QN, "true");
	}

	private static void setExcludedSearchEnabled(String value) throws BackingStoreException {
		IEclipsePreferences node= InstanceScope.INSTANCE.getNode(SearchCorePlugin.PLUGIN_ID);
		if (value != null) {
			node.put(EXCLUSION_PREFERENCE_NAME, value);
		} else {
			node.remove(EXCLUSION_PREFERENCE_NAME);
		}
		node.flush();
	}

	private record TestResult(IFile file, int offset, int length) {
	}

	private static class TestResultCollector extends TextSearchRequestor {

		private final boolean parallel;

		private final List<TestResult> result;

		private TestResultCollector(boolean parallel) {
			this.parallel= parallel;
			if (parallel) {
				result= Collections.synchronizedList(new ArrayList<>());
			} else {
				result= new ArrayList<>();
			}
		}

		private TestResult[] getResults() {
			return result.toArray(new TestResult[result.size()]);
		}

		@Override
		public boolean canRunInParallel() {
			return parallel;
		}

		@Override
		public boolean acceptPatternMatch(TextSearchMatchAccess match) throws CoreException {
			result.add(new TestResult(match.getFile(), match.getMatchOffset(), match.getMatchLength()));
			return true;
		}
	}
}
