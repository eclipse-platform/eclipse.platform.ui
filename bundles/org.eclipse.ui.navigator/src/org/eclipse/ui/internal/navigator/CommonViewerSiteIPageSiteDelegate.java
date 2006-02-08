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

package org.eclipse.ui.internal.navigator;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.part.IPageSite;

/**
 * Provides a delegate implementation of {@link ICommonViewerSite}.
 * 
 * @since 3.2
 * 
 */
public class CommonViewerSiteIPageSiteDelegate implements ICommonViewerSite {

	private IPageSite pageSite;

	private String viewerId;

	/**
	 * 
	 * @param aViewerId
	 * @param aPageSite
	 */
	public CommonViewerSiteIPageSiteDelegate(String aViewerId,
			IPageSite aPageSite) {
		viewerId = aViewerId;
		pageSite = aPageSite;
	}

	public String getId() {
		return viewerId;
	}

	public Object getAdapter(Class adapter) {
		return pageSite.getAdapter(adapter);
	}

	public ISelectionProvider getSelectionProvider() {
		return pageSite.getSelectionProvider();
	}

	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		pageSite.setSelectionProvider(aSelectionProvider);
	}

	public Shell getShell() {
		return pageSite.getShell();
	}

}
