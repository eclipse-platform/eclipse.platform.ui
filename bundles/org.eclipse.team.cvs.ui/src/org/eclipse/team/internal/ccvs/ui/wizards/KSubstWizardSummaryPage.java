package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.model.WorkbenchViewerSorter;

public class KSubstWizardSummaryPage extends CVSWizardPage {
	private TableViewer tableViewer;

	public KSubstWizardSummaryPage(String pageName) {
		super(pageName);
	}
	
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		setControl(top);
		createWrappingLabel(top, Policy.bind("KSubstWizardSummaryPage.contents"), 0, LABEL_WIDTH_HINT);
		createSeparator(top, SPACER_HEIGHT);
		tableViewer = createFileTableViewer(top,
			Policy.bind("KSubstWizardSummaryPage.summaryViewer.title"),
			Policy.bind("KSubstWizardSummaryPage.summaryViewer.fileHeader"),
			Policy.bind("KSubstWizardSummaryPage.summaryViewer.ksubstHeader"),
			LIST_HEIGHT_HINT);
	}
	
	/**
	 * Creates a TableViewer whose input is a Map from IFile to KSubstOption.
	 * 
	 * @param parent the parent of the viewer
	 * @param title the text for the title label
	 * @param heightHint the nominal height of the list
	 * @return the created list viewer
	 */
	public TableViewer createFileTableViewer(Composite parent, String title,
		String fileHeader, String ksubstHeader, int heightHint) {
		createLabel(parent, title);
		// create a table
		Table table = new Table(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = heightHint;
		table.setLayoutData(data);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		// add the columns
		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setText(fileHeader);
		column = new TableColumn(table, SWT.LEFT, 1);
		column.setText(ksubstHeader);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		tableLayout.addColumnData(new ColumnWeightData(2, true));
		tableLayout.addColumnData(new ColumnWeightData(1, true));

		// create a viewer for the table
		TableViewer tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {				
				Map changeSet = (Map) inputElement;
				if (changeSet == null) return new Map.Entry[0];
				return changeSet.entrySet().toArray();
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
				Map.Entry entry = (Map.Entry) element;
				if (columnIndex == 0) {
					return ((IFile) entry.getKey()).getFullPath().toString();
				} else {
					return ((KSubstOption) entry.getValue()).getLongDisplayText();
				}
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
				return super.compare(viewer, ((Map.Entry) e1).getKey(), ((Map.Entry) e2).getKey());
			}
		});
		return tableViewer;
	}
	
	public void setChangeSet(Map changeSet) {
		tableViewer.setInput(changeSet);
		tableViewer.refresh();
	}
}
