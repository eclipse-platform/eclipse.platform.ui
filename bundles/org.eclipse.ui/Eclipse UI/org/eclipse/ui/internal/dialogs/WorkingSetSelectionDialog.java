package org.eclipse.ui.internal.dialogs;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * A working set selection dialog displays a list of working
 * sets available in the workbench.
 * 
 * @see IWorkingSetSelectionDialog
 * @since 2.0
 */
public class WorkingSetSelectionDialog extends SelectionDialog implements IWorkingSetSelectionDialog {
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;

	private static class WorkingSetLabelProvider extends LabelProvider {
		public String getText(Object workingSet) {
			Assert.isTrue(workingSet instanceof IWorkingSet);
			return ((IWorkingSet) workingSet).getName();
		}
	}

	private ILabelProvider labelProvider;
	private IStructuredContentProvider contentProvider;
	private TableViewer listViewer;
	private Button newButton;
	private Button detailsButton;
	private Button removeButton;
	private IWorkingSet[] result;

	/**
	 * Creates a working set selection dialog.
	 *
	 * @param parentShell the parent shell
	 */
	public WorkingSetSelectionDialog(Shell parentShell) {
		super(parentShell);
		setTitle(WorkbenchMessages.getString("WorkingSetSelectionDialog.title")); //$NON-NLS-1$;
		contentProvider = new ListContentProvider();
		labelProvider = new WorkingSetLabelProvider();
		setMessage(WorkbenchMessages.getString("WorkingSetSelectionDialog.message")); //$NON-NLS-1$
	}
	/**
	 * Adds the modify buttons to the dialog.
	 * 
	 * @param composite Composite to add the buttons to
	 */
	private void addModifyButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);

		int id = IDialogConstants.CLIENT_ID + 1;
		newButton = createButton(buttonComposite, id++, WorkbenchMessages.getString("WorkingSetSelectionDialog.newButton.label"), false); //$NON-NLS-1$
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createWorkingSet();
			}
		});

		detailsButton = createButton(buttonComposite, id++, WorkbenchMessages.getString("WorkingSetSelectionDialog.detailsButton.label"), false); //$NON-NLS-1$
		detailsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSelectedWorkingSet();
			}
		});

		removeButton = createButton(buttonComposite, id++, WorkbenchMessages.getString("WorkingSetSelectionDialog.removeButton.label"), false); //$NON-NLS-1$
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeSelectedWorkingSets();
			}
		});
	}
	/** 
	 * Overrides method from Window.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		// TODO: Needs help
		//		WorkbenchHelp.setHelp(shell, new Object[] {IHelpContextIds.LIST_SELECTION_DIALOG});
	}
	/**
	 * Create the dialog widgets.
	 * Overrides method from Dialog.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		createMessageArea(composite);

		listViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);

		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(contentProvider);
		listViewer.setSorter(new WorkbenchViewerSorter());

		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged();
			}
		});

		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		addModifyButtons(composite);
		listViewer.setInput(Arrays.asList(WorkbenchPlugin.getDefault().getWorkingSetManager().getWorkingSets()));

		return composite;
	}
	/**
	 * Sets the initial selection, if any.
	 * Overrides method from Dialog.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		if (getInitialSelections() != null && !getInitialSelections().isEmpty()) {
			listViewer.setSelection(new StructuredSelection(getInitialSelections()), true);
		}
		updateButtonAvailability();

		return control;
	}
	/**
	 * Called when the user selects to create a new working set.
	 */
	private void createWorkingSet() {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
		IWorkingSetDialog dlg = registry.getDefaultWorkingSetDialog();

		if (dlg != null) {
			dlg.init(getShell());
			if (dlg.open() == Window.OK) {
				IWorkingSet workingSet = dlg.getSelection();
				listViewer.add(workingSet);
				listViewer.setSelection(new StructuredSelection(workingSet), true);
				manager.addWorkingSet(workingSet);
			}
		}
	}
	/**
	 * Opens a working set dialog for the currently selected working set.
	 * 
	 * @see org.eclipse.ui.IWorkingSetDialog
	 */
	private void editSelectedWorkingSet() {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		IWorkingSet workingSet = (IWorkingSet) getSelectedWorkingSets().get(0);
		IWorkingSetDialog dlg = registry.getWorkingSetDialog(workingSet);

		if (dlg != null) {
			dlg.init(getShell());
			dlg.setSelection(workingSet);
			if (dlg.open() == Window.OK) {
				listViewer.update(dlg.getSelection(), null);
			}
		}
	}
	/**
	 * Sets the selected working sets as the dialog result.
	 * Overrides method from Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		List newResult = getSelectedWorkingSets();

		result = (IWorkingSet[]) newResult.toArray(new IWorkingSet[newResult.size()]);
		setResult(newResult);
		super.okPressed();
	}
	/**
	 * Called when the selection has changed.
	 */
	private void handleSelectionChanged() {
		updateButtonAvailability();
	}
	/**
	 * Updates the modify buttons' enabled state based on the 
	 * current seleciton.
	 */
	private void updateButtonAvailability() {
		ISelection selection = listViewer.getSelection();
		boolean hasSelection = selection != null && !selection.isEmpty();
		boolean hasSingleSelection = hasSelection;

		removeButton.setEnabled(hasSelection);
		if (hasSelection && selection instanceof IStructuredSelection) {
			hasSingleSelection = ((IStructuredSelection) selection).size() == 1;
		}
		detailsButton.setEnabled(hasSingleSelection);
	}
	/**
	 * Removes the selected working sets from the workbench.
	 */
	private void removeSelectedWorkingSets() {
		ISelection selection = listViewer.getSelection();
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();

		if (selection instanceof IStructuredSelection) {
			Iterator iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				manager.removeWorkingSet(((IWorkingSet) iter.next()));
			}
			listViewer.remove(((IStructuredSelection) selection).toArray());
		}
	}
	/**
	 * Implements IWorkingSetSelectionDialog.
	 *
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#getSelection()
	 */
	public IWorkingSet[] getSelection() {
		return result;
	}
	/**
	 * Returns the selected working sets.
	 * 
	 * @return the selected working sets
	 */
	private List getSelectedWorkingSets() {
		ISelection selection = listViewer.getSelection();
		if (selection instanceof IStructuredSelection)
			return ((IStructuredSelection) selection).toList();
		return null;
	}
	/**
	 * Implements IWorkingSetSelectionDialog.
	 *
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#setSelection(IWorkingSet[])
	 */
	public void setSelection(IWorkingSet[] workingSets) {
		result = workingSets;
		setInitialSelections(workingSets);
	}
}