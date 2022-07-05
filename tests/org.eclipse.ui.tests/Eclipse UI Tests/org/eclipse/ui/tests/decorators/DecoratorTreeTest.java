/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * The DecoratorTreeTest tests the font and color support on
 * tree viewers.
 */
@RunWith(JUnit4.class)
@Ignore("Disabled due to timing issues")
public class DecoratorTreeTest extends DecoratorViewerTest {

	public DecoratorTreeTest() {
		super(DecoratorTreeTest.class.getSimpleName());

	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();

	}

	@Override
	protected void backgroundCheck(IViewPart view) {
		TreeItem first = ((DecoratorTreeView) view).viewer.getTree().getItems()[0];
		assertEquals(BackgroundColorDecorator.color.getRGB(), first.getBackground().getRGB());
	}

	@Override
	protected void foregroundCheck(IViewPart view) {

		TreeItem first = ((DecoratorTreeView) view).viewer.getTree().getItems()[0];
		assertEquals(ForegroundColorDecorator.color.getRGB(), first.getForeground().getRGB());

	}

	@Override
	protected IViewPart openView(IWorkbenchPage page) throws PartInitException {

		return page.showView("org.eclipse.ui.tests.decorators.TreeViewTest");

	}

	@Override
	protected void fontCheck(IViewPart view) {
		TreeItem first = ((DecoratorTreeView) view).viewer.getTree().getItems()[0];
		assertEquals(FontDecorator.font.getFontData()[0], first.getFont().getFontData()[0]);
	}
}
