package org.eclipse.ui.internal.dialogs;

import java.text.MessageFormat;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.*;

public class TasksPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage, ModifyListener {

	private static final String POSITIVE_MESSAGE =
		WorkbenchMessages.getString("FileHistory.mustBePositive");
	private static final String INVALID_VALUE_MESSAGE =
		WorkbenchMessages.getString("FileHistory.invalid");
	private static final int FAILED_VALUE = -1;

	Text text;

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		// button group
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(
			new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		Label label = new Label(composite, SWT.LEFT);
		label.setText("Maximum Displayed Tasks");

		text = new Text(composite, SWT.LEFT | SWT.BORDER);
		GridData data = new GridData();
		text.addModifyListener(this);
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		text.setLayoutData(data);

		//Now get the value
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		int displayedTasks = store.getInt(IPreferenceConstants.DISPLAYED_TASKS_COUNT);
		text.setText(String.valueOf(displayedTasks));

		return composite;
	}
	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * @see ModifyListener#modifyText(ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		setValid(getTaskCount() > 0);
	}

	/**
	 * Get the integer value of the text field
	 */

	private int getTaskCount() {

		int value;

		try {
			value = Integer.parseInt(text.getText());

		} catch (NumberFormatException exception) {
			setErrorMessage(
				MessageFormat.format(
					INVALID_VALUE_MESSAGE,
					new Object[] { exception.getLocalizedMessage()}));
			return FAILED_VALUE;
		}

		//Be sure all values are non zero and positive
		if (value <= 0) {
			setErrorMessage(POSITIVE_MESSAGE);
			return FAILED_VALUE;
		}

		return value;

	}

	/**
	* The default button has been pressed. 
	*/
	protected void performDefaults() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		int editorTopValue =
			store.getDefaultInt(IPreferenceConstants.DISPLAYED_TASKS_COUNT);
		this.text.setText(String.valueOf(editorTopValue));
		super.performDefaults();
	}
	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();

		// store the tasks displayed value
		store.setValue(
			IPreferenceConstants.DISPLAYED_TASKS_COUNT,
			(Integer.valueOf(text.getText()).intValue()));

		return true;
	}

}