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
package org.eclipse.team.internal.ccvs.ui.repo;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.actions.OpenRemoteFileAction;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.*;

/**
 * This class acts as a superclass to all remote CVS tree views.
 */
public abstract class RemoteViewPart extends ViewPart implements ISelectionListener {
	
	// The tree viewer
	protected TreeViewer viewer;

	// Drill down adapter
	private DrillDownAdapter drillPart;
	
	private Action refreshAction;
	private Action collapseAllAction;
	
	private Action selectWorkingSetAction;
	private Action deselectWorkingSetAction;
	private Action editWorkingSetAction;
	private OpenRemoteFileAction openAction;
	
	private RemoteContentProvider contentProvider;
	private IDialogSettings settings;
	private static final String SELECTED_WORKING_SET = "SelectedWorkingSet"; //$NON-NLS-1$

	private class ChangeWorkingSetAction extends Action {
		String name;
		public ChangeWorkingSetAction(String name, int index) {
			super(NLS.bind(CVSUIMessages.RepositoriesView_workingSetMenuItem, new String[] { new Integer(index).toString(), name })); 
			this.name = name;
		}
		public void run() {
			IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
			setWorkingSet(manager.getWorkingSet(name), true);
		}
	}
	
	protected RemoteViewPart(String partName) {
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		settings = workbenchSettings.getSection(partName);
		if (settings == null) {
			settings = workbenchSettings.addNewSection(partName);
		}
		String name = settings.get(SELECTED_WORKING_SET);
		IWorkingSet set = null;
		if (name != null)
			set = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(name);
		setWorkingSet(set, false);
	}
	
	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		getSite().setSelectionProvider(viewer);
		viewer.setInput(getTreeInput());
		viewer.setSorter(new RepositorySorter());
		viewer.getControl().addKeyListener(getKeyListener());
		drillPart = new DrillDownAdapter(viewer);
		
		contributeActions();
		initializeListeners();

		// F1 Help
		String helpID = getHelpContextId();
		if (helpID != null)
            PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), helpID);
	}
	
	/**
	 * @see WorkbenchPart#setFocus
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	/**
	 * Method getHelpContextId should be overridden by subclasses to provide the
	 * appropriate help id.
	 * 
	 * @return String
	 */
	protected String getHelpContextId() {
		return null;
	}
	
	/**
	 * Method getTreeInput.
	 * @return Object
	 */
	protected abstract Object getTreeInput();
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.repo.RemoteViewPart#getContentProvider()
	 */
	protected RemoteContentProvider getContentProvider() {
		if (contentProvider == null) {
			contentProvider = new RemoteContentProvider();
		}
		return contentProvider;
	};

	protected KeyAdapter getKeyListener() {
		return new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.F5) {
					refreshAction.run();
				}
			}
		};
	}

	protected void initializeListeners() {
		// listen for selection changes in the repo view
		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
	}
	
	protected IWorkingSet getWorkingSet() {
		return getContentProvider().getWorkingSet();
	}
	
	protected void setWorkingSet(IWorkingSet workingSet, boolean refreshViewer) {
		if (settings != null) {
			String name = null;
			if (workingSet != null)
				name = workingSet.getName();
			settings.put(SELECTED_WORKING_SET, name);
		}
		getContentProvider().setWorkingSet(workingSet);
		String toolTip;
		if (workingSet == null) {
			toolTip = null;
		} else {
			toolTip = NLS.bind(CVSUIMessages.RemoteViewPart_workingSetToolTip, new String[] { workingSet.getName() }); 
		}
		setTitleToolTip(toolTip);
		if (refreshViewer) refreshViewer();
	}
	
	/**
	 * Contribute actions to the view
	 */
	protected void contributeActions() {
		
		final Shell shell = getShell();
		
		// Refresh (toolbar)
		CVSUIPlugin plugin = CVSUIPlugin.getPlugin();
		refreshAction = new Action(CVSUIMessages.RepositoriesView_refresh, CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_REFRESH_ENABLED)) { 
			public void run() {
				refreshAll();
			}
		};
		refreshAction.setToolTipText(CVSUIMessages.RepositoriesView_refreshTooltip); 
		refreshAction.setDisabledImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH_DISABLED));
		refreshAction.setHoverImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH));
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);

		collapseAllAction = new Action(CVSUIMessages.RepositoriesView_collapseAll, CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_COLLAPSE_ALL_ENABLED)) { 
			public void run() {
				collapseAll();
			}
		};
		collapseAllAction.setToolTipText(CVSUIMessages.RepositoriesView_collapseAllTooltip); 
		collapseAllAction.setHoverImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_COLLAPSE_ALL));
		
		// Select Working Set (popup)
		selectWorkingSetAction = new Action(CVSUIMessages.RepositoriesView_newWorkingSet) { 
			public void run() {
				IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
				IWorkingSetSelectionDialog dialog = manager.createWorkingSetSelectionDialog(shell, false);
				IWorkingSet workingSet = null;
				if (workingSet != null)
					dialog.setSelection(new IWorkingSet[]{workingSet});

				if (dialog.open() == Window.OK) {
					IWorkingSet[] result = dialog.getSelection();
					if (result != null && result.length > 0) {
						setWorkingSet(result[0], true);
						manager.addRecentWorkingSet(result[0]);
					} else {
						setWorkingSet(null, true);
					}
				}
			}
		};
        PlatformUI.getWorkbench().getHelpSystem().setHelp(selectWorkingSetAction, IHelpContextIds.SELECT_WORKING_SET_ACTION);

		// Deselect Working Set (popup)
		deselectWorkingSetAction = new Action(CVSUIMessages.RepositoriesView_deselectWorkingSet) { 
			public void run() {
				setWorkingSet(null, true);
			}
		};
        PlatformUI.getWorkbench().getHelpSystem().setHelp(deselectWorkingSetAction, IHelpContextIds.DESELECT_WORKING_SET_ACTION);

		// Edit Working Set (popup)
		editWorkingSetAction = new Action(CVSUIMessages.RepositoriesView_editWorkingSet) { 
			public void run() {
				IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
				IWorkingSet workingSet = getWorkingSet();
				if (workingSet == null) {
					setEnabled(false);
					return;
				}
				IWorkingSetEditWizard wizard = manager.createWorkingSetEditWizard(workingSet);
				if (wizard == null) {
					// todo
                    CVSUIPlugin.log(IStatus.ERROR, NLS.bind("No wizard registered for working set {0}", new Object[] {workingSet.getName()}), null); //$NON-NLS-1$
                    return;
				}
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.create();
				if (dialog.open() == Window.OK)
					setWorkingSet(wizard.getSelection(), true);
			}
		};
        PlatformUI.getWorkbench().getHelpSystem().setHelp(editWorkingSetAction, IHelpContextIds.EDIT_WORKING_SET_ACTION);

		// Create the popup menu
		MenuManager menuMgr = new MenuManager();
		Tree tree = viewer.getTree();
		Menu menu = menuMgr.createContextMenu(tree);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				addWorkbenchActions(manager);
			}

		});
		menuMgr.setRemoveAllWhenShown(true);
		tree.setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);

		// Create the local tool bar
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager tbm = bars.getToolBarManager();
		drillPart.addNavigationActions(tbm);
		tbm.add(refreshAction);
		tbm.add(new Separator());
		tbm.add(collapseAllAction);
		tbm.update(false);

		// Create the open action for double clicks
		openAction = new OpenRemoteFileAction();
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				handleDoubleClick(e);
			}
		});

		updateWorkingSetMenu();
		bars.updateActionBars();
	}
	
	/**
	 * Add the menu actions that were contributed in plugin.xml
	 * 
	 * @param manager
	 */
	protected void addWorkbenchActions(IMenuManager manager) {
		// File actions go first (view file)
		manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
		// Misc additions
		manager.add(new Separator("checkoutGroup")); //$NON-NLS-1$
		manager.add(new Separator("tagGroup")); //$NON-NLS-1$
		manager.add(new Separator("miscGroup")); //$NON-NLS-1$
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		manager.add(refreshAction);
	}
	
	/**
	 * Method getShell.
	 * @return Shell
	 */
	protected Shell getShell() {
		return viewer.getTree().getShell();
	}
	
	public void updateWorkingSetMenu() {
		IActionBars bars = getViewSite().getActionBars();
		IMenuManager mgr = bars.getMenuManager();

		mgr.removeAll();

		mgr.add(selectWorkingSetAction);
		mgr.add(deselectWorkingSetAction);
		deselectWorkingSetAction.setEnabled(getWorkingSet() != null);
		mgr.add(editWorkingSetAction);
		editWorkingSetAction.setEnabled(getWorkingSet() != null);

		mgr.add(new Separator());

		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet[] workingSets = manager.getWorkingSets();
		for (int i = 0; i < workingSets.length; i++) {
			String name = workingSets[i].getName();
			ChangeWorkingSetAction action = new ChangeWorkingSetAction(name, i + 1);
			mgr.add(action);
			action.setChecked(getWorkingSet() != null && getWorkingSet().getName().equals(name));
		}

		bars.updateActionBars();
	}
	
	protected void refreshAll() {
		//CVSUIPlugin.getPlugin().getRepositoryManager().clearCaches();
		refreshViewer();
	}

	protected void refreshViewer() {
		if (viewer == null) return;
		((RemoteContentProvider)viewer.getContentProvider()).cancelJobs(CVSUIPlugin.getPlugin().getRepositoryManager().getKnownRepositoryRoots());
		CVSUIPlugin.getPlugin().getRepositoryManager().purgeCache();
		updateWorkingSetMenu();
        viewer.getControl().setRedraw(false);
		viewer.refresh();
        viewer.getControl().setRedraw(true);
	}
	
	public void collapseAll() {
		if (viewer == null) return;
		viewer.getControl().setRedraw(false);
		viewer.collapseToLevel(viewer.getInput(), AbstractTreeViewer.ALL_LEVELS);
		viewer.getControl().setRedraw(true);
	}
	/**
	 * The mouse has been double-clicked in the tree, perform appropriate
	 * behaviour.
	 */
	private void handleDoubleClick(DoubleClickEvent e) {
		// Only act on single selection
		ISelection selection = e.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection)selection;
			if (structured.size() == 1) {
				Object first = structured.getFirstElement();
				if (first instanceof ICVSRemoteFile) {
					// It's a file, open it.
					openAction.selectionChanged(null, selection);
					openAction.run(null);
				} else {
					// Try to expand/contract
					viewer.setExpandedState(first, !viewer.getExpandedState(first));
				}
			}
		}
	}
	
	/**
	 * Returns the viewer.
	 * @return TreeViewer
	 */
	protected TreeViewer getViewer() {
		return viewer;
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);
		super.dispose();
		viewer = null;
	}
	
}
