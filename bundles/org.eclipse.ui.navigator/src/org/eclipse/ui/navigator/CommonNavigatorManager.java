/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorActionService;
import org.eclipse.ui.navigator.internal.NavigatorContentServiceDescriptionProvider;

/**
 * <p>
 * Manages the non-viewer responsibilities of the Common Navigator View Part,
 * including the display and population of the context menu and the registration
 * of extensions for opening content.
 * </p>
 * <p>
 * This class is not intended to be instantiated or subclassed by clients
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public final class CommonNavigatorManager implements ISelectionChangedListener {

	private final CommonNavigator commonNavigator;

	private final INavigatorContentService contentService;

	private INavigatorActionService actionService;

	private final IDescriptionProvider commonDescriptionProvider;

	private final IStatusLineManager statusLineManager;

	private final ILabelProvider labelProvider;

	/**
	 * <p>
	 * Adds listeners to aNavigator to listen for selection changes and respond
	 * to mouse events.
	 * </p>
	 * 
	 * @param aNavigator
	 *            The CommonNavigator managed by this class. Requires a non-null
	 *            value.
	 */
	protected CommonNavigatorManager(CommonNavigator aNavigator) {
		super();
		commonNavigator = aNavigator;
		contentService = commonNavigator.getNavigatorContentService();
		statusLineManager = commonNavigator.getViewSite().getActionBars()
				.getStatusLineManager();
		commonDescriptionProvider = new NavigatorContentServiceDescriptionProvider(
				contentService);
		labelProvider = (ILabelProvider) commonNavigator.getCommonViewer()
				.getLabelProvider();
		init();
	}

	private void init() {
		commonNavigator.getCommonViewer().addSelectionChangedListener(this);
		updateStatusBar(commonNavigator.getCommonViewer().getSelection());
		actionService = new NavigatorActionService(commonNavigator,
				commonNavigator.getCommonViewer(), contentService);
		// commonNavigator.getCommonViewer().addOpenListener(commonOpenService);
		initContextMenu();
		initViewMenu();
		

		final RetargetAction openAction = new RetargetAction(ICommonActionConstants.OPEN, CommonNavigatorMessages.Open_action_label);
		commonNavigator.getViewSite().getPage().addPartListener(openAction);
		openAction.setActionDefinitionId(ICommonActionConstants.OPEN);
		
		commonNavigator.getCommonViewer().addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) { 
				openAction.run();				
			}
		});
	}

	/**
	 * <p>
	 * Called by {@link CommonNavigator} when the View Part is disposed.
	 * 
	 */
	protected void dispose() {
		commonNavigator.getCommonViewer().removeSelectionChangedListener(this);
		// commonNavigator.getCommonViewer().removeOpenListener(commonOpenService);
		// commonOpenService.dispose();
		actionService.dispose();
	}

	/**
	 * 
	 * @param anEvent
	 *            An event indicating the current selection of the
	 *            {@link CommonViewer}
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent anEvent) {
		updateStatusBar(anEvent.getSelection());
		if (anEvent.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) anEvent
					.getSelection();
			actionService.fillActionBars(commonNavigator.getViewSite()
					.getActionBars(), structuredSelection);
		}
	}

	/**
	 * @param aMemento
	 *            Used to restore state of action extensions via the
	 *            {@link NavigatorActionService}.
	 */
	protected void restoreState(IMemento aMemento) {
		actionService.restoreState(aMemento);
	}

	/**
	 * @param aMemento
	 *            Used to save state of action extensions via the
	 *            {@link NavigatorActionService}.
	 */
	protected void saveState(IMemento aMemento) {
		actionService.saveState(aMemento);
	}

	/**
	 * <p>
	 * Fills aMenuManager with menu contributions from the
	 * {@link NavigatorActionService}.
	 * </p>
	 * 
	 * @param aMenuManager
	 *            A popup menu
	 * @see NavigatorActionService#fillContextMenu(IMenuManager,
	 *      IStructuredSelection)
	 * 
	 */
	protected void fillContextMenu(IMenuManager aMenuManager) {
		ISelection selection = commonNavigator.getCommonViewer().getSelection();
		if (selection instanceof IStructuredSelection)
			actionService.fillContextMenu(aMenuManager,
					(IStructuredSelection) selection);
		else
			actionService.fillContextMenu(aMenuManager,
					StructuredSelection.EMPTY);
	}

	/**
	 * <p>
	 * Initializes and registers the context menu.
	 * </p>
	 */
	protected void initContextMenu() {
		MenuManager menuMgr = new MenuManager(contentService
				.getViewerDescriptor().getPopupMenuId());
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		TreeViewer commonViewer = commonNavigator.getCommonViewer();
		Menu menu = menuMgr.createContextMenu(commonViewer.getTree());

		commonViewer.getTree().setMenu(menu);
		actionService.setUpdateMenu(menuMgr);

		/*
		 * Hooks into the Eclipse framework for Object contributions, and View
		 * contributions.
		 */
		commonNavigator.getSite().registerContextMenu(
				contentService.getViewerDescriptor().getPopupMenuId(), menuMgr,
				commonViewer);

	}

	protected void initViewMenu() {
		IMenuManager viewMenu = commonNavigator.getViewSite().getActionBars()
				.getMenuManager();
		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
				+ "-end"));//$NON-NLS-1$	
	}

	/**
	 * @param aSelection
	 *            The current selection from the {@link CommonViewer}
	 */
	protected void updateStatusBar(ISelection aSelection) {

		Image img = null;
		if (aSelection != null && !aSelection.isEmpty()
				&& aSelection instanceof IStructuredSelection)
			img = labelProvider.getImage(((IStructuredSelection) aSelection)
					.getFirstElement());

		statusLineManager.setMessage(img, commonDescriptionProvider
				.getDescription(aSelection));
	}

	/**
	 * 
	 * @return The action service used by this manager
	 */
	public INavigatorActionService getNavigatorActionService() {
		return actionService;
	}

}
