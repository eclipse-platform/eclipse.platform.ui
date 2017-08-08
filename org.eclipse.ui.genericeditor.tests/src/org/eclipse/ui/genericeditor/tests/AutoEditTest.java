/*******************************************************************************
 * Copyright (c) 2017 Rogue Wave Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class AutoEditTest extends AbstratGenericEditorTest {

	@Test
	public void testAutoEdit() throws Exception {
		IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
		StyledText control= (StyledText) editor.getAdapter(Control.class);
		control.setText("");
		// order of auto-edits from most specialized to least specialized
		Assert.assertEquals("AutoAddedThird!AutoAddedSecond!AutoAddedFirst!", document.get());
	}

}
