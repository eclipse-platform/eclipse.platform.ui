/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize.viewers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.actions.ExpandAllAction;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.internal.dialogs.ContainerCheckedTreeViewer;

/**
 * A <code>TreeViewerAdvisor</code> object controls various UI
 * aspects of sync info viewers like the context menu, toolbar, content
 * provider, and label provider. A configuration is created to display
 * {@link SyncInfo} objects contained in the provided {@link SyncInfoSet}.
 * <p>
 * This configuration allows viewer contributions made in a plug-in manifest to
 * be scoped to a particular unique id. As a result the context menu for the
 * viewer can be configured to show object contributions for random id schemes.
 * To enable declarative action contributions for a configuration there are two
 * steps required:
 * <ul>
 * <li>Create a viewer contribution with a <code>targetID</code> that groups
 * sets of actions that are related. A common pratice for synchronize view
 * configurations is to use the participant id as the targetID.
 * 
 * <pre>
 *  &lt;viewerContribution
 *  id=&quot;org.eclipse.team.ccvs.ui.CVSCompareSubscriberContributions&quot;
 *  targetID=&quot;org.eclipse.team.cvs.ui.compare-participant&quot;&gt;
 *  ...
 * </pre>
 * 
 * <li>Create a configuration instance with a <code>menuID</code> that
 * matches the targetID in the viewer contribution.
 * </ul>
 * <p>
 * Clients may use this class as is, or subclass to add new state and behavior.
 * The default behavior is to show sync info in a tree
 * </p>
 * @since 3.0
 */
public class TreeViewerAdvisor extends StructuredViewerAdvisor implements IPropertyChangeListener {

	private ExpandAllAction expandAllAction;
	
	public interface ITreeViewerAccessor {
		public void openSelection();

		public void createChildren(TreeItem item);
	}
	
	public static class NavigableTreeViewer extends TreeViewer implements ITreeViewerAccessor {
		public NavigableTreeViewer(Composite parent, int style) {
			super(parent, style);
		}

		public void openSelection() {
			fireOpen(new OpenEvent(this, getSelection()));
		}

		public void createChildren(TreeItem item) {	
			super.createChildren(item);
		}
	}
	
	public static class NavigableCheckboxTreeViewer extends ContainerCheckedTreeViewer implements ITreeViewerAccessor {
		public NavigableCheckboxTreeViewer(Composite parent, int style) {
			super(parent, style);
		}

		public void openSelection() {
			fireOpen(new OpenEvent(this, getSelection()));
		}

		public void createChildren(TreeItem item) {	
			super.createChildren(item);
		}
	}
	
	/**
	 * Create a <code>SyncInfoSetCompareConfiguration</code> for the given
	 * sync set.
	 * @param set
	 *            the <code>SyncInfoSet</code> to be displayed in the
	 *            resulting diff viewer.
	 */
	public TreeViewerAdvisor(SyncInfoTree set) {
		this(null, set);
	}

	/**
	 * Create a <code>SyncInfoSetCompareConfiguration</code> for the given
	 * sync set and menuId. If the menuId is <code>null</code>, then no
	 * contributed menus will be shown in the diff viewer created from this
	 * configuration.
	 * @param menuId
	 *            the id of <code>targetID</code> specified in <code>viewerContribution</code>
	 *            extension points.
	 * @param set
	 *            the <code>SyncInfoSet</code> to be displayed in the
	 *            resulting diff viewer
	 */
	public TreeViewerAdvisor(String menuId, SyncInfoTree set) {
		super(menuId, set);
		TeamUIPlugin.getPlugin().getPreferenceStore().addPropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#initializeViewer(org.eclipse.jface.viewers.StructuredViewer)
	 */
	public void initializeViewer(StructuredViewer viewer) {
		super.initializeViewer(viewer);
		Assert.isTrue(viewer instanceof AbstractTreeViewer);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#dispose()
	 */
	public void dispose() {
		TeamUIPlugin.getPlugin().getPreferenceStore().removePropertyChangeListener(this);
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#initializeListeners(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void initializeListeners(StructuredViewer viewer) {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(getViewer(), event);
			}
		});
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#initializeActions(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void initializeActions(StructuredViewer viewer) {
		super.initializeActions(viewer);
		expandAllAction = new ExpandAllAction((AbstractTreeViewer) viewer);
		Utils.initAction(expandAllAction, "action.expandAll."); //$NON-NLS-1$
	}
	
	/**
	 * Handles a double-click event from the viewer. Expands or collapses a
	 * folder when double-clicked.
	 * @param viewer
	 *            the viewer
	 * @param event
	 *            the double-click event
	 */
	protected void handleDoubleClick(StructuredViewer viewer, DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		AbstractTreeViewer treeViewer = (AbstractTreeViewer) getViewer();
		if (treeViewer.getExpandedState(element)) {
			treeViewer.collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
		} else {
			TreeViewerAdvisor.navigate((TreeViewer)getViewer(), true /* next */, false /* no-open */, true /* only-expand */);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#getDiffNodeController()
	 */
	protected SynchronizeModelProvider getDiffNodeController() {
		if(getShowCompressedFolders()) {
			return new CompressedFoldersModelProvider((SyncInfoTree)getSyncInfoSet());
		}
		return new HierarchicalModelProvider((SyncInfoTree)getSyncInfoSet());
	}
	
	private boolean getShowCompressedFolders() {
		return TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_COMPRESS_FOLDERS);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (getViewer() != null && event.getProperty().equals(IPreferenceIds.SYNCVIEW_COMPRESS_FOLDERS)) {
			try {
				prepareInput(null);
				setInput(getViewer());
			} catch (TeamException e) {
				TeamUIPlugin.log(e);
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#fillContextMenu(org.eclipse.jface.viewers.StructuredViewer, org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(StructuredViewer viewer, IMenuManager manager) {
		manager.add(expandAllAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#navigate(boolean)
	 */
	public boolean navigate(boolean next) {
		return TreeViewerAdvisor.navigate((TreeViewer)getViewer(), next, true, false);
	}
	/**
	 * Selects the next (or previous) node of the current selection.
	 * If there is no current selection the first (last) node in the tree is selected.
	 * Wraps around at end or beginning.
	 * Clients may override. 
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
}