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

import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public class ResizableWizardDialog extends WizardDialog {
	/**
	 * Creates a new resizable wizard dialog.
	 */
	public ResizableWizardDialog(Shell parent, IWizard wizard) {
		super(parent, wizard);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}	
}