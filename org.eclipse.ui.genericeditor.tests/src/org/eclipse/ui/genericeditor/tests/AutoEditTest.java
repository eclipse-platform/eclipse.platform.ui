/*******************************************************************************
 * Copyright (c) 2017 Rogue Wave Software Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Michał Niewrzał (Rogue Wave Software Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;

public class AutoEditTest extends AbstratGenericEditorTest {

	@Test
	public void testAutoEdit() throws Exception {
		IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
		StyledText control= (StyledText) editor.getAdapter(Control.class);
		control.setText("");
		// order of auto-edits from most specialized to least specialized
		Assert.assertEquals("AutoAddedThird!AutoAddedSecond!AutoAddedFirst!", document.get());
	}

	@Test
	public void testEnabledWhenAutoEdit() throws Exception {
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
		StyledText control= (StyledText) editor.getAdapter(Control.class);
		control.setText("");
		// order of auto-edits from most specialized to least specialized
		Assert.assertEquals("AutoAddedFirst!", document.get());
		cleanFileAndEditor();

		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
		control= (StyledText) editor.getAdapter(Control.class);
		control.setText("");
		// order of auto-edits from most specialized to least specialized
		Assert.assertEquals("", document.get());
	}
}
