/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.actions.OpenRemoteFileAction;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

/**
 * This class acts as a superclass to all remote CVS tree views.
 */
public abstract class RemoteViewPart extends ViewPart implements ISelectionListener {
	
	// The tree viewer
	private TreeViewer viewer;

	// Drill down adapter
	private DrillDownAdapter drillPart;
	
	private Action refreshAction;
	private Action newWorkingSetAction;
	private Action deselectWorkingSetAction;
	private Action editWorkingSetAction;
	private OpenRemoteFileAction openAction;

	public class ChangeWorkingSetAction extends Action {
		String name;
		public ChangeWorkingSetAction(String name, int index) {
			super(Policy.bind("RepositoriesView.workingSetMenuItem", new Integer(index).toString(), name));
			this.name = name;
		}
		public void run() {
			CVSUIPlugin.getPlugin().getRepositoryManager().setCurrentWorkingSet(name);
		}
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

		initializeSelectionListeners();

		// F1 Help
		String helpID = getHelpContextId();
		if (helpID != null)
			WorkbenchHelp.setHelp(viewer.getControl(), helpID);
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
	protected IContentProvider getContentProvider() {
		return new RemoteContentProvider();
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

	protected void initializeSelectionListeners() {
		// listen for selection changes in the repo view
		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
	}
	
	/**
	 * Contribute actions to the view
	 */
	protected void contributeActions() {
		
		final Shell shell = getShell();
		
		// Refresh (toolbar)
		CVSUIPlugin plugin = CVSUIPlugin.getPlugin();
		refreshAction = new Action(Policy.bind("RepositoriesView.refresh"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_REFRESH_ENABLED)) { //$NON-NLS-1$
			public void run() {
				refreshAll();
			}
		};
		refreshAction.setToolTipText(Policy.bind("RepositoriesView.refreshTooltip")); //$NON-NLS-1$
		refreshAction.setDisabledImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH_DISABLED));
		refreshAction.setHoverImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH));

		// New Working Set (popup)
		newWorkingSetAction = new Action(Policy.bind("RepositoriesView.newWorkingSet")) { //$NON-NLS-1$
			public void run() {
				WorkingSetSelectionDialog dialog = new WorkingSetSelectionDialog(shell, false);
				dialog.open();
				CVSWorkingSet[] sets = dialog.getSelection();
				if (sets != null && sets.length > 0) {
					RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
					manager.setCurrentWorkingSet(sets[0]);
					try {
						manager.saveState();
					} catch (TeamException e) {
						CVSUIPlugin.openError(null, null, null, e);
					}
				}
			}
		};
		//WorkbenchHelp.setHelp(newAction, IHelpContextIds.NEW_CVS_WORKING_SET_ACTION);

		// Deselect Working Set (popup)
		deselectWorkingSetAction = new Action(Policy.bind("RepositoriesView.deselectWorkingSet")) { //$NON-NLS-1$
			public void run() {
				String name = null;
				CVSUIPlugin.getPlugin().getRepositoryManager().setCurrentWorkingSet(name);
				refreshViewer();
			}
		};
		//WorkbenchHelp.setHelp(newAction, IHelpContextIds.NEW_CVS_WORKING_SET_ACTION);

		// Edit Working Set (popup)
		editWorkingSetAction = new Action(Policy.bind("RepositoriesView.editWorkingSet")) { //$NON-NLS-1$
			public void run() {
				String name = null;
				CVSWorkingSet set = CVSUIPlugin.getPlugin().getRepositoryManager().getCurrentWorkingSet();
				RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
				CVSWorkingSetWizard wizard = new CVSWorkingSetWizard(set);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				if (dialog.open() == Window.OK) {
					CVSWorkingSet newSet = wizard.getSelection();
					manager.addWorkingSet(newSet);
					manager.setCurrentWorkingSet(newSet);
					try {
						manager.saveState();
					} catch (TeamException e) {
						CVSUIPlugin.openError(null, null, null, e);
					}
				}
			}
		};
		//WorkbenchHelp.setHelp(newAction, IHelpContextIds.NEW_CVS_WORKING_SET_ACTION);

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

		mgr.add(newWorkingSetAction);
		mgr.add(deselectWorkingSetAction);
		deselectWorkingSetAction.setEnabled(CVSUIPlugin.getPlugin().getRepositoryManager().getCurrentWorkingSet() != null);
		mgr.add(editWorkingSetAction);
		editWorkingSetAction.setEnabled(CVSUIPlugin.getPlugin().getRepositoryManager().getCurrentWorkingSet() != null);

		mgr.add(new Separator());

		RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
		String[] workingSets = manager.getWorkingSetNames();
		CVSWorkingSet current = manager.getCurrentWorkingSet();
		for (int i = 0; i < workingSets.length; i++) {
			String name = workingSets[i];
			ChangeWorkingSetAction action = new ChangeWorkingSetAction(name, i + 1);
			mgr.add(action);
			action.setChecked(current != null && current.getName().equals(name));
		}

		bars.updateActionBars();
	}
	
	protected void refreshAll() {
		//CVSUIPlugin.getPlugin().getRepositoryManager().clearCaches();
		refreshViewer();
	}

	protected void refreshViewer() {
		if (viewer == null) return;
		updateWorkingSetMenu();
		viewer.refresh();
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
