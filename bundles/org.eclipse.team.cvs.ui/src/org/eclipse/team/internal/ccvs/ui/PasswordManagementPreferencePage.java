/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;
 
import java.util.*;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

public class PasswordManagementPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private TableViewer viewer;
	private Button removeButton;
	private Button removeAllButton;
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			ICVSRepositoryLocation entry = (ICVSRepositoryLocation)element;
			switch (columnIndex) {
				case 0:
					return entry.toString();
				case 1:
					return entry.getUsername();
				default:
					return null;
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	};
		
	public void init(IWorkbench workbench) {
		setDescription(Policy.bind("PasswordManagementPreferencePage.2")); //$NON-NLS-1$
	}
	
	/**
	 * Creates preference page controls on demand.
	 *
	 * @param parent  the parent for the preference page
	 */
	protected Control createContents(Composite ancestor) {
		
		Composite parent = new Composite(ancestor, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		parent.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		parent.setLayoutData(data);
		
		viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Table table = viewer.getTable();
		new TableEditor(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData gd = new GridData(GridData.FILL_BOTH);
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
		columns[0].setText(Policy.bind("PasswordManagementPreferencePage.3"));  //$NON-NLS-1$
		columns[1].setText(Policy.bind("PasswordManagementPreferencePage.4"));  //$NON-NLS-1$
		viewer.setColumnProperties(new String[] {"location", "username"}); //$NON-NLS-1$ //$NON-NLS-2$
		viewer.setLabelProvider(new TableLabelProvider());
		viewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public Object[] getElements(Object inputElement) {
				if (inputElement == null) return null;
				ICVSRepositoryLocation[] locations = ((KnownRepositories)inputElement).getRepositories();
				List repos = new ArrayList();
				for (int i = 0; i < locations.length; i++) {
					ICVSRepositoryLocation l = locations[i];
					if(l.getUserInfoCached()) 
						repos.add(l);
				}
				return (ICVSRepositoryLocation[]) repos.toArray(new ICVSRepositoryLocation[repos.size()]);
			}
		});
		TableLayout tl = new TableLayout();
		tl.addColumnData(new ColumnWeightData(70, 100));
		tl.addColumnData(new ColumnWeightData(30));
		table.setLayout(tl);
		
		Composite buttons = new Composite(parent, SWT.NULL);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);
		
		removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText(Policy.bind("PasswordManagementPreferencePage.5"));  //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, removeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		removeButton.setLayoutData(data);
		removeButton.setEnabled(false);
		removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				remove();
			}
		});
		removeAllButton = new Button(buttons, SWT.PUSH);
		removeAllButton.setText(Policy.bind("PasswordManagementPreferencePage.6"));  //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, removeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		removeAllButton.setLayoutData(data);
		removeAllButton.setEnabled(true);
		removeAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				removeAll();
			}
		});
		Dialog.applyDialogFont(ancestor);
		viewer.setInput(KnownRepositories.getInstance());
		handleSelection();
		
		WorkbenchHelp.setHelp(getControl(), IHelpContextIds.PASSWORD_MANAGEMENT_PAGE);
		
		return parent;
	}
	
	public boolean performOk() {
		return true;
	}
	
	protected void performDefaults() {
		super.performDefaults();
	}
	
	private void remove() {
		IStructuredSelection s = (IStructuredSelection)viewer.getSelection();
		for (Iterator it = s.iterator(); it.hasNext();) {
			ICVSRepositoryLocation location = (ICVSRepositoryLocation) it.next();
			location.flushUserInfo();
		}
		viewer.refresh();
		handleSelection();
	}
	
	private void removeAll() {
		ICVSRepositoryLocation[] locations = KnownRepositories.getInstance().getRepositories();
		for (int i = 0; i < locations.length; i++) {
			ICVSRepositoryLocation l = locations[i];
			if(l.getUserInfoCached()) 
				l.flushUserInfo();
		}
		viewer.refresh();
		handleSelection();
	}
	
	private void handleSelection() {
		if (viewer.getTable().getSelectionCount() > 0) {
			removeButton.setEnabled(true);
		} else {
			removeButton.setEnabled(false);
		}
		removeAllButton.setEnabled(viewer.getTable().getItemCount() > 0);
	}
}
