/*
 * Created on Oct 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.forms.IFormToolkitProvider;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AssistedWizardDialog extends WizardDialog implements IFormToolkitProvider {
	private ContextHelpPart contextHelpPart;
	private FormToolkit toolkit;
	private SashForm dialogContainer;

	/**
	 * @param parentShell
	 * @param newWizard
	 */
	public AssistedWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
		contextHelpPart = new ContextHelpPart(this);
	}

    protected void createButtonsForButtonBar(Composite parent) {
        Button myHelpButton = createButton(parent, IDialogConstants.HELP_ID,
                    IDialogConstants.HELP_LABEL, false);
        myHelpButton.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		helpPressed();
        	}
        });
        super.createButtonsForButtonBar(parent);
    }
    
	protected Control createButtonBar(Composite parent) {
		Control bar = super.createButtonBar(parent);
		adaptControl(bar);
		return bar;
	}
	
    protected Control createDialogArea(Composite parent) {
     	dialogContainer = new SashForm(parent, SWT.NULL);
    	toolkit = new FormToolkit(parent.getDisplay());
    	adaptControl(parent);
//    	GridLayout layout = new GridLayout();
//    	layout.numColumns = 2;
//    	layout.marginWidth = layout.marginHeight = 0;
//    	layout.horizontalSpacing = 0;
//    	layout.verticalSpacing = 0;
//    	
//    	dialogContainer.setLayout(layout);
     	final Control wizardArea = super.createDialogArea(dialogContainer);
    	//GridData gd = new GridData(GridData.FILL_BOTH);
    	//gd.verticalSpan = 3;
    	//wizardArea.setLayoutData(gd);
     	adaptControl(wizardArea);
    	//toolkit.adapt(dialogContainer);
    	GridLayout layout = new GridLayout();
    	Composite helpContainer = toolkit.createComposite(dialogContainer);
    	helpContainer.setLayout(layout);
    	layout.marginWidth = layout.marginHeight = 0;
    	layout.verticalSpacing = 0;
    	Label sep = new Label(helpContainer, SWT.SEPARATOR|SWT.HORIZONTAL);
    	sep.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    	FormToolkit helpToolkit = new FormToolkit(helpContainer.getDisplay());
    	helpToolkit.setBackground(helpToolkit.getColors().createColor("bg", 230, 240, 255));
    	contextHelpPart.createControl(helpContainer, helpToolkit);
    	Control contextHelp = contextHelpPart.getControl();
    	ScrolledForm contextForm = contextHelpPart.getForm();
    	Action closeAction = new Action() {
    		public void run() {
    			dialogContainer.setMaximizedControl(wizardArea);
    		}
    	};
    	closeAction.setImageDescriptor(ExamplesPlugin.getDefault().getImageDescriptor(ExamplesPlugin.IMG_CLOSE));
    	closeAction.setToolTipText("Close dynamic help");
		contextForm.getToolBarManager().add(closeAction);
		contextForm.getToolBarManager().update(true);
		TableWrapLayout clayout = (TableWrapLayout)contextForm.getBody().getLayout();
		clayout.topMargin = 0;
       	GridData gd= new GridData(GridData.FILL_BOTH);
    	contextHelp.setLayoutData(gd);
    	sep = new Label(helpContainer, SWT.SEPARATOR|SWT.HORIZONTAL);    	
    	sep.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
     	dialogContainer.setWeights(new int[] {5, 2});    	
    	updateContextHelp();
    	return dialogContainer;
    }
    
    public FormToolkit getToolkit() {
    	return toolkit;
    }
    
    protected void update() {
    	IWizardPage page = getCurrentPage();
    	if (page!=null) adaptPage(page);
    	super.update();
    	if (dialogContainer.getMaximizedControl()==null)
    		updateContextHelp();
     }
    
	private void adaptPage(IWizardPage page) {
		Control control = page.getControl();
		if (control==null || !(control instanceof Composite)) return;
		Object flag = control.getData("__adapted__");
		if (flag==null) {
			adaptControl(control);
			control.setData("__adapted__", Boolean.TRUE);
		}
	}
	
	private void adaptControl(Control c) {
		if (c instanceof Composite) {
			Composite parent = (Composite)c;
			Control [] children = parent.getChildren();
			for (int i=0; i<children.length; i++) {
				Control child = children[i];
				adaptControl(child);
			}
			toolkit.adapt((Composite)c);
		}
		else {
			toolkit.adapt(c, true, true);
		}
	}    
    private void updateContextHelp() {
       	IWizardPage page = getCurrentPage();
    	contextHelpPart.update(page!=null?page.getControl():null);
    }
    protected void helpPressed() {
    	dialogContainer.setMaximizedControl(null);
    	updateContextHelp();
    	super.helpPressed();
    }
}