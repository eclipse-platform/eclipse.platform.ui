/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.GenericListItem;
import org.eclipse.jface.viewers.GenericListViewer;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Baseclass for preference dialogs that will show two tabs of preferences - 
 * filtered and unfiltered.
 * 
 * @since 3.0
 */
public abstract class FilteredPreferenceDialog extends PreferenceDialog {

	private GenericListViewer genericListViewer;

	private PreferencesPageContainer newPageContainer;

	//A boolean to indicate if the new look tab was selected
	private boolean newLookSelected = false;

	/**
	 * Creates a new preference dialog under the control of the given preference 
	 * manager.
	 *
	 * @param parentShell the parent shell
	 * @param manager the preference manager
	 */
	public FilteredPreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		if (Policy.SHOW_PREFERENCES_NEWLOOK)
			return createNewLookTab(parent);
		else
			return super.createDialogArea(parent);
	}	

	/**
	 * Create a tabbed list for the new and classic looks in the composite.
	 * @param composite
	 */
	private Control createNewLookTab(Composite composite) {
		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);

		// TODO Remove this before 3.1 ships
		

		TabItem classicItem = new TabItem(tabFolder, SWT.NONE);
		classicItem.setText("Classic");//$NON-NLS-1$
		Composite classicParent = new Composite(tabFolder, SWT.NONE);
		classicItem.setControl(classicParent);
		classicParent.setLayout(new GridLayout());
		super.createDialogArea(classicParent);

		final TabItem newItem = new TabItem(tabFolder, SWT.NONE);
		newItem.setText("New Look");//$NON-NLS-1$
		Composite newParent = new Composite(tabFolder, SWT.NONE);
		newItem.setControl(newParent);
		newParent.setLayout(new GridLayout());
		createNewPreferencesArea(newParent);
		
		GridData folderData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);
		tabFolder.setLayoutData(folderData);
		
		tabFolder.addSelectionListener(new SelectionListener(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetDefaultSelected(SelectionEvent e) {
				//No default behaviour
			}
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				
				newLookSelected = (e.item == newItem);
				Iterator elements = getPreferenceManager().getElements(PreferenceManager.PRE_ORDER).iterator();
				
				while(elements.hasNext()){
					clear((IPreferenceNode) elements.next());
				}
				
			}
			/**
			 * Clear the entries in the node and its children.
			 * @param node
			 */
			private void clear(IPreferenceNode node) {
				node.disposeResources();
				IPreferenceNode [] nodes = node.getSubNodes();
				for (int i = 0; i < nodes.length; i++) {
					clear( nodes[i]);			
				}
			}
		});

		return composite;
	}

	private void createNewPreferencesArea(final Composite composite) {
		((GridLayout) composite.getLayout()).numColumns = 3;
		genericListViewer = createListViewer(composite);
		createSash(composite, genericListViewer.getControl());
		// Build the Page container
		newPageContainer = 	new PreferencesPageContainer();
		newPageContainer.createContents(composite, SWT.NULL);
		newPageContainer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	/**
	 * Create a generic list viewer for entry selection.
	 * @param composite
	 * @return GenericListViewer
	 */
	private GenericListViewer createListViewer(Composite composite) {
		final GenericListViewer viewer = new GenericListViewer(composite, SWT.BORDER){
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.GenericListViewer#createListItem(java.lang.Object, org.eclipse.swt.graphics.Color, org.eclipse.jface.viewers.GenericListViewer)
			 */
			public GenericListItem createListItem(Object element, Color color,
					GenericListViewer viewer) {
				PreferencesCategoryItem item =  new PreferencesCategoryItem(element);
				item.createControl(viewer.getComposite(),color);
				return item;
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.GenericListViewer#itemSelected(org.eclipse.jface.viewers.GenericListItem)
			 */
			protected void itemSelected(GenericListItem item) {
				showPage((IPreferenceNode) item.getElement());

			}
		};
		
		viewer.getControl().setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		
		//Register help listener on the tree to use context sensitive help
		viewer.getControl().addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent event) {
				// call perform help on the current page
				if (getCurrentPage() != null) {
					getCurrentPage().performHelp();
				}
			}
		});
		viewer.setLabelProvider(new PreferenceLabelProvider());
		viewer.setContentProvider(new FilteredPreferenceContentProvider());
		
		viewer.setInput(getPreferenceManager());
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = getLastRightWidth();
		gd.verticalSpan = 1;
		viewer.getControl().setLayoutData(gd);
		
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTreeViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		TreeViewer tree = super.createTreeViewer(parent);
		tree.setLabelProvider(new PreferenceLabelProvider());
		tree.setContentProvider(new FilteredPreferenceContentProvider());
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#showPage(org.eclipse.jface.preference.IPreferenceNode)
	 */
	protected boolean showPage(IPreferenceNode node) {
		return super.showPage(node);
	}
	
	
}