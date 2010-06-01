/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.progress.UIJob;

/**
 * <p>
 * Manages the non-viewer responsibilities of the Common Navigator View Part,
 * including the display and population of the context menu and the registration
 * of extensions for opening content.
 * </p>
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 3.4
 */
public final class CommonNavigatorManager implements ISelectionChangedListener {

	private final CommonNavigator commonNavigator;

	private final INavigatorContentService contentService;

	private NavigatorActionService actionService;

	private final IDescriptionProvider commonDescriptionProvider;

	private final IStatusLineManager statusLineManager;

	private final ILabelProvider labelProvider;

	private UpdateActionBarsJob updateActionBars;
	
	private ISelectionChangedListener statusBarListener = new ISelectionChangedListener() {

		public void selectionChanged(SelectionChangedEvent anEvent) {
			updateStatusBar(anEvent.getSelection());
		}
		
	};
	

	
	private class UpdateActionBarsJob extends UIJob {
		public UpdateActionBarsJob(String label) {
			super(label);
		}
		  
		public IStatus runInUIThread(IProgressMonitor monitor) {
			SafeRunner.run(new NavigatorSafeRunnable() {
				public void run() throws Exception {
					if(commonNavigator.getCommonViewer().getInput() != null) {
						IStructuredSelection selection = new StructuredSelection(commonNavigator.getCommonViewer().getInput());
						actionService.setContext(new ActionContext(selection));
						actionService.fillActionBars(commonNavigator.getViewSite().getActionBars());
					}
				}
			});
			return Status.OK_STATUS;
		}
	}

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
	public CommonNavigatorManager(CommonNavigator aNavigator) {
		this(aNavigator, null);
	}
	
	/**
	 * <p>
	 * Adds listeners to aNavigator to listen for selection changes and respond
	 * to mouse events.
	 * </p>
	 * 
	 * @param aNavigator
	 *            The CommonNavigator managed by this class. Requires a non-null
	 *            value.
	 * @param aMemento a memento for restoring state, or <code>null</code>
	 */
	public CommonNavigatorManager(CommonNavigator aNavigator, IMemento aMemento) {
		super();
		commonNavigator = aNavigator;
		contentService = commonNavigator.getNavigatorContentService();
		statusLineManager = commonNavigator.getViewSite().getActionBars()
				.getStatusLineManager();
		commonDescriptionProvider = contentService
				.createCommonDescriptionProvider();
		labelProvider = (ILabelProvider) commonNavigator.getCommonViewer()
				.getLabelProvider();
	
		init(aMemento);
	}


	private void init(IMemento memento) {
		
		updateActionBars = new UpdateActionBarsJob(commonNavigator.getTitle());
		
		CommonViewer commonViewer = commonNavigator.getCommonViewer();
		commonViewer.addSelectionChangedListener(this);
		commonViewer.addPostSelectionChangedListener(statusBarListener);
		updateStatusBar(commonViewer.getSelection());

		ICommonViewerSite commonViewerSite = CommonViewerSiteFactory
				.createCommonViewerSite(commonNavigator.getViewSite());
		actionService = new NavigatorActionService(commonViewerSite,
				commonViewer, commonViewer.getNavigatorContentService());

		final RetargetAction openAction = new RetargetAction(
				ICommonActionConstants.OPEN,
				CommonNavigatorMessages.Open_action_label);
		commonNavigator.getViewSite().getPage().addPartListener(openAction);
		openAction.setActionDefinitionId(ICommonActionConstants.OPEN);

		new OpenAndLinkWithEditorHelper(commonNavigator.getCommonViewer()) {
			protected void activate(ISelection selection) {
				final int currentMode = OpenStrategy.getOpenMethod();
				try {
					/*
					 * XXX:
					 * Currently the only way to activate the editor because there is no API to
					 * get an editor input for a given object.
					 */
					OpenStrategy.setOpenMethod(OpenStrategy.DOUBLE_CLICK);
					actionService.setContext(new ActionContext(commonNavigator.getCommonViewer().getSelection()));
					actionService.fillActionBars(commonNavigator.getViewSite().getActionBars());
					openAction.run();
				} finally {
					OpenStrategy.setOpenMethod(currentMode);
				}
			}

			protected void linkToEditor(ISelection selection) {
				// do nothing: this is handled by org.eclipse.ui.internal.navigator.actions.LinkEditorAction
			}

			protected void open(ISelection selection, boolean activate) {
				actionService.setContext(new ActionContext(commonNavigator.getCommonViewer().getSelection()));
				actionService.fillActionBars(commonNavigator.getViewSite().getActionBars());
				openAction.run();
			}
			
		};

		if(memento != null)
			restoreState(memento);
		
		initContextMenu();
		initViewMenu();

	}

	/**
	 * <p>
	 * Called by {@link CommonNavigator} when the View Part is disposed.
	 * 
	 */
	public void dispose() {
		commonNavigator.getCommonViewer().removeSelectionChangedListener(this);
		commonNavigator.getCommonViewer().removeSelectionChangedListener(statusBarListener);
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
		if (anEvent.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) anEvent
					.getSelection();
			actionService.setContext(new ActionContext(structuredSelection));
			actionService.fillActionBars(commonNavigator.getViewSite()
					.getActionBars());
		}
	}

	/**
	 * @param aMemento
	 *            Used to restore state of action extensions via the
	 *            {@link NavigatorActionService}.
	 */
	public void restoreState(IMemento aMemento) {
		actionService.restoreState(aMemento);
		 
	}

	/**
	 * @param aMemento
	 *            Used to save state of action extensions via the
	 *            {@link NavigatorActionService}.
	 */
	public void saveState(IMemento aMemento) {
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
	 * @see NavigatorActionService#fillContextMenu(IMenuManager)
	 * 
	 */
	protected void fillContextMenu(IMenuManager aMenuManager) {
		ISelection selection = commonNavigator.getCommonViewer().getSelection();
		actionService.setContext(new ActionContext(selection));
		actionService.fillContextMenu(aMenuManager);
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

		actionService.prepareMenuForPlatformContributions(menuMgr,
				commonViewer, false);

	}

	protected void initViewMenu() {
		IMenuManager viewMenu = commonNavigator.getViewSite().getActionBars()
				.getMenuManager();
		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
				+ "-end"));//$NON-NLS-1$
		
		updateActionBars.schedule(NavigatorPlugin.ACTION_BAR_DELAY);
		
	}

	/**
	 * @param aSelection
	 *            The current selection from the {@link CommonViewer}
	 */
	protected void updateStatusBar(ISelection aSelection) {

		Image img = null;
		if (aSelection != null && !aSelection.isEmpty()
				&& aSelection instanceof IStructuredSelection) {
			img = labelProvider.getImage(((IStructuredSelection) aSelection)
					.getFirstElement());
		}

		statusLineManager.setMessage(img, commonDescriptionProvider
				.getDescription(aSelection));
	}

	/**
	 * 
	 * @return The action service used by this manager
	 */
	public NavigatorActionService getNavigatorActionService() {
		return actionService;
	}

}
