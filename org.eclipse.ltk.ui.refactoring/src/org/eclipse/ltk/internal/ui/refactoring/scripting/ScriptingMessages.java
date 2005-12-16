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

public class ScriptingMessages extends NLS {

	public static String ApplyRefactoringScriptWizard_caption;

	public static String ApplyRefactoringScriptWizard_description;

	public static String ApplyRefactoringScriptWizard_project_pattern;

	public static String ApplyRefactoringScriptWizard_title;

	public static String ApplyRefactoringScriptWizard_workspace_caption;

	private static final String BUNDLE_NAME= "org.eclipse.ltk.internal.ui.refactoring.scripting.ScriptingMessages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ScriptingMessages.class);
	}

	private ScriptingMessages() {
	}
}