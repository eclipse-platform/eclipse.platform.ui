/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * The DecoratorTableTest is the test for decorating tables.
 */
@RunWith(JUnit4.class)
@Ignore("Disabled due to timing issues")
public class DecoratorTableTest extends DecoratorViewerTest {

	/**
	 * Create a new instance of the receiver.
	 */
	public DecoratorTableTest() {
		super(DecoratorTableTest.class.getSimpleName());
	}

	@Override
	protected void backgroundCheck(IViewPart view) {
		TableItem first = ((DecoratorTableView) view).viewer.getTable().getItem(0);
		Assert.isTrue(first.getBackground().getRGB()
				.equals(BackgroundColorDecorator.color.getRGB()));

	}

	@Override
	protected void foregroundCheck(IViewPart view) {
		TableItem first = ((DecoratorTableView) view).viewer.getTable().getItem(0);
		Assert.isTrue(first.getForeground().getRGB()
				.equals(ForegroundColorDecorator.color.getRGB()));

	}

	@Override
	protected IViewPart openView(IWorkbenchPage page) throws PartInitException {
		return page.showView("org.eclipse.ui.tests.decorator.TableViewTest");
	}

	@Override
	protected void fontCheck(IViewPart view) {
		TableItem first = ((DecoratorTableView) view).viewer.getTable().getItem(0);
		Assert.isTrue(first.getFont().getFontData()[0].equals(FontDecorator.font.getFontData()[0]));

	}

}
