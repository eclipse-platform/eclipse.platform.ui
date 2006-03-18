/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor#getContributorId()
	 */
	public String getContributorId() { 
		return contributorId;
	}

}
