/*******************************************************************************
 * Copyright (c) 2001, 2015 IBM Corporation and others.
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
package org.eclipse.ui.views.properties.tabbed;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

/**
 * An abstract implementation of a section in a tab in the tabbed property sheet
 * page.
 *
 * @author Anthony Hunter
 */
public abstract class AbstractPropertySection
	implements ISection {

	/**
	 * The standard label width when labels for sections line up on the left
	 * hand side of the composite.
	 */
	public static final int STANDARD_LABEL_WIDTH = 85;

	/**
	 * The tabbed property sheet page
	 */
	private TabbedPropertySheetPage tabbedPropertySheetPage;

	/**
	 * The current workbench selection.
	 */
	private ISelection selection;

	/**
	 * The current active workbench part.
	 */
	private IWorkbenchPart part;

	/**
	 * Get the widget factory for the property sheet page.
	 *
	 * @return the widget factory.
	 */
	public TabbedPropertySheetWidgetFactory getWidgetFactory() {
		return tabbedPropertySheetPage.getWidgetFactory();
	}

	/**
	 * Get the current workbench selection.
	 *
	 * @return the current workbench selection.
	 */
	public ISelection getSelection() {
		return selection;
	}

	/**
	 * @return Returns the part.
	 */
	public IWorkbenchPart getPart() {
		return part;
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		this.tabbedPropertySheetPage = aTabbedPropertySheetPage;
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#setInput(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		this.selection = selection;
		this.part = part;
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#aboutToBeShown()
	 */
	@Override
	public void aboutToBeShown() {
		/* empty default implementation */
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#aboutToBeHidden()
	 */
	@Override
	public void aboutToBeHidden() {
		/* empty default implementation */
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#dispose()
	 */
	@Override
	public void dispose() {
		/* empty default implementation */
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#getMinimumHeight()
	 */
	@Override
	public int getMinimumHeight() {
		return SWT.DEFAULT;
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#shouldUseExtraSpace()
	 */
	@Override
	public boolean shouldUseExtraSpace() {
		return false;
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
	 */
	@Override
	public void refresh() {
		/* empty default implementation */
	}
}
