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
package org.eclipse.team.internal.ccvs.ui.wizards;


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.wizards.KSubstWizard.KSubstChangeElement;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchViewerSorter;

public class KSubstWizardSummaryPage extends CVSWizardPage {
	private CheckboxTableViewer tableViewer = null;
	private KSubstOption[] ksubstOptions;
	private String[] ksubstOptionsDisplayText;
	private int filterType;
	
	private Button showUnaffectedFilesButton;
	private boolean showUnaffectedFiles;

	public KSubstWizardSummaryPage(String pageName, String title, ImageDescriptor image, boolean showUnaffectedFiles) {
		super(pageName, title, image);
		this.showUnaffectedFiles = showUnaffectedFiles;

		// sort the options by display text
		ksubstOptions = KSubstOption.getAllKSubstOptions();
		ksubstOptionsDisplayText = new String[ksubstOptions.length];
		Arrays.sort(ksubstOptions, new Comparator() {
			public int compare(Object a, Object b) {
				String aKey = getModeDisplayText((KSubstOption) a);
				String bKey = getModeDisplayText((KSubstOption) b);
				return aKey.compareTo(bKey);
			}
		});
		for (int i = 0; i < ksubstOptions.length; i++) {
			ksubstOptionsDisplayText[i] = getModeDisplayText(ksubstOptions[i]);
		}
	}
	
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		setControl(top);
		createWrappingLabel(top, Policy.bind("KSubstWizardSummaryPage.contents"), 0);		 //$NON-NLS-1$

		// set F1 help
		WorkbenchHelp.setHelp(top, IHelpContextIds.KEYWORD_SUBSTITUTION_SUMMARY_PAGE);
		
		createSeparator(top, 0);

		showUnaffectedFilesButton = new Button(top, SWT.CHECK);
		showUnaffectedFilesButton.setText(Policy.bind("KSubstWizardSummaryPage.showUnaffectedFiles")); //$NON-NLS-1$
		showUnaffectedFilesButton.setSelection(showUnaffectedFiles);
		showUnaffectedFilesButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				BusyIndicator.showWhile(getContainer().getShell().getDisplay(), new Runnable() {
					public void run() {
						showUnaffectedFiles = showUnaffectedFilesButton.getSelection();
						refresh(false);
					}
				});
			}
		});

		tableViewer = createFileTableViewer(top,
			Policy.bind("KSubstWizardSummaryPage.summaryViewer.title"), //$NON-NLS-1$
			Policy.bind("KSubstWizardSummaryPage.summaryViewer.fileHeader"), //$NON-NLS-1$
			Policy.bind("KSubstWizardSummaryPage.summaryViewer.ksubstHeader"), //$NON-NLS-1$
			LIST_HEIGHT_HINT);
        Dialog.applyDialogFont(parent);
	}
	
	/**
	 * Creates a TableViewer whose input is a Map from IFile to KSubstOption.
	 * 
	 * @param parent the parent of the viewer
	 * @param title the text for the title label
	 * @param heightHint the nominal height of the list
	 * @return the created list viewer
	 */
	public CheckboxTableViewer createFileTableViewer(Composite parent, String title,
		String fileHeader, String ksubstHeader, int heightHint) {
		createLabel(parent, title);
		// create a table
		Table table = new Table(parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = heightHint;
		table.setLayoutData(data);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		// add the columns
		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(fileHeader);
		column = new TableColumn(table, SWT.LEFT);
		column.setText(ksubstHeader);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		tableLayout.addColumnData(new ColumnWeightData(1, true));
		tableLayout.addColumnData(new ColumnWeightData(1, true));

		// create a viewer for the table
		final CheckboxTableViewer tableViewer = new CheckboxTableViewer(table);
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {	
				return (Object[]) inputElement;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		
		// show file name and keyword substitution mode
		tableViewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				KSubstChangeElement change = (KSubstChangeElement) element;
				if (columnIndex == 0) {
					return change.getFile().getFullPath().toString();
				} else if (columnIndex == 1) {
					return getModeDisplayText(change.getKSubst());
				}
				return null;
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		});
		
		// sort by file name
		tableViewer.setSorter(new WorkbenchViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				IFile file1 = ((KSubstChangeElement) e1).getFile();
				IFile file2 = ((KSubstChangeElement) e2).getFile();
				return super.compare(viewer, file1, file2);
			}
		});
		
		// filter
		tableViewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				KSubstChangeElement change = (KSubstChangeElement) element;
				return ( showUnaffectedFiles || change.isNewKSubstMode()) && change.matchesFilter(filterType);
			}
		});
		
		// add a check state listener
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				KSubstChangeElement change = (KSubstChangeElement) event.getElement();
				if (tableViewer.getGrayed(change)) {
					// if it's grayed then give it the appearance of being disabled
					updateCheckStatus(change);
				} else {
					// otherwise record the change
					change.setExcluded(! event.getChecked());
				}
			}
		});
		
		// add a cell editor in the Keyword Substitution Mode column
		new TableEditor(table);
		CellEditor cellEditor = new ComboBoxCellEditor(table, ksubstOptionsDisplayText);
		tableViewer.setCellEditors(new CellEditor[] { null, cellEditor });
		tableViewer.setColumnProperties(new String[] { "file", "mode" }); //$NON-NLS-1$ //$NON-NLS-2$
		tableViewer.setCellModifier(new ICellModifier() {
			public Object getValue(Object element, String property) {
				KSubstChangeElement change = (KSubstChangeElement) element;
				KSubstOption option = change.getKSubst();
				for (int i = 0; i < ksubstOptions.length; ++i) {
					if (ksubstOptions[i].equals(option)) return new Integer(i);
				}
				// XXX need to handle this better
				return null;
			}
			public boolean canModify(Object element, String property) {
				return true;
			}
			public void modify(Object element, String property, Object value) {
				// XXX The runtime type of 'element' seems to be a TableItem instead of the
				//     actual element data as with the other methods.  As a workaround, use
				//     the table's selection mechanism instead.
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				element = selection.getFirstElement();
				int index = ((Integer) value).intValue();
				// selection will be -1 if some arbitrary text was entered since the combo box is not read only
				if (index != -1) {
					KSubstChangeElement change = (KSubstChangeElement) element;
					KSubstOption newOption = ksubstOptions[index];
					if (! newOption.equals(change.getKSubst())) {
						// the option has been changed, include it by default now if it wasn't before
						// since the user has shown interest in it
						change.setKSubst(newOption);
						change.setExcluded(false);
						tableViewer.refresh(change, true /*updateLabels*/);
						updateCheckStatus(change);
					}
				}
			}
		});
		return tableViewer;
	}
	
	public void setChangeList(List changes, int filterType) {
		this.filterType = filterType;
		tableViewer.setInput(changes.toArray());
		refresh(true);
	}
	
	private void refresh(boolean updateLabels) {
		tableViewer.refresh(updateLabels);
		Object[] elements = (Object[]) tableViewer.getInput();
		for (int i = 0; i < elements.length; i++) {
			KSubstChangeElement change = (KSubstChangeElement) elements[i];
			updateCheckStatus(change);
		}
	}
	
	private void updateCheckStatus(KSubstChangeElement change) {
		if (change.isNewKSubstMode()) {
			// if the mode differs, the checkbox indicates the inclusion/exclusion status
			tableViewer.setGrayed(change, false);
			tableViewer.setChecked(change, ! change.isExcluded());
		} else {
			// otherwise, the checkbox is meaningless except to indicate that the file will not be changed
			tableViewer.setGrayed(change, true);
			tableViewer.setChecked(change, false);
		}
	}
	
	private String getModeDisplayText(KSubstOption option) {
		return option.getLongDisplayText();
	}
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			showUnaffectedFilesButton.setFocus();
		}
	}
}
