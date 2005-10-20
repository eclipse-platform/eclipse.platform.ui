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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * Encapsulates the
 * <code>org.eclipse.wst.common.navigator.internal.views.navigator.navigatorViewer</code>
 * extension.
 */
public class NavigatorViewerDescriptor {
	public static final String DEFAULT_POPUP_MENU_ID = "#CommonNavigatorPopupMenu"; //$NON-NLS-1$

	protected static final String ROOT_CONTENT_EXTENSION = "rootContentExtension"; //$NON-NLS-1$

	protected static final String ATT_VIEWER_ID = "viewerId"; //$NON-NLS-1$	
	protected static final String ATT_CONTENT_EXTENSION_ID = "rootContentExtensionId"; //$NON-NLS-1$
	protected static final String ATT_ROOTID = "rootContentExtensionId"; //$NON-NLS-1$
	private static final String ATT_POPUP_MENU_ID = "popupMenuId"; //$NON-NLS-1$	

	private final String viewerId;
	private final Set rootExtensionIds = new HashSet();
	private String popupMenuId = null;



	/**
	 * Creates a new content descriptor from a configuration element.
	 * 
	 * @param configElement
	 *            configuration element to create a descriptor from
	 */
	public NavigatorViewerDescriptor(String aViewerId) {
		super();
		this.viewerId = aViewerId;
	}

	/**
	 * Creates a new content descriptor from a configuration element.
	 * 
	 * @param configElement
	 *            configuration element to create a descriptor from
	 */
	public NavigatorViewerDescriptor(IConfigurationElement configElement) throws WorkbenchException {
		super();
		viewerId = configElement.getAttribute(ATT_VIEWER_ID);
		if (viewerId == null) {
			throw new WorkbenchException("Missing attribute: " + //$NON-NLS-1$
						ATT_VIEWER_ID + " in navigator view extension: " + //$NON-NLS-1$
						configElement.getDeclaringExtension().getUniqueIdentifier());
		}
		consume(configElement);
	}

	public void consume(IConfigurationElement element) throws WorkbenchException {

		String rootExtensionId = element.getAttribute(ATT_ROOTID);
		String thePopupMenuId = element.getAttribute(ATT_POPUP_MENU_ID);
		if (thePopupMenuId != null) {
			if (popupMenuId != null)
				NavigatorPlugin.log("Warning: popupMenuId of \""+getViewerId()+
								"\" was overridden: old value = \"" + popupMenuId + 
								"\", new value = \"" + thePopupMenuId + "\".");

			popupMenuId = thePopupMenuId;
		}

		if (rootExtensionId != null) {
			addRootContentExtensionId(rootExtensionId);
		} else {
			IConfigurationElement[] rootContentExtensions = element.getChildren(ROOT_CONTENT_EXTENSION);
			for (int i = 0; i < rootContentExtensions.length; i++)
				addRootContentExtensionId(rootContentExtensions[i]);
		}
		if (rootExtensionId == null) {
			throw new WorkbenchException("Missing attribute: " + //$NON-NLS-1$
						ATT_ROOTID + " in navigator view extension: " + //$NON-NLS-1$
						element.getDeclaringExtension().getUniqueIdentifier());
		}
	}


	/**
	 * Returns the id of the viewer targeted by this extension.
	 * 
	 * @return the id of the viewer targeted by this extension.
	 */
	public String getViewerId() {
		return viewerId;
	}

	/**
	 * Returns the id of the
	 * <code>org.eclipse.wst.common.navigator.views.navigator.navigatorContent</code> extension
	 * that supplies root elements.
	 * 
	 * @return the id of the <code>navigatorContent</code> extension that supplies root elements.
	 */
	public String getRootExtensionId() {
		if (rootExtensionIds.size() == 0)
			return viewerId;
		return (String) rootExtensionIds.toArray(new String[rootExtensionIds.size()])[0];
	}

	/**
	 * Returns the id of the
	 * <code>org.eclipse.wst.common.navigator.views.navigator.navigatorContent</code> extension
	 * that supplies root elements.
	 * 
	 * @return the id of the <code>navigatorContent</code> extension that supplies root elements.
	 */
	public String[] getRootContentExtensionIds() {
		if (rootExtensionIds.size() == 0)
			return new String[] {viewerId};
		return (String[]) rootExtensionIds.toArray(new String[rootExtensionIds.size()]);
	}

	public String getPopupMenuId() {
		return popupMenuId != null ? popupMenuId : DEFAULT_POPUP_MENU_ID;
	}

	/**
	 * @param descriptor
	 * @return
	 */
	public boolean filtersContentDescriptor(NavigatorContentDescriptor descriptor) {
		// TODO Implment a filter logic component to handle viewers that wish to isolate or exclude
		// specific content extensions

		return false;
	}

	protected boolean addRootContentExtensionId(String rootContentExtensionId) {
		if (rootContentExtensionId != null) {
			rootExtensionIds.add(rootContentExtensionId);
			return true;
		}
		return false;
	}

	protected void addRootContentExtensionId(IConfigurationElement rootContentExtension) {
		String rootContentExtensionId = rootContentExtension.getAttribute(ATT_ROOTID);
		if (!addRootContentExtensionId(rootContentExtensionId)) {
			// TODO Log Warning
		}
	}

}