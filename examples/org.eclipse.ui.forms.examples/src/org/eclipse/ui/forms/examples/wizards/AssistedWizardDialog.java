/*
 * Created on Oct 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import org.eclipse.jface.wizard.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AssistedWizardDialog extends WizardDialog {
	private ContextHelpPart contextHelpPart;
	private FormToolkit toolkit;

	/**
	 * @param parentShell
	 * @param newWizard
	 */
	public AssistedWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
		contextHelpPart = new ContextHelpPart();
	}
	
    protected Control createDialogArea(Composite parent) {
    	Composite dialogContainer = new Composite(parent, SWT.NULL);
    	GridLayout layout = new GridLayout();
    	layout.numColumns = 2;
    	layout.marginWidth = layout.marginHeight = 0;
    	layout.horizontalSpacing = 0;
    	layout.verticalSpacing = 0;
    	
    	dialogContainer.setLayout(layout);
     	Control wizardArea = super.createDialogArea(dialogContainer);
    	GridData gd = new GridData(GridData.FILL_BOTH);
    	gd.verticalSpan = 3;
    	wizardArea.setLayoutData(gd);
    	toolkit = new FormToolkit(parent.getDisplay());
    	toolkit.adapt(dialogContainer);
    	Label sep = new Label(dialogContainer, SWT.SEPARATOR|SWT.HORIZONTAL);
    	sep.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    	contextHelpPart.createControl(dialogContainer, toolkit);
    	Control contextHelp = contextHelpPart.getControl();
       	gd= new GridData(GridData.FILL_VERTICAL);
    	contextHelp.setLayoutData(gd);
    	sep = new Label(dialogContainer, SWT.SEPARATOR|SWT.HORIZONTAL);    	
    	sep.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    	updateContextHelp();
    	return dialogContainer;
    }
    
    protected void update() {
    	super.update();
    	updateContextHelp();
 
    }
    private void updateContextHelp() {
       	IWizardPage page = getCurrentPage();
    	contextHelpPart.update(page!=null?page.getControl():null);
    }
}