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
 *     Christian Walther (Indel AG) - Bug 399094, 402009: Add whole word option to file search
 *     Terry Parker <tparker@google.com> (Google Inc.) - Bug 441016 - Speed up text search by parallelizing it using JobGroups
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.eclipse.core.runtime.ContributorFactorySimple;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.internal.core.text.PatternConstructor;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.tests.ResourceHelper;
import org.eclipse.search.tests.SearchTestUtil;
import org.eclipse.search.ui.text.FileTextSearchScope;

public class FileSearchTests {

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

		protected List<TestResult> fResult;

		public TestResultCollector() {
			reset();
		}

		public TestResult[] getResults() {
			return fResult.toArray(new TestResult[fResult.size()]);
		}

		public int getNumberOfResults() {
			return fResult.size();
		}

		public void reset() {
			fResult= new ArrayList<>();
		}

	}

	private static class SerialTestResultCollector extends TestResultCollector {

		@Override
		public boolean canRunInParallel() {
			return false;
		}

		@Override
		public boolean acceptPatternMatch(TextSearchMatchAccess match) throws CoreException {
			fResult.add(new TestResult(match.getFile(), match.getMatchOffset(), match.getMatchLength()));
			return true;
		}

	}

	private static class ParallelTestResultCollector extends TestResultCollector {

		@Override
		public boolean canRunInParallel() {
			return true;
		}

		@Override
		public boolean acceptPatternMatch(TextSearchMatchAccess match) throws CoreException {
			synchronized(fResult) {
				fResult.add(new TestResult(match.getFile(), match.getMatchOffset(), match.getMatchLength()));
			}
			return true;
		}

	}

	@ClassRule
	public static JUnitSourceSetup fgJUnitSource= new JUnitSourceSetup();

	private IProject fProject;

	@Before
	public void setUp() throws Exception{
		fProject= ResourceHelper.createProject("my-project"); //$NON-NLS-1$
	}

	@After
	public void tearDown() throws Exception {
		ResourceHelper.deleteProject("my-project"); //$NON-NLS-1$
	}

	@Test
	public void testSimpleFilesSerial() throws Exception {
		testSimpleFiles(new SerialTestResultCollector());
	}

	@Test
	public void testSimpleFilesParallel() throws Exception {
		testSimpleFiles(new ParallelTestResultCollector());
	}

	private void testSimpleFiles(TestResultCollector collector) throws Exception {
		StringBuilder buf= new StringBuilder();
		buf.append("File1\n");
		buf.append("hello\n");
		buf.append("more hello\n");
		buf.append("world\n");
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		IFile file1= ResourceHelper.createFile(folder, "file1", buf.toString());
		IFile file2= ResourceHelper.createFile(folder, "file2", buf.toString());

		Pattern searchPattern= PatternConstructor.createPattern("hello", false, true);

		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] {fProject}, (String[]) null, false);
		TextSearchEngine.create().search(scope, collector, searchPattern, null);

		TestResult[] results= collector.getResults();
		assertEquals("Number of total results", 4, results.length);

		assertMatches(results, 2, file1, buf.toString(), "hello");
		assertMatches(results, 2, file2, buf.toString(), "hello");
	}

	@Test
	public void testWildCards1Serial() throws Exception {
		testWildCards1(new SerialTestResultCollector());
	}

	@Test
	public void testWildCards1Parallel() throws Exception {
		testWildCards1(new ParallelTestResultCollector());
	}

	private void testWildCards1(TestResultCollector collector) throws Exception {
		StringBuilder buf= new StringBuilder();
		buf.append("File1\n");
		buf.append("no more\n");
		buf.append("mornings\n");
		buf.append("more hello\n");
		buf.append("world\n");
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		ResourceHelper.createFile(folder, "file1", buf.toString());
		ResourceHelper.createFile(folder, "file2", buf.toString());

		Pattern searchPattern= PatternConstructor.createPattern("mor*", false, false);

		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] {fProject}, (String[]) null, false);
		TextSearchEngine.create().search(scope, collector, searchPattern, null);

		TestResult[] results= collector.getResults();
		assertEquals("Number of total results", 6, results.length);
	}

	@Test
	public void testWildCards2Serial() throws Exception {
		testWildCards2(new SerialTestResultCollector());
	}

	@Test
	public void testWildCards2Parallel() throws Exception {
		testWildCards2(new ParallelTestResultCollector());
	}

	private void testWildCards2(TestResultCollector collector) throws Exception {
		StringBuilder buf= new StringBuilder();
		buf.append("File1\n");
		buf.append("no more\n");
		buf.append("mornings\n");
		buf.append("more hello\n");
		buf.append("world\n");
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		ResourceHelper.createFile(folder, "file1", buf.toString());
		ResourceHelper.createFile(folder, "file2", buf.toString());

		Pattern searchPattern= PatternConstructor.createPattern("mo?e", false, false);

		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] {fProject}, (String[]) null, false);
		TextSearchEngine.create().search(scope, collector, searchPattern, null);

		TestResult[] results= collector.getResults();
		assertEquals("Number of total results", 4, results.length);
	}

	@Test
	public void testWildCards3Serial() throws Exception {
		testWildCards3(new SerialTestResultCollector());
	}

	@Test
	public void testWildCards3Parallel() throws Exception {
		testWildCards3(new ParallelTestResultCollector());
	}

	private void testWildCards3(TestResultCollector collector) throws Exception {

		IProject project= fgJUnitSource.getStandardProject();
		IFile openFile1= (IFile) project.findMember("junit/framework/TestCase.java");
		IFile openFile2= (IFile) project.findMember("junit/extensions/ExceptionTestCase.java");
		IFile openFile3= (IFile) project.findMember("junit/framework/Assert.java");
		IFile openFile4= (IFile) project.findMember("junit/samples/money/MoneyTest.java");

		IWorkbenchPage activePage= SearchPlugin.getActivePage();
		try {
			SearchTestUtil.openTextEditor(activePage, openFile1);
			SearchTestUtil.openTextEditor(activePage, openFile2);
			SearchTestUtil.openTextEditor(activePage, openFile3);
			SearchTestUtil.openTextEditor(activePage, openFile4);

			long start= System.currentTimeMillis();

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

	@Test
	public void testWholeWordSerial() throws Exception {
		testWholeWord(new SerialTestResultCollector());
	}

	@Test
	public void testWholeWordParallel() throws Exception {
		testWholeWord(new ParallelTestResultCollector());
	}

	private void testWholeWord(TestResultCollector collector) throws Exception {
		StringBuilder buf= new StringBuilder();
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
			collector.reset();
			engine.search(scope, collector, searchPattern, null);
			assertEquals("Number of partial-word results", 22, collector.getNumberOfResults());
		}
		{
			// wildcards, whole word = true: match only nothing and non-word chars before and after
			Pattern searchPattern= PatternConstructor.createPattern("h?ll", false, true, false, true);
			collector.reset();
			engine.search(scope, collector, searchPattern, null);
			assertEquals("Number of whole-word results", 10, collector.getNumberOfResults());
		}
		// regexp, whole word = false: match all lines
		Pattern searchPattern= PatternConstructor.createPattern("h[eio]ll", true, true, false, false);
		collector.reset();
		engine.search(scope, collector, searchPattern, null);
		assertEquals("Number of partial-word results", 22, collector.getNumberOfResults());
	}

	@Test
	public void testFileOpenInEditorSerial() throws Exception {
		testFileOpenInEditor(new SerialTestResultCollector());
	}

	@Test
	public void testFileOpenInEditorParallel() throws Exception {
		testFileOpenInEditor(new ParallelTestResultCollector());
	}

	private void testFileOpenInEditor(TestResultCollector collector) throws Exception {
		StringBuilder buf= new StringBuilder();
		buf.append("File1\n");
		buf.append("hello\n");
		buf.append("more hello\n");
		buf.append("world\n");
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		IFile file1= ResourceHelper.createFile(folder, "file1", buf.toString());
		IFile file2= ResourceHelper.createFile(folder, "file2", buf.toString());

		try {
			SearchTestUtil.openTextEditor(SearchPlugin.getActivePage(), file2);

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

	@Test
	public void testDerivedFilesSerial() throws Exception {
		testDerivedFiles(new SerialTestResultCollector());
	}

	@Test
	public void testWildcardQuotes() throws Exception {
		assertWildcardReplace("H", "Hallo", "-allo");
		assertWildcardReplace("a", "Hallo", "H-llo");
		assertWildcardReplace("al", "Hallo", "H-lo");
		assertWildcardReplace("a*", "Hallo", "H-");
		assertWildcardReplace("a?", "Hallo", "H-lo");
		assertWildcardReplace("?", "Hallo", "-----");
		assertWildcardReplace("{", "Ha({o", "Ha(-o");
		assertWildcardReplace("(", "Ha({o", "Ha-{o");
		assertWildcardReplace("\\", "Ha\\\\o", "Ha--o");
		assertWildcardReplace("\\\\", "Ha\\\\o", "Ha--o");
		assertWildcardReplace("\\*", "Hall*", "Hall-");
		assertWildcardReplace("\\?", "Ha??o?", "Ha--o-");
		assertWildcardReplace("Du?und?ich", "Du und ich nicht", "- nicht");
		assertWildcardReplace("Du*ich", "Du und ich nicht", "-t");
		assertWildcardReplace("und*ich", "Du und ich nicht", "Du -t");
		assertWildcardReplace("*ich", "Du und ich nicht", "-t");

		assertWildcardReplace("*", "Hallo", "--");
		// XXX i expect it to be "-" but ".*" indeed matches chars 0-5 and 5-5
		// it would need ".+" to not match the empty string at the end
	}

	private void assertWildcardReplace(String pattern, String in, String expected) {
		String regex= asRegEx(true, pattern);
		try {
			String replaced= in.replaceAll(regex, "-");
			assertEquals(expected, replaced);
		} catch (Exception e) {
			throw new RuntimeException("Error with pattern:" + pattern + " regex=" + regex, e);
		}
	}

	String asRegEx(boolean wildcards, String pattern) {
		StringBuilder b= new StringBuilder();
		org.eclipse.search.internal.core.text.PatternConstructor.appendAsRegEx(wildcards, pattern, b);
		return b.toString();
	}
	@Test
	public void testDerivedFilesParallel() throws Exception {
		testDerivedFiles(new ParallelTestResultCollector());
	}

	private void testDerivedFiles(TestResultCollector collector) throws Exception {
		StringBuilder buf= new StringBuilder();
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
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { fProject }, fileNamePattern, true);
			collector.reset();
			engine.search(scope, collector, searchPattern, null);
			assertEquals(5, collector.getNumberOfResults());
		}
		{
			// visit non-derived
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { fProject }, fileNamePattern, false);
			collector.reset();
			engine.search(scope, collector, searchPattern, null);
			assertEquals(2, collector.getNumberOfResults());
		}
		{
			// visit all in folder2
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { folder2 }, fileNamePattern, true);
			collector.reset();
			engine.search(scope, collector, searchPattern, null);
			assertEquals(2, collector.getNumberOfResults());
		}
		{
			// visit non-derived in folder2
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { folder2 }, fileNamePattern, false);
			collector.reset();
			engine.search(scope, collector, searchPattern, null);
			assertEquals(0, collector.getNumberOfResults());
		}
		{
			// visit all in folder3
			TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { folder3 }, fileNamePattern, true);
			collector.reset();
			engine.search(scope, collector, searchPattern, null);
			assertEquals(1, collector.getNumberOfResults());
		}
		// visit non-derived in folder3
		TextSearchScope scope= TextSearchScope.newSearchScope(new IResource[] { folder3 }, fileNamePattern, false);
		collector.reset();
		engine.search(scope, collector, searchPattern, null);
		assertEquals(0, collector.getNumberOfResults());
	}

	@Test
	public void testFileNamePatternsSerial() throws Exception {
		testFileNamePatterns(new SerialTestResultCollector());
	}

	@Test
	public void testFileNamePatternsParallel() throws Exception {
		testFileNamePatterns(new ParallelTestResultCollector());
	}


	private void testFileNamePatterns(TestResultCollector collector) throws Exception {
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		ResourceHelper.createFile(folder, "file1.x", "Test");
		ResourceHelper.createFile(folder, "file2.x", "Test");
		ResourceHelper.createFile(folder, "file2.y", "Test");
		ResourceHelper.createFile(folder, "file2.z", "Test");

		Pattern searchPattern= PatternConstructor.createPattern("Test", false, false);
		String[] fileNamePatterns= { "*" };

		TestResult[] results= performSearch(collector, fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 4, results.length);

		fileNamePatterns= new String[] { "*.x" };
		results= performSearch(collector, fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 2, results.length);

		fileNamePatterns= new String[] { "*.x", "*.y*" };
		results= performSearch(collector, fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 3, results.length);

		fileNamePatterns= new String[] { "!*.x" };
		results= performSearch(collector, fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 2, results.length);

		fileNamePatterns= new String[] { "!*.x", "!*.y" };
		results= performSearch(collector, fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 1, results.length);

		fileNamePatterns= new String[] { "*", "!*.y" };
		results= performSearch(collector, fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 3, results.length);

		fileNamePatterns= new String[] { "*", "!*.*" };
		results= performSearch(collector, fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 0, results.length);

		fileNamePatterns= new String[] { "*.x", "*.y*", "!*.y" };
		results= performSearch(collector, fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 2, results.length);

		fileNamePatterns= new String[] { "file*", "!*.x*", "!*.y" };
		results= performSearch(collector, fileNamePatterns, searchPattern);
		assertEquals("Number of total results", 1, results.length);
	}

	private TestResult[] performSearch(TestResultCollector collector, String[] fileNamePatterns, Pattern searchPattern) {
		collector.reset();
		FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] {fProject}, fileNamePatterns, false);
		TextSearchEngine.create().search(scope, collector, searchPattern, null);

		return collector.getResults();

	}

	@Test
	public void testBinaryContentTypeWithDescriberSerial() throws Exception {
		testBinaryContentTypeWithDescriber(new SerialTestResultCollector());
	}

	@Test
	public void testBinaryContentTypeWithDescriberParallel() throws Exception {
		testBinaryContentTypeWithDescriber(new ParallelTestResultCollector());
	}

	private void testBinaryContentTypeWithDescriber(TestResultCollector collector) throws Exception {
		IExtensionRegistry registry= Platform.getExtensionRegistry();

		Field field= org.eclipse.core.internal.registry.ExtensionRegistry.class
				.getDeclaredField("masterToken");
		field.setAccessible(true);
		Object masterToken= field.get(registry);

		IContributor contributor= ContributorFactorySimple.createContributor(this);

		try (java.io.InputStream is= new ByteArrayInputStream("""
				<?xml version="1.0"?>
				<plugin>
					<extension point="org.eclipse.core.contenttype.contentTypes">
					   <content-type
					         id="org.eclipse.search.tests.binaryFile"
					         name="Search Test Binary File"
					         priority="low">
					      <describer
					            class="org.eclipse.core.runtime.content.BinarySignatureDescriber"
					            plugin="org.eclipse.core.contenttype">
					         <!-- "binary" in ASCII encoding -->
					         <parameter name="signature" value="62 69 6E 61 72 79"/>
					      </describer>
					   </content-type>
					   <file-association
					         content-type="org.eclipse.search.tests.binaryFile"
					         file-patterns="[^.]+"/> <!-- no file extension -->
					</extension>
				</plugin>""".getBytes())) {
			registry.addContribution(is, contributor, false, null, null, masterToken);
			try (AutoCloseable c= () -> {
				Arrays.stream(registry.getExtensions(contributor))
						.forEach(extension -> registry.removeExtension(extension, masterToken));
			}) {

				IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
				IFile textfile= ResourceHelper.createFile(folder, "textfile", "text hello");
				IFile binaryfile= ResourceHelper.createFile(folder, "binaryfile", "binary hello");

				Pattern searchPattern= PatternConstructor.createPattern("hello", true, false);

				FileTextSearchScope scope= FileTextSearchScope.newSearchScope(new IResource[] { fProject }, (String[]) null, false);
				TextSearchEngine.create().search(scope, collector, searchPattern, null);

				TestResult[] results= collector.getResults();
				assertEquals("Number of total results", 1, results.length);

				assertMatches(results, 1, textfile, "text hello", "hello");
				assertMatches(results, 0, binaryfile, "binary hello", "hello");
			}
		}
	}



	private void assertMatches(TestResult[] results, int expectedCount, IFile file, String fileContent, String string) {
		int k= 0;
		for (TestResult curr : results) {
			if (file.equals(curr.resource)) {
				k++;
				assertEquals("Wrong positions", string, fileContent.substring(curr.offset, curr.offset + curr.length));
			}
		}
		assertEquals("Number of results in file", expectedCount, k);
	}


}
