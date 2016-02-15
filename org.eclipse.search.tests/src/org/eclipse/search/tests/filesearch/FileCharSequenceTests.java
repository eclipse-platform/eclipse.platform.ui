/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import org.eclipse.search.internal.core.text.FileCharSequenceProvider;
import org.eclipse.search.tests.ResourceHelper;

public class FileCharSequenceTests {

	private final String TEST_CONTENT= "ABCDEFGHIJKLMNOPQRSTUVWXYZÜöäüèéùabcdefghijklmnopqrstuvwxyz1234567890@\'\"\n$¢"; //€
	
	private IProject fProject;
	
	@Before
	public void setUp() throws Exception {
		fProject= ResourceHelper.createProject("my-project"); //$NON-NLS-1$
	}
	
	@After
	public void tearDown() throws Exception {
		ResourceHelper.deleteProject("my-project"); //$NON-NLS-1$
	}
	
	@Test
	public void testFileCharSequence() throws Exception {
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < 500; i++) {
			buf.append(TEST_CONTENT);
		}
		String encoding= "ISO-8859-1";
		testForEncoding(buf, encoding);
	}

	@Test
	public void testFileCharSequence2() throws Exception {
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < 2000; i++) {
			buf.append(TEST_CONTENT);
		}
		String encoding= "UTF-8";
		testForEncoding(buf, encoding);
	}

	@Test
	public void testFileCharSequence3() throws Exception {
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < FileCharSequenceProvider.BUFFER_SIZE * 2; i++) {
			buf.append(TEST_CONTENT);
		}
		String encoding= "UTF-16";
		testForEncoding(buf, encoding);
	}

	private void testForEncoding(CharSequence buf, String encoding) throws CoreException, IOException {
		IFolder folder= ResourceHelper.createFolder(fProject.getFolder("folder1"));
		IFile file1= ResourceHelper.createFile(folder, "file1", buf.toString(), encoding);
		
		FileCharSequenceProvider provider= new FileCharSequenceProvider();
		CharSequence cs= null;
		try {
			cs= provider.newCharSequence(file1);
			
			assertEquals(encoding, cs, buf);
			
			assertSubSequence(encoding, cs, buf, 0, buf.length() / 6);
			assertSubSequence(encoding, cs, buf, buf.length() / 6, FileCharSequenceProvider.BUFFER_SIZE);
			
			int pos= 8 * buf.length() / 9;
			assertSubSequence(encoding, cs, buf, pos, buf.length() - pos);
			
			CharSequence seq1a= cs.subSequence(100, 100 + FileCharSequenceProvider.BUFFER_SIZE);
			CharSequence seq1e= buf.subSequence(100, 100 +  FileCharSequenceProvider.BUFFER_SIZE);
			assertSubSequence(encoding, seq1a, seq1e, 500, 500);
			
			assertSubSequence(encoding, seq1a, seq1e, FileCharSequenceProvider.BUFFER_SIZE, 0);
			
		} finally {
			if (cs != null) {
				provider.releaseCharSequence(cs);
			}
			file1.delete(true, null);
		}
	}
	
	private void assertSubSequence(String message, CharSequence actual, CharSequence expected, int start, int length) {
		CharSequence actualSub= actual.subSequence(start, start + length);
		CharSequence expectedSub= expected.subSequence(start, start + length);
		assertEquals(message + " - subsequence(" + start + ", " + length + ")", actualSub, expectedSub);
	}
	
	private void assertEquals(String desc, CharSequence actual, CharSequence expected) {
		for (int i= 0; i < expected.length(); i++) {
			Assert.assertEquals(desc + " - forward " + i, expected.charAt(i), actual.charAt(i));
		}
		Assert.assertEquals(desc + " - length", expected.length(), actual.length());
		for (int i= expected.length() - 1; i >= 0; i--) {
			Assert.assertEquals(desc + " - backward " + i, expected.charAt(i), actual.charAt(i));
		}
		for (int i= 0; i < expected.length(); i+= 567) {
			Assert.assertEquals(desc + " - forward - steps" + i, expected.charAt(i), actual.charAt(i));
		}
		for (int i= 0; i < expected.length(); i+= FileCharSequenceProvider.BUFFER_SIZE) {
			Assert.assertEquals(desc + " - forward - buffersize" + i, expected.charAt(i), actual.charAt(i));
		}
		
		assertOutOfBound(desc + "access at length", actual, expected.length());
		assertOutOfBound(desc + "access at -1", actual, -1);

		Assert.assertEquals(desc + " - length", actual.toString(), expected.toString());
	}
	
	
	private void assertOutOfBound(String message, CharSequence cs, int i) {
		try {
			cs.charAt(i);
		} catch (IndexOutOfBoundsException e) {
			return;
		}
		assertFalse(message, true);
	}


	
}
