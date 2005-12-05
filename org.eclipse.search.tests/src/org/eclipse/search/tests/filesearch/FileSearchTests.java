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

import org.eclipse.search.internal.core.text.PatternConstructor;
import org.eclipse.search.internal.core.text.FileNamePatternSearchScope;
import org.eclipse.search.internal.ui.SearchPlugin;

import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;

import org.eclipse.search.tests.ResourceHelper;

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
	
	
	/*
	private void createFile(File file, String contents) throws IOException {
		FileWriter fs= new FileWriter(file);
		try {
			fs.write(contents);
			System.out.println(fs.getEncoding());
		} finally {
			fs.close();
		}
	}
	
	public void testFileNIOTest1() throws Exception {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038
		StringBuffer buf= new StringBuffer();
		buf.append("File1\n");
		buf.append("hello\n");
		buf.append("more hello\n");
		buf.append("world\n");
		

		File file1= File.createTempFile("test", "txt");
		createFile(file1, buf.toString());
		
		File file2= File.createTempFile("test", "txt");
		createFile(file2, buf.toString());
		assertTrue(file2.delete());
		
		
		InputStream stream= null;
		try {
			stream= new FileInputStream(file1);
			if (stream instanceof FileInputStream) {
				FileChannel channel= ((FileInputStream) stream).getChannel();
				try {
					MappedByteBuffer mappedBuffer= channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
					
					Charset charset= Charset.forName("Cp1252");
					CharsetDecoder decoder = charset.newDecoder();
					
					CharSequence searchInput= decoder.decode(mappedBuffer);

					int len= Math.min(searchInput.length(), 5);
					CharSequence sequence= searchInput.subSequence(0, len);
					System.out.println(sequence.toString());
				} finally {
					channel.close();
				}
			}
		} finally {
			stream.close();
		}
		assertTrue(file1.delete());
	}
	*/
	
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

		FileNamePatternSearchScope scope= FileNamePatternSearchScope.newSearchScope("test-project", new IResource[] { fProject });
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
		
		FileNamePatternSearchScope scope= FileNamePatternSearchScope.newSearchScope("test-project", new IResource[] { fProject });
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
		
		FileNamePatternSearchScope scope= FileNamePatternSearchScope.newSearchScope("test-project", new IResource[] { fProject });
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

			FileNamePatternSearchScope scope= FileNamePatternSearchScope.newSearchScope("test-project", new IResource[] {project});
			TextSearchEngine.create().search(scope, collector, searchPattern, null);

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
			
			TestResultCollector collector= new TestResultCollector();
			Pattern searchPattern= PatternConstructor.createPattern("hello", false, true);

			FileNamePatternSearchScope scope= FileNamePatternSearchScope.newSearchScope("test-project", new IResource[] {fProject});
			TextSearchEngine.create().search(scope, collector, searchPattern, null);

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
