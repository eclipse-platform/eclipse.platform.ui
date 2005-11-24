/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.compare.internal.INavigatable;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.actions.StatusLineContributionGroup;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;

/**
 * A <code>StructuredViewerAdvisor</code> controls various UI
 * aspects of viewers that show {@link SyncInfoSet} like the context menu, toolbar, 
 * content provider, label provider, navigation, and model provider. The 
 * advisor allows decoupling viewer behavior from the viewers presentation. This
 * allows viewers that aren't in the same class hierarchy to re-use basic
 * behavior. 
 * <p>
 * This advisor allows viewer contributions made in a plug-in manifest to
 * be scoped to a particular unique id. As a result the context menu for the
 * viewer can be configured to show object contributions for random id schemes.
 * To enable declarative action contributions for a configuration there are two
 * steps required:
 * <ul>
 * <li>Create a viewer contribution with a <code>targetID</code> that groups
 * sets of actions that are related. A common pratice for synchronize view
 * configurations is to use the participant id as the targetID.
 * 
 * <pre>
 *  &lt;viewerContribution
 *  id=&quot;org.eclipse.team.ccvs.ui.CVSCompareSubscriberContributions&quot;
 *  targetID=&quot;org.eclipse.team.cvs.ui.compare-participant&quot;&gt;
 *  ...
 * </pre>
 * 
 * <li>Create a configuration instance with a <code>menuID</code> that
 * matches the targetID in the viewer contribution.
 * </ul>
 * </p><p>
 * Clients may subclass to add behavior for concrete structured viewers.
 * </p>
 * 
 * @see TreeViewerAdvisor
 * @since 3.0
 */
public abstract class StructuredViewerAdvisor extends AbstractViewerAdvisor implements IAdaptable {
	
	// Special actions that could not be contributed using an ActionGroup
	private StatusLineContributionGroup statusLine;
	
	private INavigatable nav;
	
	// Property change listener which reponds to:
	//    - working set selection by the user
	//    - decorator format change selected by the user
	private IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			// Change to showing of sync state in text labels preference
			if(event.getProperty().equals(IPreferenceIds.SYNCVIEW_VIEW_SYNCINFO_IN_LABEL)) {
				StructuredViewer viewer = getViewer();
				if(viewer != null && !viewer.getControl().isDisposed()) {
					viewer.refresh(true /* update labels */);
				}
			}
		}
	};

	/**
	 * Create an advisor that will allow viewer contributions with the given <code>targetID</code>. This
	 * advisor will provide a presentation model based on the given sync info set. The model is disposed
	 * when the viewer is disposed.
	 * 
	 * @param targetID the targetID defined in the viewer contributions in a plugin.xml file.
	 * @param site the workbench site with which to register the menuId. Can be <code>null</code> in which
	 * case a site will be found using the default workbench page.
	 * @param set the set of <code>SyncInfo</code> objects that are to be shown to the user.
	 */
	public StructuredViewerAdvisor(ISynchronizePageConfiguration configuration) {
		super(configuration);
	}
	
	/**
	 * Install a viewer to be configured with this advisor. An advisor can only be installed with
	 * one viewer at a time. When this method completes the viewer is considered initialized and
	 * can be shown to the user. 
	 * @param viewer the viewer being installed
	 */
	public void initializeViewer(final StructuredViewer viewer) {
		super.initializeViewer(viewer);
		initializeListeners(viewer);
		hookContextMenu(viewer);
	}
	
	/* (non-Javadoc)
	 * Allow adding an advisor to the PartNavigator and support coordinated
 	 * navigation between several objects.
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if(adapter == INavigatable.class) {
			if(nav == null) {
				nav = new INavigatable() {
					public boolean gotoDifference(boolean next) {
						return StructuredViewerAdvisor.this.navigate(next);
					}
				};
			}
			return nav;
		}
		return null;
	}
	
	private void initializeStatusLine() {
		statusLine = new StatusLineContributionGroup(
				getConfiguration().getSite().getShell(), 
				getConfiguration());
	}
	
	/**
	 * Must be called when an advisor is no longer needed.
	 */
	public void dispose() {
		if (statusLine != null) {
			statusLine.dispose();
		}
		if (getActionGroup() != null) {
			getActionGroup().dispose();
		}
		TeamUIPlugin.getPlugin().getPreferenceStore().removePropertyChangeListener(propertyListener);
	}
	
	/**
	 * Subclasses must implement to allow navigation of their viewers.
	 * 
	 * @param next if <code>true</code> then navigate forwards, otherwise navigate
	 * backwards.
	 * @return <code>true</code> if the end is reached, and <code>false</code> otherwise.
	 */
	public abstract boolean navigate(boolean next);
	
	/**
	 * Method invoked from <code>initializeViewer(Composite, StructuredViewer)</code>
	 * in order to initialize any listeners for the viewer.
	 *
	 * @param viewer the viewer being initialize
	 */
	protected void initializeListeners(final StructuredViewer viewer) {
		viewer.getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				StructuredViewerAdvisor.this.dispose();
			}
		});
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				handleOpen();
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(viewer, event);
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// Update the action bars enablement for any contributed action groups
				updateActionBars((IStructuredSelection)viewer.getSelection());
			}
		});
		TeamUIPlugin.getPlugin().getPreferenceStore().addPropertyChangeListener(propertyListener);
	}
	
	/**
	 * Handles a double-click event. If <code>false</code> is returned,
	 * subclasses may handle the double click.
	 */
	protected boolean handleDoubleClick(StructuredViewer viewer, DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object node = selection.getFirstElement();
		if (node != null && node instanceof SyncInfoModelElement) {
			SyncInfoModelElement syncNode = (SyncInfoModelElement) node;
			IResource resource = syncNode.getResource();
			if (syncNode != null && resource != null && resource.getType() == IResource.FILE) {
				// The open is handled by the open strategy but say we handled
			    // it so that overriding methods will not do anything
				return true;
			}
		}
		return false;
	}
	
	private void handleOpen() {
		Object o = getConfiguration().getProperty(SynchronizePageConfiguration.P_OPEN_ACTION);
		if (o instanceof IAction) {
			IAction action = (IAction)o;
			action.run();
		}
	}
	
	/**
	 * Method invoked from the synchronize page when the action
	 * bars are set. The advisor uses the configuration to determine
	 * which groups appear in the action bar menus and allows all
	 * action groups registered with the configuration to fill the action bars.
	 * @param actionBars the Action bars for the page
	 */
	public final void setActionBars(IActionBars actionBars) {
		if(actionBars != null) {
			IToolBarManager manager = actionBars.getToolBarManager();
			
			// Populate the toobar menu with the configured groups
			Object o = getConfiguration().getProperty(ISynchronizePageConfiguration.P_TOOLBAR_MENU);
			if (!(o instanceof String[])) {
				o = ISynchronizePageConfiguration.DEFAULT_TOOLBAR_MENU;
			}
			String[] groups = (String[])o;
			for (int i = 0; i < groups.length; i++) {
				String group = groups[i];
				// The groupIds must be converted to be unique since the toolbar is shared
				manager.add(new Separator(getGroupId(group)));
			}

			// view menu
			IMenuManager menu = actionBars.getMenuManager();
			if (menu != null) {
				// Populate the view dropdown menu with the configured groups
				o = getConfiguration().getProperty(ISynchronizePageConfiguration.P_VIEW_MENU);
				if (!(o instanceof String[])) {
					o = ISynchronizePageConfiguration.DEFAULT_VIEW_MENU;
				}
				groups = (String[]) o;
				initializeStatusLine();
				for (int i = 0; i < groups.length; i++) {
					String group = groups[i];
					// The groupIds must be converted to be unique since the
					// view menu is shared
					menu.add(new Separator(getGroupId(group)));
				}
			}
			// status line
			IStatusLineManager statusLineMgr = actionBars.getStatusLineManager();
			if (statusLineMgr != null && statusLine != null) {
				statusLine.fillActionBars(actionBars);
			}
			
			getActionGroup().fillActionBars(actionBars);
			updateActionBars((IStructuredSelection) getViewer().getSelection());
			Object input = getViewer().getInput();
			if (input instanceof ISynchronizeModelElement) {
				getActionGroup().modelChanged((ISynchronizeModelElement) input);
			}
		}		
	}
	
	/*
	 * Method invoked from <code>initializeViewer(StructuredViewer)</code>
	 * in order to configure the viewer to call <code>fillContextMenu(StructuredViewer, IMenuManager)</code>
	 * when a context menu is being displayed in viewer.
	 * 
	 * @param viewer the viewer being initialized
	 * @see fillContextMenu(StructuredViewer, IMenuManager)
	 */
	private void hookContextMenu(final StructuredViewer viewer) {
		String targetID;
		Object o = getConfiguration().getProperty(ISynchronizePageConfiguration.P_OBJECT_CONTRIBUTION_ID);
		if (o instanceof String) {
			targetID = (String)o;
		} else {
			targetID = null;
		}
		final MenuManager menuMgr = new MenuManager(targetID); 
		
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
	
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(viewer, manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		menu.addMenuListener(new MenuListener() {
	
			public void menuHidden(MenuEvent e) {
			}
	
			// Hack to allow action contributions to update their
			// state before the menu is shown. This is required when
			// the state of the selection changes and the contributions
			// need to update enablement based on this.
			// TODO: Is this hack still needed
			public void menuShown(MenuEvent e) {
				IContributionItem[] items = menuMgr.getItems();
				for (int i = 0; i < items.length; i++) {
					IContributionItem item = items[i];
					if (item instanceof ActionContributionItem) {
						IAction actionItem = ((ActionContributionItem) item).getAction();
						if (actionItem instanceof SynchronizeModelAction) {
							((SynchronizeModelAction) actionItem).selectionChanged(viewer.getSelection());
						}
					}
				}
			}
		});
		viewer.getControl().setMenu(menu);
		if (targetID != null) {
			IWorkbenchSite workbenchSite = getConfiguration().getSite().getWorkbenchSite();
			IWorkbenchPartSite ws = null;
			if (workbenchSite instanceof IWorkbenchPartSite)
				ws = (IWorkbenchPartSite)workbenchSite;
			if (ws != null) {
				ws.registerContextMenu(targetID, menuMgr, viewer);
			}
		}
	}
	
	/*
	 * Callback that is invoked when a context menu is about to be shown in the
	 * viewer. Subsclasses must implement to contribute menus. Also, menus can
	 * contributed by creating a viewer contribution with a <code>targetID</code> 
	 * that groups sets of actions that are related.
	 * 
	 * @param viewer the viewer in which the context menu is being shown.
	 * @param manager the menu manager to which actions can be added.
	 */
	private void fillContextMenu(StructuredViewer viewer, final IMenuManager manager) {
		// Populate the menu with the configured groups
		Object o = getConfiguration().getProperty(ISynchronizePageConfiguration.P_CONTEXT_MENU);
		if (!(o instanceof String[])) {
			o = ISynchronizePageConfiguration.DEFAULT_CONTEXT_MENU;
		}
		String[] groups = (String[])o;
		for (int i = 0; i < groups.length; i++) {
			String group = groups[i];
			// There is no need to adjust the group ids in a contetx menu (see setActionBars)
			manager.add(new Separator(group));
		}
		getActionGroup().setContext(new ActionContext(viewer.getSelection()));
		getActionGroup().fillContextMenu(manager);
	}
	
	private void updateActionBars(IStructuredSelection selection) {
		ActionGroup group = getActionGroup();
		if (group != null) {
			group.setContext(new ActionContext(selection));
			group.updateActionBars();
		}
	}
	
	protected SynchronizePageActionGroup getActionGroup() {
		return (SynchronizePageActionGroup)getConfiguration();
	}
	
	private String getGroupId(String group) {
		return ((SynchronizePageConfiguration)getConfiguration()).getGroupId(group);
	}
}
