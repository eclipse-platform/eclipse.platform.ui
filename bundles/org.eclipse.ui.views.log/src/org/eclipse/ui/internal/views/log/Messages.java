/*******************************************************************************
 * Copyright (c) 2007, 2019 IBM Corporation and others.
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
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bugs 202583, 207344
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 218648
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String LogReader_warn_noEntryWithinMaxLogTailSize;

	public static String LogView_column_message;
	public static String LogView_column_plugin;
	public static String LogView_column_date;
	public static String LogView_clear;
	public static String LogView_clear_tooltip;
	public static String LogView_copy;
	public static String LogView_delete;
	public static String LogView_delete_tooltip;
	public static String LogView_export;
	public static String LogView_exportLog;
	public static String LogView_export_tooltip;
	public static String LogView_exportEntry;
	public static String LogView_exportLogEntry;
	public static String LogView_exportEntry_tooltip;
	public static String LogView_import;
	public static String LogView_import_tooltip;
	public static String LogView_filter;
	public static String LogView_readLog_reload;
	public static String LogView_readLog_restore;
	public static String LogView_readLog_restore_tooltip;
	public static String LogView_show_filter_text;
	public static String LogView_show_filter_initialText;
	
	public static String LogView_SessionStarted;
	public static String LogView_severity_error;
	public static String LogView_severity_warning;
	public static String LogView_severity_info;
	public static String LogView_severity_ok;
	public static String LogView_confirmDelete_title;
	public static String LogView_confirmDelete_message;
	public static String LogView_confirmDelete_deleteButton;
	public static String LogView_confirmOverwrite_message;
	public static String LogView_operation_importing;
	public static String LogView_operation_reloading;
	public static String LogView_activate;
	public static String LogView_AddingBatchedEvents;
	public static String LogView_view_currentLog;
	public static String LogView_view_currentLog_tooltip;
	public static String LogView_properties_tooltip;

	public static String LogView_FileCouldNotBeFound;
	public static String LogView_FilterDialog_title;
	public static String LogView_FilterDialog_eventTypes;
	public static String LogView_FilterDialog_information;
	public static String LogView_FilterDialog_warning;
	public static String LogView_FilterDialog_error;
	public static String LogView_FilterDialog_limitTo;
	public static String LogView_FilterDialog_maxLogTailSize;
	public static String LogView_FilterDialog_eventsLogged;
	public static String LogView_FilterDialog_allSessions;
	public static String LogView_FilterDialog_ok;
	public static String LogView_FilterDialog_recentSession;
	public static String LogView_GroupBy;
	public static String LogView_GroupByNone;
	public static String LogView_GroupByPlugin;
	public static String LogView_GroupBySession;
	public static String LogView_LogFileTitle;
	public static String LogView_OpenFile;
	public static String LogView_WorkspaceLogFile;

	public static String LogViewLabelProvider_Session;
	public static String LogViewLabelProvider_truncatedMessage;

	public static String EventDetailsDialog_title;
	public static String EventDetailsDialog_action;
	public static String EventDetailsDialog_plugIn;
	public static String EventDetailsDialog_severity;
	public static String EventDetailsDialog_date;
	public static String EventDetailsDialog_message;
	public static String EventDetailsDialog_exception;
	public static String EventDetailsDialog_session;
	public static String EventDetailsDialog_noStack;
	public static String EventDetailsDialog_previous;
	public static String EventDetailsDialog_next;
	public static String EventDetailsDialog_copy;
	public static String EventDetailsDialog_FilterDialog;
	public static String EventDetailsDialog_ShowFilterDialog;

	public static String FilterDialog_Add;
	public static String FilterDialog_AddFilterTitle;
	public static String FilterDialog_AddFliterLabel;
	public static String FilterDialog_EnableFiltersCheckbox;
	public static String FilterDialog_FilterShouldntContainSemicolon;
	public static String FilterDialog_Remove;

	public static String OpenLogDialog_title;
	public static String OpenLogDialog_message;
	public static String OpenLogDialog_cannotDisplay;

	public static String ImportLogAction_noLaunchHistory;
	public static String ImportLogAction_reloadWorkspaceLog;

	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.log.messages"; //$NON-NLS-1$

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
