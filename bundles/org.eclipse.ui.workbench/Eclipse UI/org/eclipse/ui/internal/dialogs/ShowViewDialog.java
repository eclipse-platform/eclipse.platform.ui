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
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.activities.ws.ActivityMessages;
import org.eclipse.ui.internal.registry.Category;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;
import org.eclipse.ui.internal.registry.ViewRegistry;

/**
 * The Show View dialog.
 */
public class ShowViewDialog
	extends org.eclipse.jface.dialogs.Dialog
	implements ISelectionChangedListener, IDoubleClickListener {

	private static final String DIALOG_SETTING_SECTION_NAME = "ShowViewDialog"; //$NON-NLS-1$
	private static final int LIST_HEIGHT = 300;
	private static final int LIST_WIDTH = 250;
	private static final String STORE_EXPANDED_FILTERED_CATEGORIES_ID = DIALOG_SETTING_SECTION_NAME + ".STORE_EXPANDED_FILTERED_CATEGORIES_ID"; //$NON-NLS-1$
	private static final String STORE_EXPANDED_UNFILTERED_CATEGORIES_ID = DIALOG_SETTING_SECTION_NAME + ".STORE_EXPANDED_UNFILTERED_CATEGORIES_ID"; //$NON-NLS-1$
	private static final String UNFILTERED_TAB_SELECTED = DIALOG_SETTING_SECTION_NAME + ".UNFILTERED_TAB_SELECTED"; //$NON-NLS-1$    
	private TreeViewer filteredTree, unfilteredTree;
	private Button okButton;
	private TabFolder tabFolder;
	private IViewDescriptor[] viewDescs = new IViewDescriptor[0];
	private IViewRegistry viewReg;

	/**
	 * Constructs a new ShowViewDialog.
	 */
	public ShowViewDialog(Shell parentShell, IViewRegistry viewReg) {
		super(parentShell);
		this.viewReg = viewReg;
	}

	/**
	 * This method is called if a button has been pressed.
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID)
			saveWidgetValues();
		super.buttonPressed(buttonId);
	}

	/**
	 * Notifies that the cancel button of this dialog has been pressed.
	 */
	protected void cancelPressed() {
		viewDescs = new IViewDescriptor[0];
		super.cancelPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(WorkbenchMessages.getString("ShowView.shellTitle")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(shell, IHelpContextIds.SHOW_VIEW_DIALOG);
	}

	/**
	 * Adds buttons to this dialog's button bar.
	 * <p>
	 * The default implementation of this framework method adds standard ok and
	 * cancel buttons using the <code>createButton</code> framework method.
	 * Subclasses may override.
	 * </p>
	 * 
	 * @param parent
	 *            the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		okButton =
			createButton(
				parent,
				IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL,
				true);
		createButton(
			parent,
			IDialogConstants.CANCEL_ID,
			IDialogConstants.CANCEL_LABEL,
			false);
	}

	/**
	 * Creates and returns the contents of the upper part of this dialog (above
	 * the button bar).
	 * 
	 * @param the
	 *            parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {
		// Run super.
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setFont(parent.getFont());

		if (WorkbenchActivityHelper.isFiltering()) {
			tabFolder = new TabFolder(composite, SWT.NONE);
			tabFolder.setFont(parent.getFont());

			layoutTopControl(tabFolder);

			// Add filtered view list.
			filteredTree = createViewer(tabFolder, true);
			unfilteredTree = createViewer(tabFolder, false);

		} else {
			unfilteredTree = createViewer(composite, false);
			layoutTopControl(unfilteredTree.getControl());
		}

		// Restore the last state
		restoreWidgetValues();

		// Return results.
		return composite;
	}

	/**
	 * Layout the top control.
	 * 
	 * @param control
	 *            the control.
	 */
	private void layoutTopControl(Control control) {
		GridData spec = new GridData(GridData.FILL_BOTH);
		spec.widthHint = LIST_WIDTH;
		spec.heightHint = LIST_HEIGHT;
		control.setLayoutData(spec);
	}

	/**
	 * Create a new viewer in the parent.
	 * 
	 * @param parent
	 *            the parent <code>Composite</code>.
	 * @param filtering
	 *            whether the viewer should be filtering based on activities.
	 * @return <code>TreeViewer</code>
	 */
	private TreeViewer createViewer(Composite parent, boolean filtering) {
		TreeViewer tree =
			new TreeViewer(
				parent,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tree.setLabelProvider(new ViewLabelProvider());
		tree.setContentProvider(new ViewContentProvider(filtering));
		tree.setSorter(new ViewSorter((ViewRegistry) viewReg));
		tree.setInput(viewReg);
		tree.addSelectionChangedListener(this);
		tree.addDoubleClickListener(this);
		tree.getTree().setFont(parent.getFont());

		if (parent instanceof TabFolder) {
			TabItem tabItem = new TabItem((TabFolder) parent, SWT.NONE);
			tabItem.setControl(tree.getControl());
			tabItem.setText(filtering ? ActivityMessages.getString("ActivityFiltering.filtered") //$NON-NLS-1$
			: ActivityMessages.getString("ActivityFiltering.unfiltered")); //$NON-NLS-1$
		}

		return tree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		TreeViewer tree = null;
		if (event.getViewer() instanceof TreeViewer)
			tree = (TreeViewer) event.getViewer();

		if (tree == null)
			return;

		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object element = s.getFirstElement();
		if (tree.isExpandable(element)) {
			tree.setExpandedState(element, !tree.getExpandedState(element));
		} else if (viewDescs.length > 0) {
			saveWidgetValues();
			setReturnCode(OK);
			close();
		}
	}

	/**
	 * Expand categories for a given <code>TreeViewer</code>
	 * 
	 * @param expandedCategoryIds
	 *            the categories to expand
	 * @param tree
	 *            the <code>TreeViewer</code> to expand
	 */
	private void expandTree(String[] expandedCategoryIds, TreeViewer tree) {
		if (expandedCategoryIds == null)
			return;

		ViewRegistry reg = (ViewRegistry) viewReg;
		ArrayList categoriesToExpand =
			new ArrayList(expandedCategoryIds.length);
		for (int i = 0; i < expandedCategoryIds.length; i++) {
			Category category = reg.findCategory(expandedCategoryIds[i]);
			if (category != null) // ie.- it still exists
				categoriesToExpand.add(category);
		}

		if (!categoriesToExpand.isEmpty())
			tree.setExpandedElements(categoriesToExpand.toArray());
	}

	/**
	 * Return the dialog store to cache values into
	 */
	protected IDialogSettings getDialogSettings() {
		IDialogSettings workbenchSettings =
			WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings section =
			workbenchSettings.getSection(DIALOG_SETTING_SECTION_NAME);
		if (section == null)
			section =
				workbenchSettings.addNewSection(DIALOG_SETTING_SECTION_NAME);
		return section;
	}

	/**
	 * Returns the descriptors for the selected views.
	 */
	public IViewDescriptor[] getSelection() {
		return viewDescs;
	}

	/**
	 * Use the dialog store to restore widget values to the values that they
	 * held last time this dialog was used to completion.
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();

		if (WorkbenchActivityHelper.isFiltering()) {
			expandTree(
				settings.getArray(STORE_EXPANDED_FILTERED_CATEGORIES_ID),
				filteredTree);
		}

		expandTree(
			settings.getArray(STORE_EXPANDED_UNFILTERED_CATEGORIES_ID),
			unfilteredTree);
		if (WorkbenchActivityHelper.isFiltering()
			&& settings.getBoolean(UNFILTERED_TAB_SELECTED)) {
			tabFolder.setSelection(1);
		}
	}

	/**
	 * Save the expanded settings for the given <code>TreeViewer</code> into
	 * the given <code>String</code> key.
	 * 
	 * @param settings
	 *            the <code>IDialogSettings</code> to set against
	 * @param tree
	 *            the <code>TreeViewer</code> to preserve
	 * @param key
	 *            the key to use in the <code>IDialogSettings</code>
	 */
	private void saveExpanded(
		IDialogSettings settings,
		TreeViewer tree,
		String key) {
		// Collect the ids of the all expanded categories
		Object[] expandedElements = tree.getExpandedElements();
		String[] expandedCategoryIds = new String[expandedElements.length];
		for (int i = 0; i < expandedElements.length; ++i)
			expandedCategoryIds[i] = ((Category) expandedElements[i]).getId();

		// Save them for next time.
		settings.put(key, expandedCategoryIds);
	}

	/**
	 * Since OK was pressed, write widget values to the dialog store so that
	 * they will persist into the next invocation of this dialog
	 */
	protected void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();

		if (WorkbenchActivityHelper.isFiltering()) {
			saveExpanded(
				settings,
				filteredTree,
				STORE_EXPANDED_FILTERED_CATEGORIES_ID);
		}
		saveExpanded(
			settings,
			unfilteredTree,
			STORE_EXPANDED_UNFILTERED_CATEGORIES_ID);
		if (WorkbenchActivityHelper.isFiltering()) {
			settings.put(
				UNFILTERED_TAB_SELECTED,
				tabFolder.getSelectionIndex() == 1 ? true : false);
		}
	}

	/**
	 * Notifies that the selection has changed.
	 * 
	 * @param event
	 *            event object describing the change
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		updateSelection(event);
		updateButtons();
	}

	/**
	 * Update the button enablement state.
	 */
	protected void updateButtons() {
		okButton.setEnabled(getSelection() != null);
	}

	/**
	 * Update the selection object.
	 */
	protected void updateSelection(SelectionChangedEvent event) {
		TreeViewer tree = null;
		if (event.getSelectionProvider() instanceof TreeViewer)
			tree = (TreeViewer) event.getSelectionProvider();

		if (tree == null)
			return;

		ArrayList descs = new ArrayList();
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		for (Iterator i = sel.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof IViewDescriptor) {
				descs.add(o);
			}
		}
		viewDescs = new IViewDescriptor[descs.size()];
		descs.toArray(viewDescs);
	}
}
