/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal.dialogs;

import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.model.WorkbenchViewerSorter;

public class WorkingSetSelectionDialog extends SelectionDialog {

	private static class WorkingSetLabelProvider extends LabelProvider {
		public String getText(Object workingSet) {
			Assert.isTrue(workingSet instanceof IWorkingSet);
			return ((IWorkingSet) workingSet).getName();
		}

		//		public Image getImage(Object workingSet) {
		//			return null;
		//		}
	}

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;

	// Providers for populating this dialog
	private ILabelProvider labelProvider;
	private IStructuredContentProvider contentProvider;

	// The visual selection widget group
	private TableViewer listViewer;

	// Modify buttons
	private Button newButton;
	private Button detailsButton;
	private Button removeButton;

	private IWorkingSet result;

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
	 * Add the modify buttons to the dialog.
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
				WorkingSetRegistry registry = getWorkingSetRegistry();
				if (registry != null) {
					// TODO - determine which type of dialog to open
					IWorkingSetDialog dlg = registry.getWorkingSetDialog(WorkingSet.class);

					if (dlg != null) {
						dlg.init(getShell());
						if (dlg.open() == IWorkingSetDialog.OK) {
							IWorkingSet workingSet = dlg.getWorkingSet();
							listViewer.add(workingSet);
							listViewer.setSelection(new StructuredSelection(workingSet), true);
							registry.add(workingSet);
						}
					}
				}
			}
		});

		detailsButton = createButton(buttonComposite, id++, WorkbenchMessages.getString("WorkingSetSelectionDialog.detailsButton.label"), false);//$NON-NLS-1$
		detailsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				WorkingSetRegistry registry = getWorkingSetRegistry();
				if (registry != null) {
					IWorkingSet workingSet = getWorkingSet();
					IWorkingSetDialog dlg = registry.getWorkingSetDialog(workingSet.getClass());
					
					if (dlg != null) {
						dlg.init(getShell(), workingSet);
						if (dlg.open() == IWorkingSetDialog.OK) {
							listViewer.update(dlg.getWorkingSet(), null);
						}
					}
				}
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
	 * Visually checks the previously-specified elements in this dialog's list 
	 * viewer.
	 */
	private void checkInitialSelections() {
		listViewer.setSelection(new StructuredSelection(getInitialSelections()), true);
	}

	/* 
	 * Overrides method from Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		// XXX: Needs help
		//		WorkbenchHelp.setHelp(shell, new Object[] {IHelpContextIds.LIST_SELECTION_DIALOG});
	}

	/*
	 * Overrides method from Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
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
		initializeViewer();

		return composite;
	}

	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		if (getInitialSelections() != null && !getInitialSelections().isEmpty())
			checkInitialSelections();
		updateButtonAvailability();

		return control;
	}
	private WorkingSetRegistry getWorkingSetRegistry() {
		IWorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();

		if (registry instanceof WorkingSetRegistry) {
			return (WorkingSetRegistry) registry;
		}
		return null;
	}
	/**
	 * Initializes this dialog's viewer after it has been laid out.
	 */
	private void initializeViewer() {
		listViewer.setInput(Arrays.asList(WorkbenchPlugin.getDefault().getWorkingSetRegistry().getWorkingSets()));
	}

	/*
	 * Overrides method from Dialog
	 */
	protected void okPressed() {
		List newResult = new ArrayList(1);
		result = getSelectedWorkingSet();
		if (result != null)
			newResult.add(result);
		setResult(newResult);
		super.okPressed();
	}

	private void handleSelectionChanged() {
		updateButtonAvailability();
		result = getSelectedWorkingSet();
	}

	private void updateButtonAvailability() {
		ISelection selection = listViewer.getSelection();
		boolean hasSelection = selection != null && !selection.isEmpty();
		removeButton.setEnabled(hasSelection);
		boolean hasSingleSelection = hasSelection;
		if (hasSelection && selection instanceof IStructuredSelection)
			hasSingleSelection = ((IStructuredSelection) selection).size() == 1;
		detailsButton.setEnabled(hasSingleSelection);
		getOkButton().setEnabled(hasSingleSelection || !hasSelection);
	}

	private void removeSelectedWorkingSets() {
		ISelection selection = listViewer.getSelection();
		WorkingSetRegistry registry = getWorkingSetRegistry();
			
		if (selection instanceof IStructuredSelection && registry != null) {
			Iterator iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				registry.remove(((IWorkingSet) iter.next()));
			}
			listViewer.remove(((IStructuredSelection) selection).toArray());
		}
	}

	/**
	 * Returns the selected working set, or <code>null</code> if
	 * the selection was canceled.
	 *
	 * @return the selected <code>IWorkingSet</code> or <code>null</code> if Cancel was pressed
	 */
	private IWorkingSet getWorkingSet() {
		return result;
	}

	private IWorkingSet getSelectedWorkingSet() {
		ISelection selection = listViewer.getSelection();
		if (selection instanceof IStructuredSelection)
			return (IWorkingSet) ((IStructuredSelection) selection).getFirstElement();
		return null;
	}
}