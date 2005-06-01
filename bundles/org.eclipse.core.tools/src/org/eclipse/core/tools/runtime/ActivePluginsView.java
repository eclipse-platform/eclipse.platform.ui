/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
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
import org.eclipse.core.runtime.internal.stats.BundleStats;
import org.eclipse.core.runtime.internal.stats.StatsManager;
import org.eclipse.core.tools.CoreToolsPlugin;
import org.eclipse.core.tools.TableWithTotalView;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;

/**
 * View used to display the activated plugins 
 */
public class ActivePluginsView extends TableWithTotalView {
	private Action refreshAction;
	private Action displayClassesInfoAction;
	private Action displayStackAction;

	public static String VIEW_ID = ActivePluginsView.class.getName();
	private static String columnHeaders[] = {"Plug-in", "Classes", "Alloc", "Used", "Startup time", "Order", "Timestamp", "Class load time", "Startup method time", "RAM Alloc", "RAM Used", "ROM Alloc", "ROM Used"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$
	private static int columnWidths[] = {500, 150, 200, 200, 150, 100, 0, 0, 0, 0, 0, 0, 0};

	protected String[] getColumnHeaders() {
		return columnHeaders;
	}

	protected ColumnLayoutData[] getColumnLayout() {
		ColumnLayoutData[] result = new ColumnLayoutData[columnWidths.length];
		for (int i = 0; i < columnWidths.length; i++) {
			int width = columnWidths[i];
			result[i] = width == 0 ? (ColumnLayoutData) new ColumnPixelData(width) : (ColumnLayoutData) new ColumnWeightData(width);
		}
		return result;
	}

	protected void createToolbar() {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager manager = actionBars.getToolBarManager();
		manager.add(refreshAction);
		manager.add(displayClassesInfoAction);
		manager.add(displayStackAction);
	}

	protected void createContextMenu() {
		// Create menu manager.
		MenuManager manager = new MenuManager();
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});

		// Create menu.
		Menu menu = manager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// Register menu for extension.
		getSite().registerContextMenu(manager, viewer);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator());
		manager.add(displayClassesInfoAction);
		manager.add(displayStackAction);
	}

	protected String[] computeTotalLine(Iterator iterator) {
		int sumOfClasses = 0;
		int sumOfMemoryUsed = 0;
		int sumOfMemoryAlloc = 0;
		long sumOfStartupTime = 0;
		long sumOfClassloadtime = 0;
		int sumOfRAMAlloc = 0;
		int sumOfRAMUsed = 0;
		long sumOfStartupMethodTime = 0;
		int sumOfROMAlloc = 0;
		int sumOfROMUsed = 0;
		int count = 0;
		if (!iterator.hasNext()) {
			Object[] elements = ((ITreeContentProvider) viewer.getContentProvider()).getElements(viewer.getInput());
			iterator = Arrays.asList(elements).iterator();
		}
		for (; iterator.hasNext();) {
			BundleStats element = (BundleStats) iterator.next();
			if (element != null) {
				VMClassloaderInfo vmInfo = VMClassloaderInfo.getClassloader(element.getSymbolicName());
				sumOfClasses += element.getClassLoadCount();
				sumOfMemoryUsed += (vmInfo.getUsedRAM() + vmInfo.getUsedROM());
				sumOfMemoryAlloc += (vmInfo.getAllocRAM() + vmInfo.getAllocROM());
				sumOfStartupTime = sumOfStartupTime + element.getStartupTime();
				sumOfClassloadtime = sumOfClassloadtime + element.getClassLoadTime();
				sumOfStartupMethodTime = sumOfStartupMethodTime + element.getStartupMethodTime();
				sumOfRAMAlloc += vmInfo.getAllocRAM();
				sumOfRAMUsed += vmInfo.getUsedRAM();
				sumOfROMAlloc += vmInfo.getAllocROM();
				sumOfROMUsed += vmInfo.getUsedROM();
				count++;
			}
		}
		String[] totalLine = new String[getColumnHeaders().length];
		totalLine[0] = "Total: " + count; //$NON-NLS-1$
		totalLine[1] = "" + sumOfClasses; //$NON-NLS-1$
		totalLine[2] = "" + sumOfMemoryAlloc; //$NON-NLS-1$
		totalLine[3] = "" + sumOfMemoryUsed; //$NON-NLS-1$
		totalLine[4] = "" + sumOfStartupTime; //$NON-NLS-1$
		totalLine[8] = "" + sumOfStartupMethodTime; //$NON-NLS-1$
		totalLine[9] = "" + sumOfRAMAlloc; //$NON-NLS-1$
		totalLine[10] = "" + sumOfRAMUsed; //$NON-NLS-1$
		totalLine[11] = "" + sumOfROMAlloc; //$NON-NLS-1$
		totalLine[12] = "" + sumOfROMUsed; //$NON-NLS-1$
		return totalLine;
	}

	public void createPartControl(Composite parent) {
		if (!StatsManager.MONITOR_ACTIVATION) {
			Text text = new Text(parent, 0);
			text.setText("Plug-in monitoring is not enabled"); //$NON-NLS-1$
			return;
		}
		super.createPartControl(parent);
		viewer.setInput(BundleStats.class);
		getSite().setSelectionProvider(viewer);
		viewer.setSelection(StructuredSelection.EMPTY);
	}

	protected void createActions() {
		refreshAction = new Action("Refresh") { //$NON-NLS-1$
			public void run() {
				VMClassloaderInfo.refreshInfos();
				getViewer().refresh();
			}
		};
		refreshAction.setImageDescriptor(CoreToolsPlugin.createImageDescriptor("refresh.gif")); //$NON-NLS-1$
		refreshAction.setToolTipText("Refresh the data"); //$NON-NLS-1$
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);

		displayClassesInfoAction = new Action("Classes") { //$NON-NLS-1$
			public void run() {
				try {
					LoadedClassesView view = (LoadedClassesView) getSite().getPage().showView(LoadedClassesView.VIEW_ID);
					IStructuredSelection selection = ((IStructuredSelection) getViewer().getSelection());
					if (selection == null)
						return;

					view.setInput(selection.toArray());

				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		};
		displayClassesInfoAction.setToolTipText("Display classes loaded by the selected plug-in"); //$NON-NLS-1$
		displayClassesInfoAction.setImageDescriptor(CoreToolsPlugin.createImageDescriptor("classes.gif")); //$NON-NLS-1$

		displayStackAction = new Action("Stack &Trace") { //$NON-NLS-1$
			public void run() {
				try {
					StackTraceView view = (StackTraceView) getSite().getPage().showView(StackTraceView.VIEW_ID);
					BundleStats info = (BundleStats) ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
					if (info == null)
						return;
					view.setInput(StatsManager.TRACE_FILENAME, info.getTraceStart(), info.getTraceEnd());
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		};
		displayStackAction.setToolTipText("Display the plug-in activation stack trace"); //$NON-NLS-1$
		displayStackAction.setImageDescriptor(CoreToolsPlugin.createImageDescriptor("trace.gif")); //$NON-NLS-1$
	}

	protected ITreeContentProvider getContentProvider() {
		return new ActivePluginsViewContentProvider();
	}

	protected ITableLabelProvider getLabelProvider() {
		return new ActivePluginsViewLabelProvider();
	}

	protected ViewerSorter getSorter(int column) {
		return new ActivePluginsViewSorter(column);
	}

	/**
	 * @see org.eclipse.core.tools.TableWithTotalView#getStatusLineMessage(Object)
	 */
	protected String getStatusLineMessage(Object element) {
		return ""; //$NON-NLS-1$
	}

	protected TableTreeViewer getViewer() {
		return viewer;
	}

	public void dispose() {
		// if there is no viewer then we were not monitoring so there
		// is nothing to dispose.
		if (viewer == null)
			return;
		refreshAction.setImageDescriptor(null);
		displayClassesInfoAction.setImageDescriptor(null);
		displayStackAction.setImageDescriptor(null);
	}

}
