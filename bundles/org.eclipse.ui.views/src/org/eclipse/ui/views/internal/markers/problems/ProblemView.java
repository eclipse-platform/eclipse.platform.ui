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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.internal.markers.CreationTime;
import org.eclipse.ui.views.internal.markers.Folder;
import org.eclipse.ui.views.internal.markers.IField;
import org.eclipse.ui.views.internal.markers.IFilter;
import org.eclipse.ui.views.internal.markers.LineNumber;
import org.eclipse.ui.views.internal.markers.MarkerRegistry;
import org.eclipse.ui.views.internal.markers.MarkerView;
import org.eclipse.ui.views.internal.markers.Message;
import org.eclipse.ui.views.internal.markers.Resource;

public class ProblemView extends MarkerView {
	
	private final static ColumnLayoutData[] DEFAULT_COLUMN_LAYOUTS = { 
		new ColumnPixelData(19, false), 
		new ColumnWeightData(200), 
		new ColumnWeightData(75), 
		new ColumnWeightData(150), 
		new ColumnWeightData(60) 
	};
		
	private final static IField[] HIDDEN_FIELDS = { 
		new CreationTime() 
	};
	
	private final static String[] ROOT_TYPES = { 
		IMarker.PROBLEM 
	};
	
	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.problem"; //$NON-NLS-1$
	
	private final static IField[] VISIBLE_FIELDS = { 
		new ProblemSeverity(), 
		new Message(), 
		new Resource(), 
		new Folder(), 
		new LineNumber() 
	};
	
	private MarkerRegistry markerRegistry;
	private ProblemFilter problemFilter;
	private ResolveMarkerAction resolveMarkerAction;

	public void dispose() {
		if (resolveMarkerAction != null)
			resolveMarkerAction.dispose();
		
		super.dispose();
	}

	public void init(IViewSite viewSite, IMemento memento) throws PartInitException {
		super.init(viewSite, memento);
		problemFilter = new ProblemFilter();
		IDialogSettings dialogSettings = getDialogSettings();
		
		if (problemFilter != null)
			problemFilter.restoreState(dialogSettings);
			
		markerRegistry = new MarkerRegistry();
		markerRegistry.setType(IMarker.PROBLEM); 		
		markerRegistry.setFilter(problemFilter);
		markerRegistry.setInput((IResource) getViewerInput());
	}

	public void saveState(IMemento memento) {
		IDialogSettings dialogSettings = getDialogSettings();
		
		if (problemFilter != null)
			problemFilter.saveState(dialogSettings);
		
		super.saveState(memento);	
	}

	protected ColumnLayoutData[] getDefaultColumnLayouts() {
		return DEFAULT_COLUMN_LAYOUTS;
	}

	protected IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);
		
		if (settings == null)
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);

		return settings;
	}

	protected void createActions() {
		super.createActions();
		propertiesAction = new ProblemPropertiesAction(this, getViewer());
		resolveMarkerAction = new ResolveMarkerAction(this, getViewer());
	}
	
	protected void createColumns(Table table) {
		super.createColumns(table);
		TableColumn[] columns = table.getColumns();
		
		if (columns != null && columns.length >= 1)
			columns[0].setResizable(false);
	}

	protected void fillContextMenuAdditions(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(resolveMarkerAction);
	}

	protected IFilter getFilter() {
		return problemFilter;
	}
	
	protected Dialog getFiltersDialog() {
		return new ProblemFiltersDialog(getSite().getShell(), problemFilter);
	}
	
	protected IField[] getHiddenFields() {
		return HIDDEN_FIELDS;
	}

	protected MarkerRegistry getRegistry() {
		return markerRegistry;
	}

	protected String[] getRootTypes() {
		return ROOT_TYPES;
	}

	protected Object getViewerInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	protected IField[] getVisibleFields() {
		return VISIBLE_FIELDS;
	}
	
	protected void initActionBars(IActionBars actionBars) {
		super.initActionBars(actionBars);
		IMenuManager menu = actionBars.getMenuManager();
		menu.add(new Separator());
		menu.add(new ShowAction());
	}
}
