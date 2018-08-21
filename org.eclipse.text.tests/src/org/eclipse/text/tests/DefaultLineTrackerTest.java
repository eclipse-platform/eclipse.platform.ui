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
package org.eclipse.text.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class DefaultLineTrackerTest extends TestCase {

	public DefaultLineTrackerTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(DefaultLineTrackerTest.class);
	}

	public void testLineDelimiter() {
		IDocument document= new Document("abc\r\n123\r\nxyz");
		Assert.assertTrue(document.getNumberOfLines() == 3);

		try {

			for (int i= 0; i < 2; i++) {
				Assert.assertTrue(document.getLineLength(i) == 5);
				Assert.assertEquals(document.getLineDelimiter(i), "\r\n");
			}

			Assert.assertTrue(document.getLineLength(2) == 3);
			Assert.assertEquals(document.getLineDelimiter(2), null);

		} catch (BadLocationException x) {
			Assert.fail();
		}
	}
}
