/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.preferences;

 
import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.IFileTypeInfo;
import org.eclipse.team.core.Team;
import org.eclipse.team.internal.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
/**
 * This preference page displays all patterns which determine whether a resource
 * is to be treated as a text file or not. The page allows the user to add or
 * remove entries from this table, and change their values from Text to Binary.
 */
public class TextPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	// Some string constants for display purposes
	private static final String TEXT = Policy.bind("TextPreferencePage.text"); //$NON-NLS-1$
	private static final String BINARY = Policy.bind("TextPreferencePage.binary"); //$NON-NLS-1$
	
	// The input for the table viewer
	private List input;
	
	// Widgets
	private TableViewer viewer;
	private Button removeButton;
	private Button changeButton;
	
	/**
	 * TableEntry is a pair of strings representing an entry in the table
	 */
	class TableEntry {
		String ext;
		String value;
		public TableEntry(String ext, String value) {
			this.ext = ext;
			this.value = value;
		}
		public String getExtension() {
			return ext;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	/**
	 * TableLabelProvider provides labels for TableEntrys.
	 */
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			TableEntry entry = (TableEntry)element;
			switch (columnIndex) {
				case 0:
					return entry.getExtension();
				case 1:
					return entry.getValue();
				default:
					return null;
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	};
		
	/*
	 * Method declared on IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}
	/*
	 * @see PreferencePage#createControl
	 */
	protected Control createContents(Composite ancestor) {
		
		Composite parent = new Composite(ancestor, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		parent.setLayout(layout);
	
		// set F1 help
		WorkbenchHelp.setHelp(parent, IHelpContextIds.FILE_TYPE_PREFERENCE_PAGE);
				
		Label l1 = new Label(parent, SWT.NULL);
		l1.setText(Policy.bind("TextPreferencePage.description")); //$NON-NLS-1$
		GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		data.horizontalSpan = 2;
		l1.setLayoutData(data);
		
		viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Table table = viewer.getTable();
		new TableEditor(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = convertWidthInCharsToPixels(30);
		/*
		 * The hardcoded hint does not look elegant, but in reality
		 * it does not make anything bound to this 100-pixel value,
		 * because in any case the tree on the left is taller and
		 * that's what really determines the height.
		 */
		gd.heightHint = 100;
		table.setLayoutData(gd);
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleSelection();
			}
		});
		// Create the table columns
		new TableColumn(table, SWT.NULL);
		new TableColumn(table, SWT.NULL);
		TableColumn[] columns = table.getColumns();
		columns[0].setText(Policy.bind("TextPreferencePage.extension")); //$NON-NLS-1$
		columns[1].setText(Policy.bind("TextPreferencePage.contents")); //$NON-NLS-1$
		
		CellEditor editor = new ComboBoxCellEditor(table, new String[] {TEXT, BINARY});
		viewer.setCellEditors(new CellEditor[] {null, editor});
		viewer.setColumnProperties(new String[] {"extension", "contents"}); //$NON-NLS-1$ //$NON-NLS-2$
		viewer.setCellModifier(new ICellModifier() {
			public Object getValue(Object element, String property) {
				String value = ((TableEntry)element).getValue();
				if (value.equals(TEXT)) {
					return new Integer(0);
				} else {
					return new Integer(1);
				}
			}
			public boolean canModify(Object element, String property) {
				return true;
			}
			public void modify(Object element, String property, Object value) {
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				TableEntry entry = (TableEntry)selection.getFirstElement();
				if (((Integer)value).intValue() == 0) {
					entry.setValue(TEXT);
				} else {
					entry.setValue(BINARY);
				}
				viewer.refresh(entry);
			}
		});
		viewer.setLabelProvider(new TableLabelProvider());
		viewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public Object[] getElements(Object inputElement) {
				if (inputElement == null) return null;
				return ((List)inputElement).toArray();
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection == null || !(selection instanceof IStructuredSelection)) {
					return;
				}
				viewer.editElement(((IStructuredSelection)selection).getFirstElement(), 1);
			}
		});
		viewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				TableEntry entry1 = (TableEntry)e1;
				TableEntry entry2 = (TableEntry)e2;
				return super.compare(viewer, entry1.getExtension(), entry2.getExtension());
			}
		});
		TableLayout tl = new TableLayout();
		tl.addColumnData(new ColumnWeightData(50));
		tl.addColumnData(new ColumnWeightData(50));
		table.setLayout(tl);
		
		Composite buttons = new Composite(parent, SWT.NULL);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);
		
		Button addButton = new Button(buttons, SWT.PUSH);
		addButton.setText(Policy.bind("TextPreferencePage.add")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, addButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		addButton.setLayoutData(data);
		addButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addPattern();
			}
		});
		
		changeButton = new Button(buttons, SWT.PUSH);
		changeButton.setText(Policy.bind("TextPreferencePage.change")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, changeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		changeButton.setLayoutData(data);
		changeButton.setEnabled(false);
		changeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				changePattern();
			}
		});
		
		removeButton= new Button(buttons, SWT.PUSH);
		removeButton.setText(Policy.bind("TextPreferencePage.remove")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, removeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		removeButton.setLayoutData(data);
		removeButton.setEnabled(false);
		removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				removePattern();
			}
		});
		
		fillTable(Team.getAllTypes());
		Dialog.applyDialogFont(parent);
		return parent;
	}
	
	protected void performDefaults() {
		super.performDefaults();
		IFileTypeInfo[] infos = Team.getDefaultTypes();
		fillTable(infos);
	}
	
	/**
	 * Do anything necessary because the OK button has been pressed.
	 *
	 * @return whether it is okay to close the preference page
	 */
	public boolean performOk() {
		int size = input.size();
		String[] extensions = new String[size];
		int[] types = new int[size];
		int i = 0;
		
		Iterator it = input.iterator();
		while (it.hasNext()) {
			TableEntry entry = (TableEntry)it.next();
			String value = entry.getValue();
			if (value.equals(TEXT)) {
				types[i] = Team.TEXT;
			} else {
				types[i] = Team.BINARY;
			}
			extensions[i] = entry.getExtension();
			i++;
		}
		Team.setAllTypes(extensions, types);
		return true;
	}
	/**
	 * Fill the table with the values from the file type registry
	 */
	private void fillTable(IFileTypeInfo[] infos) {
		this.input = new ArrayList();
		for (int i = 0; i < infos.length; i++) {
			IFileTypeInfo info = infos[i];
			int type = info.getType();
			switch (type) {
				case Team.TEXT:
					input.add(new TableEntry(info.getExtension(), TEXT));
					break;
				case Team.BINARY:
					input.add(new TableEntry(info.getExtension(), BINARY));
					break;
			}
		}
		viewer.setInput(input);
	}
	/**
	 * Add a new item to the table with the default type of Text.
	 */
	private void addPattern() {
		InputDialog dialog = new InputDialog(getShell(), Policy.bind("TextPreferencePage.enterExtensionShort"), Policy.bind("TextPreferencePage.enterExtensionLong"), null, null); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.open();
		if (dialog.getReturnCode() != InputDialog.OK) return;
		String pattern = dialog.getValue();
		if (pattern.equals("")) return; //$NON-NLS-1$
		// Check if the item already exists
		Iterator it = input.iterator();
		while (it.hasNext()) {
			TableEntry entry = (TableEntry)it.next();
			if (entry.getExtension().equals(pattern)) {
				MessageDialog.openWarning(getShell(), Policy.bind("TextPreferencePage.extensionExistsShort"), Policy.bind("TextPreferencePage.extensionExistsLong")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
		input.add(new TableEntry(pattern, TEXT));
		viewer.refresh();
	}
	/**
	 * Remove the selected items from the table
	 */
	private void removePattern() {
		ISelection selection = viewer.getSelection();
		if (selection == null || !(selection instanceof IStructuredSelection)) {
			return;
		}
		IStructuredSelection ss = (IStructuredSelection)selection;
		Iterator it = ss.iterator();
		while (it.hasNext()) {
			TableEntry entry = (TableEntry)it.next();
			input.remove(entry);
		}
		viewer.refresh();
	}
	/**
	 * Toggle the selected items' content types
	 */
	private void changePattern() {
		ISelection selection = viewer.getSelection();
		if (selection == null || !(selection instanceof IStructuredSelection)) {
			return;
		}
		IStructuredSelection ss = (IStructuredSelection)selection;
		Iterator it = ss.iterator();
		while (it.hasNext()) {
			TableEntry entry = (TableEntry)it.next();
			String string = entry.getValue();
			if (string.equals(TEXT)) {
				entry.setValue(BINARY);
			} else {
				entry.setValue(TEXT);
			}
			viewer.refresh(entry);
		}
	}
	/**
	 * The table viewer selection has changed. Update the remove and change button enablement.
	 */
	private void handleSelection() {
		boolean empty = viewer.getSelection().isEmpty();
		removeButton.setEnabled(!empty);
		changeButton.setEnabled(!empty);
	}
}
