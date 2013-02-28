/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christian Walther (Indel AG) - Bug 399094: Add whole word option to file search
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;

import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.internal.core.text.PatternConstructor;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.tests.ResourceHelper;
import org.eclipse.search.ui.text.FileTextSearchScope;

public class FileSearchTests extends TestCase {
	
	private static class TestResult {
		public IResource resource;
		public int offset;
		public int length;

		public TestResult(IResource resource, int offset, int length) {
			this.resource= resource;
			this.offset= offset;
			this.length= length;
		}
	}
	
	
	private static class TestResultCollector extends TextSearchRequestor {
		
		private List fResult;

		public TestResultCollector() {
			fResult= new ArrayList();
		}
		
		public boolean acceptPatternMatch(TextSearchMatchAccess match) throws CoreException {
			fResult.add(new TestResult(match.getFile(), match.getMatchOffset(), match.getMatchLength()));
			return true;
		}
				
		public TestResult[] getResults() {
			return (TestResult[]) fResult.toArray(new TestResult[fResult.size()]);
		}
		
		public int getNumberOfResults() {
			return fResult.size();
		}
		
	}
	
	private IProject fProject;
	
	public FileSearchTests(String name) {
		super(name);
	}
	
	public static Test allTests() {
		return setUpTest(new TestSuite(FileSearchTests.class));
	}
	
	public static Test setUpTest(Test test) {
		return new JUnitSourceSetup(test);
	}
	
	public static Test suite() {
		return allTests();
	}
	
	protected void setUp() throws Exception {
		fProject= ResourceHelper.createProject("my-project"); //$NON-NLS-1$
	}
	
	protected void tearDown() throws Exception {
		ResourceHelper.deleteProject("my-project"); //$NON-NLS-1$
	}
	
	
	public void testSimpleFiles() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("File1\n");
		buf.append("hello\n");
		buf.append("more hello\n");
		buf.append("world\n");
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		IFile file1= ResourceHelper.createFile(folder, "file1", buf.toString());
		IFile file2= ResourceHelper.createFile(folder, "file2", buf.toString());

		TestResultCollector collector= new TestResultCollector();
		Pattern searchPattern= PatternConstructor.createPattern("hello", false, true);

		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] {fProject}, (String[]) null, false);
		TextSearchEngine.create().search(scope, collector, searchPattern, null);
		
		TestResult[] results= collector.getResults();
		assertEquals("Number of total results", 4, results.length);
		
		assertMatches(results, 2, file1, buf.toString(), "hello");
		assertMatches(results, 2, file2, buf.toString(), "hello");
	}
	
	public void testWildCards1() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("File1\n");
		buf.append("no more\n");
		buf.append("mornings\n");
		buf.append("more hello\n");
		buf.append("world\n");
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		ResourceHelper.createFile(folder, "file1", buf.toString());
		ResourceHelper.createFile(folder, "file2", buf.toString());

		TestResultCollector collector= new TestResultCollector();
		Pattern searchPattern= PatternConstructor.createPattern("mor*", false, false);
		
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] {fProject}, (String[]) null, false);
		TextSearchEngine.create().search(scope, collector, searchPattern, null);
		
		TestResult[] results= collector.getResults();
		assertEquals("Number of total results", 6, results.length);
	}
	
	public void testWildCards2() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("File1\n");
		buf.append("no more\n");
		buf.append("mornings\n");
		buf.append("more hello\n");
		buf.append("world\n");
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		ResourceHelper.createFile(folder, "file1", buf.toString());
		ResourceHelper.createFile(folder, "file2", buf.toString());

		TestResultCollector collector= new TestResultCollector();
		Pattern searchPattern= PatternConstructor.createPattern("mo?e", false, false);
		
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] {fProject}, (String[]) null, false);
		TextSearchEngine.create().search(scope, collector, searchPattern, null);
		
		TestResult[] results= collector.getResults();
		assertEquals("Number of total results", 4, results.length);
	}
	
	public void testWildCards3() throws Exception {
		
		IProject project= JUnitSourceSetup.getStandardProject();
		IFile openFile1= (IFile) project.findMember("junit/framework/TestCase.java");
		IFile openFile2= (IFile) project.findMember("junit/extensions/ExceptionTestCase.java");
		IFile openFile3= (IFile) project.findMember("junit/framework/Assert.java");
		IFile openFile4= (IFile) project.findMember("junit/samples/money/MoneyTest.java");
		
		IWorkbenchPage activePage= SearchPlugin.getActivePage();
		try {
			IDE.openEditor(activePage, openFile1, true);
			IDE.openEditor(activePage, openFile2, true);
			IDE.openEditor(activePage, openFile3, true);
			IDE.openEditor(activePage, openFile4, true);
			
			long start= System.currentTimeMillis();

			TestResultCollector collector= new TestResultCollector();
			Pattern searchPattern= PatternConstructor.createPattern("\\w*\\(\\)", false, true);

			// search in Junit sources

			FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] {project}, (String[]) null, false);
			TextSearchEngine.create().search(scope, collector, searchPattern, null);

			TestResult[] results= collector.getResults();
			assertEquals("Number of total results", 748, results.length);
			long end= System.currentTimeMillis();
			System.out.println("time= " + (end - start));
		} finally {
			activePage.closeAllEditors(false);
		}
		

	}
	
	public void testWholeWord() throws Exception {
		StringBuffer buf= new StringBuffer();
		// nothing after
		buf.append("hell\n"); // nothing before
		buf.append("hill\n"); // nothing before
		buf.append("$hell\n"); // non-word char before
		buf.append("shell\n"); // word char before
		// non-word char after
		buf.append("hell.freeze()\n"); // nothing before
		buf.append("freeze(hell)\n"); // non-word char before
		buf.append("shell-script\n"); // word char before
		// word char after
		buf.append("hello\n"); // nothing before
		buf.append("world.hello()\n"); // non-word char before
		buf.append("shilling\n"); // word char before
		buf.append("holler\n"); // nothing before
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		ResourceHelper.createFile(folder, "file1", buf.toString());
		ResourceHelper.createFile(folder, "file2", buf.toString());

		TextSearchEngine engine= TextSearchEngine.create();
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] { fProject }, (String[])null, false);

		{
			// wildcards, whole word = false: match all lines
			Pattern searchPattern= PatternConstructor.createPattern("h?ll", false, true, false, false);
			TestResultCollector collector= new TestResultCollector();
			engine.search(scope, collector, searchPattern, null);
			assertEquals("Number of partial-word results", 22, collector.getNumberOfResults());
		}
		{
			// wildcards, whole word = true: match only nothing and non-word chars before and after
			Pattern searchPattern= PatternConstructor.createPattern("h?ll", false, true, false, true);
			TestResultCollector collector= new TestResultCollector();
			engine.search(scope, collector, searchPattern, null);
			assertEquals("Number of whole-word results", 10, collector.getNumberOfResults());
		}
		{
			// regexp, whole word = false: match all lines
			Pattern searchPattern= PatternConstructor.createPattern("h[eio]ll", true, true, false, false);
			TestResultCollector collector= new TestResultCollector();
			engine.search(scope, collector, searchPattern, null);
			assertEquals("Number of partial-word results", 22, collector.getNumberOfResults());
		}
		{
			// regexp, whole word = true: match only nothing and non-word chars before and after
			Pattern searchPattern= PatternConstructor.createPattern("h[eio]ll", true, true, false, true);
			TestResultCollector collector= new TestResultCollector();
			engine.search(scope, collector, searchPattern, null);
			assertEquals("Number of whole-word results", 10, collector.getNumberOfResults());
		}
	}
	

	public void testFileOpenInEditor() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("File1\n");
		buf.append("hello\n");
		buf.append("more hello\n");
		buf.append("world\n");
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		IFile file1= ResourceHelper.createFile(folder, "file1", buf.toString());
		IFile file2= ResourceHelper.createFile(folder, "file2", buf.toString());

		try {
			IDE.openEditor(SearchPlugin.getActivePage(), file2, true);
			
			TestResultCollector collector= new TestResultCollector();
			Pattern searchPattern= PatternConstructor.createPattern("hello", false, true);

			FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] {fProject}, (String[]) null, false);
			TextSearchEngine.create().search(scope, collector, searchPattern, null);

			TestResult[] results= collector.getResults();
			assertEquals("Number of total results", 4, results.length);

			assertMatches(results, 2, file1, buf.toString(), "hello");
			assertMatches(results, 2, file2, buf.toString(), "hello");
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}
	
	public void testDerivedFiles() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("hello\n");
		IFolder folder1= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		ResourceHelper.createFile(folder1, "file1", buf.toString());
		IFile file2= ResourceHelper.createFile(folder1, "file2", buf.toString());
		file2.setDerived(true, null);
		
		IFolder folder2= ResourceHelper.createFolder(folder1.getFolder("folder2"));
		folder2.setDerived(true, null);
		ResourceHelper.createFile(folder2, "file3", buf.toString());

		IFolder folder3= ResourceHelper.createFolder(folder2.getFolder("folder3"));
		ResourceHelper.createFile(folder3, "file4", buf.toString());
		
		IFolder folder4= ResourceHelper.createFolder(folder1.getFolder("folder4"));
		ResourceHelper.createFile(folder4, "file5", buf.toString());
		
		/**
		 * folder1
		 *     file1
		 *     file2*
		 *     folder2*
		 *       file3
		 *       folder3
		 *         file4
		 *     folder4
		 *       file5
		 */
		
		
		Pattern searchPattern= PatternConstructor.createPattern("hello", false, true);
		Pattern fileNamePattern= PatternConstructor.createPattern("*", false, false);
		TextSearchEngine engine= TextSearchEngine.create();
		{
			// visit all
			TestResultCollector collector= new TestResultCollector();
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { fProject }, fileNamePattern, true);
			engine.search(scope, collector, searchPattern, null);
			assertEquals(5, collector.getNumberOfResults());
		}
		{
			// visit non-derived
			TestResultCollector collector= new TestResultCollector();
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { fProject }, fileNamePattern, false);
			engine.search(scope, collector, searchPattern, null);
			assertEquals(2, collector.getNumberOfResults());
		}
		{
			// visit all in folder2
			TestResultCollector collector= new TestResultCollector();
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { folder2 }, fileNamePattern, true);
			engine.search(scope, collector, searchPattern, null);
			assertEquals(2, collector.getNumberOfResults());
		}
		{
			// visit non-derived in folder2
			TestResultCollector collector= new TestResultCollector();
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { folder2 }, fileNamePattern, false);
			engine.search(scope, collector, searchPattern, null);
			assertEquals(0, collector.getNumberOfResults());
		}
		{
			// visit all in folder3
			TestResultCollector collector= new TestResultCollector();
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { folder3 }, fileNamePattern, true);
			engine.search(scope, collector, searchPattern, null);
			assertEquals(1, collector.getNumberOfResults());
		}
		{
			// visit non-derived in folder3
			TestResultCollector collector= new TestResultCollector();
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { folder3 }, fileNamePattern, false);
			engine.search(scope, collector, searchPattern, null);
			assertEquals(0, collector.getNumberOfResults());
		}
	}

	
	public void testFileNamePatterns() throws Exception {
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		ResourceHelper.createFile(folder, "file1.x", "Test");
		ResourceHelper.createFile(folder, "file2.x", "Test");
		ResourceHelper.createFile(folder, "file2.y", "Test");
		ResourceHelper.createFile(folder, "file2.z", "Test");

		Pattern searchPattern= PatternConstructor.createPattern("Test", false, false);
		String[] fileNamePatterns= { "*" };
				
		TestResult[] results= performSearch(fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 4, results.length);
		
		fileNamePatterns= new String[] { "*.x" };
		results= performSearch(fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 2, results.length);
		
		fileNamePatterns= new String[] { "*.x", "*.y*" };
		results= performSearch(fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 3, results.length);
		
		fileNamePatterns= new String[] { "!*.x" };
		results= performSearch(fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 2, results.length);
		
		fileNamePatterns= new String[] { "!*.x", "!*.y" };
		results= performSearch(fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 1, results.length);
		
		fileNamePatterns= new String[] { "*", "!*.y" };
		results= performSearch(fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 3, results.length);
		
		fileNamePatterns= new String[] { "*", "!*.*" };
		results= performSearch(fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 0, results.length);
		
		fileNamePatterns= new String[] { "*.x", "*.y*", "!*.y" };
		results= performSearch(fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 2, results.length);
		
		fileNamePatterns= new String[] { "file*", "!*.x*", "!*.y" };
		results= performSearch(fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 1, results.length);
	}
	
	private TestResult[] performSearch(String[] fileNamePatterns, Pattern searchPattern) {
		TestResultCollector collector= new TestResultCollector();
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] {fProject}, fileNamePatterns, false);
		TextSearchEngine.create().search(scope, collector, searchPattern, null);
		
		return collector.getResults();

	}
	


	private void assertMatches(TestResult[] results, int expectedCount, IFile file, String fileContent, String string) {
		int k= 0;
		for (int i= 0; i < results.length; i++) {
			TestResult curr= results[i];
			if (file.equals(curr.resource)) {
				k++;
				assertEquals("Wrong positions", string, fileContent.substring(curr.offset, curr.offset + curr.length));
			}
		}
		assertEquals("Number of results in file", expectedCount, k);
	}
	
	
}
