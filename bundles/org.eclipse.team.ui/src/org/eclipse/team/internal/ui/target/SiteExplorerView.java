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
package org.eclipse.team.internal.ui.target;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.target.IRemoteTargetResource;
import org.eclipse.team.internal.core.target.ISiteListener;
import org.eclipse.team.internal.core.target.Site;
import org.eclipse.team.internal.core.target.TargetManager;
import org.eclipse.team.internal.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

/**
 * Is a view that allows browsing remote target sites. It is modeled after
 * a file explorer: a tree of folders is show with a table of the folder's
 * contents.
 * <p>
 * Progress is shown in the main workbench window's status line progress 
 * monitor.</p>
 * 
 * @see Site
 * @see IRemoteTargetResource
 */
public class SiteExplorerView extends ViewPart implements ISiteListener {

	public static final String VIEW_ID = "org.eclipse.team.ui.target.SiteExplorerView"; //$NON-NLS-1$

	// The tree viewer showing the folders and sites
	private TreeViewer folderTree;
	
	// The table view that shows the resources in the currently selected folder
	// from the folders tree.
	private TableViewer folderContentsTable;
	
	// The root
	private SiteRootsElement root;
	
	// Embedded progress monitor part used to display progress when contacting the server
	// Note: this feature is not enabled yet and is still under construction
	private IProgressMonitor progressMonitorPart;
	
	// The view's actions
	private Action addSiteAction;
	private Action newFolderAction;
	private Action deleteAction;
	
	/**
	 * Sorter for the folderContents table
	 */
	class FolderListingSorter extends ViewerSorter {
		private boolean reversed = false;
		private int columnNumber;
		
		public static final int NAME = 0;
		public static final int SIZE = 1;
		public static final int MODIFIED = 2;
				
		// column headings:	"Name" "Size" "Modified"
		private int[][] SORT_ORDERS_BY_COLUMN = {
			{NAME},	/* name */ 
			{SIZE, NAME},	/* size */
			{MODIFIED, NAME, SIZE},	/* modified */
		};
		
		public FolderListingSorter(int columnNumber) {
			this.columnNumber = columnNumber;
		}
		
		public int compare(Viewer viewer, Object o1, Object o2) {
			RemoteResourceElement e1 = (RemoteResourceElement)o1;
			RemoteResourceElement e2 = (RemoteResourceElement)o2;
			int[] columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];
			int result = 0;
			for (int i = 0; i < columnSortOrder.length; ++i) {
				result = compareColumnValue(columnSortOrder[i], e1, e2);
				if (result != 0)
					break;
			}
			if (reversed)
				result = -result;
			return result;
		}
		
		int compareColumnValue(int columnNumber, RemoteResourceElement e1, RemoteResourceElement e2) {
			IRemoteTargetResource r1 = e1.getRemoteResource();
			IRemoteTargetResource r2 = e2.getRemoteResource();
			switch (columnNumber) {
				case NAME:
					if (r1.isContainer() && r2.isContainer())
						return compareNames(r1, r2);
					else if (r1.isContainer())
						return -1;
					else if (r2.isContainer())
						return 1;
					return compareNames(r1, r2);
				case SIZE:
					return new Integer(e1.getSize()).compareTo(new Integer(e2.getSize()));
				case MODIFIED:
					return getCollator().compare(e1.getLastModified(), e2.getLastModified());
				default:
					return 0;
			}
		}
		
		protected int compareNames(IRemoteTargetResource resource1, IRemoteTargetResource resource2) {
			return resource1.getName().compareTo(resource2.getName());
		}
		
		/**
		 * Returns the number of the column by which this is sorting.
		 */
		public int getColumnNumber() {
			return columnNumber;
		}
		
		/**
		 * Returns true for descending, or false
		 * for ascending sorting order.
		 */
		public boolean isReversed() {
			return reversed;
		}
		
		/**
		 * Sets the sorting order.
		 */
		public void setReversed(boolean newReversed) {
			reversed = newReversed;
		}
	}
		
	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite top) {
		Composite p = new Composite(top, SWT.NULL);
		GridData data = new GridData (GridData.FILL_BOTH);		
		p.setLayoutData(data);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		p.setLayout (gridLayout);
		
		SashForm sash = new SashForm(p, SWT.HORIZONTAL);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		sash.setLayoutData(data);
		
		folderTree = new TreeViewer(sash, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		folderTree.setContentProvider(new SiteLazyContentProvider());
		folderTree.setLabelProvider(new WorkbenchLabelProvider());

		folderTree.setSorter(new SiteViewSorter());
		folderTree.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.F5) {
					RemoteResourceElement[] selectedFolders = getSelectedRemoteFolder((IStructuredSelection)folderTree.getSelection());
					if(selectedFolders.length == 1) {
						selectedFolders[0].setCachedChildren(null);
						folderTree.refresh(selectedFolders[0]);
						updateFileTable(selectedFolders[0]);
					}
				}
			}
		});
		
		folderTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)folderTree.getSelection();
				final RemoteResourceElement[] remoteFolders = getSelectedRemoteFolder(selection);
				if(remoteFolders.length == 1) {
					updateFileTable(remoteFolders[0]);
				}
			}
		});
		
		folderTree.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				IStructuredSelection selection = (IStructuredSelection)folderTree.getSelection();
				if(selection.size() == 1) {
					expandInTreeCurrentSelection(selection, true /*toggle expanded*/);
				}
			}
		});
		
		folderTree.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				String name1 = ""; //$NON-NLS-1$
				String name2 = ""; //$NON-NLS-1$
				if(e1 instanceof RemoteResourceElement) {
					name1 = ((RemoteResourceElement)e1).getRemoteResource().getName();
				} else if(e1 instanceof SiteElement) {
					name1 = ((SiteElement)e1).getSite().getURL().toExternalForm();
				}
				if(e2 instanceof RemoteResourceElement) {
					name2 = ((RemoteResourceElement)e2).getRemoteResource().getName();
				} else if(e2 instanceof SiteElement) {
					name2 = ((SiteElement)e2).getSite().getURL().toExternalForm();
				}
				
				return getCollator().compare(name1, name2);
			}
		});
		
		// show only folders in tree
		folderTree.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if(element instanceof RemoteResourceElement) {
					return ((RemoteResourceElement)element).getRemoteResource().isContainer();
				}
				return false;
			}
		});
		
		Table table = new Table(sash, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setHeaderVisible(true);
		
		TableColumn tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText(Policy.bind("SiteExplorerView.Name_1")); //$NON-NLS-1$
		tableColumn.addSelectionListener(getColumnListener());
		layout.addColumnData(new ColumnWeightData(30, true));

		tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText(Policy.bind("SiteExplorerView.Size_2")); //$NON-NLS-1$
		tableColumn.setAlignment(SWT.RIGHT);
		tableColumn.addSelectionListener(getColumnListener());
		layout.addColumnData(new ColumnWeightData(10, true));

		tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText(Policy.bind("SiteExplorerView.Modified_3")); //$NON-NLS-1$
		tableColumn.addSelectionListener(getColumnListener());
		layout.addColumnData(new ColumnWeightData(30, true));

		tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText(Policy.bind("SiteExplorerView.URL_4")); //$NON-NLS-1$
		layout.addColumnData(new ColumnWeightData(30, true));

		folderContentsTable = new TableViewer(table);
		folderContentsTable.setContentProvider(new SiteLazyContentProvider());
		folderContentsTable.setLabelProvider(new SiteExplorerViewLabelProvider());
		
		folderContentsTable.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.F5) {
					RemoteResourceElement folder = (RemoteResourceElement)folderContentsTable.getInput();
					if(folder != null) {
						folder.setCachedChildren(null);
						folderContentsTable.refresh();
					}
				}
			}
		});
		
		folderContentsTable.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				IStructuredSelection selection = (IStructuredSelection)folderContentsTable.getSelection();
				if(selection.size() == 1) {
					final RemoteResourceElement[] remoteFolders = getSelectedRemoteFolder(selection);
					if(remoteFolders.length == 1) {
						IStructuredSelection treeSelection = (IStructuredSelection)folderTree.getSelection();
						expandInTreeCurrentSelection(treeSelection, false /*don't toggle*/);
						folderTree.setSelection(new StructuredSelection(remoteFolders[0]));
					}
				}
			}
		});
		FolderListingSorter sorter = new FolderListingSorter(FolderListingSorter.NAME);
		sorter.setReversed(false);
		folderContentsTable.setSorter(sorter);

		sash.setWeights(new int[] {33, 67});
		
		TargetManager.addSiteListener(this);
		
		root = new SiteRootsElement(getViewSite().getWorkbenchWindow());
		initalizeActions();
		folderTree.setInput(root);
		
		// F1 Help
		WorkbenchHelp.setHelp(folderTree.getControl(), IHelpContextIds.SITE_EXPLORER_VIEW);
	}

	private Shell getShell() {
		return folderTree.getTree().getShell();
	}

	private RemoteResourceElement[] getSelectedRemoteFolder(IStructuredSelection selection) {		
		if (!selection.isEmpty()) {
			final List folders = new ArrayList();
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof RemoteResourceElement) {
					folders.add(o);
				}
			}
			return (RemoteResourceElement[]) folders.toArray(new RemoteResourceElement[folders.size()]);
		}
		return new RemoteResourceElement[0];
	}
	
	private void expandInTreeCurrentSelection(IStructuredSelection selection, boolean toggle) {
		if (!selection.isEmpty()) {
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object element = it.next();
				if(toggle) {
					folderTree.setExpandedState(element, !folderTree.getExpandedState(element));
				} else {
					folderTree.setExpandedState(element, true);
				}
			}
		}
	}

	/**
	 * Method updateFileTable.
	 */
	private void updateFileTable(RemoteResourceElement remoteFolder) {
		if(remoteFolder != null && !remoteFolder.equals(folderContentsTable.getInput())) {
			folderContentsTable.setInput(remoteFolder);
		}
	}

	private void initalizeActions() {
		final Shell shell = folderContentsTable.getTable().getShell();
		// Create actions
		
		// Refresh (toolbar)
		addSiteAction = new Action(Policy.bind("SiteExplorerView.addSiteAction"), TeamImages.getImageDescriptor(ISharedImages.IMG_SITE_ELEMENT)) { //$NON-NLS-1$
			public void run() {
				ConfigureTargetWizard wizard = new ConfigureTargetWizard();
				wizard.init(null, null);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
			}
		};
		addSiteAction.setToolTipText(Policy.bind("SiteExplorerView.addSiteActionTooltip")); //$NON-NLS-1$
		
		newFolderAction = new Action(Policy.bind("SiteExplorerView.newFolderAction"), WorkbenchImages.getImageDescriptor(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER)) { //$NON-NLS-1$
			public void run() {
				final Shell shell = folderTree.getTree().getShell();
				try {
					// assume that only one folder is selected in the folder tree, this
					// is enforced by isEnable() method for this action
					IStructuredSelection selection = (IStructuredSelection)folderTree.getSelection();
					Object currentSelection = selection.getFirstElement();
					
					RemoteResourceElement selectedFolder;
					if(!selection.isEmpty()) {
						selectedFolder = getSelectedRemoteFolder(selection)[0];
					} else {
						selectedFolder = (RemoteResourceElement)folderContentsTable.getInput();
					}
										
					IRemoteTargetResource newFolder = CreateNewFolderAction.createDir(shell, selectedFolder.getRemoteResource(), Policy.bind("CreateNewFolderAction.newFolderName")); //$NON-NLS-1$
					if (newFolder == null)
						return;

					// force a refresh
					selectedFolder.setCachedChildren(null);
					
					// select the newly added folder
					RemoteResourceElement newFolderUIElement = new RemoteResourceElement(newFolder);
					folderTree.refresh(currentSelection);
					expandInTreeCurrentSelection(new StructuredSelection(currentSelection), false);
					folderTree.setSelection(new StructuredSelection(newFolderUIElement));
				} catch (TeamException e) {
					TeamUIPlugin.handle(e);
					return;
				}
			}
			public boolean isEnabled() {
				return folderContentsTable.getInput() != null ||
		 				getSelectedRemoteFolder((IStructuredSelection)folderTree.getSelection()).length == 1;
			}				
		};

		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager tbm = bars.getToolBarManager();
		tbm.add(addSiteAction);
		tbm.update(false);
		
		
		MenuManager treeMgr = new MenuManager();
		MenuManager tableMgr = new MenuManager();
		Tree tree = folderTree.getTree();
		Table table = folderContentsTable.getTable();
		Menu treeMenu = treeMgr.createContextMenu(tree);
		Menu tableMenu = tableMgr.createContextMenu(table);
		IMenuListener menuListener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				// Misc additions
				MenuManager sub = new MenuManager(Policy.bind("SiteExplorerView.newMenu"), IWorkbenchActionConstants.GROUP_ADD); //$NON-NLS-1$
				sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(sub);
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				sub.add(addSiteAction);
				sub.add(newFolderAction);
			}
		};
		treeMgr.addMenuListener(menuListener);
		treeMgr.setRemoveAllWhenShown(true);
		tableMgr.addMenuListener(menuListener);
		tableMgr.setRemoveAllWhenShown(true);
		tree.setMenu(treeMenu);
		table.setMenu(tableMenu);
		getSite().registerContextMenu(tableMgr, folderContentsTable);
		getSite().registerContextMenu(treeMgr, folderTree);
	}
	
	/**
	 * Add the new site to the viewer and make it the current selection.
	 * 
	 * @see ISiteListener#siteAdded(Site)
	 */
	public void siteAdded(Site site) {
		SiteElement element = new SiteElement(site, getViewSite().getWorkbenchWindow());
		folderTree.add(root, element);
		folderTree.setSelection(new StructuredSelection(element));
	}

	/**
	 * Remote the site from the viewer and select the next site in the
	 * tree.
	 * 
	 * @see ISiteListener#siteRemoved(Site)
	 */
	public void siteRemoved(Site site) {
		folderTree.remove(new SiteElement(site));	
		selectNextObjectInTreeViewer();	
	}
	
	private void selectNextObjectInTreeViewer() {
		Object[] items = folderTree.getVisibleExpandedElements();
		if(items.length > 0) {
			folderTree.setSelection(new StructuredSelection(items[0]));
		} else {
			folderContentsTable.setInput(null);
		}
	}

	/**
	 * Adds the listener that sets the sorter.
	 */
	private SelectionListener getColumnListener() {
		/**
	 	 * This class handles selections of the column headers.
		 * Selection of the column header will cause resorting
		 * of the shown tasks using that column's sorter.
		 * Repeated selection of the header will toggle
		 * sorting order (ascending versus descending).
		 */
		return new SelectionAdapter() {
			/**
			 * Handles the case of user selecting the
			 * header area.
			 * <p>If the column has not been selected previously,
			 * it will set the sorter of that column to be
			 * the current tasklist sorter. Repeated
			 * presses on the same column header will
			 * toggle sorting order (ascending/descending).
			 */
			public void widgetSelected(SelectionEvent e) {
				// column selected - need to sort
				// only allow sorting on name for now
				int column = folderContentsTable.getTable().indexOf((TableColumn) e.widget);
				if(column == FolderListingSorter.NAME) {
					FolderListingSorter oldSorter = (FolderListingSorter)folderContentsTable.getSorter();
					if (oldSorter != null && column == oldSorter.getColumnNumber()) {
						oldSorter.setReversed(!oldSorter.isReversed());
						folderContentsTable.refresh();
					} else {
						folderContentsTable.setSorter(new FolderListingSorter(column));
					}
				}
			}
		};
	}

	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
}