package org.eclipse.debug.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * A tree view of launch configurations
 */
public class LaunchConfigurationView extends AbstractDebugView implements ILaunchConfigurationListener {
	
	private Viewer fViewer;
	
	/**
	 * The launch group to display
	 */
	private LaunchGroupExtension fLaunchGroup;
	
	/**
	 * Actions
	 */
	private CreateLaunchConfigurationAction fCreateAction;
	private DeleteLaunchConfigurationAction fDeleteAction;
	private DuplicateLaunchConfigurationAction fDuplicateAction;

	/**
	 * Manager for working set related a actions.
	 */
	private LaunchConfigurationWorkingSetActionManager fWorkingSetActionManager;
	
	/**
	 * Constructs a launch configuration view for the given launch group
	 */
	public LaunchConfigurationView(LaunchGroupExtension launchGroup) {
		super();
		fLaunchGroup = launchGroup;
	}
	
	/**
	 * Returns the launch group this view is displaying.
	 * 
	 * @return the launch group this view is displaying
	 */
	protected LaunchGroupExtension getLaunchGroup() {
		return fLaunchGroup;
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		TreeViewer treeViewer = new TreeViewer(parent);
		treeViewer.setLabelProvider(DebugUITools.newDebugModelPresentation());
		treeViewer.setSorter(new WorkbenchViewerSorter());
		treeViewer.setContentProvider(new LaunchConfigurationTreeContentProvider(fLaunchGroup.getMode(), parent.getShell()));
		treeViewer.addFilter(new LaunchGroupFilter(getLaunchGroup()));
		treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		treeViewer.expandAll();
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);

		IPropertyChangeListener titleUpdater= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property= event.getProperty();
				if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE.equals(property)) {
					// TODO: tooltip
					//updateTreeLabelTooltip();
				}
			}
		};		
		setWorkingSetActionManager(new LaunchConfigurationWorkingSetActionManager(treeViewer, parent.getShell(), titleUpdater));
		
		return treeViewer;
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		
		fCreateAction = new CreateLaunchConfigurationAction(getViewer());
		setAction(CreateLaunchConfigurationAction.ID_CREATE_ACTION, fCreateAction);
		
		fDeleteAction = new DeleteLaunchConfigurationAction(getViewer());
		setAction(DeleteLaunchConfigurationAction.ID_DELETE_ACTION, fDeleteAction);
		setAction(IDebugView.REMOVE_ACTION, fDeleteAction);
		
		fDuplicateAction = new DuplicateLaunchConfigurationAction(getViewer());
		setAction(DuplicateLaunchConfigurationAction.ID_DUPLICATE_ACTION, fDuplicateAction);
		
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.LAUNCH_CONFIGURATION_VIEW;
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(fCreateAction);
		menu.add(fDuplicateAction);
		menu.add(fDeleteAction);
		menu.add(new Separator());
		getWorkingSetActionManager().contributeToMenu(menu);
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
	}
	
	/**
	 * Returns this view's tree viewer
	 * 
	 * @return this view's tree viewer 
	 */
	protected TreeViewer getTreeViewer() {
		return (TreeViewer)getViewer();
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getWorkingSetActionManager().dispose();
		fCreateAction.dispose();
		fDeleteAction.dispose();
		fDuplicateAction.dispose();
		DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		try {
			getTreeViewer().add(configuration.getType(), configuration);
		} catch (CoreException e) {
		}
		getTreeViewer().setSelection(new StructuredSelection(configuration), true);
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		ILaunchConfigurationType type = null;
		int typeIndex= -1; // The index of the deleted configuration's type
		int configIndex= -1; // The index of the deleted configuration		
		// Initialize data used to set the selection after deletion
		TreeItem[] items= getTreeViewer().getTree().getItems();
		TreeItem typeItem;
		for (int i= 0, numTypes= items.length; (i < numTypes && type == null); i++) {
			typeItem= items[i];
			typeIndex= i;
			TreeItem[] configs= typeItem.getItems();
			for (int j= 0, numConfigs= configs.length; j < numConfigs; j++) {
				if (configuration.equals(configs[j].getData())) {
					configIndex= j;
					type = (ILaunchConfigurationType)typeItem.getData();
					break;
				}
			}
		}			
			
		getTreeViewer().remove(configuration);
		if (getViewer().getSelection().isEmpty()) {
			IStructuredSelection newSelection= null;
			if (typeIndex != -1 && configIndex != -1) {
				// Reset selection to the next config
				TreeItem[] configItems= getTreeViewer().getTree().getItems()[typeIndex].getItems();
				int numItems= configItems.length;
				if (numItems > configIndex) { // Select the item at the same index as the deleted
					newSelection= new StructuredSelection(configItems[configIndex].getData());
				} else if (numItems > 0) { // Deleted the last item(s). Select the last item
					newSelection= new StructuredSelection(configItems[numItems - 1].getData());
				}
			}
			if (newSelection == null && type != null) {
				// Reset selection to the config type of the first selected configuration
				newSelection = new StructuredSelection(type);
			}
			getTreeViewer().setSelection(newSelection);
		}
	}

	/**
	 * This is similar to IWorkbenchPart#createPartControl(Composite), but it is
	 * called by the launch dialog when creating the launch config tree view.
	 * Since this view is not contained in the workbench, we cannot do all the
	 * usual initialzation (toolbars, etc).
	 */
	public void createLaunchDialogControl(Composite parent) {
		fViewer = createViewer(parent);
		createActions();
		createContextMenu(getViewer().getControl());
		WorkbenchHelp.setHelp(parent, getHelpContextId());
		getViewer().getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		if (getViewer() instanceof StructuredViewer) {
			((StructuredViewer)getViewer()).addDoubleClickListener(this);
		}
	}
	
	

	/**
	 * @see org.eclipse.debug.ui.IDebugView#getViewer()
	 */
	public Viewer getViewer() {
		return fViewer;
	}
	
	private void setWorkingSetActionManager(LaunchConfigurationWorkingSetActionManager actionManager) {
		fWorkingSetActionManager = actionManager;
	}

	protected LaunchConfigurationWorkingSetActionManager getWorkingSetActionManager() {
		return fWorkingSetActionManager;
	}

	

}
