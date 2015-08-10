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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.tests.views.properties.tabbed.override.OverrideTestsView;

import org.eclipse.ui.tests.views.properties.tabbed.override.items.EmptyItem;
import org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem;

/**
 * The empty TabFolder is displayed when there is no selected element in the
 * override tests view.
 * <p>
 * The OverrideTestsTabFolderPropertySheetPage example is a before look at the
 * properties view before the migration to the tabbed properties view and the
 * override tabs support. When elements are selected in the OverrideTestsView,
 * TabFolder/TabItem are displayed for the elements.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class EmptyTabFolder extends AbstractTabFolder {

	public boolean appliesTo(IWorkbenchPart part, ISelection selection) {
		if (part instanceof OverrideTestsView) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				if (structuredSelection.equals(StructuredSelection.EMPTY)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public IOverrideTestsItem[] getItem() {
		return new IOverrideTestsItem[] { new EmptyItem() };
	}

}
