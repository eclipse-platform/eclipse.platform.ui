package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

/**
 * Displays information about the product.
 *
 * @private
 *		This class is internal to the workbench and must not be called outside the workbench
 */
public class AboutDialog extends Dialog {
	private	Image 			image;	//image to display on  dialog
	private	ProductInfo 	info;	//the product info
/**
 * Create an instance of the AboutDialog
 */
public AboutDialog(Shell parentShell) {
	super(parentShell);
	info = ((Workbench)PlatformUI.getWorkbench()).getProductInfo();
}
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
	newShell.setText(WorkbenchMessages.format("AboutDialog.shellTitle", new Object[] {info.getName()})); //$NON-NLS-1$
	WorkbenchHelp.setHelp(newShell, new Object[] {IHelpContextIds.ABOUT_DIALOG});
}
/**
 * Add buttons to the dialog's button bar.
 *
 * Subclasses should override.
 *
 * @param parent the button bar composite
 */
protected void createButtonsForButtonBar(Composite parent) {
	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
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

	image =  info.getAboutImage();	// may be null
		
	// page group
	Composite outer = (Composite)super.createDialogArea(parent);
	outer.setSize(outer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	// the image & text	
	Composite topContainer = new Composite(outer, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = (image == null ? 1: 2);
	layout.marginWidth = 0;
	topContainer.setLayout(layout);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	topContainer.setLayoutData(data);

	//image on left side of dialog
	if (image != null) {
		Label imageLabel = new Label(topContainer, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		imageLabel.setLayoutData(data);
		imageLabel.setImage(image);
	}
	
	// text on the right
	Label label = new Label(topContainer, SWT.LEFT | SWT.WRAP );
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.verticalAlignment = GridData.BEGINNING;
	label.setText(text());
	label.setLayoutData(data);
	label.setFont(parent.getFont());

	Label spacer;
	
	spacer = new Label(outer, SWT.LEFT);
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.horizontalSpan = 1;
	spacer.setLayoutData(data);

	return outer;
}
/**
 * Answer the text to show on the right side of the dialog.
 */ 
protected String text() {
	if (info.getBuildID().length() == 0) {
		return WorkbenchMessages.format("AboutText.withoutBuildNumber", new Object[] {info.getDetailedName(),info.getVersion(),info.getCopyright()}); //$NON-NLS-1$
	} else {
		return WorkbenchMessages.format("AboutText.withBuildNumber", new Object[] {info.getDetailedName(),info.getVersion(),info.getBuildID(),info.getCopyright()}); //$NON-NLS-1$
	}
}
}
