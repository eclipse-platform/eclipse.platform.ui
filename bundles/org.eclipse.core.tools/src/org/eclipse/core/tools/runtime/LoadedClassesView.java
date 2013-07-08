/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
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
//import org.eclipse.core.runtime.internal.stats.ClassStats;
//import org.eclipse.core.runtime.internal.stats.StatsManager;
import org.eclipse.core.tools.CoreToolsPlugin;
import org.eclipse.core.tools.TableWithTotalView;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;

/**
 * View that display information about classes
 */
public class LoadedClassesView extends TableWithTotalView {
	private Action displayStackAction;

	public static String VIEW_ID = LoadedClassesView.class.getName();
	private String[] columnHeaders = new String[] {"Class", "Order", "Memory", "Plug-in", "Timestamp", "RAM", "ROM"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	private ColumnLayoutData[] columnLayout = new ColumnLayoutData[] {new ColumnWeightData(500), new ColumnWeightData(100), new ColumnWeightData(100), new ColumnWeightData(200), new ColumnPixelData(0), new ColumnPixelData(0), new ColumnPixelData(0)};

	protected String[] getColumnHeaders() {
		return columnHeaders;
	}

	protected ColumnLayoutData[] getColumnLayout() {
		return columnLayout;
	}

	public void createPartControl(Composite parent) {
//		if (!StatsManager.MONITOR_CLASSES) {
			Text text = new Text(parent, 0);
			text.setText("Class monitoring is not enabled."); //$NON-NLS-1$
//			return;
//		}
//		super.createPartControl(parent);
//		viewer.setSelection(StructuredSelection.EMPTY);
	}

	protected ITreeContentProvider getContentProvider() {
		return new LoadedClassesViewContentProvider();
	}

	protected ITableLabelProvider getLabelProvider() {
		return new LoadedClassesViewLabelProvider();
	}

	protected ViewerSorter getSorter(int column) {
		return new LoadedClassesViewSorter(column);
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

	protected void createActions() {
		displayStackAction = new Action("Stack &Trace") { //$NON-NLS-1$
			public void run() {
//				try {
//					StackTraceView view = (StackTraceView) getSite().getPage().showView(StackTraceView.VIEW_ID);
//					ClassStats clazz = (ClassStats) ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
//					if (clazz == null)
//						return;
//					view.setInput(StatsManager.TRACE_FILENAME, clazz.getTraceStart(), clazz.getTraceEnd());
//				} catch (PartInitException e) {
//					e.printStackTrace();
//				}
			}
		};
		displayStackAction.setToolTipText("Display the class activation stack trace"); //$NON-NLS-1$
		displayStackAction.setImageDescriptor(CoreToolsPlugin.createImageDescriptor("trace.gif")); //$NON-NLS-1$
	}

	protected void createToolbar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
		manager.add(displayStackAction);
	}

	public void setInput(Object input) {
		viewer.setInput(input);
		viewer.setSelection(StructuredSelection.EMPTY);
	}

	protected String[] computeTotalLine(Iterator iterator) {
		String[] totals = new String[getColumnHeaders().length];
		int ramTotal = 0;
		int romTotal = 0;
		int count = 0;
		if (!iterator.hasNext()) {
			Object[] elements = ((ITreeContentProvider) viewer.getContentProvider()).getElements(viewer.getInput());
			iterator = Arrays.asList(elements == null ? new Object[0] : elements).iterator();
		}
//		while (iterator.hasNext()) {
//			ClassStats clazz = (ClassStats) iterator.next();
//			VMClassloaderInfo loader = VMClassloaderInfo.getClassloader(clazz.getClassloader().getId());
//			VMClassInfo classInfo = loader.getClass(clazz.getClassName());
//			ramTotal += classInfo.getRAMSize();
//			romTotal += classInfo.getROMSize();
//			count++;
//		}
		totals[0] = "Total: " + count; //$NON-NLS-1$
		totals[2] = Integer.toString(ramTotal + romTotal);
		totals[5] = Integer.toString(ramTotal);
		totals[6] = Integer.toString(romTotal);
		return totals;
	}

	public void dispose() {
		if (displayStackAction != null)
			displayStackAction.setImageDescriptor(null);
	}

	protected void createContextMenu() {
		MenuManager manager = new MenuManager();
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu = manager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(manager, viewer);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(displayStackAction);
	}
}
