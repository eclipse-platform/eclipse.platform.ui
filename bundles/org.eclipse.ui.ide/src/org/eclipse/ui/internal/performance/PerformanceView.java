/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.performance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.runtime.PerformanceStats.PerformanceListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

/**
 * The PerformanceView is the view that tracks performance warnings in the user
 * interface.
 */
public class PerformanceView extends ViewPart {

	// Table of Column Indices
	final static int COLUMN_EVENT = 0;

	final static int COLUMN_BLAME = 1;

	final static int COLUMN_CONTEXT = 2;

	final static int COLUMN_COUNT = 3;

	final static int COLUMN_TIME = 4;

	private static final String EMPTY_STRING = "";//$NON-NLS-1$ 

	private String columnHeaders[] = {
			PerformanceMessages.PerformanceView_eventHeader,
			PerformanceMessages.PerformanceView_blameHeader,
			PerformanceMessages.PerformanceView_contextHeader,
			PerformanceMessages.PerformanceView_countHeader,
			PerformanceMessages.PerformanceView_timeHeader };

	private ColumnLayoutData columnLayouts[] = { new ColumnWeightData(80), // event
			new ColumnWeightData(180), // blame
			new ColumnWeightData(40), // context
			new ColumnPixelData(65), // count
			new ColumnPixelData(65) }; // total time

	Clipboard clipboard;

	Action resetAction;

	Action copyAction;
	
	Action selectAllAction;

	TableViewer viewer;

	private PerformanceStats.PerformanceListener performanceListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {

		clipboard = new Clipboard(parent.getDisplay());

		viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(getLabelProvider());
		viewer.setSorter(new EventsSorter(0));
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		TableLayout tableLayout = new TableLayout();
		viewer.getTable().setLayout(tableLayout);

		for (int i = 0; i < columnHeaders.length; i++) {
			final int index = i;
			tableLayout.addColumnData(columnLayouts[i]);
			TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE, i);
			column.setResizable(true);
			column.setText(columnHeaders[i]);
			column.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e){
					viewer.setSorter(new EventsSorter(index));
					viewer.refresh();
				}
			});
		}

		performanceListener = createPerformanceListener();
		PerformanceStats.addListener(performanceListener);
		viewer.setInput(""); //$NON-NLS-1$

		createCommonActions(viewer);
		createContextMenu(viewer);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		PerformanceStats.removeListener(performanceListener);
		super.dispose();
	}

	private PerformanceListener createPerformanceListener() {
		return new PerformanceStats.PerformanceListener() {

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.PerformanceStats$PerformanceListener#eventFailed(org.eclipse.core.runtime.PerformanceStats, long)
			 */
			public void eventFailed(PerformanceStats event, long duration) {
				// We only log failures
				viewer.getControl().getDisplay().asyncExec(new Runnable() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						if (!viewer.getControl().isDisposed())
							viewer.refresh();
					}
				});

			}

		};
	}

	/**
	 * Create the context menu for the viewer.
	 * 
	 * @param viewer
	 */
	private void createContextMenu(StructuredViewer viewer) {
		// creates a context menu with actions and adds it to the viewer control
		MenuManager menuMgr = new MenuManager();
		menuMgr.add(resetAction);
		menuMgr.add(copyAction);
		menuMgr.add(selectAllAction);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

	}

	/**
	 * Create the actions for the site.
	 * 
	 * @param viewer
	 * 
	 */
	private void createCommonActions(final TableViewer viewer) {
		copyAction = new Action(PerformanceMessages.PerformanceView_copyActionName) {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				String result = ""; //$NON-NLS-1$
				String[] columnHeaders = PerformanceView.this.columnHeaders;
				for (int i = 0; i < columnHeaders.length; i++)
					result += columnHeaders[i] + ","; //$NON-NLS-1$
				result += "\n\n"; //$NON-NLS-1$

				ITableLabelProvider labelProvider = (ITableLabelProvider) viewer
						.getLabelProvider();
				for (Iterator iterator = selection.iterator(); iterator
						.hasNext();) {
					Object selectedItem = iterator.next();
					for (int i = 0; i < columnHeaders.length; i++)
						result += labelProvider.getColumnText(selectedItem, i)
								+ ","; //$NON-NLS-1$
					result += "\n"; //$NON-NLS-1$
				}
				clipboard.setContents(new Object[] { result },
						new Transfer[] { TextTransfer.getInstance() });
			}
		};
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
				copyAction);

		selectAllAction = new Action(PerformanceMessages.PerformanceView_selectAllActionName) {
			public void run() {
				viewer.getTable().selectAll();
				// force viewer selection change
				viewer.setSelection(viewer.getSelection());
			}
		};
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
				selectAllAction);

		resetAction = new Action(
				PerformanceMessages.PerformanceView_resetAction) {
			public void run() {
				PerformanceStats.clear();
				viewer.setInput(EMPTY_STRING);
			}
		};
		resetAction
				.setToolTipText(PerformanceMessages.PerformanceView_resetTooltip);

	}

	private IStructuredContentProvider getContentProvider() {
		return new IStructuredContentProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				Object[] all = PerformanceStats.getAllStats();
				Collection result = new ArrayList();
				for (int i = 0; i < all.length; i++) {
					PerformanceStats stats = (PerformanceStats) all[i];
					if (stats.isFailure())
						result.add(stats);
				}
				return result.toArray();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

		};
	}

	private ITableLabelProvider getLabelProvider() {
		return new ITableLabelProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
			 *      int)
			 */
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
			 *      int)
			 */
			public String getColumnText(Object element, int columnIndex) {

				if (!(element instanceof PerformanceStats)) {
					return PerformanceMessages.PerformanceView_InvalidInput;
				}
				PerformanceStats stats = (PerformanceStats) element;
				switch (columnIndex) {
				case COLUMN_EVENT:
					return stats.getEvent();
				case COLUMN_BLAME:
					return stats.getBlameString();
				case COLUMN_CONTEXT:
					return stats.getContext();
				case COLUMN_COUNT:
					return Integer.toString(stats.getRunCount());
				case COLUMN_TIME:
					return Long.toString(stats.getRunningTime()/stats.getRunCount());
				}
				return PerformanceMessages.PerformanceView_InvalidColumn;

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
			 */
			public void addListener(ILabelProviderListener listener) {

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
			 */
			public void dispose() {

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
			 *      java.lang.String)
			 */
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
			 */
			public void removeListener(ILabelProviderListener listener) {

			}

		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
