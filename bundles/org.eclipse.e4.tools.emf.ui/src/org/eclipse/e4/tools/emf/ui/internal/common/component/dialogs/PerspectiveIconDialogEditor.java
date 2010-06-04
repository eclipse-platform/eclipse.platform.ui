package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.swt.widgets.Shell;

public class PerspectiveIconDialogEditor extends AbstractIconDialog {

	public PerspectiveIconDialogEditor(Shell parentShell, IProject project, EditingDomain editingDomain, MPerspective element) {
		super(parentShell, project, editingDomain, element, UiPackageImpl.Literals.UI_LABEL__ICON_URI);
	}

	@Override
	protected String getShellTitle() {
		return "Perspective Icon Search";
	}

	@Override
	protected String getDialogTitle() {
		return "Perspective Icon Search";
	}

	@Override
	protected String getDialogMessage() {
		return "Search for GIF, PNG and JPG icons in the current project";
	}

}
