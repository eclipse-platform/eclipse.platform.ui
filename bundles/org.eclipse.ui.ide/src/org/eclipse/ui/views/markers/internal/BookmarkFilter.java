/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

public class BookmarkFilter extends MarkerFilter {

	private final static String TAG_CONTAINS = "contains"; //$NON-NLS-1$

	private final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

	final static boolean DEFAULT_CONTAINS = true;

	final static String DEFAULT_DESCRIPTION = ""; //$NON-NLS-1$

	private boolean contains;

	private String description;

	/**
	 * Create a new instance of the recevier with the default name.
	 * 
	 */
	public BookmarkFilter() {
		this(MarkerMessages.MarkerFilter_defaultFilterName);
	}

	/**
	 * Create a new instance of the recevier with the filterName
	 * 
	 * @param filterName
	 */
	public BookmarkFilter(String filterName) {
		super(filterName, new String[] { IMarker.BOOKMARK });
	}

	/**
	 * Returns true iff the given marker is accepted by this filter
	 */
	public boolean selectMarker(ConcreteMarker marker) {
		return !isEnabled()
				|| (super.selectMarker(marker) && selectByDescription(marker));
	}

	private boolean selectByDescription(ConcreteMarker marker) {
		if (description == null || description.equals("")) { //$NON-NLS-1$
			return true;
		}

		String markerDescription = marker.getDescription();
		int index = markerDescription.indexOf(description);
		return contains ? (index >= 0) : (index < 0);
	}

	boolean getContains() {
		return contains;
	}

	String getDescription() {
		return description;
	}

	void setContains(boolean contains) {
		this.contains = contains;
	}

	void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#resetState()
	 */
	void resetState() {
		super.resetState();
		contains = DEFAULT_CONTAINS;
		description = DEFAULT_DESCRIPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#restoreFilterSettings(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	public void restoreFilterSettings(IDialogSettings settings) {

		super.restoreFilterSettings(settings);

		String setting = settings.get(TAG_CONTAINS);

		if (setting != null) {
			contains = Boolean.valueOf(setting).booleanValue();
		}

		setting = settings.get(TAG_DESCRIPTION);

		if (setting != null) {
			description = new String(setting);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#restoreFilterSettings(org.eclipse.ui.IMemento)
	 */
	protected void restoreFilterSettings(IMemento memento) {
		super.restoreFilterSettings(memento);

		String setting = memento.getString(TAG_CONTAINS);

		if (setting != null) {
			contains = Boolean.valueOf(setting).booleanValue();
		}

		setting = memento.getString(TAG_DESCRIPTION);

		if (setting != null) {
			description = new String(setting);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#saveFilterSettings(org.eclipse.ui.IMemento)
	 */
	public void saveFilterSettings(IMemento memento) {
		super.saveFilterSettings(memento);
		memento.putString(TAG_CONTAINS, String.valueOf(contains));
		memento.putString(TAG_DESCRIPTION, description);
	}

}
