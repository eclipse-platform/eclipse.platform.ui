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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.tests.views.properties.tabbed.model.Element;
import org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem;
import org.eclipse.ui.views.properties.tabbed.IOverridableTabList;

/**
 * Interface for a tab list used by the tabbed properties view for the override
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
public interface IOverrideTestsTabList extends IOverridableTabList {

	/**
	 * Determines if this tab list applies to the element.
	 * 
	 * @param element
	 *            the element.
	 * @return <code>true</code> if this folder applies to the element.
	 */
	public boolean appliesTo(Element element);

	/**
	 * Creates the controls for the tab list.
	 * 
	 * @param parent
	 *            the parent composite for the contents.
	 */
	public void createControls(Composite parent);

	/**
	 * Dispose the controls for the tab list.
	 */
	public void dispose();

	/**
	 * Get the items for this tab list.
	 * 
	 * @return the items for this tab list.
	 */
	public IOverrideTestsItem[] getItems();

	/**
	 * Notifies the tab list that the selected element has changed.
	 * 
	 * @param element
	 *            the selected element.
	 */
	public void selectionChanged(Element element);

}
