/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.core.tools.runtime;

import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.tools.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * Stats View
 * 
 * This spy utility will display globally available statistical
 * information on various plug-ins.  The statistics available are those that
 * are available through the PluginStats class of
 * org.eclipse.core.resource/src/org/eclipse/core/internal/utils
 * 
 * Currently available information includes:
 * 	the id of the statistic 
 * 	the number of notification this plug-in received
 * 	the amount of time spent receiving these notifications
 * 	the number of build requests on this plug-in
 * 	the amount of time spent building this plug-in
 * 
 * Build statistics will only be displayed if the tracing/debug option
 * "build/invoking" in the plug-in org.eclipse.core.resources is set to 
 * true.
 */

public class EventsView extends TableWithTotalView {
	class EventsViewContentProvider implements ITreeContentProvider {
		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// do nothing
		}

		/** @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object) */
		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		/** @see IStructuredContentProvider#getElements(Object) */
		public Object[] getElements(Object input) {
			return PerformanceStats.getAllStats();
		}

		/** @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object) */
		public Object getParent(Object element) {
			return null;
		}

		/** @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object) */
		public boolean hasChildren(Object element) {
			return false;
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
			// do nothing
		}
	}

	/**
	 * Class to display the labels for the stats view table.
	 */
	class EventsViewLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {

		/**
		 * @see ITableLabelProvider#getColumnImage(Object, int)
		 */
		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof PerformanceStats)) {
				return Messages.stats_badStat;
			}
			PerformanceStats stats = (PerformanceStats) element;
			switch (columnIndex) {
				case COLUMN_EVENT :
					return stats.getEvent();
				case COLUMN_BLAME :
					return stats.getBlameString();
				case COLUMN_CONTEXT :
					return stats.getContext(); //$NON-NLS-1$
				case COLUMN_COUNT :
					return Integer.toString(stats.getRunCount());
				case COLUMN_TIME :
					return Long.toString(stats.getRunningTime());
			}
			return Messages.stats_badColumn;
		}

		public Color getForeground(Object element) {
			if (!(element instanceof PerformanceStats)) {
				return null;
			}
			PerformanceStats stats = (PerformanceStats) element;
			if (stats.isFailure())
				return Display.getDefault().getSystemColor(SWT.COLOR_RED);
			return null;
		}

		public Color getBackground(Object element) {
			return null;
		}
	}

	class StatsListener extends PerformanceStats.PerformanceListener {
		private void asyncExec(Runnable runnable) {
			final Control control = viewer.getControl();
			if (control == null || control.isDisposed())
				return;
			final Display display = control.getDisplay();
			if (display.isDisposed())
				return;
			display.asyncExec(runnable);
		}

		/**
		 * @see PerformanceStats.PerformanceListener#eventsOccurred(PerformanceStats[])
		 */
		public void eventsOccurred(final PerformanceStats[] event) {
			asyncExec(new Runnable() {
				public void run() {
					if (!getViewer().getControl().isDisposed())
						getViewer().refresh();
				}
			});
		}

		public void eventFailed(final PerformanceStats event, final long duration) {
			asyncExec(new Runnable() {
				public void run() {
					String msg = "Performance event failure: " + event.getEvent() + " blame: " + event.getBlameString() + " context: " + event.getContext() + " duration: " + duration; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					getViewSite().getActionBars().getStatusLineManager().setErrorMessage(msg);
					//					MessageDialog.openError(getSite().getShell(), "Performance failure", msg);
				}
			});
		}
	}

	// Table of Column Indices
	public final static int COLUMN_EVENT = 0;
	public final static int COLUMN_BLAME = 1;
	public final static int COLUMN_CONTEXT = 2;
	public final static int COLUMN_COUNT = 3;
	public final static int COLUMN_TIME = 4;

	private String columnHeaders[] = {Messages.stats_eventHeader, //
			Messages.stats_blameHeader, //
			Messages.stats_contextHeader, //
			Messages.stats_countHeader, //
			Messages.stats_timeHeader, //
	};
	private ColumnLayoutData columnLayouts[] = {new ColumnWeightData(80), // event
			new ColumnWeightData(180), // blame
			new ColumnWeightData(40), // context
			new ColumnPixelData(65), // count 
			new ColumnPixelData(65)}; // total time
	private CopyStructuredSelectionAction copyAction;
	private Action resetAction;
	private final StatsListener statsListener = new StatsListener();

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#computeTotalLine(java.util.Iterator)
	 */
	protected String[] computeTotalLine(Iterator iter) {
		String[] totals = new String[getColumnHeaders().length];
		int count = 0;
		int events = 0;
		int time = 0;
		if (!iter.hasNext()) {
			Object[] elements = ((ITreeContentProvider) viewer.getContentProvider()).getElements(viewer.getInput());
			iter = Arrays.asList(elements == null ? new Object[0] : elements).iterator();
		}
		while (iter.hasNext()) {
			PerformanceStats element = (PerformanceStats) iter.next();
			events += element.getRunCount();
			time += element.getRunningTime();
			count++;
		}
		totals[0] = "Total: " + count; //$NON-NLS-1$
		totals[2] = "" + events; //$NON-NLS-1$
		totals[3] = "" + time; //$NON-NLS-1$
		return totals;
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#createActions()
	 */
	protected void createActions() {
		resetAction = new Action("Reset") { //$NON-NLS-1$
			public void run() {
				PerformanceStats.clear();
				getViewer().setInput("");
				updateTotals();
			}
		};
		resetAction.setToolTipText("Reset all event statistics"); //$NON-NLS-1$
		resetAction.setImageDescriptor(CoreToolsPlugin.createImageDescriptor("clear.gif")); //$NON-NLS-1$
		// Add copy selection action

		IActionBars bars = getViewSite().getActionBars();
		copyAction = new CopyStructuredSelectionAction(new TableSelectionProviderDecorator(viewer));
		copyAction.registerAsGlobalAction(bars);
		bars.updateActionBars();
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#createContextMenu()
	 */
	protected void createContextMenu() {
		// creates a context menu with actions and adds it to the viewer control
		MenuManager menuMgr = new MenuManager();
		menuMgr.add(resetAction);
		menuMgr.add(copyAction);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	/**
	 * @see IWorkbenchPart#createPartControl
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		PerformanceStats.addListener(statsListener);
		viewer.setInput(""); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#createToolbar()
	 */
	protected void createToolbar() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(resetAction);
	}

	/**
	 * @see WorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		PerformanceStats.removeListener(statsListener);
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#getColumnHeaders()
	 */
	protected String[] getColumnHeaders() {
		return columnHeaders;
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#getColumnLayout()
	 */
	protected ColumnLayoutData[] getColumnLayout() {
		return columnLayouts;
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#getContentProvider()
	 */
	protected ITreeContentProvider getContentProvider() {
		return new EventsViewContentProvider();
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#getLabelProvider()
	 */
	protected ITableLabelProvider getLabelProvider() {
		return new EventsViewLabelProvider();
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#getSorter(int)
	 */
	protected ViewerSorter getSorter(int column) {
		return new EventsSorter(column);
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#getStatusLineMessage(Object)
	 */
	protected String getStatusLineMessage(Object element) {
		if (!(element instanceof PerformanceStats))
			return ""; //$NON-NLS-1$
		return ((PerformanceStats) element).getBlameString();
	}

	protected TableTreeViewer getViewer() {
		return viewer;
	}
}
