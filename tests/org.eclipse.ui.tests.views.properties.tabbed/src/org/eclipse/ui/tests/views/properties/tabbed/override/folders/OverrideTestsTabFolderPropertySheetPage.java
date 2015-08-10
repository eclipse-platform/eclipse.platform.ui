/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.override.folders;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * The OverrideTestsTabFolderPropertySheetPage example is a before look at the
 * properties view before the migration to the tabbed properties view and the
 * override tabs support. When elements are selected in the OverrideTestsView,
 * TabFolder/TabItem are displayed for the elements.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class OverrideTestsTabFolderPropertySheetPage implements
		IPropertySheetPage {

	private Composite composite;

	private OverrideTestsTabFolderPropertySheetPageContentManager contentManager;

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		contentManager = new OverrideTestsTabFolderPropertySheetPageContentManager(
				composite);
	}

	@Override
	public void dispose() {
		composite.dispose();
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		contentManager.selectionChanged(part, selection);
	}

	@Override
	public void setActionBars(IActionBars actionBars) {
		// Not implemented
	}

	@Override
	public void setFocus() {
		composite.setFocus();
	}

}
