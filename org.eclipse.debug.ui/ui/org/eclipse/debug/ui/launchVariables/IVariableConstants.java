/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.launchVariables;

public interface IVariableConstants {
	/**
	 * Variable that expands to the type of build (value <code>build_type</code>). See
	 * <code>BUILD_TYPE_*</code> constants for possible values.
	 */
	public static final String VAR_BUILD_TYPE = "build_type"; //$NON-NLS-1$
	/**
	 * Variable that expands to the current editor cursor column (value <code>editor_cur_col</code>).
	 */
	public static final String VAR_EDITOR_CUR_COL = "editor_cur_col"; //$NON-NLS-1$
	/**
	 * Variable that expands to the current editor cursor line (value <code>editor_cur_line</code>).
	 */
	public static final String VAR_EDITOR_CUR_LINE = "editor_cur_line"; //$NON-NLS-1$
	/**
	 * Variable that expands to the current editor selected text (value <code>editor_sel_text</code>).
	 */
	public static final String VAR_EDITOR_SEL_TEXT = "editor_sel_text"; //$NON-NLS-1$
	/**
	 * XML tag used to designate the root of the persisted IWorkingSet
	 */
	public static final String TAG_LAUNCH_CONFIGURATION_WORKING_SET= "launchConfigurationWorkingSet"; //$NON-NLS-1$
	/**
	 * XML tag used for setting / getting the factory ID of the persisted IWorkingSet
	 * Bug 37143
	 */	
	public static final String TAG_FACTORY_ID = "factoryID"; //$NON-NLS-1$
}