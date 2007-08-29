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

import org.eclipse.ui.tests.views.properties.tabbed.model.Element;
import org.eclipse.ui.tests.views.properties.tabbed.override.items.EmptyItem;
import org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem;

/**
 * The empty tab list is displayed when there is no selected element in the
 * override tests view.
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
public class EmptyTabList extends AbstractTabList {

	private IOverrideTestsItem[] sampleViewItems;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.views.properties.tabbed.override.tablist.AbstractTabList#appliesTo(org.eclipse.ui.tests.views.properties.tabbed.model.Element)
	 */
	public boolean appliesTo(Element element) {
		if (element == null) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.views.properties.tabbed.override.tablist.IOverrideTestsTabList#getItems()
	 */
	public IOverrideTestsItem[] getItems() {
		if (sampleViewItems == null) {
			sampleViewItems = new IOverrideTestsItem[] { new EmptyItem() };
		}
		return sampleViewItems;
	}

}
