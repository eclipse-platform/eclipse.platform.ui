package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.emf.edit.domain.EditingDomain;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.TitleAreaDialog;

public class FindImportElementDialog extends TitleAreaDialog {
	
	public FindImportElementDialog(Shell parentShell, EditingDomain domain, EObject element) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		
		return comp;
	}
}