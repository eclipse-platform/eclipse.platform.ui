package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.actions.OpenRemoteFileAction;
import org.eclipse.team.internal.ccvs.ui.model.AllRootsElement;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.team.internal.ccvs.ui.wizards.NewLocationWizard;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.dialogs.PropertyDialogAction;
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
					viewer.refresh();
					viewer.setSelection(new StructuredSelection(root));
				}
			});
		}
		public void repositoryRemoved(ICVSRepositoryLocation root) {
			refresh();
		}
		public void branchTagsAdded(CVSTag[] tags, final ICVSRepositoryLocation root) {
			refresh();
		}
		public void branchTagsRemoved(CVSTag[] tags, final ICVSRepositoryLocation root) {
			refresh();
		}
		public void versionTagsAdded(CVSTag[] tags, final ICVSRepositoryLocation root) {
			refresh();
		}
		public void versionTagsRemoved(CVSTag[] tags, final ICVSRepositoryLocation root) {
			refresh();
		}
		private void refresh() {
			Display display = viewer.getControl().getDisplay();
			display.syncExec(new Runnable() {
				public void run() {
					viewer.refresh();
				}
			});
		}
	};

	/**
	 * Contribute actions to the view
	 */
	private void contributeActions() {
		final Shell shell = viewer.getTree().getShell();
		// Create actions
		
		// Refresh (toolbar)
		refreshAction = new Action(Policy.bind("RepositoriesView.refresh"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_REFRESH)) { //$NON-NLS-1$
			public void run() {
				viewer.refresh();
			}
		};
		refreshAction.setToolTipText(Policy.bind("RepositoriesView.refresh")); //$NON-NLS-1$

		// New Repository (popup)
		final Action newAction = new Action(Policy.bind("RepositoriesView.new"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_NEWLOCATION)) { //$NON-NLS-1$
			public void run() {
				NewLocationWizard wizard = new NewLocationWizard();
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
			}
		};

		// Properties
		propertiesAction = new PropertyDialogAction(shell, viewer);
		getViewSite().getActionBars().setGlobalActionHandler(IWorkbenchActionConstants.PROPERTIES, propertiesAction);		
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if (selection.size() == 1 && selection.getFirstElement() instanceof ICVSRepositoryLocation) {
			propertiesAction.setEnabled(true);
		} else {
			propertiesAction.setEnabled(false);
		}
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ss = (IStructuredSelection)event.getSelection();
				boolean enabled = ss.size() == 1 && ss.getFirstElement() instanceof ICVSRepositoryLocation;
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
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				
				manager.add(refreshAction);
				
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				if (selection.size() == 1 && selection.getFirstElement() instanceof ICVSRepositoryLocation) {
					manager.add(propertiesAction);
				}
				sub.add(newAction);
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
		
		// Add module toggling to the local pull-down menu
		IMenuManager mgr = bars.getMenuManager();
		showFoldersAction = new Action(Policy.bind("RepositoriesView.Show_Folders_6")) { //$NON-NLS-1$
			public void run() {
				CVSUIPlugin.getPlugin().getPreferenceStore().setValue(ICVSUIConstants.PREF_SHOW_MODULES, false);
				showModulesAction.setChecked(false);
				viewer.refresh();
			}
		};
		showModulesAction = new Action(Policy.bind("RepositoriesView.Show_Modules_7")) { //$NON-NLS-1$
			public void run() {
				CVSUIPlugin.getPlugin().getPreferenceStore().setValue(ICVSUIConstants.PREF_SHOW_MODULES, true);
				showFoldersAction.setChecked(false);
				viewer.refresh();
			}
		};
		boolean showModules = CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_SHOW_MODULES);
		showFoldersAction.setChecked(!showModules);
		showModulesAction.setChecked(showModules);
		mgr.add(showFoldersAction);
		mgr.add(showModulesAction);
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
					refreshAction.run();
				}
			}
		});
		drillPart = new DrillDownAdapter(viewer);
		contributeActions();
		CVSUIPlugin.getPlugin().getRepositoryManager().addRepositoryListener(listener);
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