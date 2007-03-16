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
package org.eclipse.ui.tests.navigator.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Does nothing. Used to test the Capability-awareness of the <b>commonWizard</b> extension.
 * 
 * @since 3.2
 *
 */
public class ImportWizard1 extends Wizard implements IImportWizard {

	public ImportWizard1() { 
	}
 
	public boolean performFinish() { 
		return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) { 

	}

}
