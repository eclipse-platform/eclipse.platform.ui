/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Dialog that shows a list of items with icon and label.
 */
public class ListDialog extends SelectionDialog {
	
	private static final int WIDTH_IN_CHARACTERS= 55;
	
	private IStructuredContentProvider fContentProvider;
	private ILabelProvider fLabelProvider;
	private Object fInput;
	private TableViewer fViewer;
	private boolean fCreateCancelButton= true;
	
	public ListDialog(Shell parent, Object input, String title, String message, IStructuredContentProvider sp, ILabelProvider lp) {
		super(parent);
		setTitle(title);
		setMessage(message);
		fInput= input;
		fContentProvider= sp;
		fLabelProvider= lp;
	}
	
	public void setCreateCancelButton(boolean value) {
		fCreateCancelButton= value;
	}
	
	/*
	 * Overrides method from Dialog
	 */
	protected Label createMessageArea(Composite composite) {
		Label label = new Label(composite,SWT.WRAP);
		label.setText(getMessage()); 
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(WIDTH_IN_CHARACTERS);
		label.setLayoutData(gd);
		return label;
	}
	
	/*
	 * Overrides method from Dialog
	 */
	protected Control createDialogArea(Composite container) {
		Composite parent= (Composite) super.createDialogArea(container);
		createMessageArea(parent);
		fViewer= new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		fViewer.setContentProvider(fContentProvider);

		final Table table= fViewer.getTable();
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				if (fCreateCancelButton)
					okPressed();
			}
		});
		fViewer.setLabelProvider(fLabelProvider);
		fViewer.setInput(fInput);
		if (getInitialSelections() != null)
			fViewer.setSelection(new StructuredSelection(getInitialSelections()));
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(15);
		gd.widthHint= convertWidthInCharsToPixels(WIDTH_IN_CHARACTERS);
		table.setLayoutData(gd);
		return table;
	}
	
	/*
	 * Overrides method from Dialog
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		if (! fCreateCancelButton)
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		else
			super.createButtonsForButtonBar(parent);	
	}	
	
	/*
	 * Overrides method from Dialog
	 */
	protected void okPressed() {
		// Build a list of selected children.
		ISelection selection= fViewer.getSelection();
		if (selection instanceof IStructuredSelection)
			setResult(((IStructuredSelection)fViewer.getSelection()).toList());
		super.okPressed();
	}
}


