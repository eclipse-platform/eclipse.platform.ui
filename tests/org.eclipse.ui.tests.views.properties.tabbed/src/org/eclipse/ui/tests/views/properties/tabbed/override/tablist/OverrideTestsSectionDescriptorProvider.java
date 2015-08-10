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

import org.eclipse.ui.views.properties.tabbed.ISectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptorProvider;

/**
 * Section descriptor provider for the override tests example.
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
public class OverrideTestsSectionDescriptorProvider implements
		ISectionDescriptorProvider {

	private ISectionDescriptor[] sectionDescriptors;

	@Override
	public ISectionDescriptor[] getSectionDescriptors() {
		if (sectionDescriptors == null) {
			sectionDescriptors = new ISectionDescriptor[] { new OverrideTestsSectionDescriptor() };
		}
		return sectionDescriptors;
	}
}
