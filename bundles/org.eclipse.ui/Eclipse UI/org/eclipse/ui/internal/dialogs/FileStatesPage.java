package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import java.text.MessageFormat;
/**
 * The FileStatesPage is the page used to set the file states sizes for the workbench.
 */
public class FileStatesPage
	extends PreferencePage
	implements IWorkbenchPreferencePage, Listener {

	private static final long defaultFileStateLongevity = 7;	// 7 days
	private static final long defaultMaxFileStateSize = 1; // 1 Mb
	private static final int defaultMaxFileStates = 50;

	private static final String LONGEVITY_TITLE =
		WorkbenchMessages.getString("FileHistory.longevity"); //$NON-NLS-1$
	private static final String MAX_FILE_STATES_TITLE =
		WorkbenchMessages.getString("FileHistory.entries"); //$NON-NLS-1$
	private static final String MAX_FILE_STATE_SIZE_TITLE =
		WorkbenchMessages.getString("FileHistory.diskSpace"); //$NON-NLS-1$
	private static final String POSITIVE_MESSAGE =
		WorkbenchMessages.getString("FileHistory.mustBePositive"); //$NON-NLS-1$
	private static final String INVALID_VALUE_MESSAGE =
		WorkbenchMessages.getString("FileHistory.invalid"); //$NON-NLS-1$
	private static final String SAVE_ERROR_MESSAGE =
		WorkbenchMessages.getString("FileHistory.exceptionSaving"); //$NON-NLS-1$

	private static final int FAILED_VALUE = -1;

	//Set the length of the day as we have to convert back and forth
	private static final long DAY_LENGTH = 86400000;
	private static final long MEGABYTES = 1024 * 1024;

	private Text longevityText;
	private Text maxStatesText;
	private Text maxStateSizeText;

/**
 * This method takes the string for the title of a text field and the value for the
 * text of the field.
 * @return org.eclipse.swt.widgets.Text
 * @param labelString java.lang.String
 * @param textValue java.lang.String
 * @param parent Composite 
 */
private Text addLabelAndText(String labelString, String textValue, Composite parent) {
	Label label = new Label(parent,SWT.LEFT);
	label.setText(labelString);
	
	Text text = new Text(parent, SWT.LEFT | SWT.BORDER);
	GridData data = new GridData();
	text.addListener(SWT.Modify, this);
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	data.verticalAlignment = GridData.CENTER;
	data.grabExcessVerticalSpace = false;
	text.setLayoutData(data);
	text.setText(textValue);
	return text;
}
/**
 * Recomputes the page's error state by validating all
 * the fields.
 */
private void checkState() {
	// Assume invalid if the controls not created yet
	if (longevityText == null || maxStatesText == null || maxStateSizeText == null) {
		setValid(false);
		return;
	}

	if (validateLongTextEntry(longevityText) == FAILED_VALUE) {
		setValid(false);
		return;
	}
	
	if (validateIntegerTextEntry(maxStatesText) == FAILED_VALUE) {
		setValid(false);
		return;
	}
	
	if (validateLongTextEntry(maxStateSizeText) == FAILED_VALUE) {
		setValid(false);
		return;
	}

	setValid(true);
	setErrorMessage(null);
}
/* 
* Create the contents control for the workspace file states.
* @returns Control
* @param parent Composite
*/

protected Control createContents(Composite parent) {

	WorkbenchHelp.setHelp(parent, IHelpContextIds.FILE_STATES_PREFERENCE_PAGE);

	// button group
	Composite composite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	composite.setLayout(layout);
	composite.setLayoutData(
		new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

	IWorkspaceDescription description = getWorkspaceDescription();

	//Get the current value and make sure we get at least one day out of it.
	long days = description.getFileStateLongevity() / DAY_LENGTH;
	if (days < 1)
		days = 1;

	long megabytes = description.getMaxFileStateSize() / MEGABYTES;
	if (megabytes < 1)
		megabytes = 1;

	this.longevityText =
		addLabelAndText(LONGEVITY_TITLE, String.valueOf(days), composite);
	this.maxStatesText =
		addLabelAndText(
			MAX_FILE_STATES_TITLE,
			String.valueOf(description.getMaxFileStates()),
			composite);
	this.maxStateSizeText =
		addLabelAndText(
			MAX_FILE_STATE_SIZE_TITLE,
			String.valueOf(megabytes),
			composite);

	checkState();
	
	return composite;
}
/**
 * Get the Workspace this page is operating on.
 * @return org.eclipse.core.internal.resources.IWorkspace
 */
private IWorkspace getWorkspace() {
	return ResourcesPlugin.getWorkspace();
}
/**
 * Get the Workspace Description this page is operating on.
 * @return org.eclipse.core.resources.IWorkspaceDescription
 */
private IWorkspaceDescription getWorkspaceDescription() {
	return ResourcesPlugin.getWorkspace().getDescription();
}
/**
 * Sent when an event that the receiver has registered for occurs.
 *
 * @param event the event which occurred
 */
public void handleEvent(Event event) {
	checkState();
}
/**
 * Initializes this preference page for the given workbench.
 * <p>
 * This method is called automatically as the preference page is being created
 * and initialized. Clients must not call this method.
 * </p>
 *
 * @param workbench the workbench
 */
public void init(org.eclipse.ui.IWorkbench workbench) {}
/**
 * Performs special processing when this page's Defaults button has been pressed.
 * Reset the entries to thier default values.
 */
protected void performDefaults() {
	super.performDefaults();

	this.longevityText.setText(String.valueOf(defaultFileStateLongevity));
	this.maxStatesText.setText(String.valueOf(defaultMaxFileStates));
	this.maxStateSizeText.setText(String.valueOf(defaultMaxFileStateSize));

	checkState();
}
/** 
 * Perform the result of the OK from the receiver.
 */
public boolean performOk() {

	long longevityValue = validateLongTextEntry(longevityText);
	int maxFileStates = validateIntegerTextEntry(this.maxStatesText);
	long maxStateSize = validateLongTextEntry(this.maxStateSizeText);
	if (longevityValue == FAILED_VALUE
		|| maxFileStates == FAILED_VALUE
		|| maxStateSize == FAILED_VALUE)
		return false;

	IWorkspaceDescription description = getWorkspaceDescription();
	description.setFileStateLongevity(longevityValue * DAY_LENGTH);
	description.setMaxFileStates(maxFileStates);
	description.setMaxFileStateSize(maxStateSize * MEGABYTES);

	try {
		//As it is only a copy save it back in
		ResourcesPlugin.getWorkspace().setDescription(description);
	} catch (CoreException exception) {
		MessageDialog.openError(
			getShell(),
			SAVE_ERROR_MESSAGE,
			exception.getMessage());
		return false;
	}

	return true;

}
/**
 * Validate a text entry for an integer field. Return the result if there are
 * no errors, otherwise return -1 and set the entry field error. 
 * @return int
 */
private int validateIntegerTextEntry(Text text) {

	int value;
	
	try {
		value = Integer.parseInt(text.getText());
		
	} catch (NumberFormatException exception) {
		setErrorMessage(MessageFormat.format(INVALID_VALUE_MESSAGE, new Object[] {exception.getLocalizedMessage()}));
		return FAILED_VALUE;
	}

	//Be sure all values are non zero and positive
	if(value <= 0){
		setErrorMessage(POSITIVE_MESSAGE);
		return FAILED_VALUE;
	}

	return value;
}
/**
 * Validate a text entry for a long field. Return the result if there are
 * no errors, otherwise return -1 and set the entry field error. 
 * @return long
 */
private long validateLongTextEntry(Text text) {

	long value;
	
	try {
		value = Long.parseLong(text.getText());
		
	} catch (NumberFormatException exception) {
		setErrorMessage(MessageFormat.format(INVALID_VALUE_MESSAGE, new Object[] {exception.getLocalizedMessage()}));
		return FAILED_VALUE;
	}

	//Be sure all values are non zero and positive
	if(value <= 0){
		setErrorMessage(POSITIVE_MESSAGE);
		return FAILED_VALUE;
	}

	return value;
}
}
