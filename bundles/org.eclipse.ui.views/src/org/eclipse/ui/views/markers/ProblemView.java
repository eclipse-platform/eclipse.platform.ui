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

package org.eclipse.ui.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.markers.internal.ActionProblemProperties;
import org.eclipse.ui.views.markers.internal.ActionResolveMarker;
import org.eclipse.ui.views.markers.internal.ActionShowOnBuild;
import org.eclipse.ui.views.markers.internal.DialogProblemFilter;
import org.eclipse.ui.views.markers.internal.FieldCreationTime;
import org.eclipse.ui.views.markers.internal.FieldFolder;
import org.eclipse.ui.views.markers.internal.FieldLineNumber;
import org.eclipse.ui.views.markers.internal.FieldMessage;
import org.eclipse.ui.views.markers.internal.FieldResource;
import org.eclipse.ui.views.markers.internal.FieldSeverity;
import org.eclipse.ui.views.markers.internal.IField;
import org.eclipse.ui.views.markers.internal.IFilter;
import org.eclipse.ui.views.markers.internal.MarkerRegistry;
import org.eclipse.ui.views.markers.internal.MarkerView;
import org.eclipse.ui.views.markers.internal.Messages;
import org.eclipse.ui.views.markers.internal.ProblemFilter;
import org.eclipse.ui.views.markers.internal.TableSorter;

public class ProblemView extends MarkerView {
	
	private final static ColumnLayoutData[] DEFAULT_COLUMN_LAYOUTS = { 
		new ColumnPixelData(19, false), 
		new ColumnWeightData(200), 
		new ColumnWeightData(75), 
		new ColumnWeightData(150), 
		new ColumnWeightData(60) 
	};
		
	// Direction constants - use the ones on TableSorter to stay sane
	private final static int ASCENDING = TableSorter.ASCENDING;
	private final static int DESCENDING = TableSorter.DESCENDING;
	
	private final static IField[] VISIBLE_FIELDS = { 
		new FieldSeverity(), 
		new FieldMessage(), 
		new FieldResource(), 
		new FieldFolder(), 
		new FieldLineNumber() 
	};
	
	private final static IField[] HIDDEN_FIELDS = { 
		new FieldCreationTime() 
	};
	
	// Field Tags
	// These tags MUST occur in the same order as the VISIBLE_FIELDS +
	// HIDDEN_FIELDS appear.  The TableSorter holds the priority and
	// direction order as a set of indices into an array of fields.  This
	// array of fields is set on instantiation of TableSorter (see method
	// getSorter() in this (i.e. ProblemView) class).  When we instantiate
	// TableSorter, we use the method TableView.getFields() as it is 
	// inherited and we don't override it.  TableView.getFields() will
	// return VISIBLE_FIELDS and then HIDDEN_FIELDS
	private final static int SEVERITY = 0;
	private final static int DESCRIPTION = 1;
	private final static int RESOURCE = 2;
	private final static int FOLDER = 3;
	private final static int LOCATION = 4;
	private final static int CREATION_TIME = 5;
	
	private final static int[] DEFAULT_PRIORITIES = 
		{ SEVERITY,
		  FOLDER,
		  RESOURCE,
		  LOCATION,
		  DESCRIPTION,
		  CREATION_TIME };

	private final static int[] DEFAULT_DIRECTIONS = 
		{ DESCENDING,	// severity
		  ASCENDING,    // folder
		  ASCENDING,	// resource
		  ASCENDING,	// location
		  ASCENDING,	// description
		  ASCENDING, };	// creation time
									
	private final static String[] ROOT_TYPES = { 
		IMarker.PROBLEM 
	};
	
	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.problem"; //$NON-NLS-1$
	
	private MarkerRegistry markerRegistry;
	private ProblemFilter problemFilter;
	private ActionResolveMarker resolveMarkerAction;
	private TableSorter sorter;

	private static final int ERRORS = 0;
	private static final int WARNINGS = 1;
	private static final int INFOS = 2;

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
		propertiesAction = new ActionProblemProperties(this, getViewer());
		resolveMarkerAction = new ActionResolveMarker(this, getViewer());
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
		return new DialogProblemFilter(getSite().getShell(), problemFilter);
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
	
	protected TableSorter getSorter() {
		if (sorter == null)
			sorter = new TableSorter(getFields(), DEFAULT_PRIORITIES, DEFAULT_DIRECTIONS);
		return sorter;
	}

	protected Object getViewerInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	protected IField[] getVisibleFields() {
		return VISIBLE_FIELDS;
	}
	
	protected void initMenu(IMenuManager menu ) {
		super.initMenu(menu);
		menu.add(new Separator());
		menu.add(new ActionShowOnBuild());
	}
	
	public IStructuredSelection getSelection() {
		// TODO: added because nick doesn't like public API inherited from internal classes
		return super.getSelection();
	}

	public void setSelection(IStructuredSelection structuredSelection, boolean reveal) {
		// TODO: added because nick doesn't like public API inherited from internal classes
		super.setSelection(structuredSelection, reveal);
	}
	
	/**
	 * This method will take as parameter a list of all the markers we
	 * wish to get statistical information on.  Each marker will be examined
	 * to determine its severity.  The resulting int array will contain a
	 * count of the total number of markers with each of the severities:  error,
	 * warning or info.
	 * <p>
	 * @param regList the list of markers we wish to get stats for
	 * @return a 3-element int array giving the number of markers in regList
	 *   that have severity ERROR, WARNING, and INFO.</p>
	 */
	private int[] getMarkerCounts (Object[] regList) {
		int[] visibleMarkerCounts = {0, 0, 0};
		if (regList == null || regList.length == 0)
			return visibleMarkerCounts;
		for (int i = 0; i < regList.length; i++) {
			IMarker marker = (IMarker)regList[i];
			int severity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			switch (severity) {
				case IMarker.SEVERITY_ERROR:
					visibleMarkerCounts[ERRORS]++;
					break;
				case IMarker.SEVERITY_INFO:
					visibleMarkerCounts[INFOS]++;
					break;
				case IMarker.SEVERITY_WARNING:
					visibleMarkerCounts[WARNINGS]++;
					break;
			}
		}
		return visibleMarkerCounts;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#updateStatusMessage(org.eclipse.jface.viewers.IStructuredSelection)
	 * <p>
	 * This method has been overwritten from the default version in 
	 * MarkerView.  For Problem views, the status area will indicate the
	 * total number of items in the view and how many of them have severity
	 * 'error', 'warning' or 'info' if the selection passed in is null
	 * or has a size of 0 (i.e. nothing is selected in the view).  If more
	 * than 1 item is selected, the same information will be displayed but
	 * only for the selected items.  If only 1 item is selected, the 'message'
	 * attribute of this marker will be displayed.</p>
	 */
	protected void updateStatusMessage(IStructuredSelection selection) {
		String message = ""; //$NON-NLS-1$
		
		if (selection == null || selection.size() == 0){
			// Show stats on all items in the view
			message = updateSummaryVisible();
		} else if (selection.size() == 1) {
			// Use the Message attribute of the marker
			IMarker marker = (IMarker)selection.getFirstElement();
			message = marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
		} else if (selection.size() > 1) {
			// Show stats on only those items in the selection
			message = updateSummarySelected(selection);
		}
		getViewSite().getActionBars().getStatusLineManager().setMessage(message);
	}

	/**
	 * updateSummaryVisible will retrieve statistical information (the
	 * total number of markers with each severity type) for the markers
	 * contained in the marker registry for this view.  This information
	 * will then be massaged into a String which may be displayed by the
	 * caller.
	 * <p>
	 * @return a String message ready for display</p>
	 */
	private String updateSummaryVisible() {
		Object[] regList = getRegistry().getElements();
		int[] visibleMarkerCounts = getMarkerCounts(regList);
		String message = Messages.format(
			"problem.statusSummaryVisible", //$NON-NLS-1$
			new Object[] {
				new Integer(visibleMarkerCounts[ERRORS] + visibleMarkerCounts[INFOS] + visibleMarkerCounts[WARNINGS]),
				Messages.format(
					"problem.statusSummaryBreakdown", //$NON-NLS-1$
					new Object[] {
						new Integer(visibleMarkerCounts[ERRORS]),
						new Integer(visibleMarkerCounts[WARNINGS]),
						new Integer(visibleMarkerCounts[INFOS])})
			});
		return message;
	}
	
	/**
	 * updateSummarySelected will retrieve statistical information (the
	 * total number of markers with each severity type) for the markers
	 * contained in the selection passed in.  This information will then
	 * be massaged into a String which may be displayed by the caller.
	 * <p>
	 * @param selection may be null or a valid IStructuredSelection
	 * @return a String message ready for display</p>
	 */
	private String updateSummarySelected(IStructuredSelection selection) {
		int[] selectedMarkerCounts = getMarkerCounts(selection.toArray());
		String message = Messages.format(
			"problem.statusSummarySelected", //$NON-NLS-1$
			new Object[] {
				new Integer(selectedMarkerCounts[ERRORS] + selectedMarkerCounts[INFOS] + selectedMarkerCounts[WARNINGS]),
				Messages.format(
					"problem.statusSummaryBreakdown", //$NON-NLS-1$
					new Object[] {
						new Integer(selectedMarkerCounts[ERRORS]),
						new Integer(selectedMarkerCounts[WARNINGS]),
						new Integer(selectedMarkerCounts[INFOS])})
			});
		return message;
	}
}
