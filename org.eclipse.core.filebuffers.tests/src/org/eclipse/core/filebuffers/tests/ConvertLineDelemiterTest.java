/*******************************************************************************
 * Copyright (c) 2022 Joerg Kubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Joerg Kubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Function;

import org.junit.Test;


import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.manipulation.ConvertLineDelimitersOperation;
import org.eclipse.core.filebuffers.manipulation.FileBufferOperationRunner;

public class ConvertLineDelemiterTest {

	private static final String[] DELIMS= new String[] {
			"\r",
			"\n",
			"\r\n",
	};

	@Test
	public void testWithDelimnAtEnd() throws Exception {
		test(delim -> delim + "line1" + delim + "line2" + delim + delim + "line3" + delim);
	}

	@Test
	public void testWithoutDelimnAtEnd() throws Exception {
		test(delim -> "line1" + delim + "line2" + delim + delim + "line3");
	}

	void test(Function<String, String> testFile) throws Exception {
		IProject p= ResourcesPlugin.getWorkspace().getRoot().getProject("ConvertLineDelemiterTest");
		p.create(null);
		p.open(null);
		try {
			for (String outputDelim : DELIMS) {
				IFile[] files= new IFile[DELIMS.length];
				int i= 0;
				for (String inputDelim : DELIMS) {
					String input= testFile.apply(inputDelim);
					IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/ConvertLineDelemiterTest/test" + i + ".txt"));
					InputStream s= new ByteArrayInputStream(input.getBytes());
					file.create(s, true, null);
					files[i++]= file;
				}
				FileBufferOperationRunner runner= new FileBufferOperationRunner(FileBuffers.getTextFileBufferManager(), null);
				ConvertLineDelimitersOperation op= new ConvertLineDelimitersOperation(outputDelim);
				runner.execute(Arrays.stream(files).map(f -> f.getFullPath()).toArray(IPath[]::new), op, null);
				for (IFile file : files) {
					String actual= Files.readString(file.getLocation().toFile().toPath());
					String expected= testFile.apply(outputDelim);
					assertEquals(readable(expected), readable(actual));
				}
				for (IFile file : files) {
					file.delete(true, null);
				}
			}
		} finally {
			p.delete(true, null);
		}
	}

	private String readable(String s) {
		s= s.replace("\r", "\\r");
		s= s.replace("\n", "\\n");
		return s;
	}
}
