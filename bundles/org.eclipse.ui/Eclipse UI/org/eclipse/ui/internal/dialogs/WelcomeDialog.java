package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Display a list of errors (Typically IStatus errors).
 *
 * @private
 *		This class is internal to the workbench and must not be called outside the workbench
 */
public class WelcomeDialog extends Dialog implements Listener {
	private IWorkbench workbench;
	private Image image;
	private Button showOnStartupButton;
	private WorkbenchWizardElement currentElement; //element corresponding to the currently selected radio button
	private AdaptableList wizardElements; //Wizards to appear in the welcome dialog's radiobutton menu
/**
 *	Create an instance of WelcomeDialog
 */
public WelcomeDialog(Shell parentShell, IWorkbench aWorkbench, AdaptableList wElements) {
	super(parentShell);
	setShellStyle(SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
	this.workbench = aWorkbench;
	this.wizardElements = wElements;	
}
/**
 * This method is called if a button has been pressed.
 */
protected void buttonPressed(int buttonId) {
	INewWizard aWizard;
	if (IDialogConstants.OK_ID != buttonId) {			// cancel
		super.buttonPressed(buttonId);
		return;
	}

	//check the current checkbox status and set the "show on startup" preference accordingly
	WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IWorkbenchPreferenceConstants.WELCOME_DIALOG, showOnStartupButton.getSelection());

	//if "go to workbench" radio button was selected
	if (currentElement == null) {
		super.buttonPressed(buttonId);
		return;
	}		
	
	// create instance of target wizard (currentElement is a WorkbenchWizardElement)
	try {
		aWizard = (INewWizard)currentElement.createExecutableExtension();
	} catch (CoreException e) {
		ErrorDialog.openError(getShell(), 
			"Problem Opening Wizard", 
			"The selected wizard could not be started.", 
			e.getStatus());
		return;
	}

	Wizard newWizard = (Wizard)aWizard;
	newWizard.setNeedsProgressMonitor(true);
	newWizard.setForcePreviousAndNextButtons(true);

	((INewWizard)newWizard).init(this.workbench, new StructuredSelection());
	if (newWizard.getWindowTitle() == null)
		newWizard.setWindowTitle("Welcome");

	WizardDialog dialog = new WizardDialog(getShell(), newWizard);
	dialog.open();

	super.buttonPressed(buttonId);
}
/**
 * Closes this window and disposes its shell.
 */
public boolean close() {
	//get rid of the image that was displayed on the left-hand side of the Welcome dialog
	if (image != null)
		image.dispose();
	return super.close();
}
/* (non-Javadoc)
 * Method declared on Window.
 */
protected void configureShell(Shell newShell) {
	super.configureShell(newShell);
	newShell.setText("Quick Start");	
	WorkbenchHelp.setHelp(newShell, new Object[] {IHelpContextIds.WELCOME_DIALOG});
}
/**
 * Creates and returns the contents of the upper part 
 * of the dialog (above the button bar).
 *
 * Subclasses should overide.
 *
 * @param the parent composite to contain the dialog area
 * @return the dialog area control
 */
protected Control createDialogArea(Composite parent) {
 	image = ((Workbench)PlatformUI.getWorkbench()).getProductInfo().getWelcomeImage();
 	
	// page group
	Composite outer = (Composite)super.createDialogArea(parent);

	outer.setSize( outer.computeSize(SWT.DEFAULT, SWT.DEFAULT) );

	
	// top label
	Label topLabel = new Label(outer, SWT.NONE);
	topLabel.setFont(JFaceResources.getBannerFont());
	topLabel.setText("What would you like to do?                          ");
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	topLabel.setLayoutData(data);
	topLabel.setFont(parent.getFont());

	
	// radio buttons and image container
	Composite imageAndRadioContainer = new Composite(outer, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = (image == null ? 1 : 2);
	layout.marginWidth = 0;
	imageAndRadioContainer.setLayout(layout);
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	imageAndRadioContainer.setLayoutData(data);


	//image on left side of dialog
	if (image != null) {
		Label imageLabel = new Label(imageAndRadioContainer, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = false;
		imageLabel.setLayoutData(data);
		
	 	imageLabel.setImage(image);
	}	

	//radio buttons' container	
	Composite radioButtonsGroup = new Composite(imageAndRadioContainer, SWT.NONE);
	layout = new GridLayout();
	layout.numColumns = 1;
	layout.makeColumnsEqualWidth = true;
	layout.marginWidth = 0;
	radioButtonsGroup.setLayout(layout);
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.verticalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	radioButtonsGroup.setLayoutData(data);

	Button radioButton;

	//get the information from the WorkbenchWizardElement and apply it to a radio button;
	//display a label for each item and also assign the element to the radio button as data
	Object[] children = wizardElements.getChildren();

	for (int i = 0; i < children.length; ++i) {
		WorkbenchWizardElement wizardElement = (WorkbenchWizardElement) children[i];
		radioButton = new Button( radioButtonsGroup, SWT.RADIO);
		radioButton.setText(wizardElement.getLabel(wizardElement));
		radioButton.setData(wizardElement);
		radioButton.addListener(SWT.Selection,this);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		radioButton.setLayoutData(data);
		radioButton.setFont(parent.getFont());
	}

	//add one last radiobutton to give user the option of going directly to the workbench
	radioButton = new Button( radioButtonsGroup, SWT.RADIO);
	radioButton.setText( "Go to the workbench               ");
	radioButton.setSelection(true);
	radioButton.addListener(SWT.Selection,this);

	
	//preference checkbox for showing the QuicksTart window on startup.
	showOnStartupButton = new Button(outer, SWT.CHECK);
	showOnStartupButton.setText("Show this window at startup.");
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	showOnStartupButton.setLayoutData(data);

	//check the current preferences and set the initial selection status of the checkbox accordingly 
	if (WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.WELCOME_DIALOG) )
		showOnStartupButton.setSelection(true);
	else showOnStartupButton.setSelection(false);
	
	return outer;
}
/**
 * Handle the OK and Details buttons
 *
 * @param event org.eclipse.swt.widgets.Event
 */
public void handleEvent(Event event) {
	//get the radio button that has been selected
	Widget source = event.widget;

	//make the WorkbenchWizardElement from the selected radio button the currentElement
	currentElement = (WorkbenchWizardElement) source.getData();
}
}
