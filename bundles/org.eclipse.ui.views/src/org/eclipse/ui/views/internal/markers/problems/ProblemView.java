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

package org.eclipse.ui.views.internal.markers.problems;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.internal.markers.CreationTime;
import org.eclipse.ui.views.internal.markers.Folder;
import org.eclipse.ui.views.internal.markers.IField;
import org.eclipse.ui.views.internal.markers.IFilter;
import org.eclipse.ui.views.internal.markers.LineNumber;
import org.eclipse.ui.views.internal.markers.MarkerFilter;
import org.eclipse.ui.views.internal.markers.MarkerRegistry;
import org.eclipse.ui.views.internal.markers.MarkerView;
import org.eclipse.ui.views.internal.markers.Message;
import org.eclipse.ui.views.internal.markers.Resource;


public class ProblemView extends MarkerView {
	
	protected SelectionProviderAction resolveAction;

	static final String TAG_DIALOG_SECTION = "org.eclipse.ui.views.problems"; //$NON-NLS-1$
	
	private static final ColumnLayoutData[] DEFAULT_COLUMN_LAYOUTS =
		{
			new ColumnPixelData(19, false),
			new ColumnWeightData(200),
			new ColumnWeightData(75),
			new ColumnWeightData(150),
			new ColumnWeightData(60)};
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getRootTypes()
	 */
	protected String[] getRootTypes() {
		return new String[] {IMarker.PROBLEM};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}
		return settings;
	}
	
	protected IField[] getVisibleFields() {
		return new IField[] {new ProblemSeverity(),
							   new Message(), 
							   new Resource(), 
							   new Folder(), 
							   new LineNumber()
		};
	}
	
	protected IField[] getHiddenFields() {
		return new IField[] {new CreationTime()
		};
	}
	
	private MarkerFilter filter;
	private MarkerRegistry registry;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#createColumns(org.eclipse.swt.widgets.Table)
	 */
	protected void createColumns(Table table) {
		super.createColumns(table);
		TableColumn[] columns = table.getColumns();
		if (columns != null && columns.length >= 1) {
			columns[0].setResizable(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getRegistry()
	 */
	protected MarkerRegistry getRegistry() {
		if (registry == null) {
			registry = ProblemRegistry.getInstance();
			registry.setFilter(getFilter());
			registry.setInput((IResource) getViewerInput());
		}
		return registry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#createActions()
	 */
	protected void createActions() {
		super.createActions();
		propertiesAction = new ProblemPropertiesAction(this, getViewer());
		resolveAction = new ResolveMarkerAction(this, getViewer());
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#fillContextMenuAdditions(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenuAdditions(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(resolveAction);
	}

	protected void initActionBars(IActionBars actionBars) {
		super.initActionBars(actionBars);
		IMenuManager menu = actionBars.getMenuManager();
		menu.add(new Separator());
		menu.add(new ShowAction());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getColumnLayouts()
	 */
	protected ColumnLayoutData[] getDefaultColumnLayouts() {
		return DEFAULT_COLUMN_LAYOUTS;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#dispose()
	 */
	public void dispose() {
		super.dispose();
		resolveAction.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getFilter()
	 */
	protected IFilter getFilter() {
		if (filter == null) {
			filter = new ProblemFilter();
			filter.restoreState(getDialogSettings());
		}
		return filter;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getFiltersDialog()
	 */
	protected Dialog getFiltersDialog() {
		if (getFilter() != null && getFilter() instanceof ProblemFilter) {
			return new ProblemFiltersDialog(getSite().getShell(), (ProblemFilter) getFilter());
		}
		return super.getFiltersDialog();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#getViewerInput()
	 */
	protected Object getViewerInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

}
