package org.eclipse.ui.externaltools.internal.ant.preferences;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Provides the generic implementation for a sub-page in the
 * Ant preference page.
 */
public abstract class AntPage {
	private SelectionAdapter selectionAdapter = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			buttonPressed(((Integer) e.widget.getData()).intValue());
		}
	};
	private AntPreferencePage preferencePage;
	private TableViewer tableViewer;
	private AntPageContentProvider contentProvider;
	
	protected Button editButton;
	protected Button removeButton;

	/**
	 * Creates an instance of this page.
	 */
	public AntPage(AntPreferencePage preferencePage) {
		super();
		this.preferencePage = preferencePage;
	}
	
	/**
	 * Adds buttons specific to the page.
	 */
	protected abstract void addButtonsToButtonGroup(Composite parent);
	
	/**
	 * Adds an object to the contents
	 */
	protected void addContent(Object o) {
		if (contentProvider != null) {
			contentProvider.add(o);
		}
	}
	
	/**
	 * Handles a button pressed event.
	 */
	protected void buttonPressed(int buttonId) {
	}

	/**
	 * Creates and returns a button with appropriate size and layout.
	 * 
	 * @param parent the control to create the button on
	 * @param labelKey the key to lookup the button's label
	 * @param buttonId the id to assign to this button
	 * @return a new and initialized button
	 */
	protected Button createButton(Composite parent, String labelKey, int buttonId) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(AntPreferencesMessages.getString(labelKey));
		button.setData(new Integer(buttonId));
		button.addSelectionListener(selectionAdapter);
		preferencePage.setButtonGridData(button);
		return button;
	}
	
	/**
	 * Creates the group which will contain the buttons.
	 */
	protected void createButtonGroup(Composite top) {
		Composite buttonGroup = new Composite(top, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonGroup.setLayout(layout);
		buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		addButtonsToButtonGroup(buttonGroup);
	}

	/**
	 * Creates a space between controls
	 */
	protected final Label createSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.heightHint = 4;
		separator.setLayoutData(gd);
		return separator;
	}
	
	/**
	 * Creates the table viewer.
	 */
	protected void createTable(Composite parent) {
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		table.setLayoutData(data);
		contentProvider = getContentProvider();
		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(getLabelProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				tableSelectionChanged((IStructuredSelection) event.getSelection());
			}
		});
		
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				edit((IStructuredSelection)event.getSelection());
			}
		});
	}

	/**
	 * Returns the content provider to use for the table viewer
	 * 
	 * @return AntPageContentProvider
	 */
	protected AntPageContentProvider getContentProvider() {
		return new AntPageContentProvider();
	}

	/**
	 * Returns the currently listed objects in the table.  Returns null
	 * if this widget has not yet been created or has been disposed.
	 */
	public List getContents() {
		if (tableViewer == null || tableViewer.getControl().isDisposed()) {
			return null;
		}
		Object[] elements = contentProvider.getElements(tableViewer.getInput());
		List contents= new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			contents.add(elements[i]);
		}
		return contents;
	}
	
	/**
	 * Returns the label provider the sub-page wants to use
	 * to display its content with.
	 */
	protected abstract ITableLabelProvider getLabelProvider();
	
	/**
	 * Returns the selection in the viewer, or <code>null</code> if none.
	 */
	protected final IStructuredSelection getSelection() {
		if (tableViewer == null || tableViewer.getControl().isDisposed()) {
			return null;
		}
		return ((IStructuredSelection) tableViewer.getSelection());
	}
	
	/**
	 * Returns the shell of the sub-page.
	 */
	protected final Shell getShell() {
		if (tableViewer == null || tableViewer.getControl().isDisposed())
			return null;
		return tableViewer.getControl().getShell();
	}
	
	/**
	 * Handles the remove button pressed event
	 */
	protected void removeButtonPressed() {
		IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
		Iterator enum = sel.iterator();
		while (enum.hasNext()) {
			contentProvider.remove(enum.next());
		}
	}
	
	/**
	 * Sets the contents of the table on this page.  Has no effect
	 * if this widget has not yet been created or has been disposed.
	 */
	public void setInput(List inputs) {
		if (tableViewer == null || tableViewer.getControl().isDisposed()) {
			return;
		}
		tableViewer.setInput(inputs);
		tableSelectionChanged((IStructuredSelection) tableViewer.getSelection());
	}

	/**
	 * Updates the content element in the table viewer.
	 */
	protected final void updateContent(Object element) {
		if (tableViewer == null || tableViewer.getControl().isDisposed()) {
			return;
		}
		tableViewer.update(element, null);
	}
	
	/**
	 * Creates this page's controls
	 */
	public Control createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		top.setLayout(layout);

		createTable(top);
		createButtonGroup(top);
		
		return top;
	}


	/**
	 * Content provider that maintains a generic list of objects which
	 * are shown in a table viewer.
	 */
	protected static class AntPageContentProvider implements IStructuredContentProvider {
		protected List elements = new ArrayList();
		protected TableViewer viewer;
	
		public void add(Object o) {
			if (elements.contains(o)) {
				return;
			}
			elements.add(o);
			viewer.add(o);
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object inputElement) {
			return (Object[]) elements.toArray(new Object[elements.size()]);
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.viewer = (TableViewer) viewer;
			elements.clear();
			if (newInput != null) {
				elements.addAll((List) newInput);
			}
		}
		
		public void remove(Object o) {
			elements.remove(o);
			viewer.remove(o);
		}
	}
	
	protected AntPreferencePage getPreferencePage() {
		return preferencePage;
	}
	
	protected TableViewer getTableViewer() {
		return tableViewer;
	}

	/**
	 * Handles selection changes in the table viewer.
	 */
	protected void tableSelectionChanged(IStructuredSelection newSelection) {
		int size = newSelection.size();
		editButton.setEnabled(size == 1);
		removeButton.setEnabled(size > 0);
	}
	
	protected void edit(IStructuredSelection selection) {
	}
}