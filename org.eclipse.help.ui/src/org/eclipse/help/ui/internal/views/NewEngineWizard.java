package org.eclipse.help.ui.internal.views;

import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.jface.wizard.Wizard;

public class NewEngineWizard extends Wizard {
	private EngineTypeDescriptor [] engineTypes;
	private EngineTypeWizardPage selectionPage;

	public NewEngineWizard(EngineTypeDescriptor [] engineTypes) {
		setWindowTitle(HelpUIResources.getString("NewEngineWizard.wtitle")); //$NON-NLS-1$
		this.engineTypes = engineTypes;
	}

	public void addPages() {
		selectionPage = new EngineTypeWizardPage(engineTypes);
		addPage(selectionPage);
	}

	public boolean performFinish() {
		return true;
	}

	public EngineTypeDescriptor getSelectedEngineType() {
		return selectionPage.getSelectedEngineType();
	}
}