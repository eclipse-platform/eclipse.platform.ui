package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.misc.UIHackFinder;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This class is used to prompt the user for a file name & extension.
 */
public class FileExtensionDialog extends Dialog
	implements Listener
{
	private String filename = "";
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
	shell.setText("New File Type");
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
	textLabel.setText("Enter file type to add: (e.g. *.doc or report.doc)");
	GridData data = new GridData();
	data.horizontalSpan = 2;
	textLabel.setLayoutData(data);
	textLabel.setFont(parent.getFont());

	Label label = new Label(contents, SWT.LEFT);
	label.setText("File type: ");
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	label.setLayoutData(data);
	label.setFont(parent.getFont());
	
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
	UIHackFinder.fixPR();
	// we need kernel api to validate the extension or a filename

	int index = filename.indexOf('.');
	if (index == -1) {
		MessageDialog.openWarning(
			getShell(),
			"Invalid File Type",
			"File type must include an extension.");
		filenameField.setFocus();
		return false;
	}

	if (index == filename.length() - 1) {
		MessageDialog.openWarning(
			getShell(),
			"Invalid File Type",
			"File type extension cannot be empty");
		filenameField.setFocus();
		return false;
	}
	
	return true;
}
public String getExtension() {
	UIHackFinder.fixPR();
	// we need kernel api to validate the extension or a filename
	
	int index = filename.indexOf('.');
	if (index == -1)
		return "";
	if (index == filename.length())
		return "";
	return filename.substring(index + 1,filename.length());
}
public String getName() {
	UIHackFinder.fixPR();
	// we need kernel api to validate the extension or a filename
	
	int index = filename.indexOf('.');
	if (index == -1)
		return "*";
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
