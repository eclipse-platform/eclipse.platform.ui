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
package org.eclipse.team.ui.synchronize;

import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.internal.ui.synchronize.SynchronizeModelElementLabelProvider;
import org.eclipse.ui.IActionBars;
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

	// Workbench site is used to register the context menu for the viewer
	private IWorkbenchPartSite site;
	// The id to use for registration of the context menu. If null then menu will not allow viewer contributions. 
	private String targetID;

	// The physical model shown to the user in the provided viewer. The information in 
	// this set is transformed by the model provider into the actual logical model displayed
	// in the viewer.
	private SyncInfoSet set;
	private StructuredViewer viewer;
	private ISynchronizeModelProvider modelProvider;
	
	// Listeners for model changes
	private ListenerList listeners;
	
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
	public StructuredViewerAdvisor(String targetID, IWorkbenchPartSite site, SyncInfoSet set) {
		this.set = set;
		this.targetID = targetID;
		this.site = site;
	}

	/**
	 * Create an advisor that will provide a presentation model based on the given sync info set.
	 * Note that it's important to call {@link #dispose()} when finished with an advisor.
	 * 
	 * @param set the set of <code>SyncInfo</code> objects that are to be shown to the user.
	 */
	public StructuredViewerAdvisor(SyncInfoSet set) {
		this(null, null, set);
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
	 * Sets a new selection for this viewer and optionally makes it visible. The advisor will try and
	 * convert the objects into the appropriate viewer objects. This is required because the model
	 * provider controls the actual model elements in the viewer and must be consulted in order to
	 * understand what objects can be selected in the viewer.
	 * 
	 * @param object the objects to select
	 * @param reveal <code>true</code> if the selection is to be made visible, and
	 *                  <code>false</code> otherwise
	 */
	public void setSelection(Object[] objects, boolean reveal) {
		ISelection selection = getSelection(objects);
		if (!selection.isEmpty()) {
			viewer.setSelection(selection, reveal);
		}
	}
	
	/**
	 * Gets a new selection that contains the view model objects that
	 * correspond to the given objects. The advisor will try and
	 * convert the objects into the appropriate viewer objects. 
	 * This is required because the model provider controls the actual 
	 * model elements in the viewer and must be consulted in order to
	 * understand what objects can be selected in the viewer.
	 * <p>
	 * This method does not affect the selection of the viewer itself.
	 * It's main purpose is for testing and should not be used by other
	 * clients.
	 * 
	 * @param object the objects to select
	 * @return a selection corresponding to the given objects
	 */
	public ISelection getSelection(Object[] objects) {
		if (modelProvider != null) {
	 		Object[] viewerObjects = new Object[objects.length];
			for (int i = 0; i < objects.length; i++) {
				viewerObjects[i] = modelProvider.getMapping(objects[i]);
			}
			return new StructuredSelection(viewerObjects);
		} else {
			return StructuredSelection.EMPTY;
		}
	}
	 
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
	protected void fillContextMenu(StructuredViewer viewer, IMenuManager manager) {
	}
	
	/**
	 * Allows the advisor to make contributions to the given action bars. Note that some of the 
	 * items in the action bar may not be accessible.
	 * 
	 * @param actionBars the toolbar manager to which to add actions.
	 */
	public void setActionBars(IActionBars actionBars) {	
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
		viewer.getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				StructuredViewerAdvisor.this.dispose();
			}
		});
	}
	
	/**
	 * Get the model provider that will be used to create the input
	 * for the adviser's viewer.
	 * @return the model provider
	 */
	protected abstract ISynchronizeModelProvider getModelProvider();
	
	
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
			IWorkbenchPartSite ws = getWorkbenchPartSite();
			if(ws == null)
				Utils.findSite(viewer.getControl());
			if (ws == null) 
				ws = Utils.findSite();
			if (ws != null) {
				ws.registerContextMenu(getTargetID(), menuMgr, viewer);
			} else {
				TeamUIPlugin.log(IStatus.ERROR, "Cannot add menu contributions because the site cannot be found: " + getTargetID(), null); //$NON-NLS-1$
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
		ISynchronizeModelElement input = modelProvider.getModelRoot();
		if (input instanceof DiffNode) {
			((DiffNode) input).addCompareInputChangeListener(new ICompareInputChangeListener() {
				public void compareInputChanged(ICompareInput source) {
					fireChanges();
				}
			});
		}
		viewer.setInput(modelProvider.getModelRoot());
	}
	
	/**
	 * Returns the part site in which to register the context menu viewer contributions for this
	 * advisor.
	 */
	protected IWorkbenchPartSite getWorkbenchPartSite() {
		return this.site;
	}
}