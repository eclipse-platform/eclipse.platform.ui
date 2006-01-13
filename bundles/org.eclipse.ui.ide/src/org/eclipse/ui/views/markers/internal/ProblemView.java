/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The ProblemView is the view that displays problem markers.
 * 
 */
public class ProblemView extends MarkerView {

	private final static String[] ROOT_TYPES = { IMarker.PROBLEM };

	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.problem"; //$NON-NLS-1$

	private ActionResolveMarker resolveMarkerAction;

	private IActivityManagerListener activityManagerListener;

	private IField severity = new FieldSeverity();

	private IField category = new FieldCategory();

	private IField folder = new FieldFolder();

	private IField resource = new FieldResource();

	private IField message = new FieldMessage();

	private IField lineNumber = new FieldLineNumber();

	private IField creationTime = new FieldCreationTime();

	// Add the marker ID so the table sorter won't reduce
	// errors on the same line bug 82502
	private static IField id = new FieldId();

	private class GroupingAction extends Action {

		IField groupingField;

		ProblemView problemView;

		/**
		 * Create a new instance of the receiver.
		 * 
		 * @param label
		 * @param field
		 * @param view
		 */
		public GroupingAction(String label, IField field, ProblemView view) {
			super(label, IAction.AS_RADIO_BUTTON);

			groupingField = field;
			problemView = view;
			IField categoryField = view.getMarkerAdapter().getCategorySorter()
					.getCategoryField();
			if (categoryField == null)
				setChecked(groupingField == null);
			else
				setChecked(categoryField.equals(groupingField));
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run() {
			String description = Util.EMPTY_STRING;
			if (groupingField != null)
				description = groupingField.getDescription();
			IDEWorkbenchPlugin.getDefault().getPluginPreferences().setValue(
					IDEInternalPreferences.PROBLEMS_GROUPING, description);
			problemView.getMarkerAdapter().getCurrentMarkers().clearGroups();
			problemView.getMarkerAdapter().getCategorySorter()
					.setCategoryField(groupingField);
			problemView.refreshViewer();
			getMarkerAdapter().getCategorySorter().saveState(getDialogSettings());

		}
	}

	/**
	 * Return a new instance of the receiver.
	 */
	public ProblemView() {
		super();
		creationTime.setShowing(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#dispose()
	 */
	public void dispose() {
		if (resolveMarkerAction != null)
			resolveMarkerAction.dispose();

		PlatformUI.getWorkbench().getActivitySupport().getActivityManager()
				.removeActivityManagerListener(activityManagerListener);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#getSortingFields()
	 */
	protected IField[] getSortingFields() {
		return new IField[] { severity, category, folder, resource, message,
				lineNumber, creationTime,
				// Add the marker ID so the table sorter won't reduce
				// errors on the same line bug 82502
				id };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform
				.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings settings = workbenchSettings
				.getSection(TAG_DIALOG_SECTION);

		if (settings == null)
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);

		return settings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#createActions()
	 */
	protected void createActions() {
		super.createActions();
		propertiesAction = new ActionProblemProperties(this, getViewer());
		resolveMarkerAction = new ActionResolveMarker(this, getViewer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#fillContextMenuAdditions(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenuAdditions(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(resolveMarkerAction);
	}

	protected String[] getRootTypes() {
		return ROOT_TYPES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#getAllFields()
	 */
	protected IField[] getAllFields() {

		// Add the marker ID so the table sorter won't reduce
		// errors on the same line bug 82502
		return new IField[] { message, resource, folder, category, lineNumber,
				creationTime };
	}

	void updateTitle() {
		MarkerList visibleMarkers = getVisibleMarkers();
		String breakdown = formatSummaryBreakDown(visibleMarkers);
		int filteredCount = visibleMarkers.getItemCount();
		int totalCount = getTotalMarkers();
		if (filteredCount != totalCount)
			breakdown = NLS.bind(MarkerMessages.problem_filter_matchedMessage,
					new Object[] { breakdown, new Integer(filteredCount),
							new Integer(totalCount) });
		setContentDescription(breakdown);
	}

	private String formatSummaryBreakDown(MarkerList visibleMarkers) {
		return MessageFormat.format(
				MarkerMessages.problem_statusSummaryBreakdown, new Object[] {
						new Integer(visibleMarkers.getErrors()),
						new Integer(visibleMarkers.getWarnings()),
						new Integer(visibleMarkers.getInfos()) });
	}

	private String getSummary(MarkerList markers, String messageKey) {
		String message = NLS.bind(messageKey, new Object[] {
				new Integer(markers.getItemCount()),
				formatSummaryBreakDown(markers) });
		return message;
	}

	/**
	 * Retrieves statistical information (the total number of markers with each
	 * severity type) for the markers contained in the selection passed in. This
	 * information is then massaged into a string which may be displayed by the
	 * caller.
	 * 
	 * @param selection
	 *            a valid selection or <code>null</code>
	 * @return a message ready for display
	 */
	protected String updateSummarySelected(IStructuredSelection selection) {
		Collection selectionList;

		selectionList = new ArrayList();
		Iterator selectionIterator = selection.iterator();
		while (selectionIterator.hasNext()) {
			MarkerNode next = (MarkerNode) selectionIterator.next();
			if (next.isConcrete())
				selectionList.add(next);
		}

		return getSummary(new MarkerList(selectionList),
				"problem.statusSummarySelected"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerTypes()
	 */
	protected String[] getMarkerTypes() {
		return new String[] { IMarker.PROBLEM };
	}

	protected String getStaticContextId() {
		// TODO this context is missing - add it
		return PlatformUI.PLUGIN_ID + ".problem_view_context";//$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createFiltersDialog()
	 */
	protected DialogMarkerFilter createFiltersDialog() {

		MarkerFilter[] filters = getUserFilters();
		ProblemFilter[] problemFilters = new ProblemFilter[filters.length];
		System.arraycopy(filters, 0, problemFilters, 0, filters.length);
		return new DialogProblemFilter(getSite().getShell(), problemFilters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createFilter(java.lang.String)
	 */
	protected MarkerFilter createFilter(String name) {
		return new ProblemFilter(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getSectionTag()
	 */
	protected String getSectionTag() {
		return TAG_DIALOG_SECTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerEnablementPreferenceName()
	 */
	String getMarkerEnablementPreferenceName() {
		return IDEInternalPreferences.LIMIT_PROBLEMS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerLimitPreferenceName()
	 */
	String getMarkerLimitPreferenceName() {
		return IDEInternalPreferences.PROBLEMS_LIMIT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getFiltersPreferenceName()
	 */
	String getFiltersPreferenceName() {
		return IDEInternalPreferences.PROBLEMS_FILTERS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getAllFilters()
	 */
	MarkerFilter[] getAllFilters() {
		MarkerFilter[] userFilters = super.getAllFilters();
		Collection declaredFilters = MarkerSupportRegistry.getInstance()
				.getRegisteredFilters();
		Iterator iterator = declaredFilters.iterator();

		MarkerFilter[] allFilters = new MarkerFilter[userFilters.length
				+ declaredFilters.size()];
		System.arraycopy(userFilters, 0, allFilters, 0, userFilters.length);
		int index = userFilters.length;

		while (iterator.hasNext()) {
			allFilters[index] = (MarkerFilter) iterator.next();
			index++;
		}
		return allFilters;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#addDropDownContributions(org.eclipse.jface.action.IMenuManager)
	 */
	void addDropDownContributions(IMenuManager menu) {

		MenuManager groupByMenu = new MenuManager(MarkerMessages.ProblemView_GroupByMenu);
		groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_Severity, severity, this));
		groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_Category, category, this));
		
		Iterator definedGroups = MarkerSupportRegistry.getInstance().getMarkerGroups().iterator();
		
		while(definedGroups.hasNext()){
			FieldMarkerGroup group = (FieldMarkerGroup) definedGroups.next();
			groupByMenu.add(new GroupingAction(group.getDescription(),group,this));
		}
		
		groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_None, null, this));
		menu.add(groupByMenu);

		super.addDropDownContributions(menu);
	}

	/**
	 * Resize the category column in the table.
	 */
	protected void regenerateLayout() {
		TableLayout layout = new TableLayout();
		getViewer().getTree().setLayout(layout);

		ColumnLayoutData[] columnWidths = getDefaultColumnLayouts();
		for (int i = 0; i < columnWidths.length; i++) {
			layout.addColumnData(columnWidths[i]);

		}
		getViewer().getTree().layout(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#setSorter(org.eclipse.ui.views.markers.internal.TableSorter)
	 */
	void setSorter(TableSorter sorter2) {
		getMarkerAdapter().getCategorySorter().setTableSorter(sorter2);
		getMarkerAdapter().getCategorySorter().saveState(getDialogSettings());
		refreshViewer();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getTableSorter()
	 */
	public TableSorter getTableSorter() {
		return ((CategorySorter) getViewer().getSorter()).innerSorter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		createActivityManagerListener();
		PlatformUI.getWorkbench().getActivitySupport().getActivityManager()
				.addActivityManagerListener(activityManagerListener);
	}

	/**
	 * Create a new listener for activity changes.
	 */
	private void createActivityManagerListener() {
		activityManagerListener = new IActivityManagerListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.activities.IActivityManagerListener#activityManagerChanged(org.eclipse.ui.activities.ActivityManagerEvent)
			 */
			public void activityManagerChanged(
					ActivityManagerEvent activityManagerEvent) {
				clearEnabledFilters();
				getViewer().refresh();
			}
		};

	}

	/**
	 * Return the field whose description matches description.
	 * 
	 * @param description
	 * @return IField
	 */
	public IField findField(String description) {
		IField[] fields = getSortingFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getDescription().equals(description))
				return fields[i];
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#buildSorter()
	 */
	protected ViewerSorter buildSorter() {

		TableSorter sorter = TableSorter.createTableSorter(getSortingFields());
		sorter.restoreState(getDialogSettings());
		CategorySorter category = new CategorySorter(sorter);
		category.restoreState(getDialogSettings(), this);
		return category;
	}

}
