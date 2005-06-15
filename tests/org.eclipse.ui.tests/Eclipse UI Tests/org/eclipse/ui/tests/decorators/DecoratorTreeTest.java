/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * The DecoratorTreeTest tests the font and color support on 
 * tree viewers.
 */
public class DecoratorTreeTest extends DecoratorViewerTest {

	/**
	 * @param testName
	 */
	public DecoratorTreeTest(String testName) {
		super(testName);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.decorators.DecoratorViewerTest#backgroundCheck(org.eclipse.ui.part.ViewPart)
	 */
	protected void backgroundCheck(IViewPart view) {
		TreeItem first = ((DecoratorTreeView) view).viewer.getTree().getItems()[0];
		assertEquals(BackgroundColorDecorator.color.getRGB(), first.getBackground().getRGB());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.decorators.DecoratorViewerTest#foregroundCheck(org.eclipse.ui.part.ViewPart)
	 */
	protected void foregroundCheck(IViewPart view) {

		TreeItem first = ((DecoratorTreeView) view).viewer.getTree().getItems()[0];
		assertEquals(ForegroundColorDecorator.color.getRGB(), first.getForeground().getRGB());

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.decorators.DecoratorViewerTest#openView(org.eclipse.ui.IWorkbenchPage)
	 */
	protected IViewPart openView(IWorkbenchPage page) throws PartInitException {

		return page.showView("org.eclipse.ui.tests.decorators.TreeViewTest");

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.decorators.DecoratorViewerTest#fontCheck(org.eclipse.ui.part.ViewPart)
	 */
	protected void fontCheck(IViewPart view) {
		TreeItem first = ((DecoratorTreeView) view).viewer.getTree().getItems()[0];
		assertEquals(FontDecorator.font.getFontData()[0], first.getFont().getFontData()[0]);
	}
}
