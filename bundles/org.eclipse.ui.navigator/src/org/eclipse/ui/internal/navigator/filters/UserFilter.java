/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - 226046 Allow user to specify filters
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.filters;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * This class is a bean that represents a resource filter configured by user
 * based on a regexp for resource name. They can be edited via the {@link UserFiltersTab}, are supposed to be attached
 * as a data to {@link CommonViewer#setData(String, Object)} with key {@link NavigatorPlugin#RESOURCE_REGEXP_FILTER_DATA}.
 * The view or UI element that creates the {@link CommonViewer} is supposed to set the data with the right value. A typical
 * usage is to load initial filters from a {@link IMemento} or {@link PreferenceStore}
 */
public class UserFilter {
	private String regexp = "*.something"; //$NON-NLS-1$
	private boolean enabled = false;
	private SearchPattern searchPattern;

	/**
	 * Instantiate a new UserFilter with default values
	 */
	public UserFilter() { }

	/**
	 * Instantiates a new UserFilter with specified value
	 * @param regexp
	 * @param enable
	 */
	public UserFilter(String regexp, boolean enable) {
		this.regexp = regexp;
		this.enabled = enable;
	}

	/**
	 *
	 * @return the regexp configured by this filter
	 */
	public String getRegexp() {
		return this.regexp;
	}

	/**
	 *
	 * @param newRegexp a new value for the regexp
	 */
	public void setRegexp(String newRegexp) {
		this.regexp = newRegexp;
	}

	/**
	 *
	 * @return whether this filter should be processed or not.
	 * If not enabled, a filter shouldn't have effect on the content
	 * of the viewer.
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 *
	 * @param enable whether to enable this filter or not in the viewer
	 */
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}

	/**
	 * @param string
	 * @return whether the given string matches the filter definition
	 */
	public boolean matches(String string) {
		if (this.searchPattern == null) {
			this.searchPattern = new SearchPattern();
			this.searchPattern.setPattern(this.regexp);
		}
		return this.searchPattern.matches(string);
	}
}