/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.compare.internal.INavigatable;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.actions.ExpandAllAction;
import org.eclipse.team.internal.ui.synchronize.actions.NavigateAction;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.internal.dialogs.ContainerCheckedTreeViewer;

/**
 * A <code>TreeViewerAdvisor</code> that works with TreeViewers. Two default
 * tree viewers are provided that support navigation: <code>NavigableTreeViewer</code>
 * and <code>NavigableCheckboxTreeViewer</code>. 
 * <p>
 * Note that this advisor can be used with any tree viewer. By default it provides an
 * expand all action, double click behavior on containers, and navigation support for
 * tree viewers.
 * </p><p>
 * By default this advisor supports hierarchical models and honour the compressed
 * folder Team preference for showing the sync set as compressed folders. Subclasses
 * can provide their own presentation models.
 * <p>
 * @since 3.0
 */
public class TreeViewerAdvisor extends StructuredViewerAdvisor {
	
	/**
	 * Style bit that indicates that a checkbox viewer is desired.
	 */
	public static final int CHECKBOX = 1;
	
	private ExpandAllAction expandAllAction;
	private Action collapseAll;
	private NavigateAction gotoNext;
	private NavigateAction gotoPrevious;	
	
	class NavigationActionGroup extends SynchronizePageActionGroup {
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			final StructuredViewer viewer = getViewer();
			if (viewer instanceof AbstractTreeViewer) {
				
				expandAllAction = new ExpandAllAction((AbstractTreeViewer) viewer);
				Utils.initAction(expandAllAction, "action.expandAll."); //$NON-NLS-1$
				
				collapseAll = new Action() {
					public void run() {
						if (viewer == null || viewer.getControl().isDisposed() || !(viewer instanceof AbstractTreeViewer)) return;
						viewer.getControl().setRedraw(false);		
						((AbstractTreeViewer)viewer).collapseToLevel(viewer.getInput(), TreeViewer.ALL_LEVELS);
						viewer.getControl().setRedraw(true);
					}
				};
				Utils.initAction(collapseAll, "action.collapseAll."); //$NON-NLS-1$
				
				ISynchronizeParticipant participant = configuration.getParticipant();
				ISynchronizePageSite site = configuration.getSite();

				gotoNext = new NavigateAction(site, participant, configuration, true /*next*/);		
				gotoPrevious = new NavigateAction(site, participant, configuration, false /*previous*/);
			}
		}
		public void fillContextMenu(IMenuManager manager) {
			appendToGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP, expandAllAction);
		}
		public void fillActionBars(IActionBars actionBars) {
			IToolBarManager manager = actionBars.getToolBarManager();
			appendToGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP, gotoNext);
			appendToGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP, gotoPrevious);
			appendToGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP, collapseAll);
		}
	}
	
 	/**
	 * Interface used to implement navigation for tree viewers. This interface is used by
	 * {@link TreeViewerAdvisor#navigate(TreeViewer, boolean, boolean, boolean) to open 
	 * selections and navigate.
	 */
	public interface ITreeViewerAccessor {
		public void createChildren(TreeItem item);
		public void openSelection();
	}
	
	/**
	 * A navigable checkboxec tree viewer that will work with the <code>navigate</code> method of
	 * this advisor.
	 */
	public static class NavigableCheckboxTreeViewer extends ContainerCheckedTreeViewer implements ITreeViewerAccessor {
		public NavigableCheckboxTreeViewer(Composite parent, int style) {
			super(parent, style);
			setUseHashlookup(true);
		}

		public void createChildren(TreeItem item) {	
			super.createChildren(item);
		}

		public void openSelection() {
			fireOpen(new OpenEvent(this, getSelection()));
		}
	}
	
	/**
	 * A navigable tree viewer that will work with the <code>navigate</code> method of
	 * this advisor.
	 */
	public static class NavigableTreeViewer extends TreeViewer implements ITreeViewerAccessor {
		public NavigableTreeViewer(Composite parent, int style) {
			super(parent, style);
			setUseHashlookup(true);
		}

		public void createChildren(TreeItem item) {	
			super.createChildren(item);
		}

		public void openSelection() {
			fireOpen(new OpenEvent(this, getSelection()));
		}
	}
	
	public static StructuredViewer createViewer(Composite parent, ISynchronizePageConfiguration configuration) {
		int style = ((SynchronizePageConfiguration)configuration).getViewerStyle();
		if ((style & CHECKBOX) > 0) {
			NavigableCheckboxTreeViewer v = new TreeViewerAdvisor.NavigableCheckboxTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			configuration.getSite().setSelectionProvider(v);
			return v;
		} else {
			NavigableTreeViewer v = new TreeViewerAdvisor.NavigableTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			configuration.getSite().setSelectionProvider(v);
			return v;
		}
	}

	/**
	 * Create an advisor that will allow viewer contributions with the given <code>targetID</code>. This
	 * advisor will provide a presentation model based on the given sync info set. Note that it's important
	 * to call {@link #dispose()} when finished with an advisor.
	 * 
	 * @param targetID the targetID defined in the viewer contributions in a plugin.xml file.
	 * @param site the workbench site with which to register the menuId. Can be <code>null</code> in which
	 * case a site will be found using the default workbench page.
	 * @param set the set of <code>SyncInfo</code> objects that are to be shown to the user.
	 */
	public TreeViewerAdvisor(Composite parent, ISynchronizePageConfiguration configuration) {
		super(configuration);	
		INavigatable nav = (INavigatable)configuration.getProperty(SynchronizePageConfiguration.P_NAVIGATOR);
		if (nav == null) {
			configuration.setProperty(SynchronizePageConfiguration.P_NAVIGATOR, getAdapter(INavigatable.class));
		}
		configuration.addActionContribution(new NavigationActionGroup());
		StructuredViewer viewer = TreeViewerAdvisor.createViewer(parent, configuration);
		GridData data = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(data);
		initializeViewer(viewer);		
	}

	/**
	 * Create the model manager to be used by this advisor
	 * @param configuration
	 */
	protected SynchronizeModelManager createModelManager(ISynchronizePageConfiguration configuration) {
	    ChangeSetCapability changeSetCapability = configuration.getParticipant().getChangeSetCapability();
        if (changeSetCapability != null) {
	        if (changeSetCapability.supportsActiveChangeSets() || changeSetCapability.supportsCheckedInChangeSets()) {
	            return new ChangeSetModelManager(configuration);
	        }
	    }
		return new HierarchicalModelManager(configuration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#navigate(boolean)
	 */
	public boolean navigate(boolean next) {
		return TreeViewerAdvisor.navigate((TreeViewer)getViewer(), next, false, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#initializeViewer(org.eclipse.jface.viewers.StructuredViewer)
	 */
	public boolean validateViewer(StructuredViewer viewer) {
		return viewer instanceof AbstractTreeViewer;
	}
	
	/**
	 * Handles a double-click event from the viewer. Expands or collapses a folder when double-clicked.
	 * 
	 * @param viewer the viewer
	 * @param event the double-click event
	 */
	protected boolean handleDoubleClick(StructuredViewer viewer, DoubleClickEvent event) {
		if (super.handleDoubleClick(viewer, event)) return true;
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		AbstractTreeViewer treeViewer = (AbstractTreeViewer) getViewer();
		if(element != null) {
			if (treeViewer.getExpandedState(element)) {
				treeViewer.collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
			} else {
				TreeViewerAdvisor.navigate((TreeViewer)getViewer(), true /* next */, false /* no-open */, true /* only-expand */);
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#initializeListeners(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void initializeListeners(final StructuredViewer viewer) {
		super.initializeListeners(viewer);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateStatusLine((IStructuredSelection) event.getSelection());
			}
		});
	}
	
	/* private */ void updateStatusLine(IStructuredSelection selection) {
		IWorkbenchSite ws = getConfiguration().getSite().getWorkbenchSite();
		if (ws != null && ws instanceof IViewSite) {
			String msg = getStatusLineMessage(selection);
			((IViewSite)ws).getActionBars().getStatusLineManager().setMessage(msg);
		}
	}
	
	private String getStatusLineMessage(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object first = selection.getFirstElement();
			if (first instanceof SyncInfoModelElement) {
				SyncInfoModelElement node = (SyncInfoModelElement) first;
				IResource resource = node.getResource();
				if (resource == null) {
					return node.getName();
				} else {
					return resource.getFullPath().makeRelative().toString();
				}
			}
		}
		if (selection.size() > 1) {
			return selection.size() + Policy.bind("SynchronizeView.13"); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}
	
	private static TreeItem findNextPrev(TreeViewer viewer, TreeItem item, boolean next) {
		if (item == null || !(viewer instanceof ITreeViewerAccessor))
			return null;
		TreeItem children[] = null;
		ITreeViewerAccessor treeAccessor = (ITreeViewerAccessor) viewer;
		if (!next) {
			TreeItem parent = item.getParentItem();
			if (parent != null)
				children = parent.getItems();
			else
				children = item.getParent().getItems();
			if (children != null && children.length > 0) {
				// goto previous child
				int index = 0;
				for (; index < children.length; index++)
					if (children[index] == item)
						break;
				if (index > 0) {
					item = children[index - 1];
					while (true) {
						treeAccessor.createChildren(item);
						int n = item.getItemCount();
						if (n <= 0)
							break;
						item.setExpanded(true);
						item = item.getItems()[n - 1];
					}
					// previous
					return item;
				}
			}
			// go up
			return parent;
		} else {
			item.setExpanded(true);
			treeAccessor.createChildren(item);
			if (item.getItemCount() > 0) {
				// has children: go down
				children = item.getItems();
				return children[0];
			}
			while (item != null) {
				children = null;
				TreeItem parent = item.getParentItem();
				if (parent != null)
					children = parent.getItems();
				else
					children = item.getParent().getItems();
				if (children != null && children.length > 0) {
					// goto next child
					int index = 0;
					for (; index < children.length; index++)
						if (children[index] == item)
							break;
					if (index < children.length - 1) {
						// next
						return children[index + 1];
					}
				}
				// go up
				item = parent;
			}
		}
		return item;
	}

	private static void setSelection(TreeViewer viewer, TreeItem ti, boolean fireOpen, boolean expandOnly) {
		if (ti != null) {
			Object data= ti.getData();
			if (data != null) {
				// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
				ISelection selection = new StructuredSelection(data);
				if (expandOnly) {
					viewer.expandToLevel(data, 0);
				} else {
					viewer.setSelection(selection, true);
					ISelection currentSelection = viewer.getSelection();
					if (fireOpen && currentSelection != null && selection.equals(currentSelection)) {
						if (viewer instanceof ITreeViewerAccessor) {
							((ITreeViewerAccessor) viewer).openSelection();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Selects the next (or previous) node of the current selection.
	 * If there is no current selection the first (last) node in the tree is selected.
	 * Wraps around at end or beginning.
	 * Clients may not override. 
	 *
	 * @param next if <code>true</code> the next node is selected, otherwise the previous node
	 * @return <code>true</code> if at end (or beginning)
	 */
	public static boolean navigate(TreeViewer viewer, boolean next, boolean fireOpen, boolean expandOnly) {
		Tree tree = viewer.getTree();
		if (tree == null)
			return false;
		TreeItem item = null;
		TreeItem children[] = tree.getSelection();
		if (children != null && children.length > 0)
			item = children[0];
		if (item == null) {
			children = tree.getItems();
			if (children != null && children.length > 0) {
				item = children[0];
				if (item != null && item.getItemCount() <= 0) {
					setSelection(viewer, item, fireOpen, expandOnly); // Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
					return false;
				}
			}
		}
		while (true) {
			item = findNextPrev(viewer, item, next);
			if (item == null)
				break;
			if (item.getItemCount() <= 0)
				break;
		}
		if (item != null) {
			setSelection(viewer, item, fireOpen, expandOnly); // Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
			return false;
		}
		return true;
	}
}
