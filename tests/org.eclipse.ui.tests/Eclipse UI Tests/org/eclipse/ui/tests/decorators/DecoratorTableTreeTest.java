/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * The DecoratorTableTreeTest is the test for table 
 * trees.
 */
public class DecoratorTableTreeTest extends DecoratorViewerTest {

	/**
	 * Create a new instance of the receiver.
	 * @param testName
	 */
	public DecoratorTableTreeTest(String testName) {
		super(testName);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.decorators.DecoratorViewerTest#backgroundCheck(org.eclipse.ui.IViewPart)
	 */
	protected void backgroundCheck(IViewPart view) {
		TableTreeItem first = ((DecoratorTableTreeView) view).viewer.getTableTree().getItems()[0];
		Assert.isTrue(first.getBackground().getRGB()
				.equals(BackgroundColorDecorator.color.getRGB()));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.decorators.DecoratorViewerTest#foregroundCheck(org.eclipse.ui.IViewPart)
	 */
	protected void foregroundCheck(IViewPart view) {
		TableTreeItem first = ((DecoratorTableTreeView) view).viewer.getTableTree().getItems()[0];
		Assert.isTrue(first.getForeground().getRGB()
				.equals(ForegroundColorDecorator.color.getRGB()));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.decorators.DecoratorViewerTest#openView(org.eclipse.ui.IWorkbenchPage)
	 */
	protected IViewPart openView(IWorkbenchPage page) throws PartInitException {
		return page.showView("org.eclipse.ui.tests.decorator.TableTreeTest");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.decorators.DecoratorViewerTest#fontCheck(org.eclipse.ui.IViewPart)
	 */
	protected void fontCheck(IViewPart view) {
		TableTreeItem first = ((DecoratorTableTreeView) view).viewer.getTableTree().getItems()[0];
		Assert.isTrue(first.getFont().getFontData()[0]
				.equals(FontDecorator.font.getFontData()[0]));

	}

}
