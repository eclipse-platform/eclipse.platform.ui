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

import org.eclipse.jface.dialogs.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.*;

/**
 *
 */
public class UIProblemHandler implements IProblemHandler {

	/*
	 * @see IProblemHandler#reportProblem(String)
	 */
	public boolean reportProblem(String problemText) {
		String title = UpdateUI.getString("Revert.ProblemDialog.title"); //$NON-NLS-1$
		return MessageDialog.openQuestion(UpdateUI.getActiveWorkbenchShell(), title, problemText);
	}
}
