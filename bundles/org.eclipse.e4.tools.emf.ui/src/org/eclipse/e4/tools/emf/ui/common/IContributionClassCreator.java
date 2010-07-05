package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Shell;

public interface IContributionClassCreator {
	public boolean isSupported(EClass element);

	public void createOpen(MContribution contribution, EditingDomain domain, IProject project, Shell shell);
}
