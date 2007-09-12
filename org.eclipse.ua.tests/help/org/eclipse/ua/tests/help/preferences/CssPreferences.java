/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.webapp.data.CssUtil;

import junit.framework.TestCase;

/*
 * Test the parsing of CSS preferences
 */
public class CssPreferences extends TestCase {
	
	private static final String ORG_ECLIPSE_TEST = "org.eclipse.test/";
	private static final String FILE_CSS = "file.css";
	private static final String ORG_ECLIPSE_TEST_FILE_CSS = ORG_ECLIPSE_TEST + FILE_CSS;
	private static final String FILENAME_WITH_PARAM = ORG_ECLIPSE_TEST + "${os}" + FILE_CSS;
	private static final String FILENAME_WITH_OS = ORG_ECLIPSE_TEST + Platform.getOS() + FILE_CSS;

	public void testNull() {
		String[] options = CssUtil.getCssFilenames(null);
		assertEquals(0, options.length);
	}
	
	public void testEmptyString() {
		String[] options = CssUtil.getCssFilenames("");
		assertEquals(0, options.length);
	}
	
	public void testSingleString() {
		String[] options = CssUtil.getCssFilenames(ORG_ECLIPSE_TEST_FILE_CSS);
		assertEquals(1, options.length);
		assertEquals(ORG_ECLIPSE_TEST_FILE_CSS, options[0]);
	}
	
	public void testTwoStrings() {
		String[] options = CssUtil.getCssFilenames(ORG_ECLIPSE_TEST_FILE_CSS + " , " + FILENAME_WITH_PARAM);
		assertEquals(2, options.length);
		assertEquals(ORG_ECLIPSE_TEST_FILE_CSS, options[0]);
		assertEquals(FILENAME_WITH_OS, options[1]);
	}


}
