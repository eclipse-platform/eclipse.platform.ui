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
public abstract class StructuredViewerAdvisor {
	
	private SynchronizeModelProvider modelProvider;
	private ListenerList listeners;
	private String targetID;

	private SyncInfoSet set;
	private StructuredViewer viewer;
	
	/**
	 * Create an advisor that will allow viewer contributions with the given <code>targetID</code>. This
	 * advisor will provide a presentation model based on the given sync info set. Note that it's important
	 * to call {@link #dispose()} when finished with an advisor.
	 * 
	 * @param targetID the targetID defined in the viewer contributions in a plugin.xml file.
	 * @param set the set of <code>SyncInfo</code> objects that are to be shown to the user.
	 */
	public StructuredViewerAdvisor(String targetID, SyncInfoSet set) {
		this.set = set;
		this.targetID = targetID;
	}

	/**
	 * Create an advisor that will provide a presentation model based on the given sync info set.
	 * Note that it's important to call {@link #dispose()} when finished with an advisor.
	 * 
	 * @param set the set of <code>SyncInfo</code> objects that are to be shown to the user.
	 */
	public StructuredViewerAdvisor(SyncInfoSet set) {
		this(null, set);
	}
		
	/**
	 * Install a viewer to be configured with this advisor. An advisor can only be installed with
	 * one viewer at a time. When this method completes the viewer is considered initialized and
	 * can be shown to the user. 

	 * @param viewer the viewer being installed
	 */
	public final void initializeViewer(StructuredViewer viewer) {
		Assert.isTrue(this.viewer == null, "Can only be initialized once."); //$NON-NLS-1$
		Assert.isTrue(validateViewer(viewer));
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
	
	/**
	 * This is called to add a listener to the model shown in the viewer. The listener is
	 * called when the model is changed or updated.
	 * 
	 * @param listener the listener to add
	 */
	public void addInputChangedListener(ISynchronizeModelChangeListener listener) {
		if (listeners == null)
			listeners= new ListenerList();
		listeners.add(listener);
	}

	/**
	 * Remove a model listener.
	 * 
	 * @param listener the listener to remove.
	 */
	public void removeInputChangedListener(ISynchronizeModelChangeListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty())
				listeners= null;
		}
	}
	
	/**
	 * Must be called when an advisor is no longer needed.
	 */
	public void dispose() {
		if(modelProvider != null) {
			modelProvider.dispose();
		}
	}

	/**
	 * Return the targetID that is used to obtain context menu items from the workbench. When
	 * a context menu is added to the viewer, this ID is registered with the workbench to allow
	 * viewer contributions.
	 * 
	 * @return the targetID or <code>null</code> if this advisor doesn't allow contributions.
	 */
	public String getTargetID() {
		return targetID;
	}

	/**
	 * Return the <code>SyncInfoSet</code> used to create the model shown by this advisor.
	 * 
	 * @return the <code>SyncInfoSet</code> used to create the model shown by this advisor.
	 */
	public SyncInfoSet getSyncInfoSet() {
		return set;
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
	 * Creates the model that will be shown in the viewers. This can be called before the
	 * viewer has been created.
	 * <p>
	 * The result of this method can be shown used as the input to a viewer. However, the
	 * prefered method of initializing a viewer is to call {@link #initializeViewer(StructuredViewer)}
	 * directly. This method only exists when the model must be created before the
	 * viewer.
	 * </p>
	 * @param monitor shows progress while preparing the model
	 * @return the model that can be shown in a viewer
	 */
	public Object prepareInput(IProgressMonitor monitor) throws TeamException {
		if(modelProvider != null) {
			modelProvider.dispose();
		}
		modelProvider = getModelProvider();		
		return modelProvider.prepareInput(monitor);
	}

	/**
	 * Callback that is invoked when a context menu is about to be shown in the
	 * viewer. Subsclasses must implement to contribute menus. Also, menus can
	 * contributed by creating a viewer contribution with a <code>targetID</code> 
	 * that groups sets of actions that are related.
	 * 
	 * @param viewer the viewer in which the context menu is being shown.
	 * @param manager the menu manager to which actions can be added.
	 */
	protected void fillContextMenu(final StructuredViewer viewer, IMenuManager manager) {
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
	 * </p>
	 * @param viewer the viewer being initialize
	 */
	protected void initializeActions(StructuredViewer viewer) {
	}
	
	/**
	 * Method invoked from <code>initializeViewer(Composite, StructuredViewer)</code>
	 * in order to initialize any listeners for the viewer.
	 *
	 * @param viewer the viewer being initialize
	 */
	protected void initializeListeners(final StructuredViewer viewer) {
	}
	
	/**
	 * Get the input that will be assigned to the viewer initialized by this
	 * configuration. Subclass may override.
	 * @return the viewer input
	 */
	protected abstract SynchronizeModelProvider getModelProvider();
	
	
	/**
	 * Subclasses can validate that the viewer being initialized with this advisor
	 * is of the correct type.
	 * 
	 * @param viewer the viewer to validate
	 * @return <code>true</code> if the viewer is valid, <code>false</code> otherwise.
	 */
	protected abstract boolean validateViewer(StructuredViewer viewer);
	
	/**
	 * Returns whether workbench menu items whould be included in the context
	 * menu. By default, this returns <code>true</code> if there is a menu id
	 * and <code>false</code> otherwise
	 * @return whether to include workbench context menu items
	 */
	protected boolean allowParticipantMenuContributions() {
		return getTargetID() != null;
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

	private void fireChanges() {
		if (listeners != null) {
			Object[] l= listeners.getListeners();
			for (int i= 0; i < l.length; i++)
				((ISynchronizeModelChangeListener) l[i]).modelChanged(modelProvider.getModelRoot());
		}
	}

	/**
	 * Returns the content provider for the viewer.
	 * 
	 * @return the content provider for the viewer.
	 */
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

	/**
	 * Returns the viewer configured by this advisor.
	 * 
	 * @return the viewer configured by this advisor.
	 */
	protected StructuredViewer getViewer() {
		return viewer;
	}

	/**
	 * Method invoked from <code>initializeViewer(StructuredViewer)</code>
	 * in order to configure the viewer to call <code>fillContextMenu(StructuredViewer, IMenuManager)</code>
	 * when a context menu is being displayed in viewer.
	 * 
	 * @param viewer the viewer being initialized
	 * @see fillContextMenu(StructuredViewer, IMenuManager)
	 */
	protected final void hookContextMenu(final StructuredViewer viewer) {
		final MenuManager menuMgr = new MenuManager(getTargetID()); //$NON-NLS-1$
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
				site.registerContextMenu(getTargetID(), menuMgr, viewer);
			}
		}
	}

	/**
	 * Called to set the input to a viewer. The input to a viewer is always the model created
	 * by the model provider.
	 * 
	 * @param viewer the viewer to set the input.
	 */
	protected final void setInput(StructuredViewer viewer) {
		modelProvider.setViewer(viewer);
		viewer.setSorter(modelProvider.getViewerSorter());
		DiffNode input = modelProvider.getModelRoot();
		input.addCompareInputChangeListener(new ICompareInputChangeListener() {
			public void compareInputChanged(ICompareInput source) {
				fireChanges();
			}
		});
		viewer.setInput(modelProvider.getModelRoot());
	}
}