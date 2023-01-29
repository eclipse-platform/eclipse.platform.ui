/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
			if (selection instanceof IStructuredSelection structuredSelection) {
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
