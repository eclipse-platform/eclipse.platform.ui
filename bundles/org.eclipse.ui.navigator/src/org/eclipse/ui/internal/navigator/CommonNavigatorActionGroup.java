/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.navigator.actions.CollapseAllAction;
import org.eclipse.ui.internal.navigator.actions.LinkEditorAction;
import org.eclipse.ui.internal.navigator.filters.FilterActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.LinkHelperService;

/**
 * 
 * 
 * @since 3.2
 */
public class CommonNavigatorActionGroup extends ActionGroup {

	private LinkEditorAction toggleLinkingAction;

	private CollapseAllAction collapseAllAction;

	private FilterActionGroup filterGroup;

	private final CommonViewer commonViewer;

	private CommonNavigator commonNavigator;

	private final LinkHelperService linkHelperService;

	private CollapseAllHandler collapseAllHandler;

	/**
	 * Create a action group for Collapse All, Link with editor, and Select
	 * Filters.
	 * 
	 * @param aNavigator
	 *            The IViewPart for this action group
	 * @param aViewer
	 *            The Viewer for this action group
	 * @param linkHelperService the link service helper
	 */
	public CommonNavigatorActionGroup(CommonNavigator aNavigator,
			CommonViewer aViewer, LinkHelperService linkHelperService) {
		super();
		commonNavigator = aNavigator;
		commonViewer = aViewer;
		this.linkHelperService = linkHelperService;
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
		IHandlerService service = (IHandlerService) commonNavigator.getSite()
				.getService(IHandlerService.class);

		INavigatorViewerDescriptor viewerDescriptor = commonViewer
				.getNavigatorContentService().getViewerDescriptor();
		boolean hideLinkWithEditorAction = viewerDescriptor
				.getBooleanConfigProperty(INavigatorViewerDescriptor.PROP_HIDE_LINK_WITH_EDITOR_ACTION);
		if (!hideLinkWithEditorAction) {
			toggleLinkingAction = new LinkEditorAction(commonNavigator,
					commonViewer, linkHelperService);
			ImageDescriptor syncIcon = getImageDescriptor("elcl16/synced.gif"); //$NON-NLS-1$
			toggleLinkingAction.setImageDescriptor(syncIcon);
			toggleLinkingAction.setHoverImageDescriptor(syncIcon);
			service.activateHandler(toggleLinkingAction.getActionDefinitionId(),
					new ActionHandler(toggleLinkingAction));
		}

		boolean hideCollapseAllAction = viewerDescriptor
				.getBooleanConfigProperty(INavigatorViewerDescriptor.PROP_HIDE_COLLAPSE_ALL_ACTION);
		if (!hideCollapseAllAction) {
			collapseAllAction = new CollapseAllAction(commonViewer);
			ImageDescriptor collapseAllIcon = getImageDescriptor("elcl16/collapseall.gif"); //$NON-NLS-1$
			collapseAllAction.setImageDescriptor(collapseAllIcon);
			collapseAllAction.setHoverImageDescriptor(collapseAllIcon);
			collapseAllHandler = new CollapseAllHandler(commonViewer);
			service.activateHandler(CollapseAllHandler.COMMAND_ID, collapseAllHandler);
		}

		filterGroup = new FilterActionGroup(commonViewer);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars theActionBars) {
		IMenuManager menu = theActionBars.getMenuManager();

		filterGroup.fillActionBars(theActionBars);

		if (collapseAllAction != null) {
			theActionBars.getToolBarManager().add(collapseAllAction);
		}

		if (toggleLinkingAction != null) {
			menu
					.insertAfter(IWorkbenchActionConstants.MB_ADDITIONS
							+ "-end", toggleLinkingAction); //$NON-NLS-1$

			theActionBars.getToolBarManager().add(toggleLinkingAction);
		}

		theActionBars.updateActionBars();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (toggleLinkingAction != null) {
			toggleLinkingAction.dispose();
		}
		if (collapseAllHandler!=null) {
			collapseAllHandler.dispose();
		}
	}

}
