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

	private TableOfContentsArea tocArea;
	private static int DEFAULT_NODE_SIZE = 19;
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
		tocArea = new TableOfContentsArea();
		tocArea.addWizard(getWizard());

		Control tocControl = tocArea.createControl(parent);

		FormData tocData = new FormData();
		tocData.top = new FormAttachment(bottomWidget);
		tocData.left = new FormAttachment(0, 0);
		tocData.right = new FormAttachment(100, 0);
		tocData.height = DEFAULT_NODE_SIZE;
		tocControl.setLayoutData(tocData);
		return tocControl;
	}
	/**
	 * @see org.eclipse.jface.wizard.WizardDialog#setWizard(org.eclipse.jface.wizard.IWizard)
	 */
	protected void setWizard(IWizard newWizard) {
		super.setWizard(newWizard);
		//May be called before area is created
		if (tocArea != null)
			tocArea.addWizard(newWizard);
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardContainer#showPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public void showPage(IWizardPage page) {
		super.showPage(page);
		if (tocArea != null)
			tocArea.updateFor(page);
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardContainer#updateButtons()
	 */
	public void updateButtons() {
		super.updateButtons();
		//Now also check for the page
		tocArea.updateFor(getCurrentPage());
	}

}
