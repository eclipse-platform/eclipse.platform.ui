package org.eclipse.help.internal.ui.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.ui.IHelpUIConstants;
import org.eclipse.help.internal.util.*;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
/**
 * This class implements a sample preference page that is 
 * added to the preference dialog based on the registration.
 */
public class HelpPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage, Listener {
	private Label labelServerAddr;
	private Label labelServerPort;
	private Text textServerAddr;
	private Text textServerPort;
	private Button radioButtonError;
	private Button radioButtonWarning;
	private Button radioButtonDebug;
	private Text textBrowserPath; // on linux only
	private Button radioButtonLocalServerAutomatic;
	private Button radioButtonLocalServerManual;
	private Text textInfocenterURL;
	/**
	 * Creates an new checkbox instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the checkbox
	 * @param label  the string to set into the checkbox
	 * @return the new checkbox
	 */
	private Button createCheckBox(Composite group, String label) {
		Button button = new Button(group, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		button.addListener(SWT.Selection, this);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}
	/**
	 * Creates composite control and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @return the newly-created coposite
	 */
	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.verticalSpacing = 1;
		layout.marginHeight = 2;
		composite.setLayout(layout);
		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		return composite;
	}
	/**
	 * Creates preference page controls on demand.
	 *
	 * @param parent  the parent for the preference page
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpUIConstants.PREF_PAGE);
		/* Infoceter URL */
		Composite composite_tab0 = createComposite(parent, 1);
		createLabel(
			composite_tab0,
			WorkbenchResources.getString("Infocenter_location"),
			1);
		Composite infocenterAddressComposite = createComposite(composite_tab0, 2);
		createLabel(
			infocenterAddressComposite,
			WorkbenchResources.getString("Infocenter_URL"),
			1);
		textInfocenterURL = createTextField(infocenterAddressComposite);
		/* Browser Path */
		if (!System.getProperty("os.name").startsWith("Win")) {
			Composite composite_textField2 = createComposite(parent, 2);
			WorkbenchHelp.setHelp(
				composite_textField2,
				IHelpUIConstants.BROWSER_PATH);
			Label label_textField =
				createLabel(
					composite_textField2,
					WorkbenchResources.getString("Browser_path"),
					1);
			textBrowserPath = createTextField(composite_textField2);
		}
		/* Loggin Options */
		//composite_tab2 << parent
		Composite composite_tab2 = createComposite(parent, 1);
		WorkbenchHelp.setHelp(
			composite_tab2,
			IHelpUIConstants.LOGGING_OPTIONS);
		Label label2 =
			createLabel(composite_tab2, WorkbenchResources.getString("Logging_Options"), 1);
		//composite_checkBox << composite_tab2
		Composite composite_radioButton2 = createComposite(composite_tab2, 1);
		radioButtonError =
			createRadioButton(
				composite_radioButton2,
				WorkbenchResources.getString("Errors_only"));
		radioButtonWarning =
			createRadioButton(
				composite_radioButton2,
				WorkbenchResources.getString("Warnings_and_errors"));
		radioButtonDebug =
			createRadioButton(
				composite_radioButton2,
				WorkbenchResources.getString("Everything"));
		/* Local Server Configuration */
		//composite_tab3 << parent
		Composite composite_tab3 = createComposite(parent, 1);
		WorkbenchHelp.setHelp(
			composite_tab3,
			IHelpUIConstants.LOCAL_SERVER_CONFIG);
		Label label31 =
			createLabel(
				composite_tab3,
				WorkbenchResources.getString("Local_server_config"),
				1);
		//radio button composite << tab composite
		Composite composite_radioButton3 = createComposite(composite_tab3, 2);
		radioButtonLocalServerAutomatic =
			createRadioButton(
				composite_radioButton3,
				WorkbenchResources.getString("Local_server_config_automatic"));
		tabForward(composite_radioButton3);
		radioButtonLocalServerAutomatic.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				if (((Button) event.widget).getSelection()) {
					labelServerAddr.setEnabled(false);
					labelServerPort.setEnabled(false);
					textServerAddr.setEnabled(false);
					textServerPort.setEnabled(false);
				}
			}
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);
			}
		});
		radioButtonLocalServerManual =
			createRadioButton(
				composite_radioButton3,
				WorkbenchResources.getString("Local_server_config_manual"));
		tabForward(composite_radioButton3);
		radioButtonLocalServerManual.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				if (((Button) event.widget).getSelection()) {
					labelServerAddr.setEnabled(true);
					labelServerPort.setEnabled(true);
					textServerAddr.setEnabled(true);
					textServerPort.setEnabled(true);
				}
			}
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);
			}
		});
		labelServerAddr =
			createLabel(
				composite_radioButton3,
				WorkbenchResources.getString("Server_address"),
				1);
		textServerAddr = createTextField(composite_radioButton3);
		labelServerPort =
			createLabel(
				composite_radioButton3,
				WorkbenchResources.getString("Server_port"),
				1);
		textServerPort = createTextField(composite_radioButton3);
		initializeValues();
		//font = null;
		return new Composite(parent, SWT.NULL);
	}
	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	private Label createLabel(Composite parent, String text, int columns) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = columns;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	/**
	 * Utility method that creates a push button instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new button
	 * @param label  the label for the new button
	 * @return the newly-created button
	 */
	private Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.addListener(SWT.Selection, this);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		button.setLayoutData(data);
		return button;
	}
	/**
	 * Utility method that creates a radio button instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new button
	 * @param label  the label for the new button
	 * @return the newly-created button
	 */
	private Button createRadioButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
		button.setText(label);
		button.addListener(SWT.Selection, this);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}
	/**
	 * Create a text field specific for this application
	 *
	 * @param parent  the parent of the new text field
	 * @return the new text field
	 */
	private Text createTextField(Composite parent) {
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		text.addListener(SWT.Modify, this);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		text.setLayoutData(data);
		return text;
	}
	/**
	 * Handles events generated by controls on this page.
	 *
	 * @param e  the event to handle
	 */
	public void handleEvent(Event e) {
		//get widget that generates the event
		Widget source = e.widget;
		// add the code that should react to
		// some widget event
	}
	/**
	 * @see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}
	/**
	 * Initializes states of the controls using default values
	 * in the preference store.
	 */
	private void initializeDefaults() {
		HelpPreferences store = HelpSystem.getPreferences();
		// Local Help Server Configuration
		radioButtonLocalServerAutomatic.setSelection(false);
		radioButtonLocalServerManual.setSelection(false);
		labelServerAddr.setEnabled(false);
		labelServerPort.setEnabled(false);
		textServerAddr.setEnabled(false);
		textServerPort.setEnabled(false);
		textInfocenterURL.setText(
			store.getDefaultString(HelpPreferences.INFOCENTER_URL_KEY));
		int serverConfigChoice =
			store.getDefaultInt(HelpPreferences.LOCAL_SERVER_CONFIG);
		switch (serverConfigChoice) {
			case 0 :
				radioButtonLocalServerAutomatic.setSelection(true);
				break;
			case 1 :
				radioButtonLocalServerManual.setSelection(true);
				labelServerAddr.setEnabled(true);
				labelServerPort.setEnabled(true);
				textServerAddr.setEnabled(true);
				textServerPort.setEnabled(true);
				break;
		}
		textServerAddr.setText(
			store.getDefaultString(HelpPreferences.LOCAL_SERVER_ADDRESS_KEY));
		textServerPort.setText(
			store.getDefaultString(HelpPreferences.LOCAL_SERVER_PORT_KEY));
		radioButtonError.setSelection(false);
		radioButtonWarning.setSelection(false);
		radioButtonDebug.setSelection(false);
		int choice = store.getDefaultInt(HelpPreferences.LOG_LEVEL_KEY);
		switch (choice) {
			case Logger.LOG_ERROR :
				radioButtonError.setSelection(true);
				break;
			case Logger.LOG_WARNING :
				radioButtonWarning.setSelection(true);
				break;
			case Logger.LOG_DEBUG :
				radioButtonDebug.setSelection(true);
				break;
		}
		if (!System.getProperty("os.name").startsWith("Win")) {
			textBrowserPath.setText(
				store.getDefaultString(HelpPreferences.BROWSER_PATH_KEY));
		}
	}
	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		HelpPreferences store = HelpSystem.getPreferences();
		// Local Help Server Configuration
		radioButtonLocalServerAutomatic.setSelection(false);
		radioButtonLocalServerManual.setSelection(false);
		labelServerAddr.setEnabled(false);
		labelServerPort.setEnabled(false);
		textServerAddr.setEnabled(false);
		textServerPort.setEnabled(false);
		textInfocenterURL.setText(store.getString(HelpPreferences.INFOCENTER_URL_KEY));
		int serverConfigChoice = store.getInt(HelpPreferences.LOCAL_SERVER_CONFIG);
		switch (serverConfigChoice) {
			case 0 :
				radioButtonLocalServerAutomatic.setSelection(true);
				break;
			case 1 :
				radioButtonLocalServerManual.setSelection(true);
				labelServerAddr.setEnabled(true);
				labelServerPort.setEnabled(true);
				textServerAddr.setEnabled(true);
				textServerPort.setEnabled(true);
				break;
		}
		textServerAddr.setText(
			store.getString(HelpPreferences.LOCAL_SERVER_ADDRESS_KEY));
		textServerPort.setText(store.getString(HelpPreferences.LOCAL_SERVER_PORT_KEY));
		radioButtonError.setSelection(false);
		radioButtonWarning.setSelection(false);
		radioButtonDebug.setSelection(false);
		int choice = store.getInt(HelpPreferences.LOG_LEVEL_KEY);
		switch (choice) {
			case Logger.LOG_ERROR :
				radioButtonError.setSelection(true);
				break;
			case Logger.LOG_WARNING :
				radioButtonWarning.setSelection(true);
				break;
			case Logger.LOG_DEBUG :
				radioButtonDebug.setSelection(true);
				break;
		}
		if (!System.getProperty("os.name").startsWith("Win")) {
			textBrowserPath.setText(store.getString(HelpPreferences.BROWSER_PATH_KEY));
		}
	}
	/**
	 * Does anything necessary because the default button has been pressed.
	 */
	protected void performDefaults() {
		super.performDefaults();
		initializeDefaults();
	}
	/**
	 * Do anything necessary because the OK button has been pressed.
	 *
	 * @return whether it is okay to close the preference page
	 */
	public boolean performOk() {
		storeValues();
		return true;
	}
	/**
	 * Stores the values of the controls back to the preference store.
	 */
	private void storeValues() {
		// use the old preferences object
		HelpPreferences store = HelpSystem.getPreferences();
		store.setValue(HelpPreferences.INFOCENTER_URL_KEY, textInfocenterURL.getText());
		int choice = 0;
		if (radioButtonLocalServerManual.getSelection())
			choice = 1;
		store.setValue(HelpPreferences.LOCAL_SERVER_CONFIG, choice);
		store.setValue(
			HelpPreferences.LOCAL_SERVER_ADDRESS_KEY,
			textServerAddr.getText());
		store.setValue(HelpPreferences.LOCAL_SERVER_PORT_KEY, textServerPort.getText());
		choice = 0;
		if (radioButtonWarning.getSelection())
			choice = 1;
		else if (radioButtonDebug.getSelection())
			choice = 2;
		store.setValue(HelpPreferences.LOG_LEVEL_KEY, choice);
		if (!System.getProperty("os.name").startsWith("Win")) {
			store.setValue(HelpPreferences.BROWSER_PATH_KEY, textBrowserPath.getText());
		}
		// trigger updating values in the rest of help
		HelpSystem.setPreferences(store);
		// store is saved on help plugin shutdown
	}
	/**
	 * Creates a tab of one horizontal spans.
	 *
	 * @param parent  the parent in which the tab should be created
	 */
	private void tabForward(Composite parent) {
		Label vfiller = new Label(parent, SWT.LEFT);
		GridData gridData = new GridData();
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = false;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessVerticalSpace = false;
		vfiller.setLayoutData(gridData);
	}
}