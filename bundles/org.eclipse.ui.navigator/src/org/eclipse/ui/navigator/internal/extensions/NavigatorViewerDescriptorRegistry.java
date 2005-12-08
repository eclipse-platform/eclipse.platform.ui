/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorViewerDescriptorRegistry extends RegistryReader {

	private static final NavigatorViewerDescriptorRegistry INSTANCE = new NavigatorViewerDescriptorRegistry();

	private static final String TAG_VIEWER = "viewer"; //$NON-NLS-1$

	private static final String TAG_VIEWER_CONTENT_BINDING = "viewerContentBinding"; //$NON-NLS-1$
	private static final String TAG_VIEWER_ACTION_BINDING = "viewerActionBinding"; //$NON-NLS-1$
	

	private static final String ATT_VIEWER_ID = "viewerId"; //$NON-NLS-1$

	private static final String ATT_POPUP_MENU_ID = "popupMenuId"; //$NON-NLS-1$

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
		super(NavigatorPlugin.PLUGIN_ID, TAG_VIEWER);
	}

	public NavigatorViewerDescriptor getNavigatorViewerDescriptor(
			String viewerId) {
		return getViewerDescriptor(viewerId);
	}

 
	protected boolean readElement(IConfigurationElement element) {
		if (TAG_VIEWER.equals(element.getName())) {
			String viewerId = element.getAttribute(ATT_VIEWER_ID);
			NavigatorViewerDescriptor descriptor = getViewerDescriptor(viewerId);
			String popupMenuId = element.getAttribute(ATT_POPUP_MENU_ID);
			if (popupMenuId != null)
				descriptor.setPopupMenuId(popupMenuId);
			return true;
		}
		if (TAG_VIEWER_CONTENT_BINDING.equals(element.getName())) {
			try {
				String viewerId = element.getAttribute(ATT_VIEWER_ID);
				NavigatorViewerDescriptor descriptor = getViewerDescriptor(viewerId);
				descriptor.consumeContentBinding(element);
				return true;
			} catch (WorkbenchException e) {
				// log an error since its not safe to open a dialog here
				NavigatorPlugin
						.log("Unable to create navigator view descriptor.", e.getStatus());//$NON-NLS-1$
			}
		}
		if (TAG_VIEWER_ACTION_BINDING.equals(element.getName())) {
			try {
				String viewerId = element.getAttribute(ATT_VIEWER_ID);
				NavigatorViewerDescriptor descriptor = getViewerDescriptor(viewerId);
				descriptor.consumeActionBinding(element);
				return true;
			} catch (WorkbenchException e) {
				// log an error since its not safe to open a dialog here
				NavigatorPlugin
						.log("Unable to create navigator view descriptor.", e.getStatus());//$NON-NLS-1$
			}
		}
		return false;
	}

	/**
	 * @param aViewerId
	 * @return
	 */
	private NavigatorViewerDescriptor getViewerDescriptor(
			String aViewerId) {
		NavigatorViewerDescriptor viewerDescriptor = null;
		synchronized (viewerDescriptors) {
			viewerDescriptor = (NavigatorViewerDescriptor) viewerDescriptors
					.get(aViewerId);
			if (viewerDescriptor != null)
				return viewerDescriptor;
			viewerDescriptor = new NavigatorViewerDescriptor(aViewerId);
			viewerDescriptors.put(viewerDescriptor.getViewerId(),
					viewerDescriptor);
		}
		return viewerDescriptor;
	}

}
