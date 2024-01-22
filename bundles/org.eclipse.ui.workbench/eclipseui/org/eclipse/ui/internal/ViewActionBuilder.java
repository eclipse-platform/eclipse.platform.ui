/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * This class reads the registry for extensions that plug into 'viewActions'
 * extension point.
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

	@Override
	protected ActionDescriptor createActionDescriptor(org.eclipse.core.runtime.IConfigurationElement element) {
		return new ActionDescriptor(element, ActionDescriptor.T_VIEW, targetPart);
	}

	/**
	 * Return all extended actions.
	 */
	public ActionDescriptor[] getExtendedActions() {
		if (cache == null) {
			return new ActionDescriptor[0];
		}

		ArrayList results = new ArrayList();
		for (Object element : cache) {
			BasicContribution bc = (BasicContribution) element;
			if (bc.actions != null) {
				results.addAll(bc.actions);
			}
		}
		return (ActionDescriptor[]) results.toArray(new ActionDescriptor[results.size()]);
	}

	/**
	 * Reads and apply all external contributions for this view's ID registered in
	 * 'viewActions' extension point.
	 */
	public void readActionExtensions(IViewPart viewPart) {
		targetPart = viewPart;
		readContributions(viewPart.getSite().getId(), TAG_CONTRIBUTION_TYPE,
				IWorkbenchRegistryConstants.PL_VIEW_ACTIONS);
		contributeToPart(targetPart);
	}

	public void dispose() {
		if (cache != null) {
			for (Object element : cache) {
				((BasicContribution) element).disposeActions();
			}
		}
	}
}
