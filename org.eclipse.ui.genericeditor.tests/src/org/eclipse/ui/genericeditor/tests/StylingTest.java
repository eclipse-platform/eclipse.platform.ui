/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.texteditor.AbstractTextEditor;

public class StylingTest {

	private AbstractTextEditor editor;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		GenericEditorTestUtils.setUpBeforeClass();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		GenericEditorTestUtils.tearDownAfterClass();
	}

	@Before
	public void setUp() throws Exception {
		GenericEditorTestUtils.closeIntro();

		editor = (AbstractTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(GenericEditorTestUtils.getFile()), "org.eclipse.ui.genericeditor.GenericEditor");
	}

	@After
	public void tearDown() throws Exception {
		editor.getSite().getPage().closeEditor(editor, false);
		editor= null;
	}

	@Test
	public void testStyle() throws Exception {

		editor.selectAndReveal(4, 8);
		StyledText widget = (StyledText) editor.getAdapter(Control.class);
		StyleRange style= widget.getStyleRangeAtOffset(4);//get the style of first token
		boolean isRed= style.foreground.getRGB().equals(new RGB(255, 0, 0));//is it Red?
		Assert.assertTrue("Token is not of expected color", isRed);
	}

}
