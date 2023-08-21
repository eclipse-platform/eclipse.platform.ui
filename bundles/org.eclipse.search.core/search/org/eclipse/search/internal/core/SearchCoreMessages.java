/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Christian Walther (Indel AG) - Bug 399094: Add whole word option to file search
 *     Marco Descher <marco@descher.at> - Open Search dialog with previous page instead of using the current selection to detect the page - http://bugs.eclipse.org/33710
 *     Lucas Bullen (Red Hat Inc.) - [Bug 526453] disambiguate "Selected Resources"
 *     Red Hat Inc. - add support for filtering innermost project files for file search
 *******************************************************************************/
package org.eclipse.search.internal.core;

import org.eclipse.osgi.util.NLS;

public final class SearchCoreMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.search.internal.core.SearchCoreMessages";//$NON-NLS-1$


	private SearchCoreMessages() {
		// Do not instantiate
	}

	public static String TextSearchEngineRegistry_defaulttextsearch_label;
	public static String TextSearchVisitor_canceled;
	public static String TextSearchVisitor_filesearch_task_label;
	public static String TextSearchEngine_statusMessage;
	public static String SearchPlugin_internal_error;
	public static String PatternConstructor_error_escape_sequence;
	public static String PatternConstructor_error_hex_escape_sequence;
	public static String PatternConstructor_error_line_delim_position;
	public static String PatternConstructor_error_unicode_escape_sequence;
	public static String TextSearchVisitor_patterntoocomplex0;
	public static String TextSearchVisitor_scanning;
	public static String TextSearchVisitor_error;
	public static String TextSearchVisitor_unsupportedcharset;
	public static String TextSearchVisitor_illegalcharset;
	static {
		NLS.initializeMessages(BUNDLE_NAME, SearchCoreMessages.class);
	}

}
