package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Shell;

public class PartIconDialogEditor extends AbstractIconDialog {

	public PartIconDialogEditor(Shell parentShell, IProject project, EditingDomain editingDomain, MPart element) {
		super(parentShell, project, editingDomain, element, UiPackageImpl.Literals.UI_LABEL__ICON_URI);
	}

	@Override
	protected String getShellTitle() {
		return Messages.PartIconDialogEditor_ShellTitle;
	}

	@Override
	protected String getDialogTitle() {
		return Messages.PartIconDialogEditor_DialogTitle;
	}

	@Override
	protected String getDialogMessage() {
		return Messages.PartIconDialogEditor_DialogMessage;
	}

}
