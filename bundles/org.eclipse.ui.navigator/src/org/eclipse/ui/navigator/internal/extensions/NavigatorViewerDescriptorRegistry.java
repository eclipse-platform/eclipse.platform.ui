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

	private static final String TAG_POPUP_MENU = "popupMenu"; //$NON-NLS-1$

	private static final String ATT_ID = "id"; //$NON-NLS-1$

	private static final String ATT_ALLOWS_PLATFORM_CONTRIBUTIONS = "allowsPlatformContributions"; //$NON-NLS-1$

	private static final String TAG_INSERTION_POINT = "insertionPoint"; //$NON-NLS-1$

	private static final String ATT_NAME = "name"; //$NON-NLS-1$

	private static final String ATT_SEPARATOR = "separator"; //$NON-NLS-1$

	private static final String ATT_VIEWER_ID = "viewerId"; //$NON-NLS-1$

	private static final String ATT_POPUP_MENU_ID = "popupMenuId"; //$NON-NLS-1$

	private static boolean isInitialized = false;

	private final Map viewerDescriptors = new HashMap();

	/**
	 * @return The intialized singleton instance of the viewer descriptor registry. 
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

	/**
	 * 
	 * @param viewerId The viewer id for the viewer configuration
	 * @return The viewer descriptor for the given viewer id.
	 */
	public NavigatorViewerDescriptor getNavigatorViewerDescriptor(
			String viewerId) {
		return getViewerDescriptor(viewerId);
	}

	protected boolean readElement(IConfigurationElement element) {
		if (TAG_VIEWER.equals(element.getName())) {
			String viewerId = element.getAttribute(ATT_VIEWER_ID);
			NavigatorViewerDescriptor descriptor = getViewerDescriptor(viewerId);
			String attPopupMenuId = element.getAttribute(ATT_POPUP_MENU_ID);
			IConfigurationElement[] tagPopupMenu = element
					.getChildren(TAG_POPUP_MENU);
			if (tagPopupMenu.length == 0 && attPopupMenuId != null)
				descriptor.setPopupMenuId(attPopupMenuId);
			else {
				if (attPopupMenuId != null)
					NavigatorPlugin
							.logError(0, "A popupMenuId attribute and popupMenu element may NOT be concurrently specified. (see " + element.getNamespace() + ")", null); //$NON-NLS-1$ //$NON-NLS-2$
				else if (tagPopupMenu.length > 1) 
					NavigatorPlugin
					.logError(0, "Only one 'popupMenu' child of 'viewer' id may be specified. (see " + element.getNamespace() + ")", null); //$NON-NLS-1$ //$NON-NLS-2$
				else { // valid case
					
					String popupMenuId = tagPopupMenu[0].getAttribute(ATT_ID);
					String allowsPlatformContributions = tagPopupMenu[0].getAttribute(ATT_ALLOWS_PLATFORM_CONTRIBUTIONS);
					
					if(popupMenuId != null)
						descriptor.setPopupMenuId(popupMenuId);
					
					if(allowsPlatformContributions != null) 
						descriptor.setAllowsPlatformContributions(Boolean.valueOf(allowsPlatformContributions).booleanValue());
					
					IConfigurationElement[] insertionPointElements = tagPopupMenu[0].getChildren(TAG_INSERTION_POINT);
					InsertionPoint[] insertionPoints = new InsertionPoint[insertionPointElements.length];
					String name;
					String stringAttSeparator; 
					
					boolean isSeparator;
					for(int indx=0; indx< insertionPointElements.length; indx++) {
						name = insertionPointElements[indx].getAttribute(ATT_NAME);
						stringAttSeparator = insertionPointElements[indx].getAttribute(ATT_SEPARATOR);
						isSeparator = stringAttSeparator != null ? Boolean.valueOf(stringAttSeparator).booleanValue() : false;
						insertionPoints[indx] = new InsertionPoint(name, isSeparator);
					}
					descriptor.setCustomInsertionPoints(insertionPoints);
				}
			}
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
						.log(
								"Unable to create navigator view descriptor.", e.getStatus()); //$NON-NLS-1$
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
						.log(
								"Unable to create navigator view descriptor.", e.getStatus());//$NON-NLS-1$
			}
		}
		return false;
	}

	private NavigatorViewerDescriptor getViewerDescriptor(String aViewerId) {
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
