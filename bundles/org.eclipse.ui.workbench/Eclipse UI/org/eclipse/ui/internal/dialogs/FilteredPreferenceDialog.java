/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.GenericListItem;
import org.eclipse.jface.viewers.GenericListViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.activities.WorkbenchActivityHelper;

import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Baseclass for preference dialogs that will show two tabs of preferences -
 * filtered and unfiltered.
 * 
 * @since 3.0
 */
public abstract class FilteredPreferenceDialog extends PreferenceDialog {

	private GenericListViewer genericListViewer;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		if (getGroups().length > 0)
			return createCategorizedDialogArea(parent);
		return super.createDialogArea(parent);
	}

	/**
	 * Create the dialog area with a spot for the categories
	 * @param parent
	 * @return Control
	 */
	private Control createCategorizedDialogArea(Composite parent) {
		//		 create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		genericListViewer = createListViewer(composite);

		GridData listData = new GridData(GridData.FILL_HORIZONTAL);
		genericListViewer.getControl().setLayoutData(listData);

		super.createDialogArea(composite);

		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Create a generic list viewer for entry selection.
	 * 
	 * @param composite
	 * @return GenericListViewer
	 */
	private GenericListViewer createListViewer(Composite composite) {

		final ILabelProvider labelProvider = getCategoryLabelProvider();

		final GenericListViewer listViewer = new GenericListViewer(composite, SWT.BORDER) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.GenericListViewer#createListItem(java.lang.Object,
			 *      org.eclipse.swt.graphics.Color,
			 *      org.eclipse.jface.viewers.GenericListViewer)
			 */
			public GenericListItem createListItem(Object element, Color color,
					GenericListViewer viewer) {
				PreferencesCategoryItem item = new PreferencesCategoryItem(element, labelProvider);
				item.createControl(viewer.getComposite(), color);
				return item;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.GenericListViewer#itemSelected(org.eclipse.jface.viewers.GenericListItem)
			 */
			protected void itemSelected(GenericListItem item) {
				getTreeViewer().setInput(item.getElement());
				

			}

		};

		listViewer.getControl().setBackground(
				composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		// Register help listener on the tree to use context sensitive help
		listViewer.getControl().addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent event) {
				// call perform help on the current page
				if (getCurrentPage() != null) {
					getCurrentPage().performHelp();
				}
			}
		});
		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(getCategoryContentProvider());

		listViewer.setInput(getPreferenceManager());
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = getLastRightWidth();
		gd.verticalSpan = 1;
		listViewer.getControl().setLayoutData(gd);

		return listViewer;
	}

	/**
	 * Return the label provider for the categories.
	 * @return ILabelProvider
	 */
	private ILabelProvider getCategoryLabelProvider() {
		return new LabelProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
			 */
			public Image getImage(Object element) {
				return ((WorkbenchPreferenceGroup) element).getImage();
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((WorkbenchPreferenceGroup) element).getName();
			}
		};
	}

	/**
	 * Return a content provider for the categories.
	 * 
	 * @return IContentProvider
	 */
	private IContentProvider getCategoryContentProvider() {
		return new ListContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.dialogs.ListContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object input) {
				return getGroups();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createTreeViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		TreeViewer tree = super.createTreeViewer(parent);

		if (hasGroups()) {
			tree.setContentProvider(new GroupedPreferenceContentProvider());
			tree.setLabelProvider(new GroupedPreferenceLabelProvider());

		} else {
			tree.setContentProvider(new FilteredPreferenceContentProvider());
			tree.setLabelProvider(new PreferenceLabelProvider());
		}
		return tree;
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
	 * @see org.eclipse.jface.preference.PreferenceDialog#selectSavedItem()
	 */
	protected void selectSavedItem() {
		if (hasGroups())
			return;
		super.selectSavedItem();
	}

	/**
	 * Return the categories in the receiver.
	 * @return WorkbenchPreferenceGroup[]
	 */
	private WorkbenchPreferenceGroup[] getGroups() {
		return ((WorkbenchPreferenceManager) WorkbenchPlugin.getDefault().getPreferenceManager())
				.getGroups();
	}
	
	

	/**
	 * Return whether or not there are categories.
	 * @return boolean
	 */
	private boolean hasGroups() {
		return getGroups().length > 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
	 */
	public void updateMessage() {
		if(getCurrentPage() != null)
			super.updateMessage();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
		if(getCurrentPage() != null)
			super.updateTitle();
	}
}