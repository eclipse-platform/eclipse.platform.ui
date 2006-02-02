/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.scripting;

import org.eclipse.osgi.util.NLS;

public final class ScriptingMessages extends NLS {

	public static String ApplyRefactoringScriptWizard_caption;

	public static String ApplyRefactoringScriptWizard_description;

	public static String ApplyRefactoringScriptWizard_project_pattern;

	public static String ApplyRefactoringScriptWizard_title;

	public static String ApplyRefactoringScriptWizard_workspace_caption;

	public static String ApplyRefactoringScriptWizardPage_browse_caption;

	public static String ApplyRefactoringScriptWizardPage_browse_label;

	public static String ApplyRefactoringScriptWizardPage_error_cannot_read;

	public static String ApplyRefactoringScriptWizardPage_filter_extension_script;

	public static String ApplyRefactoringScriptWizardPage_filter_extension_wildcard;

	public static String ApplyRefactoringScriptWizardPage_filter_name_script;

	public static String ApplyRefactoringScriptWizardPage_filter_name_wildcard;

	public static String ApplyRefactoringScriptWizardPage_invalid_format;

	public static String ApplyRefactoringScriptWizardPage_invalid_location;

	public static String ApplyRefactoringScriptWizardPage_invalid_script_file;

	public static String ApplyRefactoringScriptWizardPage_location_caption;

	public static String ApplyRefactoringScriptWizardPage_location_label;

	private static final String BUNDLE_NAME= "org.eclipse.ltk.internal.ui.refactoring.scripting.ScriptingMessages"; //$NON-NLS-1$

	public static String CreateRefactoringScriptWizard_caption;

	public static String CreateRefactoringScriptWizard_description;

	public static String CreateRefactoringScriptWizard_merge_button;

	public static String CreateRefactoringScriptWizard_overwrite_button;

	public static String CreateRefactoringScriptWizard_overwrite_query;

	public static String CreateRefactoringScriptWizard_title;

	public static String CreateRefactoringScriptWizardPage_browse_caption;

	public static String CreateRefactoringScriptWizardPage_script_extension;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ScriptingMessages.class);
	}

	private ScriptingMessages() {
	}
}