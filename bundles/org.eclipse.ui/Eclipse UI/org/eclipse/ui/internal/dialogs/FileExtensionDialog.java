package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This class is used to prompt the user for a file name & extension.
 */
public class FileExtensionDialog extends Dialog
	implements Listener
{
	private String filename = "";//$NON-NLS-1$
	private Text filenameField;
/**
 * Constructs a new file extension dialog.
 */
public FileExtensionDialog(Shell parentShell) {
	super(parentShell);
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setText(WorkbenchMessages.getString("FileExtension.shellTitle")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(shell, new Object[] {IHelpContextIds.FILE_EXTENSION_DIALOG});
}
public void create() {
	super.create();
	filenameField.setFocus();
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
	Composite contents = (Composite)super.createDialogArea(parent);
	((GridLayout)contents.getLayout()).numColumns = 2;

	// begin the layout
	Label textLabel = new Label(contents,SWT.WRAP);
	textLabel.setText(WorkbenchMessages.getString("FileExtension.fileTypeMessage")); //$NON-NLS-1$
	GridData data = new GridData();
	data.horizontalSpan = 2;
	textLabel.setLayoutData(data);

	Label label = new Label(contents, SWT.LEFT);
	label.setText(WorkbenchMessages.getString("FileExtension.fileTypeLabel")); //$NON-NLS-1$
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	label.setLayoutData(data);
	
	filenameField = new Text(contents, SWT.SINGLE | SWT.BORDER);
	filenameField.addListener(SWT.Modify, this);
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	filenameField.setLayoutData(data);

	filenameField.setFocus();
		
	return contents;
}
/**
 * Validate the user input for a file type
 */
private boolean fileTypeValid() {
	// We need kernel api to validate the extension or a filename

	int index = filename.indexOf('.');
	if (index == -1) {
		MessageDialog.openWarning(
			getShell(),
			WorkbenchMessages.getString("FileExtension.invalidTitle"), //$NON-NLS-1$
			WorkbenchMessages.getString("FileExtension.invalidMessage")); //$NON-NLS-1$
		filenameField.setFocus();
		return false;
	}

	if (index == filename.length() - 1) {
		MessageDialog.openWarning(
			getShell(),
			WorkbenchMessages.getString("FileExtension.invalidType"), //$NON-NLS-1$
			WorkbenchMessages.getString("FileExtension.invalidTypeMessage")); //$NON-NLS-1$
		filenameField.setFocus();
		return false;
	}
	
	return true;
}
public String getExtension() {
	// We need kernel api to validate the extension or a filename
	
	int index = filename.indexOf('.');
	if (index == -1)
		return "";//$NON-NLS-1$
	if (index == filename.length())
		return "";//$NON-NLS-1$
	return filename.substring(index + 1,filename.length());
}
public String getName() {
	// We need kernel api to validate the extension or a filename
	
	int index = filename.indexOf('.');
	if (index == -1)
		return "*";//$NON-NLS-1$
	return filename.substring(0, index);
}
public void handleEvent(Event event) {
	if (event.widget == filenameField) {
		filename = filenameField.getText().trim();
	}
}
/**
 * Notifies that the ok button of this dialog has been pressed.
 */
protected void okPressed() {
	if (fileTypeValid())
		super.okPressed();
}
}
