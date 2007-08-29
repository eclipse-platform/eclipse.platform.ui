/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Base implementation for a simple working set dialog that doesn't contain
 * references to non-editable/non-visible working sets.
 * 
 * @since 3.4
 * 
 */
public class SimpleWorkingSetSelectionDialog extends AbstractWorkingSetDialog {

	private class Filter extends ViewerFilter {

		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			return isCompatible((IWorkingSet) element);
		}

		private boolean isCompatible(IWorkingSet set) {
			if (set.isAggregateWorkingSet())
				return false;

			// original JDT code had the catch for self-updating sets that no
			// one can explain. There doesn't seem to
			// be a good reason to exclude these sets so the clause has been
			// removed.
			
			// if (set.isAggregateWorkingSet() || !set.isSelfUpdating())
			// return false;

			if (!set.isVisible())
				return false;

			if (!set.isEditable())
				return false;

			Set workingSetTypeIds = getSupportedWorkingSetIds();
			if (workingSetTypeIds == null)
				return true;

			for (Iterator i = workingSetTypeIds.iterator(); i.hasNext();) {
				String workingSetTypeId = (String) i.next();
				if (workingSetTypeId.equals(set.getId()))
					return true;
			}

			return false;
		}
	}

	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;

	private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;

	private CheckboxTableViewer viewer;

	private IWorkingSet[] initialSelection;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param shell
	 *            the shell to parent this dialog on
	 * @param workingSetTypeIds
	 *            the types of working set IDs that will be shown in this dialog
	 * @param selectedWorkingSets
	 *            the currently selected working sets (if any)
	 * @param canEdit
	 *            whether or not this dialog will display edit controls
	 */
	public SimpleWorkingSetSelectionDialog(Shell shell,
			String[] workingSetTypeIds, IWorkingSet[] selectedWorkingSets,
			boolean canEdit) {
		super(shell, workingSetTypeIds, canEdit);
		this.initialSelection = selectedWorkingSets;
		setTitle(WorkbenchMessages.WorkingSetSelectionDialog_title_multiSelect);
		setMessage(WorkbenchMessages.WorkingSetSelectionDialog_message_multiSelect);
	}

	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = (Composite) super.createDialogArea(parent);

		Composite viewerComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		viewerComposite.setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH + 300; // fudge? I like
		// fudge.
		viewerComposite.setLayoutData(data);

		viewer = CheckboxTableViewer.newCheckList(viewerComposite, SWT.BORDER);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setLabelProvider(new WorkingSetLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.addFilter(new WorkingSetFilter(null));
		viewer.setInput(PlatformUI.getWorkbench().getWorkingSetManager()
				.getWorkingSets());
		viewer.setFilters(new ViewerFilter[] { new Filter() });

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged();
			}
		});
		viewer.setCheckedElements(initialSelection);

		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;

		viewer.getControl().setLayoutData(data);
		addModifyButtons(viewerComposite);

		addSelectionButtons(composite);

		availableWorkingSetsChanged();

		Dialog.applyDialogFont(composite);

		return composite;
	}

	protected void okPressed() {
		Object[] checked = viewer.getCheckedElements();
		IWorkingSet[] workingSets = new IWorkingSet[checked.length];
		System.arraycopy(checked, 0, workingSets, 0, checked.length);
		setSelection(workingSets);
		super.okPressed();
	}

	protected List getSelectedWorkingSets() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).toList();
		}
		return null;
	}

	protected void availableWorkingSetsChanged() {
		viewer.setInput(PlatformUI.getWorkbench().getWorkingSetManager()
				.getWorkingSets());
		super.availableWorkingSetsChanged();
	}

	/**
	 * Called when the selection has changed.
	 */
	void handleSelectionChanged() {
		updateButtonAvailability();
	}

	protected void selectAllSets() {
		viewer.setCheckedElements(PlatformUI.getWorkbench()
				.getWorkingSetManager().getWorkingSets());
		updateButtonAvailability();
	}

	protected void deselectAllSets() {
		viewer.setCheckedElements(new Object[0]);
		updateButtonAvailability();
	}

}
