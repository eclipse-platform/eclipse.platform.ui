/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.internal.navigator.CommonViewerSiteDelegate;
import org.eclipse.ui.internal.navigator.CommonViewerSiteIEditorPartSiteDelegate;
import org.eclipse.ui.internal.navigator.CommonViewerSiteIPageSiteDelegate;
import org.eclipse.ui.internal.navigator.CommonViewerSiteIViewSiteDelegate;
import org.eclipse.ui.part.IPageSite;

/**
 * Allows clients to create {@link ICommonViewerSite} for a variety of contexts.
 * The {@link ICommonViewerSite} may be used by the
 * {@link NavigatorActionService} to allow customization for any
 * {@link CommonActionProvider} used by a particular instance of the Common
 * Navigator.
 *  
 * 
 * @since 3.2
 */
public final class CommonViewerSiteFactory {
	/**
	 * 
	 * @param aViewSite
	 *            The viewer site that should be delegated to to satisfy the
	 *            contract of ICommonViewerSite.
	 * @return An ICommonViewerSite that delegates to the given parameter.
	 */
	public static ICommonViewerWorkbenchSite createCommonViewerSite(
			IViewSite aViewSite) {
		return new CommonViewerSiteIViewSiteDelegate(aViewSite);
	}

	/**
	 * 
	 * @param aEditorSite
	 *            The editor site that should be delegated to to satisfy the
	 *            contract of ICommonViewerSite.
	 * @return An ICommonViewerSite that delegates to the given parameter.
	 */
	public static ICommonViewerWorkbenchSite createCommonViewerSite(
			IEditorSite aEditorSite) {
		return new CommonViewerSiteIEditorPartSiteDelegate(aEditorSite);
	}

	/**
	 * 
	 * @param anId
	 *            The unique identifier corresponding to the abstract viewer for
	 *            the returned ICommonViewerSite.
	 * 
	 * @param aSelectionProvider
	 *            The selection provider that will initially be returned by
	 *            {@link ICommonViewerSite#getSelectionProvider()}
	 * 
	 * @param aShell
	 *            The shell that will be returned by
	 *            {@link ICommonViewerSite#getShell()}
	 * @return An ICommonViewerSite that delegates to the given parameter.
	 */
	public static ICommonViewerSite createCommonViewerSite(String anId,
			ISelectionProvider aSelectionProvider, Shell aShell) {
		return new CommonViewerSiteDelegate(anId, aSelectionProvider, aShell);
	}

	/**
	 * 
	 * @param anId
	 *            The unique identifier corresponding to the abstract viewer for
	 *            the returned ICommonViewerSite.
	 * @param aPageSite
	 *            The page site that should be delegated to to satisfy the
	 *            contract of ICommonViewerSite.
	 * @return An ICommonViewerSite that delegates to the given parameter.
	 */
	public static ICommonViewerSite createCommonViewerSite(String anId,
			IPageSite aPageSite) {
		return new CommonViewerSiteIPageSiteDelegate(anId, aPageSite);
	}

}
