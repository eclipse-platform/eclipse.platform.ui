package org.eclipse.ui.internal;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This class reads the registry for extensions that plug into
 * 'popupMenus' extension point and deals only with the 'viewerContribution'
 * elements.
 */
public class ViewerActionBuilder extends PluginActionBuilder {
	public static final String TAG_CONTRIBUTION_TYPE = "viewerContribution"; //$NON-NLS-1$

	private ISelectionProvider provider;
	private IWorkbenchPart part;

	/**
	 * Basic contstructor
	 */
	public ViewerActionBuilder() {
	}
	
	/* (non-Javadoc)
	 * Method declared on PluginActionBuilder.
	 */
	protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
		ActionDescriptor desc = null;
		if (part instanceof IViewPart)
			desc = new ActionDescriptor(element, ActionDescriptor.T_VIEW, part);
		else
			desc = new ActionDescriptor(element, ActionDescriptor.T_EDITOR, part);
		if (provider != null) {
			PluginAction action = desc.getAction();
			provider.addSelectionChangedListener(action);
		}
		return desc;
	}
	
	/**
	 * Reads the contributions for a viewer menu.
	 * This method is typically used in conjunction with <code>contribute</code> to read
	 * and then insert actions for a particular viewer menu.
	 *
	 * @param id the menu id
	 * @param prov the selection provider for the control containing the menu
	 * @param part the part containing the menu.
	 * @return <code>true</code> if 1 or more items were read.  
	 */
	public boolean readViewerContributions(String id, ISelectionProvider prov, IWorkbenchPart part) {
		provider = prov;
		this.part = part;
		readContributions(id, TAG_CONTRIBUTION_TYPE, IWorkbenchConstants.PL_POPUP_MENU);
		return (cache != null);
	}
}
