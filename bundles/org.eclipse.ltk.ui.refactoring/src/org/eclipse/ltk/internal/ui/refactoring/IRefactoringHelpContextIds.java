/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring;

/**
 * Help context ids for the refactoring user interface.
 * <p>
 * The ids still forward to the <code>org.eclipse.jdt.ui</code> namespace,
 * since the documentation is in the <code>org.eclipse.jdt.ui</code> feature.
 * </p>
 */
public interface IRefactoringHelpContextIds {

	String PREFIX= "org.eclipse.jdt.ui" + '.'; //$NON-NLS-1$

	String NEXT_CHANGE_ACTION= PREFIX + "next_change_action"; //$NON-NLS-1$

	String NEXT_PROBLEM_ACTION= PREFIX + "next_problem_action"; //$NON-NLS-1$

	String PREVIOUS_CHANGE_ACTION= PREFIX + "previous_change_action"; //$NON-NLS-1$

	String PREVIOUS_PROBLEM_ACTION= PREFIX + "previous_problem_action"; //$NON-NLS-1$

	/** @since 3.2 */
	String REFACTORING_ACCEPT_REFACTORING_PAGE= PREFIX + "refactoring_accept_page"; //$NON-NLS-1$

	/** @since 3.2 */
	String REFACTORING_APPLY_SCRIPT_PAGE= PREFIX + "refactoring_apply_script_page"; //$NON-NLS-1$

	/** @since 3.2 */
	String REFACTORING_CREATE_SCRIPT_PAGE= PREFIX + "refactoring_create_script_page"; //$NON-NLS-1$

	String REFACTORING_ERROR_WIZARD_PAGE= PREFIX + "refactoring_error_wizard_page_context"; //$NON-NLS-1$

	/** @since 3.2 */
	String REFACTORING_HISTORY_WIZARD_PAGE= PREFIX + "refactoring_history_wizard_page_context"; //$NON-NLS-1$

	String REFACTORING_PREVIEW_WIZARD_PAGE= PREFIX + "refactoring_preview_wizard_page_context"; //$NON-NLS-1$

	/** @since 3.2 */
	String REFACTORING_SHOW_HISTORY_PAGE= PREFIX + "refactoring_show_history_page"; //$NON-NLS-1$

	/** @since 3.2 */
	String REFACTORING_PROPERTY_PAGE= PREFIX + "refactoring_property_page"; //$NON-NLS-1$
}
