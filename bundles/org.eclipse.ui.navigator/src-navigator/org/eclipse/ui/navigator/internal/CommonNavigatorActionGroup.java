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
package org.eclipse.ui.navigator.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.internal.actions.CollapseAllAction;
import org.eclipse.ui.navigator.internal.actions.LinkEditorAction;
import org.eclipse.ui.navigator.internal.filters.SelectFiltersAction;

/** 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class CommonNavigatorActionGroup extends ActionGroup {

	private LinkEditorAction toggleLinkingAction;
	private CollapseAllAction collapseAllAction;
	private SelectFiltersAction selectFiltersAction;
	private final CommonViewer commonViewer;
	private CommonNavigator commonNavigator;

	/**
	 *  
	 */
	public CommonNavigatorActionGroup(CommonNavigator aNavigator, CommonViewer aViewer) {
		super();
		commonNavigator = aNavigator;
		commonViewer = aViewer;
		makeActions();
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected final ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$
		try {
			NavigatorPlugin plugin = NavigatorPlugin.getDefault();
			URL installURL = plugin.getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + relativePath);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/**
	 *  
	 */
	private void makeActions() {

		toggleLinkingAction = new LinkEditorAction(commonNavigator, commonViewer);
		ImageDescriptor syncIcon = getImageDescriptor("elcl16/synced.gif"); //$NON-NLS-1$
		toggleLinkingAction.setImageDescriptor(syncIcon);
		toggleLinkingAction.setHoverImageDescriptor(syncIcon);

		collapseAllAction = new CollapseAllAction(commonViewer);
		ImageDescriptor collapseAllIcon = getImageDescriptor("elcl16/collapseall.gif"); //$NON-NLS-1$
		collapseAllAction.setImageDescriptor(collapseAllIcon);
		collapseAllAction.setHoverImageDescriptor(collapseAllIcon);

		selectFiltersAction = new SelectFiltersAction(commonNavigator);
		ImageDescriptor selectFiltersIcon = getImageDescriptor("elcl16/filter_ps.gif"); //$NON-NLS-1$
		selectFiltersAction.setImageDescriptor(selectFiltersIcon);
		selectFiltersAction.setHoverImageDescriptor(selectFiltersIcon);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars theActionBars) {
		IMenuManager menu = theActionBars.getMenuManager();

		menu.insertAfter(IWorkbenchActionConstants.MB_ADDITIONS, selectFiltersAction);
		menu.insertAfter(IWorkbenchActionConstants.MB_ADDITIONS+"-end", toggleLinkingAction); //$NON-NLS-1$

		theActionBars.getToolBarManager().add(collapseAllAction);
		theActionBars.getToolBarManager().add(toggleLinkingAction);

		theActionBars.updateActionBars();
	}

}