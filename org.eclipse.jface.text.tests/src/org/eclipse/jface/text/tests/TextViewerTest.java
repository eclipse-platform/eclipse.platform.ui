/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;

/**
 * Basic tests for TextViewer.
 */
public class TextViewerTest extends TestCase {
	public static Test suite() {
		return new TestSuite(TextViewerTest.class);
	}

	private Shell fShell;
	private TextViewer fTextViewer;
	private Document fDocument;

	protected void setUp() {
		fShell= new Shell();
		fTextViewer= new TextViewer(fShell, SWT.NONE);
	}

	protected void tearDown() {
		fShell.dispose();
	}

	public void testSetRedraw_Bug441827() throws Exception {
		fDocument= new Document("abc");
		fTextViewer.setDocument(fDocument);
		int len= fDocument.getLength();
		// Select the whole document with the caret at the beginning.
		fTextViewer.setSelectedRange(len, -len);
		assertEquals(0, fTextViewer.getSelectedRange().x);
		assertEquals(len, fTextViewer.getSelectedRange().y);
		assertEquals(0, fTextViewer.getTextWidget().getCaretOffset());
		fTextViewer.setRedraw(false);
		fTextViewer.setRedraw(true);
		// Check that the selection and the caret position are preserved.
		assertEquals(0, fTextViewer.getSelectedRange().x);
		assertEquals(len, fTextViewer.getSelectedRange().y);
		assertEquals(0, fTextViewer.getTextWidget().getCaretOffset());
	}
}
