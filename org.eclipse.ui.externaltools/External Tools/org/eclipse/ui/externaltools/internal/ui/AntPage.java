package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;

import org.eclipse.jface.dialogs.Dialog;
import java.util.List;

/**
 * Abstract superclass for all tabs that contribute to the ant classpath
 * preference page.
 */
public abstract class AntPage extends Object {
	SelectionAdapter selectionAdapter = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			buttonPressed(((Integer) e.widget.getData()).intValue());
		}
	};
	protected FontMetrics fontMetrics;
	protected TableViewer tableViewer;
	protected AntPageContentProvider contentProvider;

	/**
	 * This is where subclasses should add the buttons that are interesting for their page.
	 */
	protected abstract void addButtonsToButtonGroup(Composite parent);
	/**
	 * Should be overwritten by subclasses to handle button behaviour.
	 */
	protected void buttonPressed(int buttonId) {
	}

	/**
	 * Creates and returns a button with appropriate size and layout.
	 * @param parent
	 * @param labelKey The button text key, used to fetch the appropriate
	 * message from the externalized catalog.
	 */
	protected Button createButton(
		Composite parent,
		String labelKey,
		int buttonId) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(ToolMessages.getString(labelKey));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint =
			Dialog.convertHorizontalDLUsToPixels(
				fontMetrics,
				IDialogConstants.BUTTON_WIDTH);
		data.widthHint =
			Math.max(
				widthHint,
				button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		data.heightHint =
			Dialog.convertVerticalDLUsToPixels(
				fontMetrics,
				IDialogConstants.BUTTON_HEIGHT);
		button.setLayoutData(data);
		button.setData(new Integer(buttonId));
		button.addSelectionListener(selectionAdapter);
		return button;
	}
	protected void createButtonGroup(Composite top) {
		Composite buttonGroup = new Composite(top, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonGroup.setLayout(layout);
		buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		addButtonsToButtonGroup(buttonGroup);
	}

	protected Label createSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.NONE);
		separator.setVisible(false);
		GridData gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.verticalAlignment = gd.BEGINNING;
		gd.heightHint = 4;
		separator.setLayoutData(gd);
		return separator;
	}
	protected void createTable(Composite parent) {
		Table table =
			new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = new TableViewer(table);
		contentProvider = new AntPageContentProvider();
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(new AntPageLabelProvider());
		tableViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				tableSelectionChanged(
					(IStructuredSelection) event.getSelection());
			}
		});
	}

	/**
	 * Returns the currently listed objects in the table.  Returns null
	 * if this widget has not yet been created or has been disposed.
	 */
	public List getContents() {
		if (tableViewer == null || tableViewer.getControl().isDisposed())
			return null;
		Object[] elements = contentProvider.getElements(tableViewer.getInput());
		return Arrays.asList(elements);
	}
	protected void removeButtonPressed() {
		IStructuredSelection selection =
			(IStructuredSelection) tableViewer.getSelection();
		Object[] selected = selection.toArray();
		for (int i = 0; i < selected.length; i++) {
			contentProvider.remove(selected[i]);
		}
	}
	/**
	 * Sets the contents of the table on this page.  Has no effect
	 * if this widget has not yet been created or has been disposed.
	 */
	public void setInput(List inputs) {
		if (tableViewer == null || tableViewer.getControl().isDisposed())
			return;
		tableViewer.setInput(inputs);
		tableSelectionChanged(
			(IStructuredSelection) tableViewer.getSelection());
	}
	/**
	 * Subclasses may override to add behaviour when table selection changes.
	 */
	protected void tableSelectionChanged(IStructuredSelection newSelection) {
	}

	/**
	 * Creates and returns a control that contains this widget group.
	 */
	public Control createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		top.setLayout(layout);

		//get font metrics for DLU -> pixel conversions
		GC gc = new GC(top);
		gc.setFont(top.getFont());
		fontMetrics = gc.getFontMetrics();
		gc.dispose();

		createTable(top);
		createButtonGroup(top);
		return top;
	}

}