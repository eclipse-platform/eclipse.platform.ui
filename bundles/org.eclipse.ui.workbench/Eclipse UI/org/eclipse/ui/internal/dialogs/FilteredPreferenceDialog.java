/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceContentProvider;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.activities.WorkbenchActivityHelper;

import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExtensionNode;

/**
 * Baseclass for preference dialogs that will show two tabs of preferences -
 * filtered and unfiltered.
 * 
 * @since 3.0
 */
public abstract class FilteredPreferenceDialog extends PreferenceDialog {

	protected FilteredTree filteredTree;

	private Object pageData;

	/**
	 * Creates a new preference dialog under the control of the given preference
	 * manager.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param manager
	 *            the preference manager
	 */
	public FilteredPreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
	}

	/**
	 * Differs from super implementation in that if the node is found but should
	 * be filtered based on a call to
	 * <code>WorkbenchActivityHelper.filterItem()</code> then
	 * <code>null</code> is returned.
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#findNodeMatching(java.lang.String)
	 */
	protected IPreferenceNode findNodeMatching(String nodeId) {
		IPreferenceNode node = super.findNodeMatching(nodeId);
		if (WorkbenchActivityHelper.filterItem(node))
			return null;
		return node;
	}

	protected TreeViewer createTreeViewer(Composite parent) {
		PatternFilter filter = new PatternFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				ITreeContentProvider contentProvider = (ITreeContentProvider) getTreeViewer()
						.getContentProvider();

				IPreferenceNode node = (IPreferenceNode) element;
				Object[] children = contentProvider.getChildren(node);
				
				String[] words = node.getLabelText().split("\\W"); //$NON-NLS-1$
				for (int i = 0;  i < words.length; i++){
					if( match(words[i]))
						return true;
				}	
				if (filter(viewer, element, children).length > 0)
					return true;
				
				if(node instanceof WorkbenchPreferenceExtensionNode){
					WorkbenchPreferenceExtensionNode workbenchNode =
						(WorkbenchPreferenceExtensionNode) node;
					Collection keywordCollection = workbenchNode.getKeywordLabels();
					if(keywordCollection == null)
						return false;
					Iterator keywords = keywordCollection.iterator();
					while(keywords.hasNext()){
						if(match((String) keywords.next()))
							return true;
					}
				}
				return false;

			}
		};
		int styleBits = SWT.SINGLE | SWT.H_SCROLL;
		filteredTree = new FilteredComboTree(parent, styleBits, filter);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		filteredTree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		TreeViewer tree = filteredTree.getViewer();
		filteredTree.setInitialText(WorkbenchMessages
				.getString("WorkbenchPreferenceDialog.FilterMessage")); //$NON-NLS-1$

		setContentAndLabelProviders(tree);
		tree.setInput(getPreferenceManager());
		tree.addFilter(new CapabilityFilter());

		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				handleTreeSelectionChanged(event);
			}
		});

		super.addListeners(filteredTree.getViewer());
		return filteredTree.getViewer();
	}


	/**
	 * Set the content and label providers for the treeViewer
	 * 
	 * @param treeViewer
	 */
	protected void setContentAndLabelProviders(TreeViewer treeViewer) {
		treeViewer.setLabelProvider(new PreferenceLabelProvider());
		treeViewer.setContentProvider(new PreferenceContentProvider());
	}

	/**
	 * A selection has been made in the tree.
	 * @param event SelectionChangedEvent
	 */
	protected void handleTreeSelectionChanged(SelectionChangedEvent event) {
		//Do nothing by default
	}

	protected Control createTreeAreaContents(Composite parent) {
		Composite leftArea = new Composite(parent, SWT.NONE);
		leftArea.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		GridLayout leftLayout = new GridLayout();
		leftLayout.numColumns = 1;
		//leftLayout.marginWidth = 0;
		leftLayout.marginHeight = 0;
		leftLayout.horizontalSpacing = 0;
		leftLayout.verticalSpacing = 0;
		leftArea.setLayout(leftLayout);

		// Build the tree an put it into the composite.
		TreeViewer viewer = createTreeViewer(leftArea);
		setTreeViewer(viewer);

		updateTreeFont(JFaceResources.getDialogFont());
		GridData viewerData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		viewer.getControl().getParent().setLayoutData(viewerData);

		layoutTreeAreaControl(leftArea);

		return leftArea;
	}

	/**
	 * Show only the supplied ids.
	 * 
	 * @param filteredIds
	 */
	public void showOnly(String[] filteredIds) {
		filteredTree.getViewer().addFilter(new PreferenceNodeFilter(filteredIds));

	}

	/**
	 * Set the data to be applied to a page after it is created.
	 * @param pageData Object
	 */
	public void setPageData(Object pageData) {
		this.pageData = pageData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createPage(org.eclipse.jface.preference.IPreferenceNode)
	 */
	protected void createPage(IPreferenceNode node) {

		super.createPage(node);
		if (this.pageData == null)
			return;
		//Apply the data if it has been set.
		IPreferencePage page = node.getPage();
		if (page instanceof PreferencePage)
			((PreferencePage) page).applyData(this.pageData);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#getCurrentPage()
	 */
	public IPreferencePage getCurrentPage() {
		return super.getCurrentPage();
	}
	
	

}