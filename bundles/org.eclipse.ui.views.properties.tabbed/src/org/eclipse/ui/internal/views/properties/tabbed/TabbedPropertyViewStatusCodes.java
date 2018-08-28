/*******************************************************************************
 * Copyright (c) 2001, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.properties.tabbed;

/**
 * A list of status codes for this plug-in.
 *
 * @author Anthony Hunter
 */
public final class TabbedPropertyViewStatusCodes {

	/**
	 * This class should not be instantiated since it is a static constant
	 * class.
	 */
	private TabbedPropertyViewStatusCodes() {
		/* not used */
	}

	/**
	 * Status code indicating that everything is OK.
	 */
	public static final int OK = 0;

	/**
	 * Status code indicating that a tab was not found for the given tab id.
	 */
	public static final int NO_TAB_ERROR = 1;

	/**
	 * Status code indicating that issue was found loading the section extension
	 * configuration element.
	 */
	public static final int SECTION_ERROR = 2;

	/**
	 * Status code indicating that issue was found loading the tab extension
	 * configuration element.
	 */
	public static final int TAB_ERROR = 3;

	/**
	 * Status code indicating that issue was found loading the contributor
	 * extension configuration element.
	 */
	public static final int CONTRIBUTOR_ERROR = 4;
}
