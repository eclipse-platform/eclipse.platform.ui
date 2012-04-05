/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public abstract class FeatureAction extends Action {
	
	protected String windowTitle;
	
    protected Shell shell;

	public FeatureAction(Shell shell, String text) {
		super(text);
        this.shell = shell;
	}

	protected boolean confirm(String message) {
		return MessageDialog.openConfirm( shell, windowTitle, message);
	}
	
	public void setWindowTitle( String windowTitle) {
		this.windowTitle = windowTitle;
	}
	
	public abstract boolean canExecuteAction();
}
