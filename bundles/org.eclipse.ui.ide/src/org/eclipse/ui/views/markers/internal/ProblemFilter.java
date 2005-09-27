/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IMemento;

public class ProblemFilter extends MarkerFilter {

	private static final String TAG_CONTAINS = "contains"; //$NON-NLS-1$

	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

	private static final String TAG_SELECT_BY_SEVERITY = "selectBySeverity"; //$NON-NLS-1$

	private static final String TAG_SEVERITY = "severity"; //$NON-NLS-1$

	final static boolean DEFAULT_CONTAINS = true;

	final static String DEFAULT_DESCRIPTION = ""; //$NON-NLS-1$	

	final static boolean DEFAULT_SELECT_BY_SEVERITY = false;

	final static int DEFAULT_SEVERITY = 0;

	public final static int SEVERITY_ERROR = 1 << 2;

	public final static int SEVERITY_WARNING = 1 << 1;

	public final static int SEVERITY_INFO = 1 << 0;

	private boolean contains;

	private String description;

	private boolean selectBySeverity;

	private int severity;
	
	private String id;
	
	/**
	 * Create a new instance of the receiver with name filterName.
	 * 
	 * @param filterName
	 *            A human readable name for the filter.
	 */
	public ProblemFilter(String filterName) {
		super(filterName, new String[] { IMarker.PROBLEM });

	}

	public boolean selectMarker(ConcreteMarker marker) {
		if (!(marker instanceof ProblemMarker)) {
			return false;
		}

		ProblemMarker problemMarker = (ProblemMarker) marker;

		return !isEnabled()
				|| (super.selectMarker(problemMarker)
						&& selectByDescription(problemMarker) && selectBySeverity(problemMarker));
	}

	private boolean selectByDescription(ConcreteMarker item) {
		if (description == null || description.equals("")) //$NON-NLS-1$
			return true;

		String markerDescription = item.getDescription();
		int index = markerDescription.indexOf(description);
		return contains ? (index >= 0) : (index < 0);
	}

	private boolean selectBySeverity(ProblemMarker item) {
		if (selectBySeverity) {
			int markerSeverity = item.getSeverity();

			if (markerSeverity == IMarker.SEVERITY_ERROR)
				return (severity & SEVERITY_ERROR) > 0;
			else if (markerSeverity == IMarker.SEVERITY_WARNING)
				return (severity & SEVERITY_WARNING) > 0;
			else if (markerSeverity == IMarker.SEVERITY_INFO)
				return (severity & SEVERITY_INFO) > 0;
		}

		return true;
	}

	public boolean getContains() {
		return contains;
	}

	public String getDescription() {
		return description;
	}

	public boolean getSelectBySeverity() {
		return selectBySeverity;
	}

	public int getSeverity() {
		return severity;
	}

	public void setContains(boolean contains) {
		this.contains = contains;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSelectBySeverity(boolean selectBySeverity) {
		this.selectBySeverity = selectBySeverity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#resetState()
	 */
	public void resetState() {
		super.resetState();
		contains = DEFAULT_CONTAINS;
		description = DEFAULT_DESCRIPTION;
		selectBySeverity = DEFAULT_SELECT_BY_SEVERITY;
		severity = DEFAULT_SEVERITY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#restoreFilterSettings(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	public void restoreFilterSettings(IDialogSettings settings) {

		super.restoreFilterSettings(settings);

		String setting = settings.get(TAG_CONTAINS);

		if (setting != null)
			contains = Boolean.valueOf(setting).booleanValue();

		setting = settings.get(TAG_DESCRIPTION);

		if (setting != null)
			description = new String(setting);

		setting = settings.get(TAG_SELECT_BY_SEVERITY);

		if (setting != null)
			selectBySeverity = Boolean.valueOf(setting).booleanValue();

		setting = settings.get(TAG_SEVERITY);

		if (setting != null)
			try {
				severity = Integer.parseInt(setting);
			} catch (NumberFormatException eNumberFormat) {
			}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#restoreFilterSettings(org.eclipse.ui.IMemento)
	 */
	protected void restoreFilterSettings(IMemento memento) {

		super.restoreFilterSettings(memento);

		String setting = memento.getString(TAG_CONTAINS);

		if (setting != null)
			contains = Boolean.valueOf(setting).booleanValue();

		setting = memento.getString(TAG_DESCRIPTION);

		if (setting != null)
			description = new String(setting);

		setting = memento.getString(TAG_SELECT_BY_SEVERITY);

		if (setting != null)
			selectBySeverity = Boolean.valueOf(setting).booleanValue();

		Integer severitySetting = memento.getInteger(TAG_SEVERITY);

		if (setting != null)
			severity = severitySetting.intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#saveFilterSettings(org.eclipse.ui.IMemento)
	 */
	public void saveFilterSettings(IMemento settings) {
		super.saveFilterSettings(settings);
		settings.putString(TAG_CONTAINS, String.valueOf(contains));
		settings.putString(TAG_DESCRIPTION, description);
		settings.putString(TAG_SELECT_BY_SEVERITY, String
				.valueOf(selectBySeverity));
		settings.putInteger(TAG_SEVERITY, severity);

	}

	/**
	 * Get the id of the filter. <code>null</code> if the
	 * filter is user defined.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the id to id.
	 * @param id String
	 */
	public void setId(String id) {
		this.id = id;
	}

}
