package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.ui.actions.OpenRemoteFileAction;
import org.eclipse.team.internal.ccvs.ui.model.AllRootsElement;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.team.internal.ccvs.ui.model.Tag;
import org.eclipse.team.internal.ccvs.ui.wizards.ConfigurationWizardMainPage;
import org.eclipse.team.internal.ccvs.ui.wizards.LocationWizard;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

/**
 * RepositoriesView is a view on a set of known CVS repositories
 * which allows navigation of the structure of the repository and
 * the performing of CVS-specific operations on the repository contents.
 */
public class RepositoriesView extends ViewPart {
	public static final String VIEW_ID = "org.eclipse.team.ccvs.ui.RepositoriesView";

	// The tree viewer
	private TreeViewer viewer;
	
	// The root
	private AllRootsElement root;
	
	private OpenRemoteFileAction openAction;
	
	// Drill down adapter
	private DrillDownAdapter drillPart;
	
	// Listener
	IRepositoryListener listener = new IRepositoryListener() {
		public void repositoryAdded(ICVSRepositoryLocation root) {
			viewer.refresh();
		}
		public void repositoryRemoved(ICVSRepositoryLocation root) {
			viewer.refresh();
		}
		public void tagAdded(Tag tag, ICVSRepositoryLocation root) {
			viewer.refresh(root);
		}
		public void tagRemoved(Tag tag, ICVSRepositoryLocation root) {
			viewer.refresh(root);
		}
	};

	/**
	 * Add a new repository based on the given properties to the viewer.
	 */
	private void addRepository(Properties properties) {
		ICVSRepositoryLocation root = CVSUIPlugin.getPlugin().getRepositoryManager().getRoot(properties);
		viewer.refresh();
	}
	/**
	 * Contribute actions to the view
	 */
	private void contributeActions() {
		// Create actions
		
		// Refresh (toolbar)
		final Action refreshAction = new Action(Policy.bind("RepositoriesView.refresh"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_REFRESH)) {
			public void run() {
				viewer.refresh();
			}
		};
		refreshAction.setToolTipText(Policy.bind("RepositoriesView.refresh"));

		// New Repository (popup)
		final Action newAction = new Action(Policy.bind("RepositoriesView.new"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_NEWLOCATION)) {
			public void run() {
				LocationWizard wizard = new LocationWizard();
				WizardDialog dialog = new WizardDialog(viewer.getTree().getShell(), wizard);
				int result = dialog.open();
				if (result == WizardDialog.OK) {
					ConfigurationWizardMainPage page = (ConfigurationWizardMainPage)dialog.getCurrentPage();
					Properties properties = page.getProperties();
					addRepository(properties);
				}
			}
		};

		// Create the popup menu
		MenuManager menuMgr = new MenuManager();
		Tree tree = viewer.getTree();
		Menu menu = menuMgr.createContextMenu(tree);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				// File actions go first (view file)
				manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
				
				// New actions go next
				MenuManager sub = new MenuManager(Policy.bind("RepositoriesView.newSubmenu"), IWorkbenchActionConstants.GROUP_ADD);
				sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(sub);
				
				// Misc additions go last
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				
				manager.add(refreshAction);
				sub.add(newAction);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		tree.setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	
		// Create the local tool bar
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
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
	}
	
	/*
	 * @see WorkbenchPart#createPartControl
	 */
	public void createPartControl(Composite parent) {
		initialize();
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new RemoteContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setInput(root);
		viewer.setSorter(new RepositorySorter());
		drillPart = new DrillDownAdapter(viewer);
		contributeActions();
		CVSUIPlugin.getPlugin().getRepositoryManager().addRepositoryListener(listener);
	}
	
	/*
	 * @see WorkbenchPart#dispose
	 */
	public void dispose() {
		CVSUIPlugin.getPlugin().getRepositoryManager().remoteRepositoryListener(listener);
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