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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.TreeViewer;

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
	
	protected TabFolder tabFolder;
	
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
		
		if (WorkbenchActivityHelper.isFiltering()) {			
			tabFolder = new TabFolder(composite, SWT.NONE);
			tabFolder.setFont(composite.getFont());
						
			filteredViewer = createTreeViewer(tabFolder, true);
			unfilteredViewer = createTreeViewer(tabFolder, false);
			
			// flipping tabs updates the selected node
			tabFolder.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (tabFolder.getSelectionIndex() == 0) {
						showPage(getSingleSelection(filteredViewer.getSelection()));
					} else {
						showPage(getSingleSelection(unfilteredViewer.getSelection()));
					}
				}
			});			
			
			layoutTreeAreaControl(tabFolder);
			return tabFolder;
		}
		else {
			unfilteredViewer = createTreeViewer(composite);
			unfilteredViewer.setLabelProvider(new PreferenceLabelProvider());
			unfilteredViewer.setContentProvider(new FilteredPreferenceContentProvider(false));
			unfilteredViewer.setInput(getPreferenceManager());
			
			layoutTreeAreaControl(unfilteredViewer.getControl());	
			return unfilteredViewer.getControl();
		}
	}
		
	/**
	 * Create a new viewer in the parent.
	 * 
	 * @param parent the parent <code>TabFolder</code>.
	 * @param filtering whether the viewer should be filtering based on
	 *            activities.
	 * @return <code>TreeViewer</code>
	 */
	private TreeViewer createTreeViewer(TabFolder parent, boolean filtering) {
		TreeViewer tree = createTreeViewer(parent);
		tree.setLabelProvider(new PreferenceLabelProvider());
		tree.setContentProvider(new FilteredPreferenceContentProvider(filtering));
		tree.setInput(getPreferenceManager());
		
		TabItem tabItem = new TabItem(parent, SWT.NONE);
		tabItem.setControl(tree.getControl());
		tabItem.setText(filtering ? ActivityMessages.getString("ActivityFiltering.filtered") //$NON-NLS-1$
								  : ActivityMessages.getString("ActivityFiltering.unfiltered")); //$NON-NLS-1$
		
		return tree;
	}
		
	/**
	 * Get the tree viewer for the currently active tab.
	 */
	protected TreeViewer getTreeViewer() {
		if (tabFolder == null)
			return unfilteredViewer;
		
		if (tabFolder.getSelectionIndex() == 0) 
			return filteredViewer;
		else
			return unfilteredViewer;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#updateTreeFont(org.eclipse.swt.graphics.Font)
	 */
	protected void updateTreeFont(Font dialogFont) {
		unfilteredViewer.getControl().setFont(dialogFont);
		if (filteredViewer != null)
			filteredViewer.getControl().setFont(dialogFont);
	}	
}
