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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.search.internal.core.text.ITextSearchResultCollector;
import org.eclipse.search.internal.core.text.MatchLocator;
import org.eclipse.search.internal.core.text.TextSearchEngine;
import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.tests.ResourceHelper;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;

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
	
	
	private static class TestResultCollector implements ITextSearchResultCollector {
		
		private List fResult;

		public TestResultCollector() {
			fResult= new ArrayList();
		}
		
		public IProgressMonitor getProgressMonitor() {
			return new NullProgressMonitor();
		}

		public void aboutToStart() throws CoreException {
		}

		public void accept(IResourceProxy proxy, int start, int length) throws CoreException {
			fResult.add(new TestResult(proxy.requestResource(), start, length));
		}

		public void done() throws CoreException {
		}
		
		public TestResult[] getResults() {
			return (TestResult[]) fResult.toArray(new TestResult[fResult.size()]);
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

		TextSearchEngine engine= new TextSearchEngine();
		TestResultCollector collector= new TestResultCollector();
		MatchLocator matchLocator= new MatchLocator("hello", false, true);
		
		TextSearchScope scope= new TextSearchScope("test-project", new IResource[] { fProject });
		engine.search(fProject.getWorkspace(), scope, true, collector, matchLocator);
		
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

		TextSearchEngine engine= new TextSearchEngine();
		TestResultCollector collector= new TestResultCollector();
		MatchLocator matchLocator= new MatchLocator("mor*", false, false);
		
		TextSearchScope scope= new TextSearchScope("test-project", new IResource[] { fProject });
		engine.search(fProject.getWorkspace(), scope, true, collector, matchLocator);
		
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

		TextSearchEngine engine= new TextSearchEngine();
		TestResultCollector collector= new TestResultCollector();
		MatchLocator matchLocator= new MatchLocator("mo?e", false, false);
		
		TextSearchScope scope= new TextSearchScope("test-project", new IResource[] { fProject });
		engine.search(fProject.getWorkspace(), scope, true, collector, matchLocator);
		
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
			TextSearchEngine engine= new TextSearchEngine();
			TestResultCollector collector= new TestResultCollector();
			MatchLocator matchLocator= new MatchLocator("\\w*\\(\\)", false, true);

			// search in Junit sources

			TextSearchScope scope= new TextSearchScope("test-project", new IResource[] {project});
			engine.search(project.getWorkspace(), scope, true, collector, matchLocator);

			TestResult[] results= collector.getResults();
			assertEquals("Number of total results", 748, results.length);
			long end= System.currentTimeMillis();
			System.out.println("time= " + (end - start));
		} finally {
			activePage.closeAllEditors(false);
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
			
			TextSearchEngine engine= new TextSearchEngine();
			TestResultCollector collector= new TestResultCollector();
			MatchLocator matchLocator= new MatchLocator("hello", false, true);

			TextSearchScope scope= new TextSearchScope("test-project", new IResource[] {fProject});
			engine.search(fProject.getWorkspace(), scope, true, collector, matchLocator);

			TestResult[] results= collector.getResults();
			assertEquals("Number of total results", 4, results.length);

			assertMatches(results, 2, file1, buf.toString(), "hello");
			assertMatches(results, 2, file2, buf.toString(), "hello");
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
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
