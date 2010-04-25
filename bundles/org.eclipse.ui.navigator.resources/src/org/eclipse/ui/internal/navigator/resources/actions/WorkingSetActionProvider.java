/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     anton.leherbauer@windriver.com - bug 212389 [CommonNavigator] working set issues: 
 *         missing project, window working set inconsistency
 *******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IAggregateWorkingSet;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.internal.navigator.workingsets.WorkingSetsContentProvider;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * @since 3.2
 * 
 */
public class WorkingSetActionProvider extends CommonActionProvider {

	private static final String TAG_CURRENT_WORKING_SET_NAME = "currentWorkingSetName"; //$NON-NLS-1$

	private boolean contributedToViewMenu = false;

	private CommonViewer viewer;

	private INavigatorContentService contentService;

	private WorkingSetFilterActionGroup workingSetActionGroup;
	private WorkingSetRootModeActionGroup workingSetRootModeActionGroup;

	private Object originalViewerInput = ResourcesPlugin.getWorkspace().getRoot();

	private IExtensionStateModel extensionStateModel;

	private final ResourceWorkingSetFilter workingSetFilter = new ResourceWorkingSetFilter();
	private boolean filterAdded;

	private boolean emptyWorkingSet;
	private IWorkingSet workingSet;

	private IPropertyChangeListener topLevelModeListener;
	
	private boolean ignoreFilterChangeEvents;

	/**
	 * Provides a smart listener to monitor changes to the Working Set Manager.
	 * 
	 */
	public class WorkingSetManagerListener implements IPropertyChangeListener {

		private boolean listening = false;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org
		 * .eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();
			Object newValue = event.getNewValue();
			Object oldValue = event.getOldValue();

			String newLabel = null;
			if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property) && oldValue == workingSet) {
				newLabel = ""; //$NON-NLS-1$
				setWorkingSet(null);
			} else if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE.equals(property) && newValue == workingSet) {
				newLabel = workingSet.getLabel();
			} else if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property) && newValue == workingSet) {
				if (workingSet.isAggregateWorkingSet() && workingSet.isEmpty()) {
					// act as if the working set has been made null
					if (!emptyWorkingSet) {
						emptyWorkingSet = true;
						workingSetFilter.setWorkingSet(null);
						newLabel = null;
					}
				} else {
					// we've gone from empty to non-empty on our set.
					// Restore it.
					if (emptyWorkingSet) {
						emptyWorkingSet = false;
						workingSetFilter.setWorkingSet(workingSet);
						newLabel = workingSet.getLabel();
					}
				}
			}
			if (viewer != null) {
				if (newLabel != null)
					viewer.getCommonNavigator().setWorkingSetLabel(newLabel);
				viewer.getFrameList().reset();
				viewer.refresh();
			}
		}

		/**
		 * Begin listening to the correct source if not already listening.
		 */
		public synchronized void listen() {
			if (!listening) {
				PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(managerChangeListener);
				listening = true;
			}
		}

		/**
		 * Begin listening to the correct source if not already listening.
		 */
		public synchronized void ignore() {
			if (listening) {
				PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(managerChangeListener);
				listening = false;
			}
		}
	}

	private IPropertyChangeListener filterChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			
			if (ignoreFilterChangeEvents)
				return;
			
			IWorkingSet newWorkingSet = (IWorkingSet) event.getNewValue();

			setWorkingSet(newWorkingSet);
			if (newWorkingSet != null) {
				if (!contentService.isActive(WorkingSetsContentProvider.EXTENSION_ID)) {
					contentService.getActivationService().activateExtensions(
							new String[] { WorkingSetsContentProvider.EXTENSION_ID }, false);
					contentService.getActivationService().persistExtensionActivations();
				}
				if (newWorkingSet.isAggregateWorkingSet()) {
					IAggregateWorkingSet agWs = (IAggregateWorkingSet) newWorkingSet;
					IWorkingSet[] comps = agWs.getComponents();
					if (comps.length > 1) {
						viewer.getCommonNavigator().setWorkingSetLabel(
								WorkbenchNavigatorMessages.WorkingSetActionProvider_multipleWorkingSets);
					} else if (comps.length > 0) {
						viewer.getCommonNavigator().setWorkingSetLabel(comps[0].getLabel());
					} else {
						viewer.getCommonNavigator().setWorkingSetLabel(null);
					}
				} else
					viewer.getCommonNavigator().setWorkingSetLabel(workingSet.getLabel());
			} else {
				viewer.getCommonNavigator().setWorkingSetLabel(null);
			}

			viewer.getFrameList().reset();
		}
	};

	private WorkingSetManagerListener managerChangeListener = new WorkingSetManagerListener();

	private IExtensionActivationListener activationListener = new IExtensionActivationListener() {

		private IWorkingSet savedWorkingSet;

		public void onExtensionActivation(String aViewerId, String[] theNavigatorExtensionIds, boolean isActive) {

			for (int i = 0; i < theNavigatorExtensionIds.length; i++) {
				if (WorkingSetsContentProvider.EXTENSION_ID.equals(theNavigatorExtensionIds[i])) {
					if (isActive) {
						extensionStateModel = contentService.findStateModel(WorkingSetsContentProvider.EXTENSION_ID);
						workingSetRootModeActionGroup.setStateModel(extensionStateModel);
						extensionStateModel.addPropertyChangeListener(topLevelModeListener);

						if (savedWorkingSet != null) {
							setWorkingSet(savedWorkingSet);
						}
						managerChangeListener.listen();

					} else {
						savedWorkingSet = workingSet;
						setWorkingSet(null);
						viewer.getCommonNavigator().setWorkingSetLabel(null);
						managerChangeListener.ignore();
						workingSetRootModeActionGroup.setShowTopLevelWorkingSets(false);
						extensionStateModel.removePropertyChangeListener(topLevelModeListener);

					}
				}
			}
		}

	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator
	 * .ICommonActionExtensionSite)
	 */
	public void init(ICommonActionExtensionSite aSite) {
		viewer = (CommonViewer) aSite.getStructuredViewer();
		contentService = aSite.getContentService();

		extensionStateModel = contentService.findStateModel(WorkingSetsContentProvider.EXTENSION_ID);

		workingSetActionGroup = new WorkingSetFilterActionGroup(aSite.getViewSite().getShell(), filterChangeListener);
		workingSetRootModeActionGroup = new WorkingSetRootModeActionGroup(viewer, extensionStateModel);

		topLevelModeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				setWorkingSet(workingSet);
				viewer.getFrameList().reset();
			}
		};

		if (contentService.isActive(WorkingSetsContentProvider.EXTENSION_ID)) {
			managerChangeListener.listen();
			extensionStateModel.addPropertyChangeListener(topLevelModeListener);
		}

		contentService.getActivationService().addExtensionActivationListener(activationListener);
	}

	/**
	 * Restores the working set filter from the persistence store.
	 */
	protected void initWorkingSetFilter(String workingSetName) {
		IWorkingSet workingSet = null;

		if (workingSetName != null && workingSetName.length() > 0) {
			IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
			workingSet = workingSetManager.getWorkingSet(workingSetName);
		} else if (PlatformUI.getPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.USE_WINDOW_WORKING_SET_BY_DEFAULT)) {
			// use the window set by default if the global preference is set
			workingSet = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getAggregateWorkingSet();
		}

		if (workingSet != null) {
			setWorkingSet(workingSet);
		}
	}

	/**
	 * Set current active working set.
	 * 
	 * @param workingSet
	 *            working set to be activated, may be <code>null</code>
	 */
	protected void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
		emptyWorkingSet = workingSet != null && workingSet.isAggregateWorkingSet() && workingSet.isEmpty();

		workingSetFilter.setWorkingSet(emptyWorkingSet ? null : workingSet);

        ignoreFilterChangeEvents = true;
        try {
        	workingSetActionGroup.setWorkingSet(workingSet);
        } finally {
        	ignoreFilterChangeEvents = false;
       	}		
		
		if (viewer != null) {
			if (workingSet == null || emptyWorkingSet
					|| !extensionStateModel.getBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS)) {
				if (viewer.getInput() != originalViewerInput) {
					viewer.setInput(originalViewerInput);
				}
				if (!filterAdded) {
					viewer.addFilter(workingSetFilter);
					filterAdded = true;
				} else {
					viewer.refresh();
				}
			} else {
				viewer.removeFilter(workingSetFilter);
				filterAdded = false;
				if (!workingSet.isAggregateWorkingSet()) {
					IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
					viewer.setInput(workingSetManager.createAggregateWorkingSet(
							"", "", new IWorkingSet[] { workingSet })); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					viewer.setInput(workingSet);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.navigator.CommonActionProvider#restoreState(org.eclipse
	 * .ui.IMemento)
	 */
	public void restoreState(IMemento aMemento) {
		super.restoreState(aMemento);

		boolean showWorkingSets = true;
		if (aMemento != null) {
			Integer showWorkingSetsInt = aMemento.getInteger(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS);
			showWorkingSets = showWorkingSetsInt == null || showWorkingSetsInt.intValue() == 1;
			extensionStateModel.setBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS,
					showWorkingSets);
			workingSetRootModeActionGroup.setShowTopLevelWorkingSets(showWorkingSets);

			String lastWorkingSetName = aMemento.getString(TAG_CURRENT_WORKING_SET_NAME);
			initWorkingSetFilter(lastWorkingSetName);
		} else {
			showWorkingSets = false;

			extensionStateModel.setBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS,
					showWorkingSets);
			workingSetRootModeActionGroup.setShowTopLevelWorkingSets(showWorkingSets);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.navigator.CommonActionProvider#saveState(org.eclipse.ui
	 * .IMemento)
	 */
	public void saveState(IMemento aMemento) {
		super.saveState(aMemento);

		if (aMemento != null) {
			int showWorkingSets = extensionStateModel
					.getBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS) ? 1 : 0;
			aMemento.putInteger(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, showWorkingSets);

			if (workingSet != null) {
				aMemento.putString(TAG_CURRENT_WORKING_SET_NAME, workingSet.getName());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars
	 * )
	 */
	public void fillActionBars(IActionBars actionBars) {
		if (!contributedToViewMenu) {
			try {
				super.fillActionBars(actionBars);
				workingSetActionGroup.fillActionBars(actionBars);
				if (workingSetRootModeActionGroup != null) {
					workingSetRootModeActionGroup.fillActionBars(actionBars);
				}
			} finally {
				contributedToViewMenu = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	public void dispose() {
		super.dispose();
		workingSetActionGroup.dispose();
		if (workingSetRootModeActionGroup != null) {
			workingSetRootModeActionGroup.dispose();
		}

		managerChangeListener.ignore();
		extensionStateModel.removePropertyChangeListener(topLevelModeListener);

		contentService.getActivationService().removeExtensionActivationListener(activationListener);
	}

	/**
	 * This is used only for the tests.
	 * 
	 * @return a PropertyChangeListener
	 */
	public IPropertyChangeListener getFilterChangeListener() {
		return filterChangeListener;
	}

}
