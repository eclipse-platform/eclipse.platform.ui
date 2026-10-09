/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.rules;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.RuleBasedScanner;

/**
 * Tests that {@link RuleBasedScanner} reuses its line delimiters only while they are up to date.
 */
public class RuleBasedScannerDelimitersTest {

	private static class DelimiterDocument extends Document {
		String[] delimiters= { "\n" };

		DelimiterDocument() {
			super("scanner test");
		}

		@Override
		public String[] getLegalLineDelimiters() {
			return delimiters;
		}
	}

	@Test
	public void testDelimitersReusedForUnchangedDocument() {
		RuleBasedScanner scanner= new RuleBasedScanner();
		Document document= new Document("scanner test");
		scanner.setRange(document, 0, 4);
		char[][] delimiters= scanner.getLegalLineDelimiters();
		scanner.setRange(document, 4, 4);
		assertSame(delimiters, scanner.getLegalLineDelimiters());
	}

	@Test
	public void testDelimitersRebuiltWhenDocumentDelimitersChange() {
		RuleBasedScanner scanner= new RuleBasedScanner();
		DelimiterDocument document= new DelimiterDocument();
		scanner.setRange(document, 0, 4);
		// modified in place, as an IDocument may reuse the returned array
		document.delimiters[0]= "\r";
		scanner.setRange(document, 0, 4);
		assertArrayEquals(new char[][] { { '\r' } }, scanner.getLegalLineDelimiters());
	}

	@Test
	public void testDelimitersRebuiltWhenReplacedBySubclass() {
		class ReplacingScanner extends RuleBasedScanner {
			void replaceDelimiters() {
				fDelimiters= new char[][] { { 'x' } };
			}
		}
		ReplacingScanner scanner= new ReplacingScanner();
		DelimiterDocument document= new DelimiterDocument();
		scanner.setRange(document, 0, 4);
		scanner.replaceDelimiters();
		scanner.setRange(document, 0, 4);
		assertArrayEquals(new char[][] { { '\n' } }, scanner.getLegalLineDelimiters());
	}
}
