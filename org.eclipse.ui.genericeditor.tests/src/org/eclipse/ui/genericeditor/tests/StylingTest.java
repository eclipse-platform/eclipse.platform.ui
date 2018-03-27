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

import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;

public class StylingTest extends AbstratGenericEditorTest {

	@Test
	public void testStyle() throws Exception {
		editor.selectAndReveal(4, 8);
		StyledText widget = (StyledText) editor.getAdapter(Control.class);
		StyleRange style= widget.getStyleRangeAtOffset(4);//get the style of first token
		boolean isRed= style.foreground.getRGB().equals(new RGB(255, 0, 0));//is it Red?
		Assert.assertTrue("Token is not of expected color", isRed);
	}

	@Test
	public void testEnabledWhenStyle() throws Exception {
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		editor.selectAndReveal(4, 8);
		StyledText widget = (StyledText) editor.getAdapter(Control.class);
		StyleRange style= widget.getStyleRangeAtOffset(4);//get the style of first token
		boolean isBlue= style.foreground.getRGB().equals(new RGB(0, 0, 255));//is it Blue?
		Assert.assertTrue("Token is not of expected color", isBlue);
		cleanFileAndEditor();

		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		editor.selectAndReveal(4, 8);
		widget = (StyledText) editor.getAdapter(Control.class);
		assertNull(widget.getStyleRangeAtOffset(4));
	}

}
