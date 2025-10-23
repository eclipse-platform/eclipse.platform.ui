/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import org.eclipse.search.internal.core.text.FileCharSequenceProvider;
import org.eclipse.search.tests.ResourceHelper;

public class FileCharSequenceTests {

	private static final String TEST_CONTENT= "ABCDEFGHIJKLMNOPQRSTUVWXYZÜöäüèéùabcdefghijklmnopqrstuvwxyz1234567890@\'\"\n$¢"; //€

	private IProject fProject;

	@BeforeEach
	public void setUp() throws Exception {
		fProject= ResourceHelper.createProject("my-project"); //$NON-NLS-1$
	}

	@AfterEach
	public void tearDown() throws Exception {
		ResourceHelper.deleteProject("my-project"); //$NON-NLS-1$
	}

	@Test
	public void testFileCharSequence() throws Exception {
		StringBuilder buf= new StringBuilder();
		for (int i= 0; i < 500; i++) {
			buf.append(TEST_CONTENT);
		}
		testForEncoding(buf, StandardCharsets.ISO_8859_1.name());
	}

	@Test
	public void testFileCharSequence2() throws Exception {
		StringBuilder buf= new StringBuilder();
		for (int i= 0; i < 2000; i++) {
			buf.append(TEST_CONTENT);
		}
		testForEncoding(buf, StandardCharsets.UTF_8.name());
	}

	@Test
	public void testFileCharSequence3() throws Exception {
		StringBuilder buf= new StringBuilder();
		for (int i= 0; i < FileCharSequenceProvider.BUFFER_SIZE * 2; i++) {
			buf.append(TEST_CONTENT);
		}
		testForEncoding(buf, StandardCharsets.UTF_16.name());
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
			Assertions.assertEquals(expected.charAt(i), actual.charAt(i), desc + " - forward " + i);
		}
		Assertions.assertEquals(expected.length(), actual.length(), desc + " - length");
		for (int i= expected.length() - 1; i >= 0; i--) {
			Assertions.assertEquals(expected.charAt(i), actual.charAt(i), desc + " - backward " + i);
		}
		for (int i= 0; i < expected.length(); i+= 567) {
			Assertions.assertEquals(expected.charAt(i), actual.charAt(i), desc + " - forward - steps" + i);
		}
		for (int i= 0; i < expected.length(); i+= FileCharSequenceProvider.BUFFER_SIZE) {
			Assertions.assertEquals(expected.charAt(i), actual.charAt(i), desc + " - forward - buffersize" + i);
		}

		assertOutOfBound(desc + "access at length", actual, expected.length());
		assertOutOfBound(desc + "access at -1", actual, -1);

		Assertions.assertEquals(expected.toString(), actual.toString(), desc + " - length");
	}


	private void assertOutOfBound(String message, CharSequence cs, int i) {
		try {
			cs.charAt(i);
		} catch (IndexOutOfBoundsException e) {
			return;
		}
		assertFalse(true, message);
	}



}
