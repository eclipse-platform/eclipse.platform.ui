/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others
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

public class BasicEditionTest extends AbstratGenericEditorTest{

	@Override
	protected void createAndOpenFile() throws Exception {
		createAndOpenFile("dummy.txt", "");
	}

	@Test
	public void testNewLineHasIndent() {
		IDocument doc = getSourceViewer().getDocument();
		// Tab only
		doc.set("\t");
		getSourceViewer().setSelectedRange(doc.getLength(), 1);
		getSourceViewer().getTextWidget().insert("\n");
		assertEquals("\t\n\t", doc.get());
		// Space only
		doc.set("   ");
		getSourceViewer().setSelectedRange(doc.getLength(), 1);
		getSourceViewer().getTextWidget().insert("\n");
		assertEquals("   \n   ", doc.get());
	}
}
