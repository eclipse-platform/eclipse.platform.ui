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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class DefaultLineTrackerTest {

	@Test
	public void testLineDelimiter() throws BadLocationException {
		IDocument document = new Document("abc\r\n123\r\nxyz");
		assertEquals(3, document.getNumberOfLines());

		for (int i = 0; i < 2; i++) {
			assertEquals(5, document.getLineLength(i));
			assertEquals(document.getLineDelimiter(i), "\r\n");
		}

		assertEquals(3, document.getLineLength(2));
		assertEquals(document.getLineDelimiter(2), null);

	}
}
