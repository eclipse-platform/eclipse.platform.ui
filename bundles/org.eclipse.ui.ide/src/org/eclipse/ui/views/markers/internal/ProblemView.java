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
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The ProblemView is the view that displays problem markers.
 * 
 */
public class ProblemView extends MarkerView {

	// Direction constants - use the ones on TableSorter to stay sane
	private final static int ASCENDING = TableSorter.ASCENDING;

	private final static int DESCENDING = TableSorter.DESCENDING;

	private final IField[] VISIBLE_FIELDS = { new FieldHierarchy(),
			new FieldMessage(), new FieldResource(), new FieldFolder(),
			new FieldLineNumber() };

	private final IField[] SORTING_FIELDS = { new FieldSeverity(),
			new FieldMessage(), new FieldResource(), new FieldFolder(),
			new FieldLineNumber(), new FieldCreationTime(),
			// Add the marker ID so the table sorter won't reduce
			// errors on the same line bug 82502
			new FieldId(), new FieldProject(), new FieldMarkerType() };

	// Field Tags
	// These tags MUST occur in the same order as the VISIBLE_FIELDS +
	// HIDDEN_FIELDS appear. The TableSorter holds the priority and
	// direction order as a set of indices into an array of fields. This
	// array of fields is set on instantiation of TableSorter (see method
	// getSorter() in this (i.e. ProblemView) class). When we instantiate
	// TableSorter, we use the method TableView.getFields() as it is
	// inherited and we don't override it. TableView.getFields() will
	// return VISIBLE_FIELDS and then HIDDEN_FIELDS

	private final static int SEVERITY = 0;

	private final static int DESCRIPTION = 1;

	private final static int RESOURCE = 2;

	private final static int FOLDER = 3;

	private final static int LOCATION = 4;

	private final static int CREATION_TIME = 5;

	private final static int MARKER_ID = 6;

	private final static int PROJECT = 7;

	private final static int MARKER_TYPE = 8;

	private final static String[] ROOT_TYPES = { IMarker.PROBLEM };

	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.problem"; //$NON-NLS-1$

	private ActionResolveMarker resolveMarkerAction;

	/**
	 * Return a new instance of the receiver.
	 */
	public ProblemView() {
		super();
	}

	/**
	 * Get the default directions for the receiver.
	 * 
	 * @return int[]
	 */
	private int[] getDefaultDirections() {
		return new int[] { DESCENDING, // severity
				ASCENDING, // folder
				ASCENDING, // resource
				ASCENDING, // location
				ASCENDING, // description
				ASCENDING, // creation time
				ASCENDING, // marker id
				ASCENDING, // project
				ASCENDING // marker type
		};

	}

	/**
	 * Get the default priorities for the receiver.
	 * 
	 * @return int []
	 */
	private int[] getDefaultPriorities() {
		if (isHierarchalMode())
			return new int[] { MARKER_TYPE, PROJECT, SEVERITY, FOLDER,
					RESOURCE, DESCRIPTION, LOCATION, CREATION_TIME, MARKER_ID };
		return new int[] { SEVERITY, FOLDER, RESOURCE, LOCATION, DESCRIPTION,
				CREATION_TIME, MARKER_ID, MARKER_TYPE, PROJECT };
	}

	/**
	 * Return the width of the category field.
	 * 
	 * @return int
	 */
	private int getCategoryWidth() {
		if (isHierarchalMode())
			return 150;
		return 0;
	}

	public void dispose() {
		if (resolveMarkerAction != null)
			resolveMarkerAction.dispose();

		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#getSortingFields()
	 */
	protected IField[] getSortingFields() {
		return SORTING_FIELDS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#getDefaultColumnLayouts()
	 */
	protected ColumnPixelData[] getDefaultColumnLayouts() {
		return new ColumnPixelData[] { new ColumnPixelData(getCategoryWidth()),
				new ColumnPixelData(200), new ColumnPixelData(75),
				new ColumnPixelData(150), new ColumnPixelData(60) };
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
		propertiesAction = new ActionProblemProperties(this,
				getSelectionProvider());
		resolveMarkerAction = new ActionResolveMarker(this,
				getSelectionProvider());
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
	 * @see org.eclipse.ui.views.markers.internal.TableView#buildSorter()
	 */
	protected TableSorter buildSorter() {

		return new TableSorter(getSortingFields(), getDefaultPriorities(),
				getDefaultDirections());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#getVisibleFields()
	 */
	protected IField[] getVisibleFields() {
		return VISIBLE_FIELDS;
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
		return getSummary(new MarkerList(selection.toList()),
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
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#isHierarchalMode()
	 */
	public boolean isHierarchalMode() {
		return IDEWorkbenchPlugin.getDefault().getPluginPreferences()
				.getBoolean(IDEInternalPreferences.PROBLEMS_HIERARCHAL_MODE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#addDropDownContributions(org.eclipse.jface.action.IMenuManager)
	 */
	void addDropDownContributions(IMenuManager menu) {

		menu.add(getFlatAction());
		menu.add(getHierarchalAction());
		super.addDropDownContributions(menu);
	}

	/**
	 * Return the action for setting hierarchal mode.
	 * 
	 * @return IAction
	 */
	private IAction getHierarchalAction() {
		IAction hierarchalAction = new Action() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#getStyle()
			 */
			public int getStyle() {
				return AS_RADIO_BUTTON;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			public String getText() {
				return MarkerMessages.ProblemView_hierarchyMenu;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#getImageDescriptor()
			 */
			public ImageDescriptor getImageDescriptor() {
				return IDEInternalWorkbenchImages
						.getImageDescriptor(IDEInternalWorkbenchImages.IMG_LCL_HIERARCHICAL_LAYOUT);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
			 */
			public void runWithEvent(Event event) {
				if (isChecked()) {
					IDEWorkbenchPlugin
							.getDefault()
							.getPluginPreferences()
							.setValue(
									IDEInternalPreferences.PROBLEMS_HIERARCHAL_MODE,
									true);
					regeneratedLayout();
					getViewer().refresh();
				}
			}

		};

		hierarchalAction.setChecked(isHierarchalMode());
		return hierarchalAction;
	}

	/**
	 * Return the action for showing the flat layout.
	 * 
	 * @return IAction
	 */
	private IAction getFlatAction() {
		IAction flatAction = new Action() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#getStyle()
			 */
			public int getStyle() {
				return AS_RADIO_BUTTON;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			public String getText() {
				return MarkerMessages.ProblemView_flatMenu;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#getImageDescriptor()
			 */
			public ImageDescriptor getImageDescriptor() {
				return IDEInternalWorkbenchImages
						.getImageDescriptor(IDEInternalWorkbenchImages.IMG_LCL_FLAT_LAYOUT);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
			 */
			public void runWithEvent(Event event) {
				if (isChecked()) {
					IDEWorkbenchPlugin
							.getDefault()
							.getPluginPreferences()
							.setValue(
									IDEInternalPreferences.PROBLEMS_HIERARCHAL_MODE,
									false);
					regeneratedLayout() ;
					getViewer().refresh();
				}
			}

		};

		flatAction.setChecked(!isHierarchalMode());
		return flatAction;
	}

	/**
	 * Resize the category column in the table.
	 */
	protected void regeneratedLayout() {
		TableLayout layout = new TableLayout();
		getViewer().getTree().setLayout(layout);

		ColumnLayoutData[] columnWidths = getDefaultColumnLayouts();
		for (int i = 0; i < columnWidths.length; i++) {
			layout.addColumnData(columnWidths[i]);
			
		}
		getViewer().getTree().layout(true);
		
	}
}
