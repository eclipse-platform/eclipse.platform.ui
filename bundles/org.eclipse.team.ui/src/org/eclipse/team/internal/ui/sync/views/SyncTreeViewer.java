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
package org.eclipse.team.internal.ui.sync.views;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIPlugin;

/**
 * Subclass of TreeViewer which handles decorator events properly. We should not need to create 
 * a subclass just for this!
 */
public class SyncTreeViewer extends TreeViewer implements INavigableControl {
	
	/**
	 * Change the tree layout between using compressed folders and regular folders
	 * when the user setting is changed.
	 */
	private IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(IPreferenceIds.SYNCVIEW_COMPRESS_FOLDERS)) {
				setTreeViewerContentProvider();
			}
		}
	};
		
	public SyncTreeViewer(Composite parent, int style) {
		super(parent, style);
		getStore().addPropertyChangeListener(propertyListener);
		setTreeViewerContentProvider();
	}
	
	private void setTreeViewerContentProvider() {
		if (getStore().getBoolean(IPreferenceIds.SYNCVIEW_COMPRESS_FOLDERS)) {
			setContentProvider(new CompressedFolderContentProvider());
		} else {
			setContentProvider(new SyncSetTreeContentProvider());
		}
	}
	
	/**
	 * Return the preference store for this plugin.
	 * @return IPreferenceStore for this plugin
	 */
	private IPreferenceStore getStore() {
		return TeamUIPlugin.getPlugin().getPreferenceStore();
	}

	protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {
		Object[] changed= event.getElements();
		if (changed != null && getInput() != null) {
			ArrayList others= new ArrayList();
			for (int i= 0; i < changed.length; i++) {
				Object curr = changed[i];
				if (curr instanceof IResource) {
					IContentProvider provider = getContentProvider();
					if (provider != null && provider instanceof SyncSetContentProvider) {
						curr = ((SyncSetContentProvider)provider).getModelObject((IResource)curr);
					}
				}
				others.add(curr);
			}
			if (others.isEmpty()) {
				return;
			}
			event= new LabelProviderChangedEvent((IBaseLabelProvider) event.getSource(), others.toArray());
		}
		super.handleLabelProviderChanged(event);
	}

	/**
	 * Cleanup listeners and call super for content provider and label provider disposal.
	 */	
	protected void handleDispose(DisposeEvent event) {
		super.handleDispose(event);
		getStore().removePropertyChangeListener(propertyListener);
	}
	
	/**
	 * Selects the next (or previous) node of the current selection.
	 * If there is no current selection the first (last) node in the tree is selected.
	 * Wraps around at end or beginning.
	 * Clients may override. 
	 *
	 * @param next if <code>true</code> the next node is selected, otherwise the previous node
	 */
	public boolean gotoDifference(int direction) {	
		boolean next = direction == INavigableControl.NEXT ? true : false;
		return internalNavigate(next, false);
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
	private boolean internalNavigate(boolean next, boolean fireOpen) {
		
		Control c= getControl();
		if (!(c instanceof Tree))
			return false;
			
		Tree tree= (Tree) c;
		TreeItem item= null;
		TreeItem children[]= tree.getSelection();
		if (children != null && children.length > 0)
			item= children[0];
		if (item == null) {
			children= tree.getItems();
			if (children != null && children.length > 0) {
				item= children[0];
				if (item != null && item.getItemCount() <= 0) {
					internalSetSelection(item, fireOpen);				// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
					return false;
				}
			}
		}
			
		while (true) {
			item= findNextPrev(item, next);
			if (item == null)
				break;
			if (item.getItemCount() <= 0)
				break;
		}
		
		if (item != null) {
			internalSetSelection(item, fireOpen);	// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
			return false;
		}
		return true;
	}

	private TreeItem findNextPrev(TreeItem item, boolean next) {
		
		if (item == null)
			return null;
		
		TreeItem children[]= null;

		if (!next) {
		
			TreeItem parent= item.getParentItem();
			if (parent != null)
				children= parent.getItems();
			else
				children= item.getParent().getItems();
			
			if (children != null && children.length > 0) {
				// goto previous child
				int index= 0;
				for (; index < children.length; index++)
					if (children[index] == item)
						break;
				
				if (index > 0) {
					
					item= children[index-1];
					
					while (true) {
						createChildren(item);
						int n= item.getItemCount();
						if (n <= 0)
							break;
							
						item.setExpanded(true);
						item= item.getItems()[n-1];
					}

					// previous
					return item;
				}
			}
			
			// go up
			return parent;
					
		} else {
			item.setExpanded(true);
			createChildren(item);
			
			if (item.getItemCount() > 0) {
				// has children: go down
				children= item.getItems();
				return children[0];
			}
			
			while (item != null) {
				children= null;
				TreeItem parent= item.getParentItem();
				if (parent != null)
					children= parent.getItems();
				else
					children= item.getParent().getItems();
				
				if (children != null && children.length > 0) {
					// goto next child
					int index= 0;
					for (; index < children.length; index++)
						if (children[index] == item)
							break;
					
					if (index < children.length-1) {
						// next
						return children[index+1];
					}
				}
				
				// go up
				item= parent;
			}
		}
				
		return item;
	}
	
	private void internalSetSelection(TreeItem ti, boolean fireOpen) {
		if (ti != null) {
			Object data= ti.getData();
			if (data != null) {
				// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
				ISelection selection= new StructuredSelection(data);
				setSelection(selection, true);
				ISelection currentSelection= getSelection();
				if (fireOpen && currentSelection != null && selection.equals(currentSelection)) {
					fireOpen(new OpenEvent(this, selection));
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.views.INavigableControl#preserveState(int)
	 */
	public void preserveState(int direction) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.views.INavigableControl#restoreState(int)
	 */
	public void restoreState(int direction) {
	}
}