/*******************************************************************************
 * Copyright (c) 2020, 2025 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

public class BasicEditionTest extends AbstratGenericEditorTest{

	@Override
	protected void createAndOpenFile() throws Exception {
		createAndOpenFile("dummy.txt", "");
	}

	@Test
	public void testNewLineHasIndent() {
		ITextViewer sourceViewer = editor.getAdapter(ITextViewer.class);
		IDocument doc = sourceViewer.getDocument();
		// Tab only
		doc.set("\t");
		sourceViewer.setSelectedRange(doc.getLength(), 1);
		sourceViewer.getTextWidget().insert("\n");
		assertEquals("\t\n\t", doc.get());
		// Space only
		doc.set("   ");
		sourceViewer.setSelectedRange(doc.getLength(), 1);
		sourceViewer.getTextWidget().insert("\n");
		assertEquals("   \n   ", doc.get());
	}
}
