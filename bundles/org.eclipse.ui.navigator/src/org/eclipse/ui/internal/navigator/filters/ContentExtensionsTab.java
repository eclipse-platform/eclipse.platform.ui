/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.navigator.filters;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * @since 3.2
 *
 */
public class ContentExtensionsTab extends CustomizationTab {

	protected ContentExtensionsTab(Composite parent,
			INavigatorContentService aContentService) {
		super(parent, aContentService);
		createControl();

	}

	private void createControl() {

		createInstructionsLabel(CommonNavigatorMessages.CommonFilterSelectionDialog_Select_the_available_extensions);

		createTable();

		getTableViewer().setContentProvider(new ContentDescriptorContentProvider());
		getTableViewer().setLabelProvider(new CommonFilterLabelProvider());
		getTableViewer().setInput(getContentService());

		updateCheckedState();

	}


	private void updateCheckedState() {
		INavigatorContentDescriptor[] visibleExtensions = getContentService()
				.getVisibleExtensions();
		for (INavigatorContentDescriptor visibleExtension : visibleExtensions) {
			if (getContentService().isActive(visibleExtension.getId())) {
				getTableViewer().setChecked(visibleExtension, true);
			}
		}

	}

}
