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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IViewPart;

/**
 * This class creates a simplified debug view that can be used in wizards etc., to emulate the current debug view
 * 
 * @see WizardExportBreakpointsPage
 * @see WizardImportBreakpointsPage
 *
 * @since 3.2
 */
public class EmbeddedBreakpointsViewer {

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
	public EmbeddedBreakpointsViewer(Composite parent, Object input) {
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
	public EmbeddedBreakpointsViewer(Composite parent, Object input, IStructuredSelection selection) {
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
		IBreakpointOrganizer[] orgs = null;
		if(fView != null)
			 orgs = fView.getBreakpointOrganizers(); 
		fViewer.setContentProvider(fProvider);
		fViewer.setInput(input);
		fProvider.setOrganizers(orgs);
		initViewerState();
	}
	
	/**
	 * Performs the initialization of the viewer from a selection
	 */
	private void initViewerState() {
		Object[] items = fSelection.toArray();
		fViewer.setGrayedElements(new Object[] {});
		fViewer.setCheckedElements(new Object[] {});
		ArrayList list = new ArrayList();
		for(int i = 0; i < items.length; i++) {
			if(items[i] instanceof IBreakpoint) {
				list.add(items[i]);
			}//end if
			else {
				getBreakpointsFromContainers((BreakpointContainer)items[i], list);
			}//end if
		}//end for
		for(int i = 0; i < list.size(); i++) {
			updateCheckedState(list.get(i), true);
		}
	}//end initViewerState
	
	/**
	 * FInds the breakpoints of a given container
	 * @param container the container to get breakpoints from
	 * @param list the list of breakpoints to update state for
	 */
	private void getBreakpointsFromContainers(BreakpointContainer container, ArrayList list) {
		Object[] elements = container.getChildren();
		for(int i = 0; i < elements.length; i++) {
			if(elements[i] instanceof IBreakpoint) {
				list.add(elements[i]);
			}//end if
			else {
				getBreakpointsFromContainers((BreakpointContainer)elements[i], list);
			}//end else
		}//end for
	}//getBreakpointsFromContainers
	
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
	 * finds all occurrences of a widget to update
	 * @param element the element to search for when finding occurrences
	 * @return a list of widget occurrences to update or an empty list
	 */
    private Widget[] searchItems(Object element) {
        ArrayList list = new ArrayList();
        TreeItem[] items = fTree.getItems();
        for (int i = 0; i < items.length; i++) {
        	findAllOccurrences(items[i], element, list);
        }//end for
        return (Widget[]) list.toArray(new Widget[0]);
    }
    
    /**
     * performs the actual search for items in the tree
     * @param list the list to add matches to
     * @param item the item in the tree
     * @param element the element to compare
     */
    private void findAllOccurrences(TreeItem item, Object element, ArrayList list) {
        if (element.equals(item.getData())) {
                list.add(item);
        }//end if
        TreeItem[] items = item.getItems();
        for (int i = 0; i < items.length; i++) {
        	findAllOccurrences(items[i], element, list);
        }
    }
    
	 /**
     * Update the checked state of the given element and all of its children.
     * 
     * @param obj the object that has been changed
     * @param enable the checked status of the obj
     */
    private void updateCheckedState(Object obj, boolean enable) {
	        if (obj instanceof IBreakpoint) {
	        	Widget[] list = searchItems(obj);
	        	TreeItem item = null;
	        	for(int i = 0; i < list.length; i++) {
		        	item = (TreeItem)list[i];
		            item.setChecked(enable);
		            refreshParents(item);
	        	}//end for
	        }//end if
	        else if (obj instanceof BreakpointContainer) {
	        	ArrayList bps = new ArrayList();
	        	getBreakpointsFromContainers((BreakpointContainer)obj, bps);
	        	for(int j = 0; j < bps.size(); j++) {
	        		updateCheckedState(bps.get(j), enable);
	        	}
	        }//end if
	        //refreshParents(item);
    
     }//end updateCheckedState 

    /**
     * refreshes the grayed/checked state of the parents of item
     * @param item the item to refresh parents of
     */
    private void refreshParents(TreeItem item) {
    	TreeItem parent = item.getParentItem();
    	while (parent != null) {
    		int checked = getNumberChildrenChecked(parent);
        	if(checked == 0) {
        		parent.setGrayed(false);
            	parent.setChecked(false);
        	}//end if
        	else if(checked == parent.getItemCount()) {
        		if(getNumberChildrenGrayed(parent) > 0) {
        			parent.setGrayed(true);
        		}//end if
        		else {
        			parent.setGrayed(false);
        		}//end else
         		parent.setChecked(true);
        	}//end if
        	else {
        		parent.setGrayed(true);
            	parent.setChecked(true);
        	}//end else
    		parent = parent.getParentItem();
    	}//end while
    }//end refreshParents
    
    /**
     * Gets the number of grayed children for this parent
     * @param parent the parent to inspect
     * @return treu is any one or more children is grayed, false otherwise
     */
    private int getNumberChildrenGrayed(TreeItem parent) {
    	TreeItem[] children = parent.getItems();
    	int count = 0;
    	for(int i = 0; i < children.length; i++) {
    		if(children[i].getGrayed()) {
    			count++;
    		}//end if
    	}//end for
    	return count;
    }//end getNumberChildrenGrayed
    
    /**
     * Checks to see if all of the children under an given parent are checked or not
     * @param children the children to check
     * @return true if all children are checked, false otherwise
     */
    private int getNumberChildrenChecked(TreeItem parent) {
    	TreeItem[] children = parent.getItems();
    	int count = 0;
    	for(int i = 0; i < children.length; i++) {
    		if(children[i].getChecked()) {
    			count++;
    		}//end if
    	}//end for
    	return count;
    }//end allChildrenChecked
}//end class
