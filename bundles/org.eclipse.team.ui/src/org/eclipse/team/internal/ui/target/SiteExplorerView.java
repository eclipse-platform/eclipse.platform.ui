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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.ISiteListener;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

/**
 * Is a view that allows browsing remote target sites. It is modeled after
 * a file explorer: a tree of folders is show with a table of the folder's
 * contents.
 * 
 * @see Site
 * @see IRemoteTargetResource
 */
public class SiteExplorerView extends ViewPart implements ISiteListener {

	public static final String VIEW_ID = "org.eclipse.team.ui.target.SiteExplorerView"; //$NON-NLS-1$

	// The tree viewer
	private TableViewer tableViewer;
	private TreeViewer treeViewer;
	
	// The root
	private SiteRootsElement root;
	
	// The view's actions
	private Action addSiteAction;
	private Action newFolderAction;
	private Action deleteAction;

	
	
	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite p) {
		SashForm sash = new SashForm(p, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		root = new SiteRootsElement(TargetManager.getSites(), RemoteResourceElement.SHOW_FOLDERS);
		
		treeViewer = new TreeViewer(sash, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setContentProvider(new SiteLazyContentProvider());
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());

		treeViewer.setSorter(new SiteViewSorter());
		treeViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.F5) {
					refresh();
				}
			}
		});
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
				final IRemoteTargetResource[] remoteFolders = getSelectedRemoteFolder(selection);
				if(remoteFolders.length == 1) {
					updateFileTable(remoteFolders[0]);
				}
			}
		});
		
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
				if(selection.size() == 1) {
					expandInTreeCurrentSelection(selection, true /*toggle expanded*/);
				}
			}
		});
		
		treeViewer.setInput(root);

		Table table = new Table(sash, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		TableLayout layout = new TableLayout();
		
		TableColumn tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText(Policy.bind("SiteExplorerView.Name_1")); //$NON-NLS-1$
		layout.addColumnData(new ColumnWeightData(30, true));
		tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText(Policy.bind("SiteExplorerView.Size_2")); //$NON-NLS-1$
		tableColumn.setAlignment(SWT.RIGHT);
		layout.addColumnData(new ColumnWeightData(20, true));
		tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText(Policy.bind("SiteExplorerView.Modified_3")); //$NON-NLS-1$
		tableColumn = new TableColumn(table, SWT.NULL);
		layout.addColumnData(new ColumnWeightData(20, true));
		tableColumn.setText(Policy.bind("SiteExplorerView.URL_4")); //$NON-NLS-1$
		ColumnLayoutData cLayout = new ColumnPixelData(21);
		table.setLayout(layout);
		table.setHeaderVisible(true);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new SiteLazyContentProvider());
		tableViewer.setLabelProvider(new SiteExplorerViewLabelProvider());
		
		tableViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.F5) {
					refresh();
				}
			}
		});
		
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if(selection.size() == 1) {
					final IRemoteTargetResource[] remoteFolders = getSelectedRemoteFolder(selection);
					if(remoteFolders.length == 1) {
						IStructuredSelection treeSelection = (IStructuredSelection)treeViewer.getSelection();
						expandInTreeCurrentSelection(treeSelection, false /*don't toggle*/);
						treeViewer.setSelection(new StructuredSelection(new RemoteResourceElement(remoteFolders[0])));
					}
				}
			}
		});
		
		TargetManager.addSiteListener(this);
		initalizeActions();
	}

	private Shell getShell() {
		return treeViewer.getTree().getShell();
	}

	private IRemoteTargetResource[] getSelectedRemoteFolder(IStructuredSelection selection) {		
		if (!selection.isEmpty()) {
			final List folders = new ArrayList();
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof RemoteResourceElement) {
					folders.add(((RemoteResourceElement)o).getRemoteResource());
				} else if(o instanceof SiteElement) {
					try {
						folders.add(((SiteElement)o).getSite().getRemoteResource());
					} catch (TeamException e) {
						return new IRemoteTargetResource[0];
					}
				}
			}
			return (IRemoteTargetResource[]) folders.toArray(new IRemoteTargetResource[folders.size()]);
		}
		return new IRemoteTargetResource[0];
	}
	
	private void expandInTreeCurrentSelection(IStructuredSelection selection, boolean toggle) {
		if (!selection.isEmpty()) {
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object element = it.next();
				if(toggle) {
					treeViewer.setExpandedState(element, !treeViewer.getExpandedState(element));
				} else {
					treeViewer.setExpandedState(element, true);
				}
			}
		}
	}

	/**
	 * Method updateFileTable.
	 */
	private void updateFileTable(IRemoteTargetResource remoteFolder) {
		final Set tags = new HashSet();
		if(remoteFolder != null) {
			RemoteResourceElement folderElement = new RemoteResourceElement(remoteFolder);
			tableViewer.setInput(folderElement);
		}
	}

	private void initalizeActions() {
		final Shell shell = tableViewer.getTable().getShell();
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
				final Shell shell = treeViewer.getTree().getShell();
				try {
					// assume that only one folder is selected in the folder tree, this
					// is enforced by isEnable() method for this action
					IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
					Object currentSelection = selection.getFirstElement();
					
					final IRemoteTargetResource selectedFolder = getSelectedRemoteFolder(selection)[0];
					
					IRemoteTargetResource newFolder = CreateNewFolderAction.createDir(shell, selectedFolder);
					treeViewer.refresh(currentSelection);
					expandInTreeCurrentSelection(new StructuredSelection(currentSelection), false);
					treeViewer.setSelection(new StructuredSelection(currentSelection));
				} catch (TeamException e) {
					TeamUIPlugin.handle(e);
					return;
				}
			}
			public boolean isEnabled() {
				return getSelectedRemoteFolder((IStructuredSelection)treeViewer.getSelection()).length == 1 ||
		 				  getSelectedRemoteFolder((IStructuredSelection)tableViewer.getSelection()).length == 1;
			}				
		};

		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager tbm = bars.getToolBarManager();
		tbm.add(addSiteAction);
		tbm.update(false);
		
		
		MenuManager treeMgr = new MenuManager();
		MenuManager tableMgr = new MenuManager();
		Tree tree = treeViewer.getTree();
		Table table = tableViewer.getTable();
		Menu treeMenu = treeMgr.createContextMenu(tree);
		Menu tableMenu = tableMgr.createContextMenu(table);
		IMenuListener menuListener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				// Misc additions
				MenuManager sub = new MenuManager(Policy.bind("SiteExplorerView.newMenu"), IWorkbenchActionConstants.GROUP_ADD); //$NON-NLS-1$
				sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(sub);
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				sub.add(newFolderAction);
			}
		};
		treeMgr.addMenuListener(menuListener);
		treeMgr.setRemoveAllWhenShown(true);
		tableMgr.addMenuListener(menuListener);
		tableMgr.setRemoveAllWhenShown(true);
		tree.setMenu(treeMenu);
		table.setMenu(tableMenu);
		getSite().registerContextMenu(tableMgr, tableViewer);
		getSite().registerContextMenu(treeMgr, treeViewer);
	}
	
	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
	
	/**
	 * @see ISiteListener#siteAdded(Site)
	 */
	public void siteAdded(Site site) {
		refresh();
	}

	/**
	 * @see ISiteListener#siteRemoved(Site)
	 */
	public void siteRemoved(Site site) {
		treeViewer.remove(new SiteElement(site));	
		selectNextObjectInTreeViewer();	
	}
	
	private void selectNextObjectInTreeViewer() {
		Object[] items = treeViewer.getVisibleExpandedElements();
		if(items.length > 0) {
			treeViewer.setSelection(new StructuredSelection(items[0]));
		} else {
			tableViewer.setInput(null);
		}
	}
	
	protected void refresh() {
		root = new SiteRootsElement(TargetManager.getSites(), RemoteResourceElement.SHOW_FOLDERS);
		treeViewer.setInput(root);
		treeViewer.refresh();
	}
}