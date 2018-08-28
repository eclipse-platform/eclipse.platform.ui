/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.views.properties.tabbed.l10n;

import org.eclipse.osgi.util.NLS;

/**
 * Message Bundle class for the tabbed properties view plug-in.
 *
 * @author Anthony Hunter
 *
 */
public final class TabbedPropertyMessages
	extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.properties.tabbed.l10n.TabbedPropertyMessages";//$NON-NLS-1$

	/**
	 * Constructor for TabbedPropertyMessages.
	 */
	private TabbedPropertyMessages() {
		// private constructor
	}

    /**
     * Message when a property section extension is in error.
     */
	public static String SectionDescriptor_Section_error;

	/**
     * Message when a property section extension causes a class not found exception.
     */
	public static String SectionDescriptor_class_not_found_error;

    /**
     * Message when a property tab extension is in error.
     */
	public static String TabDescriptor_Tab_error;

    /**
     * Message when a property tab extension has an unknown category.
     */
	public static String TabDescriptor_Tab_unknown_category;

    /**
     * Message when a non existing tab is found in a property section extension.
     */
	public static String TabbedPropertyRegistry_Non_existing_tab;

    /**
     * Message when a property contributor extension is in error.
     */
	public static String TabbedPropertyRegistry_contributor_error;

    /**
     * No properties available message.
     */
	public static String TabbedPropertyList_properties_not_available;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TabbedPropertyMessages.class);
	}
}