/*
 * Created on Sep 27, 2004
 *
 * XXX To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.tests.decorators;

import org.eclipse.swt.custom.TableTreeItem;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import org.eclipse.ui.internal.misc.Assert;

/**
 * @author tod
 *
 * XXX To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
