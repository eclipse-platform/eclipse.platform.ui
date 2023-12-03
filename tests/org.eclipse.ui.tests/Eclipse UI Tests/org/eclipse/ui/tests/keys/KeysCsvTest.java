/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.keys;

import static org.junit.Assert.assertEquals;

import org.eclipse.ui.internal.util.Util;
import org.junit.Test;

/**
 * @since 3.3
 */
public class KeysCsvTest {

	@Test
	public void testReplace() throws Exception {
		final String src = "Test the \"replaceAll\"";
		final String dest = "Test the \"\"replaceAll\"\"";
		String val = Util.replaceAll(src, "\"", "\"\"");
		assertEquals(dest, val);
	}

	@Test
	public void testReplaceFirst() throws Exception {
		final String src = "\"Hello world!";
		final String dest = "\"\"Hello world!";
		String val = Util.replaceAll(src, "\"", "\"\"");
		assertEquals(dest, val);
	}
}
