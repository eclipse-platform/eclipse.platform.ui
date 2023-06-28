/*******************************************************************************
 * Copyright (c) 2020 Pierre-Yves Bigourdan and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Pierre-Yves Bigourdan - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.texteditor.DeleteLineAction;
import org.eclipse.ui.texteditor.TextViewerDeleteLineTarget;

public class TextViewerDeleteLineTargetTest {

	private Document document;

	private TextViewerDeleteLineTarget underTest;

	@Before
	public void setUp() {
		document= new Document("first line\n" +
				"\n" +
				"third line\n");

		TextViewer textViewer= new TextViewer(new Shell(), SWT.NONE);
		textViewer.setDocument(document);

		underTest= new TextViewerDeleteLineTarget(textViewer);
	}

	@Test
	public void testWholeLineDeletion() throws Exception {
		underTest.deleteLine(document, new TextSelection(document, 1, 3), DeleteLineAction.WHOLE, false);

		assertEquals("\n" +
				"third line\n", document.get());
	}

	@Test
	public void testWholeLineDeletionOnLastEmptyLine() throws Exception {
		underTest.deleteLine(document, new TextSelection(document, 23, 0), DeleteLineAction.WHOLE, false);

		assertEquals("first line\n" +
				"\n" +
				"third line", document.get());
	}

	@Test
	public void testWholeLineDeletionWithCopyToClipboard() throws Exception {
		Clipboard clipboard= new Clipboard(Display.getCurrent());
		try {
			underTest.deleteLine(document, new TextSelection(document, 1, 4), DeleteLineAction.WHOLE, true);

			assertEquals("first line\n", clipboard.getContents(TextTransfer.getInstance()));
		} finally {
			clipboard.dispose();
		}
	}

	@Test
	public void testLineDeletionToBeginning() throws Exception {
		underTest.deleteLine(document, new TextSelection(document, 6, 0), DeleteLineAction.TO_BEGINNING, false);

		assertEquals("line\n" +
				"\n" +
				"third line\n", document.get());
	}

	@Test
	public void testLineDeletionToEnd() throws Exception {
		underTest.deleteLine(document, new TextSelection(document, 17, 0), DeleteLineAction.TO_END, false);

		assertEquals("first line\n" +
				"\n" +
				"third\n", document.get());
	}

	@Test
	public void testThrowsExceptionWithUnsupportedDeleteLineActionType() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> underTest.deleteLine(document, 0, 0, IAction.AS_RADIO_BUTTON, false));
	}

}
