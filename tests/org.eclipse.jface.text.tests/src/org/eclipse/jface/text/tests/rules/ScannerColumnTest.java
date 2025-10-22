/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
package org.eclipse.jface.text.tests.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;


/**
 * @since 3.4
 */
public class ScannerColumnTest {

	private IDocument fDocument;

	@BeforeEach
	public void setUp() {
		fDocument= new Document("scanner test");
	}

	@AfterEach
	public void tearDown() {
		fDocument= null;
	}

	@Test
	public void testRuleBasedScannerColumnRead() {
		_testScannerColumnRead(new RuleBasedScanner());
	}

	@Test
	public void testRuleBasedScannerColumnUnread() {
		_testScannerColumnUnread(new RuleBasedScanner());
	}

	@Test
	public void testBufferedRuleBasedScannerColumnRead() {
		_testScannerColumnRead(new BufferedRuleBasedScanner(100));
	}

	@Test
	public void testBufferedRuleBasedScannerColumnUnread() {
		_testScannerColumnUnread(new BufferedRuleBasedScanner(100));
	}

	private void _testScannerColumnRead(RuleBasedScanner scanner) {
		scanner.setRange(fDocument, 0, 10);
		assertEquals(0, scanner.getColumn());
		assertEquals('s', scanner.read());
		assertEquals(1, scanner.getColumn());
		scanner.unread();
		assertEquals(0, scanner.getColumn());
	}

	private void _testScannerColumnUnread(RuleBasedScanner scanner) {
		scanner.setRange(fDocument, 0, 10);
		assertEquals(0, scanner.getColumn());
		assertEquals('s', scanner.read());
		assertEquals(1, scanner.getColumn());
		scanner.unread();
		assertEquals(0, scanner.getColumn());
	}

}
