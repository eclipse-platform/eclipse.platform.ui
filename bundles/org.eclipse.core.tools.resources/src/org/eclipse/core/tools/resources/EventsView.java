/**********************************************************************
 * Copyright (c) 2000,2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.core.tools.resources;

import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.core.internal.events.EventStats;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.tools.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
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
 * 	the number core exceptions associated with this plug-in
 * 
 * Build statistics will only be displayed if the tracing/debug option
 * "build/invoking" in the plug-in org.eclipse.core.resources is set to 
 * true.
 */

public class EventsView extends TableWithTotalView implements IResourceChangeListener {
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
		public Object[] getElements(Object arg0) {
			return EventStats.getAllStats();
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
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}
	}

	/**
	 * Class to display the labels for the stats view table.
	 */
	class EventsViewLabelProvider extends LabelProvider implements ITableLabelProvider {

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
			if (!(element instanceof EventStats)) {
				return Policy.bind("stats.badStat"); //$NON-NLS-1$
			}
			EventStats stats = (EventStats) element;
			boolean notification = stats.getProject() == null;
			switch (columnIndex) {
				case STAT_ID_COLUMN :
					return stats.getName();
				case PROJECT_COLUMN :
					return notification ? "" : stats.getProject().getName(); //$NON-NLS-1$
				case COUNT_COLUMN :
					return Integer.toString(notification ? stats.getNotifyCount() : stats.getBuildCount());
				case TIME_COLUMN :
					return Long.toString(notification ? stats.getNotifyRunningTime() : stats.getBuildRunningTime());
				case EXCEPTIONS_COLUMN :
					return Integer.toString(stats.getExceptionCount());
			}
			return Policy.bind("stats.badColumn"); //$NON-NLS-1$
		}
	}

	public final static int COUNT_COLUMN = 2;
	public final static int EXCEPTIONS_COLUMN = 4;
	public final static int PROJECT_COLUMN = 1;
	// Table of Column Indices
	public final static int STAT_ID_COLUMN = 0;
	public final static int TIME_COLUMN = 3;

	private String columnHeaders[] = {Policy.bind("stats.statIdHeader"), //$NON-NLS-1$
			Policy.bind("stats.projectHeader"), //$NON-NLS-1$
			Policy.bind("stats.countHeader"), //$NON-NLS-1$
			Policy.bind("stats.timeHeader"), //$NON-NLS-1$
			Policy.bind("stats.errorsHeader"), //$NON-NLS-1$
	};
	private ColumnLayoutData columnLayouts[] = {new ColumnWeightData(175), // statistics id
			new ColumnWeightData(75), // project name
			new ColumnPixelData(50), // count 
			new ColumnPixelData(60), // total time
			new ColumnPixelData(40)}; // number of exceptions
	private CopyStructuredSelectionAction copyAction;
	private Action resetAction;

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#computeTotalLine(java.util.Iterator)
	 */
	protected String[] computeTotalLine(Iterator iter) {
		String[] totals = new String[getColumnHeaders().length];
		int count = 0;
		int events = 0;
		int time = 0;
		int exceptions = 0;
		if (!iter.hasNext()) {
			Object[] elements = ((ITreeContentProvider) viewer.getContentProvider()).getElements(viewer.getInput());
			iter = Arrays.asList(elements == null ? new Object[0] : elements).iterator();
		}
		while (iter.hasNext()) {
			EventStats element = (EventStats) iter.next();
			events += element.getProject() == null ? element.getNotifyCount() : element.getBuildCount();
			time += element.getProject() == null ? element.getNotifyRunningTime() : element.getBuildRunningTime();
			exceptions += element.getExceptionCount();
			count++;
		}
		totals[0] = "Total: " + count; //$NON-NLS-1$
		totals[2] = "" + events; //$NON-NLS-1$
		totals[3] = "" + time; //$NON-NLS-1$
		totals[4] = "" + exceptions; //$NON-NLS-1$
		return totals;
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#createActions()
	 */
	protected void createActions() {
		resetAction = new Action("Reset") { //$NON-NLS-1$
			public void run() {
				EventStats.resetStats();
				getViewer().refresh();
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
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
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
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
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
		if (!(element instanceof EventStats))
			return ""; //$NON-NLS-1$
		IPluginDescriptor plugin = ((EventStats) element).getPlugin();
		return plugin == null ? "" : plugin.getUniqueIdentifier(); //$NON-NLS-1$
	}

	protected TableTreeViewer getViewer() {
		return viewer;
	}

	/**
	 * @see IResourceChangeListener#resourceChanged
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				getViewer().refresh();
			}
		});
	}
}