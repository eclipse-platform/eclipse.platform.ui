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
package org.eclipse.ui.views.properties.tabbed.internal.l10n;

import org.eclipse.osgi.util.NLS;

/**
 * Message Bundle class for the tabbed properties view plug-in.
 * 
 * @author Anthony Hunter
 * 
 */
public final class TabbedPropertyMessages
	extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ui.views.properties.tabbed.internal.l10n.TabbedPropertyMessages";//$NON-NLS-1$

	/**
	 * Constructor for TabbedPropertyMessages.
	 */
	private TabbedPropertyMessages() {
		// private constructor
	}

	public static String SectionDescriptor_Section_error;

	public static String TabDescriptor_Tab_error;

	public static String TabbedPropertyRegistry_Non_existing_tab;

	public static String TabbedPropertyRegistry_contributor_error;

	public static String TabbedPropertyList_properties_not_available;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TabbedPropertyMessages.class);
	}
}