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
package org.eclipse.ui.navigator.internal;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.internal.actions.CollapseAllAction;
import org.eclipse.ui.navigator.internal.actions.LinkEditorAction;
import org.eclipse.ui.navigator.internal.filters.SelectFiltersAction;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
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
	 * Create a action group for Collapse All, Link with editor, and Select Filters.
	 */
	public CommonNavigatorActionGroup(CommonNavigator aNavigator,
			CommonViewer aViewer) {
		super();
		commonNavigator = aNavigator;
		commonViewer = aViewer;
		makeActions();
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected final ImageDescriptor getImageDescriptor(String relativePath) { 
		return NavigatorPlugin.getImageDescriptor("icons/full/" + relativePath); //$NON-NLS-1$

	}

	/**
	 * 
	 */
	private void makeActions() {

		INavigatorViewerDescriptor viewerDescriptor = commonViewer
				.getNavigatorContentService().getViewerDescriptor();
		boolean hideLinkWithEditorAction = viewerDescriptor
				.getBooleanConfigProperty(INavigatorViewerDescriptor.PROP_HIDE_LINK_WITH_EDITOR_ACTION); 
		if (!hideLinkWithEditorAction) {
			toggleLinkingAction = new LinkEditorAction(commonNavigator,
					commonViewer);
			ImageDescriptor syncIcon = getImageDescriptor("elcl16/synced.gif"); //$NON-NLS-1$
			toggleLinkingAction.setImageDescriptor(syncIcon);
			toggleLinkingAction.setHoverImageDescriptor(syncIcon);
		}

		boolean hideCollapseAllAction = viewerDescriptor
				.getBooleanConfigProperty(INavigatorViewerDescriptor.PROP_HIDE_COLLAPSE_ALL_ACTION);
		if (!hideCollapseAllAction) {
			collapseAllAction = new CollapseAllAction(commonViewer);
			ImageDescriptor collapseAllIcon = getImageDescriptor("elcl16/collapseall.gif"); //$NON-NLS-1$
			collapseAllAction.setImageDescriptor(collapseAllIcon);
			collapseAllAction.setHoverImageDescriptor(collapseAllIcon);
		}

		boolean hideAvailableCustomizationsDialog = viewerDescriptor
				.getBooleanConfigProperty(INavigatorViewerDescriptor.PROP_HIDE_AVAILABLE_CUSTOMIZATIONS_DIALOG); 
		if (!hideAvailableCustomizationsDialog) {
			selectFiltersAction = new SelectFiltersAction(commonViewer);
			ImageDescriptor selectFiltersIcon = getImageDescriptor("elcl16/filter_ps.gif"); //$NON-NLS-1$
			selectFiltersAction.setImageDescriptor(selectFiltersIcon);
			selectFiltersAction.setHoverImageDescriptor(selectFiltersIcon);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars theActionBars) {
		IMenuManager menu = theActionBars.getMenuManager();

		if (selectFiltersAction != null)
			menu.insertAfter(IWorkbenchActionConstants.MB_ADDITIONS,
					selectFiltersAction);

		if (collapseAllAction != null)
			theActionBars.getToolBarManager().add(collapseAllAction);

		if (toggleLinkingAction != null) {
			menu
					.insertAfter(IWorkbenchActionConstants.MB_ADDITIONS
							+ "-end", toggleLinkingAction); //$NON-NLS-1$

			theActionBars.getToolBarManager().add(toggleLinkingAction);
		}

		theActionBars.updateActionBars();
	}

}
