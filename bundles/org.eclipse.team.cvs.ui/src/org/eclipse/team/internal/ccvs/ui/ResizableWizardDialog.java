/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;


import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class ResizableWizardDialog extends WizardDialog {
	/**
	 * Creates a new resizable wizard dialog.
	 */
	public ResizableWizardDialog(Shell parent, IWizard wizard) {
		super(parent, wizard);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}	
}
