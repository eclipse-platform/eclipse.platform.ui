package org.eclipse.team.internal.ccvs.ui.repo;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Properties;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.IRepositoryListener;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.WorkbenchUserAuthenticator;
import org.eclipse.team.internal.ccvs.ui.actions.OpenRemoteFileAction;
import org.eclipse.team.internal.ccvs.ui.model.AllRootsElement;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.team.internal.ccvs.ui.wizards.NewLocationWizard;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

/**
 * RepositoriesView is a view on a set of known CVS repositories
 * which allows navigation of the structure of the repository and
 * the performing of CVS-specific operations on the repository contents.
 */
public class RepositoriesView extends ViewPart {
	public static final String VIEW_ID = "org.eclipse.team.ccvs.ui.RepositoriesView"; //$NON-NLS-1$

	// The tree viewer
	private TreeViewer viewer;
	
	// The root
	private AllRootsElement root;
	
	// Drill down adapter
	private DrillDownAdapter drillPart;
	
	// Actions
	private Action showFoldersAction;
	private Action showModulesAction;
	private OpenRemoteFileAction openAction;	
	private Action refreshAction;
	private PropertyDialogAction propertiesAction;
	
	IRepositoryListener listener = new IRepositoryListener() {
		public void repositoryAdded(final ICVSRepositoryLocation root) {
			viewer.getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					refreshViewer();
					viewer.setSelection(new StructuredSelection(root));
				}
			});
		}
		public void repositoryRemoved(ICVSRepositoryLocation root) {
			refresh();
		}
		public void repositoriesChanged(ICVSRepositoryLocation[] roots) {
			refresh();
		}
		public void workingSetChanged(CVSWorkingSet set) {
			refresh();
		}
		private void refresh() {
			Display display = viewer.getControl().getDisplay();
			display.syncExec(new Runnable() {
				public void run() {
					RepositoriesView.this.refreshViewer();
				}
			});
		}
	};
	private Action newWorkingSetAction;
	private Action deselectWorkingSetAction;
	private Action editWorkingSetAction;
	
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
	
	private void refreshAll() {
		CVSUIPlugin.getPlugin().getRepositoryManager().clearCaches();
		refreshViewer();
	}
	
	private void refreshViewer() {
		updateWorkingSetMenu();
		viewer.refresh();
	}
	
	/**
	 * Contribute actions to the view
	 */
	private void contributeActions() {
		final Shell shell = viewer.getTree().getShell();
		// Create actions
		
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

		// New Repository (popup)
		final Action newAction = new Action(Policy.bind("RepositoriesView.new"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_NEWLOCATION)) { //$NON-NLS-1$
			public void run() {
				NewLocationWizard wizard = new NewLocationWizard();
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
			}
		};
		WorkbenchHelp.setHelp(newAction, IHelpContextIds.NEW_REPOSITORY_LOCATION_ACTION);
		
		final Action newAnonAction = new Action(Policy.bind("RepositoriesView.newAnonCVS"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_NEWLOCATION)) { //$NON-NLS-1$
			public void run() {
				Properties p = new Properties();
				p.setProperty("connection", "pserver"); //$NON-NLS-1$ //$NON-NLS-2$
				p.setProperty("user", "anonymous"); //$NON-NLS-1$ //$NON-NLS-2$
				p.setProperty("host", "dev.eclipse.org"); //$NON-NLS-1$ //$NON-NLS-2$
				p.setProperty("root", "/home/eclipse"); //$NON-NLS-1$ //$NON-NLS-2$
				NewLocationWizard wizard = new NewLocationWizard(p);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
			}
		};
		WorkbenchHelp.setHelp(newAnonAction, IHelpContextIds.NEW_DEV_ECLIPSE_REPOSITORY_LOCATION_ACTION);

		// New Working Set (popup)
		newWorkingSetAction = new Action(Policy.bind("RepositoriesView.newWorkingSet")) { //$NON-NLS-1$
			public void run() {
				WorkingSetSelectionDialog dialog = new WorkingSetSelectionDialog(shell, false);
				dialog.open();
				CVSWorkingSet[] sets = dialog.getSelection();
				if (sets != null && sets.length > 0) {
					CVSUIPlugin.getPlugin().getRepositoryManager().setCurrentWorkingSet(sets[0]);
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
				}
			}
		};
		//WorkbenchHelp.setHelp(newAction, IHelpContextIds.NEW_CVS_WORKING_SET_ACTION);
		
		// Properties
		propertiesAction = new PropertyDialogAction(shell, viewer);
		getViewSite().getActionBars().setGlobalActionHandler(IWorkbenchActionConstants.PROPERTIES, propertiesAction);		
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if (selection.size() == 1 && selection.getFirstElement() instanceof RepositoryRoot) {
			propertiesAction.setEnabled(true);
		} else {
			propertiesAction.setEnabled(false);
		}
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ss = (IStructuredSelection)event.getSelection();
				boolean enabled = ss.size() == 1 && ss.getFirstElement() instanceof RepositoryRoot;
				propertiesAction.setEnabled(enabled);
			}
		});

		// Create the popup menu
		MenuManager menuMgr = new MenuManager();
		Tree tree = viewer.getTree();
		Menu menu = menuMgr.createContextMenu(tree);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				// File actions go first (view file)
				manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
				
				// New actions go next
				MenuManager sub = new MenuManager(Policy.bind("RepositoriesView.newSubmenu"), IWorkbenchActionConstants.GROUP_ADD); //$NON-NLS-1$
				sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(sub);
				
				// Misc additions
				manager.add(new Separator("checkoutGroup")); //$NON-NLS-1$
				manager.add(new Separator("tagGroup")); //$NON-NLS-1$
				manager.add(new Separator("miscGroup")); //$NON-NLS-1$
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				
				manager.add(refreshAction);
				
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				if (selection.size() == 1 && selection.getFirstElement() instanceof RepositoryRoot) {
					manager.add(propertiesAction);
				}
				sub.add(newAction);
				sub.add(newAnonAction);
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
	
	/*
	 * @see WorkbenchPart#createPartControl
	 */
	public void createPartControl(Composite parent) {
		initialize();
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new RemoteContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		getSite().setSelectionProvider(viewer);
		viewer.setInput(root);
		viewer.setSorter(new RepositorySorter());
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.F5) {
					if (WorkbenchUserAuthenticator.USE_ALTERNATE_PROMPTER) {
						try {
							ICVSRepositoryLocation[] locations = CVSProviderPlugin.getPlugin().getKnownRepositories();
							for (int i = 0; i < locations.length; i++) {
								locations[i].flushUserInfo();
							}
						} catch (CVSException e) {
							// Do nothing
						}
					} else {
						refreshAction.run();
					}
				} else if (event.keyCode == SWT.F9 && WorkbenchUserAuthenticator.USE_ALTERNATE_PROMPTER) {
					refreshAction.run();
				}
			}
		});
		drillPart = new DrillDownAdapter(viewer);
		contributeActions();
		CVSUIPlugin.getPlugin().getRepositoryManager().addRepositoryListener(listener);
		
		// F1 Help
		WorkbenchHelp.setHelp(viewer.getControl(), IHelpContextIds.REPOSITORIES_VIEW);
	}
	
	/*
	 * @see WorkbenchPart#dispose
	 */
	public void dispose() {
		CVSUIPlugin.getPlugin().getRepositoryManager().removeRepositoryListener(listener);
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
	 * Initialize the repositories and actions
	 */
	private void initialize() {
		root = new AllRootsElement();
	}
	/*
	 * @see WorkbenchPart#setFocus
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}