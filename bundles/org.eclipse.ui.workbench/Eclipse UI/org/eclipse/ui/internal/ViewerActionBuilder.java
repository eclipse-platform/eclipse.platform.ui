/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;


import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate2;
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
	
	/* (non-Javadoc)
	 * Method declared on PluginActionBuilder.
	 */
	protected BasicContribution createContribution() {
		return new ViewerContribution(provider);
	}

	/**
	 * Dispose of the action builder
	 */
	public void dispose() {
		if (cache != null) {
			for (int i = 0; i < cache.size(); i++) {
				ArrayList actions = ((BasicContribution)cache.get(i)).actions;
				if (actions != null) {
					for (int j = 0; j < actions.size(); j++) {
						PluginAction proxy = ((ActionDescriptor) actions.get(j)).getAction();
						if (proxy.getDelegate() instanceof IActionDelegate2)
							 ((IActionDelegate2) proxy.getDelegate()).dispose();
					}
				}
			}
			cache = null;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on PluginActionBuilder.
	 */
	protected boolean readElement(IConfigurationElement element) {
		String tag = element.getName();
		
		// Found visibility sub-element
		if (tag.equals(PluginActionBuilder.TAG_VISIBILITY)) {
			((ViewerContribution)currentContribution).setVisibilityTest(element);
			return true;
		} 
		
		return super.readElement(element);
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


	/**
	 * Helper class to collect the menus and actions defined within a
	 * contribution element.
	 */
	private static class ViewerContribution extends BasicContribution {
		private ISelectionProvider selProvider;
		private ActionExpression visibilityTest;

		public ViewerContribution(ISelectionProvider selProvider) {
			super();
			this.selProvider = selProvider;
		}
		
		public void setVisibilityTest(IConfigurationElement element) {
			visibilityTest = new ActionExpression(element);
		}
		
		/* (non-Javadoc)
		 * Method declared on BasicContribution.
		 */
		public void contribute(IMenuManager menu, boolean menuAppendIfMissing, IToolBarManager toolbar, boolean toolAppendIfMissing) {
			boolean visible = true;
			
			if (visibilityTest != null) {
				ISelection selection = selProvider.getSelection();
				if (selection instanceof IStructuredSelection) {
					visible = visibilityTest.isEnabledFor((IStructuredSelection) selection);
				} else {
					visible = visibilityTest.isEnabledFor(selection);
				}
			}
			
			if (visible)
				super.contribute(menu, menuAppendIfMissing, toolbar, toolAppendIfMissing);
		}
	}
}
