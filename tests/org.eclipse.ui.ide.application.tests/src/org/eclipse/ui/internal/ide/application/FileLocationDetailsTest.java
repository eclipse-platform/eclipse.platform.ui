/*******************************************************************************
 * Copyright (c) 2016 Manumitting Technologies Inc and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Manumitting Technologies Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link FileLocationDetails}, used to parse file names from
 * --launcher.openFile
 *
 * @since 3.5
 */
public class FileLocationDetailsTest {
	File tempFile;
	String path;

	@Before
	public void setUp() throws IOException {
		tempFile = File.createTempFile(getClass().getSimpleName(), "java");
		path = tempFile.getAbsolutePath();
	}

	@After
	public void tearDown() {
		tempFile.delete();
	}

	@Test
	public void testPlainFile() {
		FileLocationDetails details = FileLocationDetails.resolve(path);
		assertEquals(path, details.path.toString());
		assertEquals(-1, details.line);
		assertEquals(-1, details.column);
	}

	@Test
	public void testInvalidFormats() {
		assertNull(FileLocationDetails.resolve(path + ":abc"));
		assertNull(FileLocationDetails.resolve(path + "+abc"));
		assertNull(FileLocationDetails.resolve(path + ":-1"));
		assertNull(FileLocationDetails.resolve(path + "+-1"));
	}

	@Test
	public void testLineAsPlus() {
		FileLocationDetails details = FileLocationDetails.resolve(path + "+178");
		assertEquals(path, details.path.toString());
		assertEquals(178, details.line);
		assertEquals(-1, details.column);
	}

	@Test
	public void testLineAsColon() {
		FileLocationDetails details = FileLocationDetails.resolve(path + ":178");
		assertEquals(path, details.path.toString());
		assertEquals(178, details.line);
		assertEquals(-1, details.column);
	}

	@Test
	public void testLineAsPlus_ColumnAsColon() {
		FileLocationDetails details = FileLocationDetails.resolve(tempFile + "+178:3");
		assertEquals(path, details.path.toString());
		assertEquals(178, details.line);
		assertEquals(3, details.column);
	}

	@Test
	public void testLineAsColon_ColumnAsColon() {
		FileLocationDetails details = FileLocationDetails.resolve(tempFile + ":178:3");
		assertEquals(path, details.path.toString());
		assertEquals(178, details.line);
		assertEquals(3, details.column);
	}

	@Test
	public void testLineAsPlus_ColumnAsPlus() {
		FileLocationDetails details = FileLocationDetails.resolve(tempFile + "+178+3");
		assertEquals(path, details.path.toString());
		assertEquals(178, details.line);
		assertEquals(3, details.column);
	}
}
