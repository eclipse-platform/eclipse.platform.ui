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
package org.eclipse.ant.internal.ui.preferences;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.internal.core.AntObject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Provides the generic implementation for a sub-page in the
 * Ant preference page.
 */
public abstract class AntPage {
	protected SelectionAdapter selectionAdapter = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			buttonPressed(((Integer) e.widget.getData()).intValue());
		}
	};
	private AntRuntimePreferencePage preferencePage;
	private TableViewer tableViewer;
	private AntContentProvider contentProvider;
	
	protected Button editButton;
	protected Button removeButton;

	/**
	 * Creates an instance of this page.
	 */
	public AntPage(AntRuntimePreferencePage preferencePage) {
		super();
		this.preferencePage = preferencePage;
	}
	
	/**
	 * Adds buttons specific to the page.
	 */
	protected abstract void addButtonsToButtonGroup(Composite parent);
	
	/**
	 * Give this page a chance to initialize itself
	 */
	protected abstract void initialize();
	
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
	protected abstract void buttonPressed(int buttonId);

	/**
	 * Creates and returns a button with appropriate size and layout.
	 * 
	 * @param parent the control to create the button on
	 * @param labelKey the key to lookup the button's label
	 * @param buttonId the id to assign to this button
	 * @return a new and initialized button
	 */
	protected Button createPushButton(Composite parent, String buttonText, int buttonId) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(buttonText);
		button.setData(new Integer(buttonId));
		button.addSelectionListener(selectionAdapter);
		preferencePage.setButtonLayoutData(button);
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
		buttonGroup.setFont(top.getFont());
		
		addButtonsToButtonGroup(buttonGroup);
	}
	
	/**
	 * Creates the table viewer.
	 */
	protected void createTable(Composite parent) {
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = table.getItemHeight();
		data.horizontalSpan= 1;
		table.setLayoutData(data);
		table.setFont(parent.getFont());
		
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
				if (!event.getSelection().isEmpty() && editButton.isEnabled()) {
					edit((IStructuredSelection)event.getSelection());
				}
			}
		});
		
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (editButton.isEnabled() && event.character == SWT.DEL && event.stateMask == 0) {
					remove(tableViewer);
				}
			}
		});	
	}

	/**
	 * Returns the content provider to use for the table viewer
	 * 
	 * @return AntPageContentProvider
	 */
	protected AntContentProvider getContentProvider() {
		return new AntContentProvider();
	}

	/**
	 * Returns the currently listed objects in the table if the library
	 * for that entry is still included in the preferences.  Default objects
	 * are included depending on the value of the <code>forDisplay</code> parameter.
	 * Returns <code>null</code> if this widget has not yet been created
	 * or has been disposed.
	 * @param forDisplay Whether the result is to be displayed in the UI or stored in the preferences
	 * @return The list of objects currently displayed in the table
	 */
	protected List getContents(boolean forDisplay) {
		if (tableViewer == null || tableViewer.getControl().isDisposed()) {
			return null;
		}
		List entries= getPreferencePage().getLibraryEntries();
		
		Object[] elements = contentProvider.getElements(tableViewer.getInput());
		List contents= new ArrayList(elements.length);
		Object element;
		AntObject antObject;
		for (int i = 0; i < elements.length; i++) {
			element= elements[i];
			if (element instanceof AntObject) {
				antObject= (AntObject)element;
				if (forDisplay) {
					if (!antObject.isDefault() && !entries.contains(antObject.getLibraryEntry())) {
						continue;
					}
				} else if (antObject.isDefault() || !entries.contains(antObject.getLibraryEntry())) {
					continue;
				}
			}
			contents.add(element);
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
		if (tableViewer == null || tableViewer.getControl().isDisposed()) {
			return null;
		}
		return tableViewer.getControl().getShell();
	}
	
	/**
	 * Handles the remove button pressed event
	 */
	protected void remove() {
		remove(tableViewer);
	}
	
	protected void remove(TableViewer viewer) {
		AntContentProvider antContentProvider= (AntContentProvider)viewer.getContentProvider();
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		Iterator itr = sel.iterator();
		while (itr.hasNext()) {
			antContentProvider.remove(itr.next());
		}
	}
	
	/**
	 * Sets the contents of the table on this page.  Has no effect
	 * if this widget has not yet been created or has been disposed.
	 */
	protected void setInput(List inputs) {
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
	 * Creates the default contents of this page
	 */
	protected Composite createContents(Composite top) {
		WorkbenchHelp.setHelp(top, getHelpContextId());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		top.setLayout(layout);
		
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		createTable(top);
		createButtonGroup(top);
		
		return top;
	}
	
	protected AntRuntimePreferencePage getPreferencePage() {
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
		boolean enabled= true;
		
		Iterator itr= newSelection.iterator();
		while (itr.hasNext()) {
			Object element = itr.next();
			if (element instanceof AntObject) {
				AntObject antObject= (AntObject)element;
				if (antObject.isDefault()) {
					enabled= false;
					break;
				}
			}
		}
		editButton.setEnabled(enabled && size == 1);
		removeButton.setEnabled(enabled && size > 0);
	}
	
	/**
	 * Allows the user to edit a custom Ant object.
	 *
	 * @param selection The selection containing the object to edit
	 */
	protected abstract void edit(IStructuredSelection selection);
	
	/**
	 * Returns this page's help context id, which is hooked
	 * to this page on creation.
	 * 
	 * @return help context id
	 */
	protected abstract String getHelpContextId();

	protected void connectToFolder(final TabItem item, TabFolder folder) {
		folder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.item == item) {
					//remove ant objects whose library has been removed
					setInput(getContents(true));
				}
			}
		});		
	}
}
