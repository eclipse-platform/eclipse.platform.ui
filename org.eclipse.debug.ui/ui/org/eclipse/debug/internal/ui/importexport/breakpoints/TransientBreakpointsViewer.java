/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.importexport.breakpoints;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsContentProvider;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsLabelProvider;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsSorter;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsViewer;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointOrganizer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;

/**
 * This class creates a simplified debug view that can be used in wizards etc., to emulate the current debug view
 * 
 * @see WizardExportBreakpointsPage
 * @see WizardImportBreakpointsPage
 *
 * @since 3.2
 */
public class TransientBreakpointsViewer {

	//widgets
	private IStructuredSelection fSelection = null;
	private BreakpointsView fView = null;
	private BreakpointsContentProvider fProvider = null;
	private Tree fTree = null;
	private BreakpointsViewer fViewer = null;
	private ICheckStateListener fCheckListener = new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			updateCheckedState(event.getElement(), event.getChecked());
		}
	};
	
	//constants
	private static final int HEIGHT_HINT = 150;
	
	/**
	 * This constructor uses the selection from the debug view to initialize its selection
	 * 
	 * <p>
	 * Neither parent nor input can be null
	 * </p>
	 * 
	 * @param parent the parent to add this composite to
	 * @param input the input to the viewer
	 */
	public TransientBreakpointsViewer(Composite parent, Object input) {
		Assert.isNotNull(parent);
		Assert.isNotNull(input);
		createControl(parent, input, null);
	}//end constructor
	
	/**
	 * This constructor allows a specific selction to be used in stead of the default
	 * 
	 * @param parent the parent composite to add this one to
	 * @param input the input to the viewer
	 * @param selection the selection to set on the viewer
	 */
	public TransientBreakpointsViewer(Composite parent, Object input, IStructuredSelection selection) {
		Assert.isNotNull(parent);
		Assert.isNotNull(input);
		createControl(parent, input, selection);
	}
	
	/**
	 * Creates the control initialized to the current view, selection, and organization of the breakpoints view
	 * @param parent the parent composite to add this one to.
	 * 
	 * @param parent the parent composite to add this one to
	 * @param input the input for the viewer
	 * @param selection the selection for the viewer to be initialized to. If null the selection from the breakpoints view is used
	 */
	private void createControl(Composite parent, Object input, IStructuredSelection selection) {
		fSelection = selection;
		if(fSelection == null) {
			IViewPart fViewpart = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage().findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
			if(fViewpart != null) {
				fSelection = (IStructuredSelection)fViewpart.getViewSite().getSelectionProvider().getSelection();
			}//end if
			else {
				fSelection = new StructuredSelection();
			}//end else
		}//end if
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1, true));
		GridData grid = new GridData(GridData.FILL_BOTH);
		grid.heightHint = HEIGHT_HINT;
		composite.setLayoutData(grid);
		composite.setFont(font);
		
		// create the treeview
		fTree = new Tree(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK);
		fProvider = new BreakpointsContentProvider();
		fView = ((BreakpointsView)DebugUIPlugin.getActiveWorkbenchWindow().getActivePage().findView(IDebugUIConstants.ID_BREAKPOINT_VIEW));
		fTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewer = new BreakpointsViewer(fTree);
		//fix for bug #109008
		IBaseLabelProvider labelProvider = null;
		if (fView == null) {
			labelProvider = new BreakpointsLabelProvider();
		} else {
			labelProvider = fView.getCheckboxViewer().getLabelProvider();
		}
		fViewer.setSorter(new BreakpointsSorter());
		fViewer.setLabelProvider(labelProvider);
		fViewer.addCheckStateListener(fCheckListener);
		fViewer.addTreeListener(new ITreeViewerListener() {
			public void treeExpanded(TreeExpansionEvent event) {
				fViewer.updateCheckedState(event.getElement());
			}
			public void treeCollapsed(TreeExpansionEvent event) {
			}
		});
		IBreakpointOrganizer[] orgs = null;
		if(fView != null)
			 orgs = fView.getBreakpointOrganizers(); 
		fViewer.setContentProvider(fProvider);
		fViewer.setInput(input);
		fProvider.setOrganizers(orgs);
		Object[] items = fSelection.toArray();
		fViewer.setCheckedElements(items);
		for(int i = 0; i < items.length; i++) {
			updateCheckedState(items[i], true);
		}//end for
	}
	
	/**
	 * Returns the selection from the viewer with no duplicates
	 * @return the selection from the viewer with no duplicates
	 */
	public IStructuredSelection getCheckedElements() {
		Object[] list = fViewer.getCheckedElements();
		Vector selected = new Vector();
		for(int i = 0; i < list.length; i++) {
			if(!selected.contains(list[i])) {
				selected.addElement(list[i]);
			}//end if
		}//end for
		return new StructuredSelection(selected);
	}
	
	/**
	 * Allows access to the viewer
	 * @return the viewer
	 */
	public BreakpointsViewer getViewer() {
		return fViewer;
	}//end getViewer
	
	 /**
     * Update the checked state of the given element and all of its children.
     * 
     * @param obj the object that has been changed
     * @param enable the checked status of the obj
     */
    private void updateCheckedState(Object obj, boolean enable) {
    	//TreeItem item = (TreeItem)fViewer.searchItem(obj);
    	ArrayList list = findAllItemOccurances(obj);
    	TreeItem item = null;
    	for(int i = 0; i < list.size(); i++) {
    		item = (TreeItem)list.get(i);
	        if (obj instanceof IBreakpoint) {
	            item.setChecked(enable);
	            TreeItem parent = item.getParentItem();
	            if(parent != null) {
		            if(otherChildrenChecked(parent.getItems())) {
		            	if(allChildrenChecked(parent.getItems())) {
		            		parent.setGrayed(false);
		            		parent.setChecked(true);
		            	}
		            	else {
			            	parent.setGrayed(true);
			            	parent.setChecked(true);
		            	}//end else
		            }//endif
		            else {
		            	parent.setGrayed(false);
		            	parent.setChecked(false);
		            }//end else
	            }//end if
	        }//end if
	        else if (obj instanceof BreakpointContainer) {
	        	item.setGrayed(false);
	        	item.setChecked(enable);
	        	TreeItem[] children = item.getItems();
	        	for(int j = 0; j < children.length; j++) {
	        		children[j].setChecked(enable);
	        	}//end for
	        }//end if
    	}//end for
     }//end updateCheckedState

    /**
     * Finds all of the objects in the current tree view based on what is showing, not on findItem
     * which only finds the first occurance of the object in the tree
     * @param object the object to look for
     * @return the list of objects or an empty list, never null
     * 
     * @since 3.2
     */
    private ArrayList findAllItemOccurances(Object object) {
    	ArrayList results = new ArrayList();
    	fTree.selectAll();
    	TreeItem[] items = fTree.getSelection();
    	fTree.deselectAll();
    	for(int i = 0; i < items.length; i++) {
    		if(object.equals(items[i].getData())) {
    			results.add(items[i]);
    		}//end if
    	}//end for
    	return results;
    }
    
    /**
     * Checks to see if there is at least one other checked child from the list of children
     * @param children the children treeitems to inspect
     * @return true it at least oneo ther child is checked, false otherwise
     */
    private boolean otherChildrenChecked(TreeItem[] children) {
    	for(int i = 0; i < children.length; i++) {
    		if(children[i].getChecked()) {
    			return true;
    		}//end if
    	}//end ofr
    	return false;
    }
    
    /**
     * Checks to see if all of the children under an given parent are checked or not
     * @param children the children to check
     * @return true if all children are checked, false otherwise
     */
    private boolean allChildrenChecked(TreeItem[] children) {
    	boolean checked = true;
    	for(int i = 0; i < children.length; i++) {
    		checked = checked & children[i].getChecked();
    	}//end for
    	return checked;
    }//end allChildrenChecked

}//end class
