package org.eclipse.jface.wizard;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Shell;

/**
 * The TableOfContentsWizardDialog is a wizard dialog with
 * a table of contents area.
 * 
 */
public class TableOfContentsWizardDialog extends WizardDialog {

	/*
	 * @see WizardDialog()
	 */
	public TableOfContentsWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	/**
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createTitleArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createTitleArea(Composite parent) {
		Control bottomWidget = super.createTitleArea(parent);
		TableOfContentsArea tocArea = new TableOfContentsArea();
		tocArea.addWizard(getWizard());
		
		Control tocControl = tocArea.createControl(parent);
		
		FormData tocData = new FormData();
		tocData.top = new FormAttachment(bottomWidget);
		tocData.left = new FormAttachment(0,0);
		tocData.right = new FormAttachment(100,0);
		tocData.height = TableOfContentsArea.NODE_SIZE;
		tocControl.setLayoutData(tocData);
		return tocControl;
	}
}
