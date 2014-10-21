package org.eclipse.e4.tools.emf.editor3x;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.internal.tools.wizards.model.ExtractContributionModelWizard;
import org.eclipse.e4.tools.emf.ui.common.IModelExtractor;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public class ModelExtractor implements IModelExtractor {

	@Override
	public boolean extract(Shell shell, IProject project,
		List<MApplicationElement> maes) {
		final ExtractContributionModelWizard extractContributionModelWizard = new ExtractContributionModelWizard(maes);
		extractContributionModelWizard.setup(project);
		final WizardDialog wizardDialog = new WizardDialog(shell, extractContributionModelWizard);
		return wizardDialog.open() == Window.OK;
	}

}
