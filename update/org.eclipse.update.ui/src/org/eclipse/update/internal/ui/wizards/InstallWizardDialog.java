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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.wizard.*;

public class InstallWizardDialog extends WizardDialog {
	
	public InstallWizardDialog(Shell shell, IWizard wizard) {
		super(shell, wizard);
	}
	
	public void cancel() {
		cancelPressed();
	}
}

