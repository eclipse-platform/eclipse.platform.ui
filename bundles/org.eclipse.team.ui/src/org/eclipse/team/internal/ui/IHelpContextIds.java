/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui;

public interface IHelpContextIds {
	public static final String PREFIX = TeamUIPlugin.ID + "."; //$NON-NLS-1$

	// Preference Pages
	public static final String TEAM_PREFERENCE_PAGE = PREFIX + "team_preference_page_context"; //$NON-NLS-1$
	public static final String IGNORE_PREFERENCE_PAGE = PREFIX + "ignore_preference_page_context"; //$NON-NLS-1$
	public static final String FILE_TYPE_PREFERENCE_PAGE = PREFIX + "file_type_preference_page_context"; //$NON-NLS-1$
	
	// Wizard Pages
	public static final String SHARE_PROJECT_PAGE = PREFIX + "share_project_page_context"; //$NON-NLS-1$
	public static final String IMPORT_PROJECT_SET_PAGE = PREFIX + "import_project_set_page_context"; //$NON-NLS-1$
	public static final String EXPORT_PROJECT_SET_PAGE = PREFIX + "export_project_set_page_context"; //$NON-NLS-1$
	
}
