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
package org.eclipse.ui.tests.views.properties.tabbed.override.tablist;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractOverridableTabListPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabItem;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * Section for a tab list used by the tabbed properties view for the override
 * tests example.
 * <p>
 * The OverrideTestsView TabbedPropertySheetPage example is a look at the
 * properties view after the migration of a TabFolder/TabItem framework to the
 * tabbed properties view. In the case of a TabFolder, the folder (provider)
 * knows both the tab labels and tab items. This aligns to the tabbed properties
 * view, but the tab labels are tab descriptors and tab items are section
 * descriptions. This does not work with the default framework as the tabs
 * provide the sections. In this case, the IOverridableTabListContentProvider
 * framework has been provided.
 * <p>
 * The overridable tab list is a content provider that provides both the
 * sections and the tab labels.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class OverrideTestsSection extends
		AbstractOverridableTabListPropertySection {
	private OverrideTestsTabListsContentsManager contentsManager;

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		contentsManager = new OverrideTestsTabListsContentsManager(parent,
				tabbedPropertySheetPage, this);
	}

	@Override
	public void dispose() {
		if (contentsManager != null) {
			contentsManager.dispose();
			contentsManager = null;
		}
	}

	@Override
	public ITabItem[] getTabs() {
		if (contentsManager != null) {
			return contentsManager.getTabs();
		}
		return null;
	}

	@Override
	public void selectTab(int index) {
		if (contentsManager != null) {
			contentsManager.selectTab(index);
		}
	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		if (contentsManager != null) {
			contentsManager.selectionChanged(getPart(), getSelection());
		}
	}

	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}
}
