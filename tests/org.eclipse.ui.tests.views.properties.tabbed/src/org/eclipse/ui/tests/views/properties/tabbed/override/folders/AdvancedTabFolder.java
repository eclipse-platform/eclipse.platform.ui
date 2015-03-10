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

import org.eclipse.ui.tests.views.properties.tabbed.model.Element;
import org.eclipse.ui.tests.views.properties.tabbed.model.File;
import org.eclipse.ui.tests.views.properties.tabbed.model.Folder;
import org.eclipse.ui.tests.views.properties.tabbed.override.items.FileItem;
import org.eclipse.ui.tests.views.properties.tabbed.override.items.FolderItem;
import org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem;

/**
 * The advanced TabFolder is displayed when Information, Warning or Error is the
 * selected element in the override tests view.
 * <p>
 * The OverrideTestsTabFolderPropertySheetPage example is a before look at the
 * properties view before the migration to the tabbed properties view and the
 * override tabs support. When elements are selected in the OverrideTestsView,
 * TabFolder/TabItem are displayed for the elements.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class AdvancedTabFolder extends AbstractTabFolder {

	/*
	 * (non-Javadoc)
	 *
	 * @see asd.views.folders.AbstractSampleViewFolder#appliesTo(asd.views.elements.ISampleViewElement)
	 */
	public boolean appliesTo(Element element) {
		return ((element instanceof File) || (element instanceof Folder));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see asd.views.folders.IAaaFolder#getAaaItem()
	 */
	public IOverrideTestsItem[] getItem() {
		return new IOverrideTestsItem[] { new FileItem(), new FolderItem() };
	}

}
