/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public class NavigatorViewerDescriptorRegistry extends RegistryReader {

	private static final NavigatorViewerDescriptorRegistry INSTANCE = new NavigatorViewerDescriptorRegistry();

	protected static final String NAVIGATOR_VIEWER = "navigatorViewer"; //$NON-NLS-1$
	protected static final String ATT_VIEWER_ID = "viewerId"; //$NON-NLS-1$
	private static boolean isInitialized = false;

	private final Map viewerDescriptors = new HashMap();


	/**
	 *  
	 */
	public static NavigatorViewerDescriptorRegistry getInstance() {
		if (isInitialized)
			return INSTANCE;
		synchronized (INSTANCE) {
			if (!isInitialized) {
				INSTANCE.readRegistry();
				isInitialized = true;
			}
		}
		return INSTANCE;
	}

	/**
	 * @param aPluginId
	 * @param anExtensionPoint
	 */
	protected NavigatorViewerDescriptorRegistry() {
		super(NavigatorPlugin.PLUGIN_ID, NAVIGATOR_VIEWER);
	}

	public NavigatorViewerDescriptor getNavigatorViewerDescriptor(String viewerId) {
		return getOrCreateNavigatorViewerDescriptor(viewerId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.extensions.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	protected boolean readElement(IConfigurationElement element) {
		if (NAVIGATOR_VIEWER.equals(element.getName())) {
			try {
				String viewerId = element.getAttribute(ATT_VIEWER_ID);
				NavigatorViewerDescriptor descriptor = getOrCreateNavigatorViewerDescriptor(viewerId);
				descriptor.consume(element);
				return true;
			} catch (WorkbenchException e) {
				// log an error since its not safe to open a dialog here
				NavigatorPlugin.log("Unable to create navigator view descriptor.", e.getStatus());//$NON-NLS-1$
			}
		}
		return false;
	}


	/**
	 * @param aViewerId
	 * @return
	 */
	private NavigatorViewerDescriptor getOrCreateNavigatorViewerDescriptor(String aViewerId) {
		NavigatorViewerDescriptor viewerDescriptor = null;
		synchronized (viewerDescriptors) {
			viewerDescriptor = (NavigatorViewerDescriptor) viewerDescriptors.get(aViewerId);
			if (viewerDescriptor != null)
				return viewerDescriptor;
			viewerDescriptor = new NavigatorViewerDescriptor(aViewerId);
			viewerDescriptors.put(viewerDescriptor.getViewerId(), viewerDescriptor);
		}
		return viewerDescriptor;
	}



}