/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.activities.ws.ActivityMessages;

/**
 * Baseclass for preference dialogs that will show two tabs of preferences - 
 * filtered and unfiltered.
 * 
 * @since 3.0
 */
public abstract class FilteredPreferenceDialog extends PreferenceDialog {

	protected TreeViewer filteredViewer, unfilteredViewer;

	private StackLayout stackLayout;
	private Composite stackComposite;
	private Button showAllCheck;


	/**
	 * Creates a new preference dialog under the control of the given preference 
	 * manager.
	 *
	 * @param shell the parent shell
	 * @param manager the preference manager
	 */
	public FilteredPreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTreeArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createTreeAreaContents(Composite composite) {
	    Composite treeArea = new Composite(composite, SWT.None);
	    GridLayout layout = new GridLayout();
	    layout.marginHeight = 0;
	    layout.marginWidth = 0;
	    treeArea.setLayout(layout);
		stackComposite = new Composite(treeArea, SWT.NONE);
		stackLayout = new StackLayout();
		stackComposite.setLayout(stackLayout);
		stackComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		stackComposite.setFont(composite.getFont());
		layoutTreeAreaControl(treeArea);
		
	    filteredViewer = createTreeViewer(stackComposite, true);
	    if (WorkbenchActivityHelper.showAll()) {

			unfilteredViewer = createTreeViewer(stackComposite, false);

			showAllCheck = new Button(treeArea, SWT.CHECK);
			showAllCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			showAllCheck.setText(ActivityMessages.getString("ActivityFiltering.showAll")); //$NON-NLS-1$
			
			// flipping tabs updates the selected node
			showAllCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
				    if (!showAllCheck.getSelection()) {
					    filteredViewer.setExpandedElements(unfilteredViewer.getExpandedElements());
					    filteredViewer.setSelection(unfilteredViewer.getSelection());				        
						stackLayout.topControl = filteredViewer.getControl();	
						stackComposite.layout();
						showPage(getSingleSelection(filteredViewer.getSelection()));

					} else {
					    unfilteredViewer.setExpandedElements(filteredViewer.getExpandedElements());
					    unfilteredViewer.setSelection(filteredViewer.getSelection());					    
						stackLayout.topControl = unfilteredViewer.getControl();	
						stackComposite.layout();					    
						showPage(getSingleSelection(unfilteredViewer.getSelection()));
					}
				}
			});
		} 
	    
	    stackLayout.topControl = filteredViewer.getControl();
	    return stackComposite;
	}

	/**
	 * Create a new viewer in the parent.
	 * 
	 * @param parent the parent <code>Composite</code>.
	 * @param filtering whether the viewer should be filtering based on
	 *            activities.
	 * @return <code>TreeViewer</code>
	 */
	private TreeViewer createTreeViewer(Composite parent, boolean filtering) {
		TreeViewer tree = createTreeViewer(parent);
		tree.setLabelProvider(new FilteredPreferenceLabelProvider());
		tree.setContentProvider(new FilteredPreferenceContentProvider(filtering));
		tree.setInput(getPreferenceManager());
		return tree;
	}

	/**
	 * Differs from super implementation in that if the node is found but should
	 * be filtered based on a call to 
	 * <code>WorkbenchActivityHelper.filterItem()</code> then <code>null</code> 
	 * is returned.
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#findNodeMatching(java.lang.String)
	 */
	protected IPreferenceNode findNodeMatching(String nodeId) {
		IPreferenceNode node = super.findNodeMatching(nodeId);
		if (WorkbenchActivityHelper.filterItem(node))
			return null;
		return node;
	}

	/**
	 * Get the tree viewer for the currently active tab.
	 */
	protected TreeViewer getTreeViewer() {
		if (unfilteredViewer == null)
			return filteredViewer;

		if (showAllCheck.getSelection())
			return unfilteredViewer;
		else
			return filteredViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#updateTreeFont(org.eclipse.swt.graphics.Font)
	 */
	protected void updateTreeFont(Font dialogFont) {
		filteredViewer.getControl().setFont(dialogFont);
		if (unfilteredViewer != null)
			unfilteredViewer.getControl().setFont(dialogFont);
	}
}
