package org.eclipse.help.ui.internal.views;

import org.eclipse.jface.wizard.Wizard;

public class NewEngineWizard extends Wizard {
	private EngineTypeDescriptor [] engineTypes;
	private EngineTypeWizardPage selectionPage;

	public NewEngineWizard(EngineTypeDescriptor [] engineTypes) {
		setWindowTitle("New Search Engine");
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