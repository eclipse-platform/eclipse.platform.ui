/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.keys;

import org.eclipse.ui.internal.util.Util;

import junit.framework.TestCase;

/**
 * @since 3.3
 *
 */
public class KeysCsvTest extends TestCase {

	public void testReplace() throws Exception {
		final String src = "Test the \"replaceAll\"";
		final String dest = "Test the \"\"replaceAll\"\"";
		String val = Util.replaceAll(src, "\"", "\"\"");
		assertEquals(dest, val);
	}
	
	public void testReplaceFirst() throws Exception {
		final String src = "\"Hello world!";
		final String dest = "\"\"Hello world!";
		String val = Util.replaceAll(src, "\"", "\"\"");
		assertEquals(dest, val);
	}
}
