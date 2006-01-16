/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.history;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.internal.ui.TeamUIMessages;

public class LocalHistoryTableProvider {
	/**
	 * The Local history label provider.
	 */
	class LocalHistoryLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider, IFontProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			IFileState entry = adaptToFileState(element);
			if (entry == null)
				return ""; //$NON-NLS-1$
			switch (columnIndex) {
				case COL_DATE :
					String revision = DateFormat.getInstance().format(new Date(entry.getModificationTime()));
					return revision;
				case COL_AUTHOR:
					return "local user";
					
				case COL_COMMENT:
					return "local revision";
			}
			return ""; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			/*IFileState = adaptToFileState(element);
			 if (!entry.exists()) {
			 return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
			 }*/

			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
		 */
		public Font getFont(Object element) {
			IFileState entry = adaptToFileState(element);
			 if (entry == null)
			 return null;
			 /*String revision = entry.getContentIdentifier();
			 String tempCurrentRevision = getCurrentRevision();
			 if (tempCurrentRevision != null && tempCurrentRevision.equals(revision)) {
			 if (currentRevisionFont == null) {
				 Font defaultFont = JFaceResources.getDefaultFont();
				 FontData[] data = defaultFont.getFontData();
				 for (int i = 0; i < data.length; i++) {
				 data[i].setStyle(SWT.ITALIC);
				 }
				 currentRevisionFont = new Font(viewer.getTable().getDisplay(), data);
			 }
			 return currentRevisionFont;
			 }*/
			return null;
		}
	}

	//column constants
	private static final int COL_DATE = 0;
	private static final int COL_AUTHOR = 1;
	private static final int COL_COMMENT = 2;
	
	private IFileHistory currentFileHistory;
	private Object currentFile;
	private Object currentRevision;

	protected IFileState adaptToFileState(Object element) {
		// Get the log entry for the provided object
		IFileState entry = null;
		if (element instanceof IFileState) {
			entry = (IFileState) element;
		} else if (element instanceof IAdaptable) {
			entry = (IFileState) ((IAdaptable) element).getAdapter(IFileState.class);
		}
		return entry;
	}

	/**
	 * Create a TableViewer that can be used to display a list of IFileRevision instances.
	 * Ths method provides the labels and sorter but does not provide a content provider
	 * 
	 * @param parent
	 * @return TableViewer
	 */
	public TableViewer createTable(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);

		TableLayout layout = new TableLayout();
		table.setLayout(layout);

		TableViewer viewer = new TableViewer(table);

		createColumns(table, layout, viewer);

		viewer.setLabelProvider(new LocalHistoryLabelProvider());

		// By default, reverse sort by revision.
		//HistorySorter sorter = new HistorySorter(COL_REVISIONID);
		//sorter.setReversed(true);
		//viewer.setSorter(sorter);

		/*	table.addDisposeListener(new DisposeListener() {
		 public void widgetDisposed(DisposeEvent e) {
		 if (currentRevisionFont != null) {
		 currentRevisionFont.dispose();
		 }
		 }
		 });*/

		return viewer;
	}

	/**
	 * Creates the columns for the history table.
	 */
	private void createColumns(Table table, TableLayout layout, TableViewer viewer) {
		//SelectionListener headerListener = getColumnListener(viewer);

		// creation date
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(TeamUIMessages.GenericHistoryTableProvider_RevisionTime);
		//col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));

		// author
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(TeamUIMessages.GenericHistoryTableProvider_Author);
		//col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));

		//comment
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(TeamUIMessages.GenericHistoryTableProvider_Comment);
		//col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(50, true));
	}

	public void setFile(IFileHistory fileHistory, IFile newfile) {
		this.currentFileHistory = fileHistory;
		this.currentFile = newfile;
	}

}
