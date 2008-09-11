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

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;


/**
 * @since 3.4
 */
public class ScannerColumnTest extends TestCase {

	private IDocument fDocument;

	protected void setUp() throws Exception {
		fDocument= new Document("scanner test");
	}

	protected void tearDown() throws Exception {
		fDocument= null;
	}

	public void testRuleBasedScannerColumnRead() {
		_testScannerColumnRead(new RuleBasedScanner());
	}

	public void testRuleBasedScannerColumnUnread() {
		_testScannerColumnUnread(new RuleBasedScanner());
	}

	public void testBufferedRuleBasedScannerColumnRead() {
		_testScannerColumnRead(new BufferedRuleBasedScanner(100));
	}

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
