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
package org.eclipse.team.ui.synchronize.viewers;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.PluginAction;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;

/**
 * A <code>StructuredViewerAdvisor</code> object controls various UI
 * aspects of viewers that show {@link SyncInfoSet} like the context menu, toolbar, content
 * provider, label provider, and model provider.
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
 * <p>
 * Clients may use this class as is, or subclass to add new state and behavior.
 * The default behavior is to show sync info in a tree
 * </p>
 * @since 3.0
 */
public abstract class StructuredViewerAdvisor {
	private SynchronizeModelProvider modelProvider;
	private ListenerList listeners;
	private String menuId;

	private SyncInfoSet set;
	private StructuredViewer viewer;
	
	public StructuredViewerAdvisor(String menuId, SyncInfoSet set) {
		this.set = set;
		this.menuId = menuId;
	}

	public StructuredViewerAdvisor(SyncInfoSet set) {
		this(null, set);
	}
		
	/**
	 * Initialize the viewer with the elements of this configuration, including
	 * content and label providers, sorter, input and menus. This method is
	 * invoked from the constructor of <code>SyncInfoDiffTreeViewer</code> to
	 * initialize the viewers. A configuration instance may only be used with
	 * one viewer.

	 * @param viewer the viewer being initialized
	 */
	public void initializeViewer(StructuredViewer viewer) {
		Assert.isTrue(this.viewer == null, "Can only be initialized once."); //$NON-NLS-1$
		this.viewer = viewer;
	
		initializeListeners(viewer);
		hookContextMenu(viewer);
		initializeActions(viewer);
		viewer.setLabelProvider(getLabelProvider());
		viewer.setContentProvider(getContentProvider());
		
		// The input may of been set already. In that case, don't change it and
		// simply assign it to the view.
		if(modelProvider == null) {
			modelProvider = getModelProvider();
			modelProvider.prepareInput(null);
		}
		setInput(viewer);
	}
	
	public void addInputChangedListener(ISynchronizeModelChangeListener listener) {
		if (listeners == null)
			listeners= new ListenerList();
		listeners.add(listener);
	}

	/**
	 * Cleanup listeners
	 */
	public void dispose() {
		if(modelProvider != null) {
			modelProvider.dispose();
		}
	}

	/**
	 * Return the menu id that is used to obtain context menu items from the
	 * workbench.
	 * @return the menuId.
	 */
	public String getMenuId() {
		return menuId;
	}

	/**
	 * Return the <code>SyncInfoSet</code> being shown by the viewer
	 * associated with this configuration.
	 * @return a <code>SyncInfoSet</code>
	 */
	public SyncInfoSet getSyncInfoSet() {
		return set;
	}
	
	public abstract boolean navigate(boolean next);

	/**
	 * Creates the input for this view and initializes it. At the time this method
	 * is called the viewer may not of been created yet. 
	 * 
	 * @param monitor shows progress while preparing the input
	 * @return the input that can be shown in a viewer
	 */
	public Object prepareInput(IProgressMonitor monitor) throws TeamException {
		if(modelProvider != null) {
			modelProvider.dispose();
		}
		modelProvider = getModelProvider();		
		return modelProvider.prepareInput(monitor);
	}

	public void removeInputChangedListener(ISynchronizeModelChangeListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty())
				listeners= null;
		}
	}

	/**
	 * Method invoked from <code>initializeViewer(Composite, StructuredViewer)</code>
	 * in order to initialize any listeners for the viewer.
	 * @param viewer
	 *            the viewer being initialize
	 */
	protected abstract void initializeListeners(final StructuredViewer viewer);
	
	/**
	 * Get the input that will be assigned to the viewer initialized by this
	 * configuration. Subclass may override.
	 * @return the viewer input
	 */
	protected abstract SynchronizeModelProvider getModelProvider();
	
	/**
	 * Callback that is invoked when a context menu is about to be shown in the
	 * viewer. Subsclasses must implement to contribute menus. Also, menus can
	 * contributed by creating a viewer contribution with a <code>targetID</code> 
	 * that groups sets of actions that are related.
	 * 
	 * @param viewer
	 *            the viewer
	 * @param manager
	 *            the menu manager
	 */
	protected void fillContextMenu(final StructuredViewer viewer, IMenuManager manager) {
		// subclasses will add actions
	}
	
	/**
	 * Method invoked from <code>initializeViewer(Composite, StructuredViewer)</code>
	 * in order to initialize any actions for the viewer. It is invoked before
	 * the input is set on the viewer in order to allow actions to be
	 * initialized before there is any reaction to the input being set (e.g.
	 * selecting and opening the first element).
	 * <p>
	 * The default behavior is to add the up and down navigation nuttons to the
	 * toolbar. Subclasses can override.
	 * @param viewer
	 *            the viewer being initialize
	 */
	protected void initializeActions(StructuredViewer viewer) {
	}
	
	/**
	 * Returns whether workbench menu items whould be included in the context
	 * menu. By default, this returns <code>true</code> if there is a menu id
	 * and <code>false</code> otherwise
	 * @return whether to include workbench context menu items
	 */
	protected boolean allowParticipantMenuContributions() {
		return getMenuId() != null;
	}

	/**
	 * Run the runnable in the UI thread.
	 * @param r the runnable to run in the UI thread.
	 */
	protected void aSyncExec(Runnable r) {
		final Control ctrl = viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.getDisplay().asyncExec(r);
		}
	}

	protected void fireChanges() {
		if (listeners != null) {
			Object[] l= listeners.getListeners();
			for (int i= 0; i < l.length; i++)
				((ISynchronizeModelChangeListener) l[i]).modelChanged(modelProvider.getInput());
		}
	}

	protected IStructuredContentProvider getContentProvider() {
		return new BaseWorkbenchContentProvider();
	}


	/**
	 * Get the label provider that will be assigned to the viewer initialized
	 * by this configuration. Subclass may override but should either wrap the
	 * default one provided by this method or subclass <code>TeamSubscriberParticipantLabelProvider</code>.
	 * In the later case, the logical label provider should still be assigned
	 * to the subclass of <code>TeamSubscriberParticipantLabelProvider</code>.
	 * @param logicalProvider
	 *            the label provider for the selected logical view
	 * @return a label provider
	 * @see SynchronizeModelElementLabelProvider
	 */
	protected ILabelProvider getLabelProvider() {
		return new SynchronizeModelElementLabelProvider();
	}

	protected StructuredViewer getViewer() {
		return viewer;
	}

	/**
	 * Method invoked from <code>initializeViewer(Composite, StructuredViewer)</code>
	 * in order to configure the viewer to call <code>fillContextMenu(StructuredViewer, IMenuManager)</code>
	 * when a context menu is being displayed in the diff tree viewer.
	 * @param viewer
	 *            the viewer being initialized
	 * @see fillContextMenu(StructuredViewer, IMenuManager)
	 */
	protected final void hookContextMenu(final StructuredViewer viewer) {
		final MenuManager menuMgr = new MenuManager(getMenuId()); //$NON-NLS-1$
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
			public void menuShown(MenuEvent e) {
				IContributionItem[] items = menuMgr.getItems();
				for (int i = 0; i < items.length; i++) {
					IContributionItem item = items[i];
					if (item instanceof ActionContributionItem) {
						IAction actionItem = ((ActionContributionItem) item).getAction();
						if (actionItem instanceof PluginAction) {
							((PluginAction) actionItem).selectionChanged(viewer.getSelection());
						}
					}
				}
			}
		});
		viewer.getControl().setMenu(menu);
		if (allowParticipantMenuContributions()) {
			IWorkbenchPartSite site = Utils.findSite(viewer.getControl());
			if (site == null) {
				site = Utils.findSite();
			}
			if (site != null) {
				site.registerContextMenu(getMenuId(), menuMgr, viewer);
			}
		}
	}

	/**
	 * @param viewer
	 */
	protected final void setInput(StructuredViewer viewer) {
		modelProvider.setViewer(viewer);
		viewer.setSorter(modelProvider.getViewerSorter());
		DiffNode input = modelProvider.getInput();
		input.addCompareInputChangeListener(new ICompareInputChangeListener() {
			public void compareInputChanged(ICompareInput source) {
				fireChanges();
			}
		});
		viewer.setInput(modelProvider.getInput());
	}
}
