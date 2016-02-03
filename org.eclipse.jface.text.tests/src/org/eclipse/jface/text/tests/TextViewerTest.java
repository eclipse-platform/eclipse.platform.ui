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


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;

/**
 * Basic tests for TextViewer.
 */
public class TextViewerTest {
	
	@Test
	public void testSetRedraw_Bug441827() throws Exception {
		Shell shell= new Shell();
		try {
			TextViewer textViewer= new TextViewer(shell, SWT.NONE);
			Document document= new Document("abc");
			textViewer.setDocument(document);
			int len= document.getLength();
			// Select the whole document with the caret at the beginning.
			textViewer.setSelectedRange(len, -len);
			assertEquals(0, textViewer.getSelectedRange().x);
			assertEquals(len, textViewer.getSelectedRange().y);
			assertEquals(0, textViewer.getTextWidget().getCaretOffset());
			textViewer.setRedraw(false);
			textViewer.setRedraw(true);
			// Check that the selection and the caret position are preserved.
			assertEquals(0, textViewer.getSelectedRange().x);
			assertEquals(len, textViewer.getSelectedRange().y);
			assertEquals(0, textViewer.getTextWidget().getCaretOffset());
		} finally {
			shell.dispose();
		}
	}
}
