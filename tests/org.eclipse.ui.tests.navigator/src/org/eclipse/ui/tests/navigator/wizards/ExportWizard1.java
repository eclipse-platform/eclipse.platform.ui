package org.eclipse.ui.tests.navigator.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
/**
 * Does nothing. Used to test the Capability-awareness of the <b>commonWizard</b> extension.
 * 
 * @since 3.2
 *
 */
public class ExportWizard1 extends Wizard implements IExportWizard {

	public ExportWizard1() { 
	}

	@Override
	public boolean performFinish() { 
		return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) { 

	}

}
