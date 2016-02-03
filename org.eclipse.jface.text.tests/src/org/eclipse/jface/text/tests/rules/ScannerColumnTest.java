/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.rules;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;


/**
 * @since 3.4
 */
public class ScannerColumnTest {

	private IDocument fDocument;

	@Before
	public void setUp() {
		fDocument= new Document("scanner test");
	}

	@After
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
