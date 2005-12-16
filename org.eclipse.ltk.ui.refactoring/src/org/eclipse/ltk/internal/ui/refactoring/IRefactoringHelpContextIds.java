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
package org.eclipse.ltk.internal.ui.refactoring;

public interface IRefactoringHelpContextIds {

	// copied from JDT_UI so we still forward to this documentation since we are
	// placed in the same feature.
	public static final String PREFIX= "org.eclipse.jdt.ui" + '.'; //$NON-NLS-1$
	
	public static final String NEXT_PROBLEM_ACTION=			PREFIX + "next_problem_action";			//$NON-NLS-1$	
	public static final String PREVIOUS_PROBLEM_ACTION=		PREFIX + "previous_problem_action";		//$NON-NLS-1$
	
	public static final String NEXT_CHANGE_ACTION=			PREFIX + "next_change_action"; 	 //$NON-NLS-1$	
	public static final String PREVIOUS_CHANGE_ACTION=		PREFIX + "previous_change_action"; 	 //$NON-NLS-1$
	
	public static final String REFACTORING_PREVIEW_WIZARD_PAGE= PREFIX + "refactoring_preview_wizard_page_context"; //$NON-NLS-1$
	public static final String REFACTORING_ERROR_WIZARD_PAGE=	PREFIX + "refactoring_error_wizard_page_context";  //$NON-NLS-1$

	/** @since 3.2 */
	public static final String REFACTORING_HISTORY_WIZARD_PAGE= PREFIX + "refactoring_history_wizard_page_context"; //$NON-NLS-1$

	/** @since 3.2 */
	public static final String REFACTORING_HISTORY_DIALOG= PREFIX + "refactoring_history_dialog"; //$NON-NLS-1$

	/** @since 3.2 */
	public static final String REFACTORING_HISTORY_EXPORT_DIALOG= PREFIX + "refactoring_history_export_dialog"; //$NON-NLS-1$

	/** @since 3.2 */
	public static final String REFACTORING_APPLY_SCRIPT_PAGE= PREFIX + "refactoring_apply_script_page"; //$NON-NLS-1$
}
