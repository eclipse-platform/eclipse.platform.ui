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
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Subclass of TreeViewer which handles decorator events properly. We should not need to create 
 * a subclass just for this!
 */
public class SyncTreeViewer extends TreeViewer implements INavigableControl {
	private final SyncViewer viewer;
	public SyncTreeViewer(SyncViewer viewer, Composite parent, int style) {
		super(parent, style);
		this.viewer = viewer;
	}
	protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {
		Object[] changed= event.getElements();
		if (changed != null && this.viewer.getInput() != null) {
			ArrayList others= new ArrayList();
			for (int i= 0; i < changed.length; i++) {
				Object curr = changed[i];
				if (curr instanceof IResource) {
					// TODO: This may be the wrong model object!
					SyncSetContentProvider provider = viewer.getContentProvider();
					if (provider != null) {
						curr = provider.getModelObject((IResource)curr);
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
	
}