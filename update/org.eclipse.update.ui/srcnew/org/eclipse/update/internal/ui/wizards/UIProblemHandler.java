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
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.update.configuration.IProblemHandler;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 *
 */
public class UIProblemHandler implements IProblemHandler {
	private static final String KEY_TITLE = "Revert.ProblemDialog.title";

	/*
	 * @see IProblemHandler#reportProblem(String)
	 */
	public boolean reportProblem(String problemText) {
		String title = UpdateUI.getString(KEY_TITLE);
		return MessageDialog.openQuestion(UpdateUI.getActiveWorkbenchShell(), title, problemText);
	}
}
