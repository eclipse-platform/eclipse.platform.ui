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

import java.util.ArrayList;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;

/**
 * This class reads the registry for extensions that plug into
 * 'viewActions' extension point.
 */
public class ViewActionBuilder extends PluginActionBuilder {
	public static final String TAG_CONTRIBUTION_TYPE = "viewContribution"; //$NON-NLS-1$

	private IViewPart targetPart;

	/**
	 * Basic constructor
	 */
	public ViewActionBuilder() {
	}
	
	/**
	 * Contribute the external menus and actions applicable for this view part.
	 */
	private void contributeToPart(IViewPart part) {
		IActionBars bars = part.getViewSite().getActionBars();
		contribute(bars.getMenuManager(), bars.getToolBarManager(), true);
	}
	
	/* (non-Javadoc)
	 * Method declared on PluginActionBuilder.
	 */
	protected ActionDescriptor createActionDescriptor(org.eclipse.core.runtime.IConfigurationElement element) {
		return new ActionDescriptor(element, ActionDescriptor.T_VIEW, targetPart);
	}
	
	/**
	 * Return all extended actions.
	 */
	public ActionDescriptor[] getExtendedActions() {
		if (cache == null)
			return new ActionDescriptor[0];

		ArrayList results = new ArrayList();
		for (int i = 0; i < cache.size(); i++) {
			BasicContribution bc = (BasicContribution)cache.get(i);
			if (bc.actions != null)
				results.addAll(bc.actions);
		}
		return (ActionDescriptor[]) results.toArray(new ActionDescriptor[results.size()]);
	}
	
	/**
	 * Reads and apply all external contributions for this view's ID registered
	 * in 'viewActions' extension point.
	 */
	public void readActionExtensions(IViewPart viewPart) {
		targetPart = viewPart;
		readContributions(viewPart.getSite().getId(), TAG_CONTRIBUTION_TYPE, IWorkbenchConstants.PL_VIEW_ACTIONS);
		contributeToPart(targetPart);
	}
}
