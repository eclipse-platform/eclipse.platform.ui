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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExtensionNode;
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTreeViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		PatternFilter filter = new PatternFilter() {
			/**
			 * TODO: this cache is needed because
			 * WorkbenchPreferenceExtensionNode.getKeywordLabels() is expensive.
			 * When it tracks keyword changes effectively then this cache can be
			 * removed.
			 */
			private Map keywordCache = new HashMap();
			
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				ITreeContentProvider contentProvider = (ITreeContentProvider) getTreeViewer()
						.getContentProvider();

				IPreferenceNode node = (IPreferenceNode) element;
				Object[] children = contentProvider.getChildren(node);
				String text = node.getLabelText();
				
				// Break the label up into words, separating based on whitespace and common punctuation.
				// Previously used String.split(..., "\\W"), where "\W" is a regular expression (see the Javadoc for class Pattern).
				// Need to avoid both String.split and regular expressions, in order to compile against JCL Foundation (bug 80053).
				// Also need to do this in an NL-sensitive way.  The use of BreakIterator was suggested in bug 90579.  
				BreakIterator iter = BreakIterator.getWordInstance();
				iter.setText(text);
				int i = iter.first(); 
				while (i != java.text.BreakIterator.DONE && i < text.length()) {
					int j = iter.following(i);
					if (j == java.text.BreakIterator.DONE)
						j = text.length();
					if (Character.isLetterOrDigit(text.charAt(i))) {
						String word = text.substring(i, j);
						if (match(word))
							return true;
					}
					i = j;
				}				
				
				if (filter(viewer, element, children).length > 0)
					return true;
				
				if(node instanceof WorkbenchPreferenceExtensionNode){
					WorkbenchPreferenceExtensionNode workbenchNode =
						(WorkbenchPreferenceExtensionNode) node;
					
					Collection keywordCollection = (Collection) keywordCache
							.get(node);
					if (keywordCollection == null) {
						keywordCollection = workbenchNode.getKeywordLabels();
						keywordCache.put(node, keywordCollection);
					}
					if(keywordCollection.isEmpty())
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
		filteredTree.setInitialText(WorkbenchMessages.WorkbenchPreferenceDialog_FilterMessage);

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
		Composite leftArea = new Composite(parent, getTreeAreaStyle());
		leftArea.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

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
	 * Get the style bits for the tree area.
	 * @return int
	 */
	public int getTreeAreaStyle() {
		return SWT.NONE;
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
		if(workingCopyManager != null)
			try {
				workingCopyManager.applyChanges();
			} catch (BackingStoreException e) {
				IStatus errorStatus =
					new Status(
							IStatus.ERROR,
							WorkbenchPlugin.PI_WORKBENCH,
							IStatus.ERROR,
							WorkbenchMessages.FilteredPreferenceDialog_PreferenceSaveFailed,
							e);
				ErrorDialog.openError(
						getShell(),
						WorkbenchMessages.PreferencesExportDialog_ErrorDialogTitle,
						WorkbenchMessages.FilteredPreferenceDialog_PreferenceSaveFailed,
						errorStatus);
			}
			
	   //Run the update jobs
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

}
