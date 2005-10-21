/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceContentProvider;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.preferences.WorkingCopyManager;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Baseclass for preference dialogs that will show two tabs of preferences -
 * filtered and unfiltered.
 * 
 * @since 3.0
 */
public abstract class FilteredPreferenceDialog extends PreferenceDialog implements IWorkbenchPreferenceContainer{

	protected FilteredTree filteredTree;

	private Object pageData;
	
	IWorkingCopyManager workingCopyManager;
	
	private Collection updateJobs = new ArrayList();
	
	//Composite toolBarComposite;

	
	/**
	 * The preference page history.
	 * 
	 * @since 3.1
	 */
	PreferencePageHistory history;

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
		history = new PreferencePageHistory(this);
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTreeViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		PatternFilter filter = new PreferencePatternFilter(true); 
		int styleBits = SWT.SINGLE | SWT.H_SCROLL;
		filteredTree = new FilteredTree(parent, styleBits, filter);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		filteredTree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		TreeViewer tree = filteredTree.getViewer();
		filteredTree.setInitialText(WorkbenchMessages.WorkbenchPreferenceDialog_FilterMessage);

		setContentAndLabelProviders(tree);
		tree.setInput(getPreferenceManager());
		
		//if the tree has only one or zero pages, make the combo area disable
		if(hasAtMostOnePage(tree)){
			filteredTree.getFilterControl().setEnabled(false);
		}		
		
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
	 * Return whether or not there are less than two pages.
	 * @param tree
	 * @return <code>true</code> if there are less than two
	 * pages.
	 */
	private boolean hasAtMostOnePage(TreeViewer tree){
		ITreeContentProvider contentProvider = (ITreeContentProvider ) tree.getContentProvider();
		Object[] children= contentProvider.getElements(tree.getInput());
		
		if(children.length <= 1){
			if(children.length == 0)
				return true;
			return !contentProvider.hasChildren(children[0]);				
		}
		return false;
	}
	/**
	 * Set the content and label providers for the treeViewer
	 * 
	 * @param treeViewer
	 */
	protected void setContentAndLabelProviders(TreeViewer treeViewer) {
		treeViewer.setLabelProvider(new PreferenceBoldLabelProvider(filteredTree));
		treeViewer.setContentProvider(new PreferenceContentProvider());
	}

	/**
	 * A selection has been made in the tree.
	 * @param event SelectionChangedEvent
	 */
	protected void handleTreeSelectionChanged(SelectionChangedEvent event) {
		//Do nothing by default
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTreeAreaContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createTreeAreaContents(Composite parent) {
		Composite leftArea = new Composite(parent, SWT.NONE);
		leftArea.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		leftArea.setFont(parent.getFont());
		GridLayout leftLayout = new GridLayout();
		leftLayout.numColumns = 1;
		leftLayout.marginHeight = 0;
		leftLayout.marginTop = IDialogConstants.VERTICAL_MARGIN;
		leftLayout.marginWidth = 0;
		leftLayout.marginLeft = IDialogConstants.HORIZONTAL_MARGIN;
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
		filteredTree.addFilter(new PreferenceNodeFilter(filteredIds));
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.preferences.IWorkbenchPreferenceContainer#openPage(java.lang.String, java.lang.Object)
	 */
	public boolean openPage(String pageId, Object data) {
		setPageData(data);
		setCurrentPageId(pageId);
		IPreferencePage page = getCurrentPage();
		if (page instanceof PreferencePage)
			((PreferencePage) page).applyData(data);
		return true;
	}

	/**
	 * Selects the current page based on the given preference page identifier.
	 * If no node can be found, then nothing will change.
	 * 
	 * @param preferencePageId
	 *            The preference page identifier to select; should not be
	 *            <code>null</code>.
	 */
	public final void setCurrentPageId(final String preferencePageId) {
		final IPreferenceNode node = findNodeMatching(preferencePageId);
		if (node != null) {
			getTreeViewer().setSelection(new StructuredSelection(node));
			showPage(node);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.preferences.IWorkbenchPreferenceContainer#getWorkingCopyManager()
	 */
	public IWorkingCopyManager getWorkingCopyManager() {
		if(workingCopyManager == null){
			workingCopyManager = new WorkingCopyManager();
		}
		return workingCopyManager;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		super.okPressed();
		if (workingCopyManager != null)
			try {
				workingCopyManager.applyChanges();
			} catch (BackingStoreException e) {
				String msg = e.getMessage();
				if (msg == null)
					msg = WorkbenchMessages.FilteredPreferenceDialog_PreferenceSaveFailed;
				IStatus errorStatus = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, msg, e);
				ErrorDialog
						.openError(
								getShell(),
								WorkbenchMessages.PreferencesExportDialog_ErrorDialogTitle,
								WorkbenchMessages.FilteredPreferenceDialog_PreferenceSaveFailed,
								errorStatus);
			}

		// Run the update jobs
		Iterator updateIterator = updateJobs.iterator();
		while (updateIterator.hasNext()) {
			((Job) updateIterator.next()).schedule();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.preferences.IWorkbenchPreferenceContainer#registerUpdateJob(org.eclipse.core.runtime.jobs.Job)
	 */
	public void registerUpdateJob(Job job){
		updateJobs.add(job);
	}

	/**
	 * Get the toolbar for the container
	 * 
	 * @return Control
	 */
	Control getContainerToolBar(Composite composite) {
	
		ToolBar historyBar = new ToolBar(composite, SWT.HORIZONTAL | SWT.FLAT);
		ToolBarManager historyManager = new ToolBarManager(historyBar);
	
		history.createHistoryControls(historyBar, historyManager);
		
		historyManager.update(false);
	
		return historyBar;
	}



	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#showPage(org.eclipse.jface.preference.IPreferenceNode)
	 */
	protected boolean showPage(IPreferenceNode node) {
		final boolean success = super.showPage(node);
		if (success) {
			history.addHistoryEntry(new PreferenceHistoryEntry(node.getId(), node.getLabelText(),
					null));
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		history.dispose();
		return super.close();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTitleArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Composite createTitleArea(Composite parent) {
				
		GridLayout parentLayout = (GridLayout) parent.getLayout();
		parentLayout.numColumns = 2;
		parentLayout.marginHeight = 0;
		parentLayout.marginTop = IDialogConstants.VERTICAL_MARGIN;		
		parent.setLayout(parentLayout);
		
		Composite titleComposite = super.createTitleArea(parent);
		
		Composite toolbarArea=new Composite(parent, SWT.NONE);
		GridLayout toolbarLayout = new GridLayout();
		toolbarLayout.marginHeight = 0;
		toolbarLayout.verticalSpacing = 0;
		toolbarArea.setLayout(toolbarLayout);
		toolbarArea.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		Control topBar = getContainerToolBar(toolbarArea);
		topBar.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		
		return titleComposite;
	}

	protected void selectSavedItem() {
		getTreeViewer().setInput(getPreferenceManager());
		super.selectSavedItem();
		if(getTreeViewer().getTree().getItemCount() > 1) {
			//unfortunately super will force focus to the list but we want the type ahead combo to get it.
			filteredTree.getFilterControl().setFocus();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#updateTreeFont(org.eclipse.swt.graphics.Font)
	 */
	protected void updateTreeFont(Font dialogFont) {
		applyDialogFont(filteredTree);
	}
}
