/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.workingsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.WorkbenchViewerSorter;

import org.eclipse.search.ui.IWorkingSet;

import org.eclipse.search.internal.ui.util.ListContentProvider;

/**
 * @deprecated use org.eclipse.ui.IWorkingSet support - this class will be removed soon
 */
public class WorkingSetSelectionDialog extends SelectionDialog {

	private static class WorkingSetLabelProvider extends LabelProvider {
		public String getText(Object workingSet) {
			Assert.isTrue(workingSet instanceof IWorkingSet);
			return ((IWorkingSet)workingSet).getName();
		}

//		public Image getImage(Object workingSet) {
//			return null;
//		}
	}

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT= 200;
	private final static int SIZING_SELECTION_WIDGET_WIDTH= 50;

	// Providers for populating this dialog
	private ILabelProvider fLabelProvider;
	private IStructuredContentProvider fContentProvider;

	// The visual selection widget group
	private TableViewer fListViewer;

	// Modify buttons
	private Button fNewButton;
	private Button fDetailsButton;
	private Button fRemoveButton;

	private IWorkingSet fResult;
	
	/**
	 * Creates a working set selection dialog.
	 *
	 * @param parentShell the parent shell
	 */
	public WorkingSetSelectionDialog(Shell parentShell) {
		super(parentShell);
		setTitle(WorkingSetMessages.getString("WorkingSetSelectionDialog.title")); //$NON-NLS-1$;
		fContentProvider= new ListContentProvider();
		fLabelProvider= new WorkingSetLabelProvider();
		setMessage(WorkingSetMessages.getString("WorkingSetSelectionDialog.message")); //$NON-NLS-1$

	}

	/**
	 * Add the modify buttons to the dialog.
	 */
	private void addModifyButtons(Composite composite) {
	
		Composite buttonComposite= new Composite(composite, SWT.RIGHT);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		buttonComposite.setLayout(layout);
		GridData data =
			new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace= true;
		composite.setData(data);
	
		int id= IDialogConstants.CLIENT_ID + 1;
		fNewButton= createButton(buttonComposite, id++, WorkingSetMessages.getString("WorkingSetSelectionDialog.newButton.label"), false); //$NON-NLS-1$
		fNewButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				WorkingSetDialog dlg= new WorkingSetDialog(getShell());
				if (dlg.open() == dlg.OK) {
					fListViewer.add(dlg.getWorkingSet());
					fListViewer.setSelection(new StructuredSelection(dlg.getWorkingSet()), true);
					WorkingSet.add(dlg.getWorkingSet());
				}
			}
		});

		fDetailsButton= createButton(buttonComposite, id++, WorkingSetMessages.getString("WorkingSetSelectionDialog.detailsButton.label"), false); //$NON-NLS-1$
		fDetailsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				WorkingSetDialog dlg= new WorkingSetDialog(getShell(), getWorkingSet());
				if (dlg.open() == dlg.OK)
					fListViewer.update(dlg.getWorkingSet(), null);
			}
		});
	
		fRemoveButton= createButton(buttonComposite, id++, WorkingSetMessages.getString("WorkingSetSelectionDialog.removeButton.label"), false); //$NON-NLS-1$
		fRemoveButton.addSelectionListener(new SelectionAdapter() {
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
		fListViewer.setSelection(new StructuredSelection(getInitialSelections()), true);
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
		Composite composite= (Composite)super.createDialogArea(parent);
	
		createMessageArea(composite);
	
		fListViewer= new TableViewer(composite, SWT.BORDER | SWT.MULTI);
		GridData data= new GridData(GridData.FILL_BOTH);
		data.heightHint= SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint= SIZING_SELECTION_WIDGET_WIDTH;
		fListViewer.getTable().setLayoutData(data);
	
		fListViewer.setLabelProvider(fLabelProvider);
		fListViewer.setContentProvider(fContentProvider);
		fListViewer.setSorter(new WorkbenchViewerSorter());
		
		fListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged();
			}
		});

		fListViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		addModifyButtons(composite);
		initializeViewer();
		
		return composite;
	}

	protected Control createContents(Composite parent) {
		Control control= super.createContents(parent);
		if (getInitialSelections() != null && !getInitialSelections().isEmpty())
			checkInitialSelections();
		updateButtonAvailability();
		
		return control;
	}

	/**
	 * Initializes this dialog's viewer after it has been laid out.
	 */
	private void initializeViewer() {
		fListViewer.setInput(Arrays.asList(WorkingSet.getWorkingSets()));
	}

	/*
	 * Overrides method from Dialog
	 */
	protected void okPressed() {
		List result= new ArrayList(1);
		fResult= getSelectedWorkingSet();
		if (fResult != null)
			result.add(fResult);
		setResult(result);
		super.okPressed();
	}

	private void handleSelectionChanged() {
		updateButtonAvailability();
		fResult= getSelectedWorkingSet();
	}
	
	private void updateButtonAvailability() {
		ISelection selection= fListViewer.getSelection();
		boolean hasSelection= selection != null && !selection.isEmpty();
		fRemoveButton.setEnabled(hasSelection);
		boolean hasSingleSelection= hasSelection;
		if (hasSelection && selection instanceof IStructuredSelection)
			hasSingleSelection= ((IStructuredSelection)selection).size() == 1;
		fDetailsButton.setEnabled(hasSingleSelection);
		getOkButton().setEnabled(hasSingleSelection || !hasSelection);
	}

	private void removeSelectedWorkingSets() {
		ISelection selection= fListViewer.getSelection();
		if (selection instanceof  IStructuredSelection) {
			Iterator iter= ((IStructuredSelection)selection).iterator();
			while (iter.hasNext())
				WorkingSet.remove(((IWorkingSet)iter.next()));
			fListViewer.remove(((IStructuredSelection)selection).toArray());
		}
	}

	/**
	 * Returns the selected working set, or <code>null</code> if
	 * the selection was canceled.
	 *
	 * @return the selected <code>IWorkingSet</code> or <code>null</code> if Cancel was pressed
	 */
	private IWorkingSet getWorkingSet() {
		return fResult;
	}


	private IWorkingSet getSelectedWorkingSet() {
		ISelection selection= fListViewer.getSelection();
		if (selection instanceof  IStructuredSelection)
			return (IWorkingSet)((IStructuredSelection)selection).getFirstElement();
		return null;
	}
}
