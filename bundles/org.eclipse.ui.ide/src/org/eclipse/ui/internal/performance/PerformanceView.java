package org.eclipse.ui.internal.performance;

/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

/**
 * The PerformanceView is the view that tracks performance warnings in the user
 * interface.
 */
public class PerformanceView extends ViewPart {

	// Table of Column Indices
	public final static int COLUMN_EVENT = 0;

	public final static int COLUMN_BLAME = 1;

	public final static int COLUMN_CONTEXT = 2;

	public final static int COLUMN_COUNT = 3;

	public final static int COLUMN_TIME = 4;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {

		clipboard = new Clipboard(parent.getDisplay());

		TreeViewer viewer = new TreeViewer(parent, SWT.NONE);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(getLabelProvider());
		viewer.setSorter(new EventsSorter(0));

		createCommonActions(viewer);
		getViewSite().getActionBars().getToolBarManager().add(resetAction);
		createContextMenu(viewer);
	}

	/**
	 * Create the context menu for the viewer.
	 * @param viewer
	 */
	private void createContextMenu(TreeViewer viewer) {
		//creates a context menu with actions and adds it to the viewer control
		MenuManager menuMgr = new MenuManager();
		menuMgr.add(resetAction);
		menuMgr.add(copyAction);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		
	}

	/**
	 * Create the actions for the site.
	 * 
	 * @param viewer
	 * 
	 */
	private void createCommonActions(final TreeViewer viewer) {
		copyAction = new Action() {
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

		Action selectAllAction = new Action() {
			public void run() {
				viewer.getTree().selectAll();
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

	private ITreeContentProvider getContentProvider() {
		return new ITreeContentProvider() {

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
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
			 */
			public Object[] getChildren(Object parentElement) {
				return new Object[0];
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
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
			 */
			public Object getParent(Object element) {
				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
			 */
			public boolean hasChildren(Object element) {
				return false;
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
					return Long.toString(stats.getRunningTime());
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
