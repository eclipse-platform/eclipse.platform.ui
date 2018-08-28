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
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.workbench;

import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

/**
 * A tabbed property view contributor for the Project Explorer.
 *
 * @since 3.2
 */
public class TabbedPropertySheetProjectExplorerContributor implements
		ITabbedPropertySheetPageContributor {

	private final String contributorId;

	protected TabbedPropertySheetProjectExplorerContributor(CommonNavigator aCommonNavigator) {
		contributorId = aCommonNavigator.getViewSite().getId();
	}

	@Override
	public String getContributorId() {
		return contributorId;
	}

}
