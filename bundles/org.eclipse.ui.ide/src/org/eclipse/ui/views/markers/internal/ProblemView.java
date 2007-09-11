/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de>
 *     	 - Fix for Bug 109361 [Markers] Multiselection in problems view yields invalid status message
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import com.ibm.icu.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * The ProblemView is the view that displays problem markers.
 * 
 */
public class ProblemView extends MarkerView {

	private final static String[] ROOT_TYPES = { IMarker.PROBLEM };

	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.problem"; //$NON-NLS-1$

	private static final String TAG_SYSTEM_FILTER_ENTRY = "systemFilter";//$NON-NLS-1$

	private ActionResolveMarker resolveMarkerAction;

	private IHandlerService handlerService;

	private IHandlerActivation resolveMarkerHandlerActivation;

	private IActivityManagerListener activityManagerListener;

	private IField severityAndMessage = new FieldSeverityAndMessage();

	private IField folder = new FieldFolder();

	private IField resource = new FieldResource();

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
			if (categoryField == null) {
				setChecked(groupingField == null);
			} else {
				setChecked(categoryField.equals(groupingField));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run() {

			if (isChecked()) {
				Job categoryJob = new Job(
						MarkerMessages.ProblemView_UpdateCategoryJob) {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
					 */
					protected IStatus run(IProgressMonitor monitor) {
						try {
							markerProcessJob.join();
						} catch (InterruptedException e) {
							return Status.CANCEL_STATUS;
						}
						problemView.selectCategoryField(groupingField,
								problemView.getMarkerAdapter()
										.getCategorySorter());

						getMarkerAdapter().getCategorySorter().saveState(
								getDialogSettings());
						return Status.OK_STATUS;
					}
				};
				categoryJob.setSystem(true);
				problemView.preserveSelection();

				IWorkbenchSiteProgressService progressService = getProgressService();
				if (progressService == null)
					categoryJob.schedule();
				else
					getProgressService().schedule(categoryJob);

			}

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
		if (resolveMarkerAction != null) {
			resolveMarkerAction.dispose();
		}
		if (resolveMarkerHandlerActivation != null && handlerService != null) {
			handlerService.deactivateHandler(resolveMarkerHandlerActivation);
		}

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
		return new IField[] { severityAndMessage, folder, resource, lineNumber,
				creationTime,
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
		IDialogSettings workbenchSettings = IDEWorkbenchPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings settings = workbenchSettings
				.getSection(TAG_DIALOG_SECTION);

		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}

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
	 * @see org.eclipse.ui.views.internal.tableview.TableView#registerGlobalActions(org.eclipse.ui.IActionBars)
	 */
	protected void registerGlobalActions(IActionBars actionBars) {
		super.registerGlobalActions(actionBars);

		String quickFixId = "org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals"; //$NON-NLS-1$
		resolveMarkerAction.setActionDefinitionId(quickFixId);

		handlerService = (IHandlerService) getViewSite().getService(
				IHandlerService.class);
		if (handlerService != null) {
			resolveMarkerHandlerActivation = handlerService.activateHandler(
					quickFixId, new ActionHandler(resolveMarkerAction));
		}
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
		return new IField[] { severityAndMessage, resource, folder, lineNumber,
				creationTime };
	}

	void updateTitle() {
		MarkerList visibleMarkers = getVisibleMarkers();
		String breakdown = formatSummaryBreakDown(visibleMarkers);
		int filteredCount = visibleMarkers.getItemCount();
		int totalCount = getTotalMarkers();
		if (filteredCount != totalCount) {
			breakdown = NLS.bind(MarkerMessages.problem_filter_matchedMessage,
					new Object[] { breakdown, new Integer(filteredCount),
							new Integer(totalCount) });
		}
		setContentDescription(breakdown);
	}

	private String formatSummaryBreakDown(MarkerList visibleMarkers) {
		return MessageFormat.format(
				MarkerMessages.problem_statusSummaryBreakdown, new Object[] {
						new Integer(visibleMarkers.getErrors()),
						new Integer(visibleMarkers.getWarnings()),
						new Integer(visibleMarkers.getInfos()) });
	}

	private String getSummary(MarkerList markers) {
		String message = MessageFormat.format(
				MarkerMessages.marker_statusSummarySelected, new Object[] {
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
			if (next.isConcrete()) {
				selectionList.add(next);
			}
		}

		return getSummary(new MarkerList(selectionList));
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

		MenuManager groupByMenu = new MenuManager(
				MarkerMessages.ProblemView_GroupByMenu);

		Iterator definedGroups = MarkerSupportRegistry.getInstance()
				.getMarkerGroups().iterator();

		while (definedGroups.hasNext()) {
			MarkerGroup group = (MarkerGroup) definedGroups.next();
			groupByMenu.add(new GroupingAction(group.getField()
					.getDescription(), group.getField(), this));
		}

		groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_None,
				null, this));
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
	void setComparator(TableComparator sorter2) {
		getMarkerAdapter().getCategorySorter().setTableSorter(sorter2);
		getMarkerAdapter().getCategorySorter().saveState(getDialogSettings());
		updateForNewComparator(sorter2);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getTableSorter()
	 */
	public TableComparator getTableSorter() {
		return ((CategoryComparator) getViewer().getComparator()).innerSorter;
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
				refreshViewer();
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
			if (fields[i].getDescription().equals(description)) {
				return fields[i];
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#buildComparator()
	 */
	protected ViewerComparator buildComparator() {

		TableComparator sorter = createTableComparator();
		CategoryComparator category = new CategoryComparator(sorter);
		category.restoreState(getDialogSettings(), this);
		return category;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#canBeEditable()
	 */
	boolean canBeEditable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#initToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void initToolBar(IToolBarManager tbm) {
		tbm.add(getFilterAction());
		tbm.update(false);
	}

	/**
	 * Select the category for the receiver.
	 * 
	 * @param description
	 * @param sorter -
	 *            the sorter to select for
	 */
	public void selectCategory(String description, CategoryComparator sorter) {

		if (description == null)
			selectCategoryField(null, sorter);

		Iterator definedGroups = MarkerSupportRegistry.getInstance()
				.getMarkerGroups().iterator();
		while (definedGroups.hasNext()) {
			MarkerGroup group = (MarkerGroup) definedGroups.next();
			if (group.getField().getDescription().equals(description)) {
				selectCategoryField(group.getField(), sorter);
				return;
			}
		}
		selectCategoryField(null, sorter);

	}

	/**
	 * Select the field groupingField.
	 * 
	 * @param groupingField
	 * @param sorter
	 */
	void selectCategoryField(IField groupingField, CategoryComparator sorter) {
		sorter.setCategoryField(groupingField);

		// Do not refresh if the input has not been set yet
		if (getMarkerAdapter() != null) {
			getMarkerAdapter().getCurrentMarkers().clearGroups();
			refreshViewer();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#writeFiltersSettings(org.eclipse.ui.XMLMemento)
	 */
	protected void writeFiltersSettings(XMLMemento memento) {
		super.writeFiltersSettings(memento);

		// Add the system filters
		Iterator filters = MarkerSupportRegistry.getInstance()
				.getRegisteredFilters().iterator();

		while (filters.hasNext()) {
			MarkerFilter filter = (MarkerFilter) filters.next();
			IMemento child = memento.createChild(TAG_SYSTEM_FILTER_ENTRY,
					filter.getName());
			child.putString(MarkerFilter.TAG_ENABLED, String.valueOf(filter
					.isEnabled()));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#restoreFilters(org.eclipse.ui.IMemento)
	 */
	void restoreFilters(IMemento memento) {

		super.restoreFilters(memento);

		if (memento == null)
			return;

		IMemento[] sections = memento.getChildren(TAG_SYSTEM_FILTER_ENTRY);

		Collection registered = MarkerSupportRegistry.getInstance()
				.getRegisteredFilters();
		MarkerFilter[] filters = new MarkerFilter[registered.size()];
		registered.toArray(filters);

		if (sections != null) {

			for (int i = 0; i < sections.length; i++) {
				String filterName = sections[i].getID();
				boolean enabled = Boolean.valueOf(
						sections[i].getString(MarkerFilter.TAG_ENABLED))
						.booleanValue();
				setEnablement(filterName, enabled, filters);

			}
		}

	}

	/**
	 * Set the enablement state of the filter called filterName to enabled.
	 * 
	 * @param filterName
	 * @param enabled
	 * @param filters
	 */
	private void setEnablement(String filterName, boolean enabled,
			MarkerFilter[] filters) {
		for (int i = 0; i < filters.length; i++) {
			if (filters[i].getName().equals(filterName)) {
				filters[i].setEnabled(enabled);
				return;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerName()
	 */
	protected String getMarkerName() {
		return MarkerMessages.problem_title;
	}
}
