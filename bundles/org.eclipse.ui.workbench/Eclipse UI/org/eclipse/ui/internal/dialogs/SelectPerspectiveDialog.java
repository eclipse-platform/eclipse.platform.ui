/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *      IBM Corporation - initial API and implementation 
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *      activated and used by other components.
************************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.activities.ws.ActivityMessages;
import org.eclipse.ui.model.PerspectiveLabelProvider;

/**
 * A dialog for perspective creation
 */
public class SelectPerspectiveDialog
	extends org.eclipse.jface.dialogs.Dialog
	implements ISelectionChangedListener {

	final private static int LIST_HEIGHT = 300;
	final private static int LIST_WIDTH = 200;
	private TableViewer filteredList, unfilteredList;
	private Button okButton;
	private IPerspectiveDescriptor perspDesc;
	private IPerspectiveRegistry perspReg;

	/**
	 * PerspectiveDialog constructor comment.
	 */
	public SelectPerspectiveDialog(
		Shell parentShell,
		IPerspectiveRegistry perspReg) {
		super(parentShell);
		this.perspReg = perspReg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		perspDesc = null;
		super.cancelPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(WorkbenchMessages.getString("SelectPerspective.shellTitle")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(shell, IHelpContextIds.SELECT_PERSPECTIVE_DIALOG);
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
			TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
			tabFolder.setFont(parent.getFont());
			layoutTopControl(tabFolder);

			filteredList = createViewer(tabFolder, true);
			unfilteredList = createViewer(tabFolder, false);

		} else {
			unfilteredList = createViewer(composite, false);
			layoutTopControl(unfilteredList.getControl());
		}

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
	 * @return <code>TableViewer</code>
	 */
	private TableViewer createViewer(Composite parent, boolean filtering) {
		// Add perspective list.
		TableViewer list =
			new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		list.getTable().setFont(parent.getFont());
		list.setLabelProvider(new PerspectiveLabelProvider());
		list.setContentProvider(new PerspContentProvider(filtering));
		list.setSorter(new ViewerSorter());
		list.setInput(perspReg);
		list.addSelectionChangedListener(this);
		list.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClickEvent();
			}
		});

		if (parent instanceof TabFolder) {
			TabItem tabItem = new TabItem((TabFolder) parent, SWT.NONE);
			tabItem.setControl(list.getControl());
			tabItem.setText(filtering ? ActivityMessages.getString("ActivityFiltering.filtered") //$NON-NLS-1$
			: ActivityMessages.getString("ActivityFiltering.unfiltered")); //$NON-NLS-1$
		}
		return list;
	}

	/**
	 * Returns the current selection.
	 */
	public IPerspectiveDescriptor getSelection() {
		return perspDesc;
	}

	/**
	 * Handle a double click event on the list
	 */
	protected void handleDoubleClickEvent() {
		okPressed();
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
		perspDesc = null;
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		if (!sel.isEmpty()) {
			Object obj = sel.getFirstElement();
			if (obj instanceof IPerspectiveDescriptor)
				perspDesc = (IPerspectiveDescriptor) obj;
		}
	}
}
