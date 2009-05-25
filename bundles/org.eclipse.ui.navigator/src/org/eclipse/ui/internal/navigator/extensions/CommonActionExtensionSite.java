/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;

/**
 * 
 * Provides access to information required for the initialization of
 * CommonActionProviders.
 * 
 * <p>
 * See the documentation of the <b>org.eclipse.ui.navigator.navigatorContent</b>
 * extension point and {@link CommonActionProvider} for more information on
 * declaring {@link CommonActionProvider}s.
 * </p>
 * 
 * 
 * @since 3.2
 */
public final class CommonActionExtensionSite extends CommonExtensionSite
		implements ICommonActionExtensionSite {

	private String extensionId;

	private String pluginId;
	
	private ICommonViewerSite commonViewerSite;

	private StructuredViewer structuredViewer;

	/**
	 * Create a config element for the initialization of Common Action
	 * Providers.
	 * 
	 * @param anExtensionId
	 *            The unique identifier of the associated content extension or
	 *            the top-level action provider. <b>May NOT be null.</b>
	 * @param aPluginId
	 *            The id of the plugin that contributes this CommonActionProvider
	 * @param aCommonViewerSite
	 *            The common viewer site may be used to access information about
	 *            the part for which the instantiated CommonActionProvider will
	 *            be used. <b>May NOT be null.</b>
	 * @param aContentService
	 *            The associated content service to allow coordination with
	 *            content extensions via the IExtensionStateModel. Clients may
	 *            access the content providers and label providers as necessary
	 *            also to render labels or images in their UI. <b>May NOT be
	 *            null.</b>
	 * @param aStructuredViewer
	 *            The viewer control that will use the instantiated Common
	 *            Action Provider. <b>May NOT be null.</b>
	 */
	public CommonActionExtensionSite(String anExtensionId,
			String aPluginId,
			ICommonViewerSite aCommonViewerSite,
			NavigatorContentService aContentService,
			StructuredViewer aStructuredViewer) {
		super(aContentService, anExtensionId); 

		Assert.isNotNull(aCommonViewerSite);
		Assert.isNotNull(aStructuredViewer);
		extensionId = anExtensionId;
		pluginId = aPluginId;
		commonViewerSite = aCommonViewerSite;
		structuredViewer = aStructuredViewer;

	}

	/**
	 * 
	 * @return The unique identifier of the associated content extension or the
	 *         top-level Common Action Provider.
	 */
	public String getExtensionId() {
		return extensionId;
	}

	/**
	 * 
	 * @return The plugin id of associated Common Action Provider
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * 
	 * @return The associated structured viewer for the instantiated Common
	 *         Action Provider.
	 */
	public StructuredViewer getStructuredViewer() {
		return structuredViewer;
	}

	/**
	 * 
	 * @return The ICommonViewerSite from the CommonViewer.
	 */
	public ICommonViewerSite getViewSite() {
		return commonViewerSite;
	}
}
