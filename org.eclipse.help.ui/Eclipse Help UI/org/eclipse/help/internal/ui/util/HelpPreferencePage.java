package org.eclipse.help.internal.ui.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.events.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.ui.WorkbenchHelpPlugin;
import org.eclipse.help.internal.ui.IHelpUIConstants;

/**
 * This class implements a sample preference page that is 
 * added to the preference dialog based on the registration.
 */
public class HelpPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage, Listener {
	// Constants
	public static final String LOG_LEVEL_KEY = "log_level";
	public static final String LOCAL_SERVER_CONFIG = "local_server_config";
	public static final String LOCAL_SERVER_ADDRESS_KEY = "local_server_addr";
	public static final String LOCAL_SERVER_PORT_KEY = "local_server_port";
	public static final String INSTALL_OPTION_KEY = "install";
	public static final String SERVER_PATH_KEY = "server_path";
	public static final String BROWSER_PATH_KEY = "browser_path";

	private Button radioButtonLocal;
	private Button radioButtonClient;
	private Text textServerAddr;
	private Text textServerPort;
	private Text textServerPath;

	private Button radioButtonError;
	private Button radioButtonWarning;
	private Button radioButtonDebug;

	private Text textBrowserPath; // on linux only

	private Button radioButtonLocalServerAutomatic;
	private Button radioButtonLocalServerManual;

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
		WorkbenchHelp.setHelp(
			parent,
			new String[] { IHelpUIConstants.PREF_PAGE });

		/* Installation Options */
		//composite_tab << parent
		Composite composite_tab = createComposite(parent, 1);
		WorkbenchHelp.setHelp(
			composite_tab,
			new String[] {
				IHelpUIConstants.INSTALL_OPTIONS,
				IHelpUIConstants.PREF_PAGE});

		Label label1 =
			createLabel(
				composite_tab,
				WorkbenchResources.getString("Installation_Options"),
				1);

		//radio button composite << tab composite
		Composite composite_radioButton = createComposite(composite_tab, 2);
		radioButtonLocal =
			createRadioButton(
				composite_radioButton,
				WorkbenchResources.getString("Local_install"));
		tabForward(composite_radioButton);
		radioButtonLocal.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				if (((Button) event.widget).getSelection()) {
					textServerPath.setEnabled(false);
				}
			}
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);
			}
		});

		radioButtonClient =
			createRadioButton(
				composite_radioButton,
				WorkbenchResources.getString("Client_only"));
		tabForward(composite_radioButton);
		radioButtonClient.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				if (((Button) event.widget).getSelection()) {
					textServerPath.setEnabled(true);
				}
			}
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);
			}
		});

		Label label_textField =
			createLabel(
				composite_radioButton,
				WorkbenchResources.getString("Server_path"),
				1);
		textServerPath = createTextField(composite_radioButton);

		/* Browser Path */
		if (!System.getProperty("os.name").startsWith("Win")) {
			Composite composite_textField2 = createComposite(parent, 2);
			WorkbenchHelp.setHelp(
				composite_textField2,
				new String[] {
					IHelpUIConstants.BROWSER_PATH,
					IHelpUIConstants.PREF_PAGE});

			label_textField =
				createLabel(
					composite_textField2,
					WorkbenchResources.getString("Browser_path"),
					1);
			textBrowserPath = createTextField(composite_textField2);
		}

		/* Advanced Group */
		Group advancedGroup =
			createGroup(parent, WorkbenchResources.getString("Advanced_Properties"));

		/* Loggin Options */
		//composite_tab2 << parent
		Composite composite_tab2 = createComposite(advancedGroup, 1);
		WorkbenchHelp.setHelp(
			composite_tab2,
			new String[] {
				IHelpUIConstants.LOGGING_OPTIONS,
				IHelpUIConstants.PREF_PAGE});
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
		Composite composite_tab3 = createComposite(advancedGroup, 1);
		WorkbenchHelp.setHelp(
			composite_tab3,
			new String[] {
				IHelpUIConstants.LOCAL_SERVER_CONFIG,
				IHelpUIConstants.PREF_PAGE});
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
					textServerAddr.setEnabled(true);
					textServerPort.setEnabled(true);
				}
			}
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);
			}
		});
		label_textField =
			createLabel(
				composite_radioButton3,
				WorkbenchResources.getString("Server_address"),
				1);
		textServerAddr = createTextField(composite_radioButton3);

		label_textField =
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
	 * Utility method that creates a group instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new button
	 * @param label  the label for the new group
	 * @return the newly-created group
	 */
	private Group createGroup(Composite parent, String label) {
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(label);
		//GridLayout
		group.setLayout(new GridLayout());
		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		group.setLayoutData(data);

		return group;
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
	 * Returns preference store that belongs to the our plugin.
	 * This is important because we want to store
	 * our preferences separately from the desktop.
	 *
	 * @return the preference store for this plugin
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchHelpPlugin.getDefault().getPreferenceStore();
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
		IPreferenceStore store = getPreferenceStore();

		// Local Help Server Configuration
		radioButtonLocalServerAutomatic.setSelection(false);
		radioButtonLocalServerManual.setSelection(false);
		textServerAddr.setEnabled(false);
		textServerPort.setEnabled(false);
		int serverConfigChoice = store.getDefaultInt(LOCAL_SERVER_CONFIG);
		switch (serverConfigChoice) {
			case 0 :
				radioButtonLocalServerAutomatic.setSelection(true);
				break;
			case 1 :
				radioButtonLocalServerManual.setSelection(true);
				textServerAddr.setEnabled(true);
				textServerPort.setEnabled(true);
				break;
		}
		textServerAddr.setText(store.getDefaultString(LOCAL_SERVER_ADDRESS_KEY));
		textServerPort.setText(store.getDefaultString(LOCAL_SERVER_PORT_KEY));

		// Documentation local/remote
		radioButtonLocal.setSelection(false);
		radioButtonClient.setSelection(false);
		int choice = store.getDefaultInt(INSTALL_OPTION_KEY);
		switch (choice) {
			case 0 :
				radioButtonLocal.setSelection(true);
				textServerPath.setEnabled(false);
				break;
			case 1 :
				radioButtonClient.setSelection(true);
				textServerPath.setEnabled(true);
				break;
		}
		textServerPath.setText(store.getDefaultString(SERVER_PATH_KEY));

		radioButtonError.setSelection(false);
		radioButtonWarning.setSelection(false);
		radioButtonDebug.setSelection(false);
		choice = store.getDefaultInt(LOG_LEVEL_KEY);
		switch (choice) {
			case HelpSystem.LOG_ERROR :
				radioButtonError.setSelection(true);
				break;
			case HelpSystem.LOG_WARNING :
				radioButtonWarning.setSelection(true);
				break;
			case HelpSystem.LOG_DEBUG :
				radioButtonDebug.setSelection(true);
				break;
		}

		if (!System.getProperty("os.name").startsWith("Win")) {
			textBrowserPath.setText(store.getDefaultString(BROWSER_PATH_KEY));
		}
	}
	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();

		// Local Help Server Configuration
		radioButtonLocalServerAutomatic.setSelection(false);
		radioButtonLocalServerManual.setSelection(false);
		textServerAddr.setEnabled(false);
		textServerPort.setEnabled(false);
		int serverConfigChoice = store.getInt(LOCAL_SERVER_CONFIG);
		switch (serverConfigChoice) {
			case 0 :
				radioButtonLocalServerAutomatic.setSelection(true);
				break;
			case 1 :
				radioButtonLocalServerManual.setSelection(true);
				textServerAddr.setEnabled(true);
				textServerPort.setEnabled(true);
				break;
		}
		textServerAddr.setText(store.getString(LOCAL_SERVER_ADDRESS_KEY));
		textServerPort.setText(store.getString(LOCAL_SERVER_PORT_KEY));

		// Documentation local/remote
		radioButtonLocal.setSelection(false);
		radioButtonClient.setSelection(false);
		int choice = store.getInt(INSTALL_OPTION_KEY);
		switch (choice) {
			case 0 :
				radioButtonLocal.setSelection(true);
				textServerPath.setEnabled(false);
				break;
			case 1 :
				radioButtonClient.setSelection(true);
				textServerPath.setEnabled(true);
				break;
		}
		textServerPath.setText(store.getString(SERVER_PATH_KEY));

		radioButtonError.setSelection(false);
		radioButtonWarning.setSelection(false);
		radioButtonDebug.setSelection(false);
		choice = store.getInt(LOG_LEVEL_KEY);
		switch (choice) {
			case HelpSystem.LOG_ERROR :
				radioButtonError.setSelection(true);
				break;
			case HelpSystem.LOG_WARNING :
				radioButtonWarning.setSelection(true);
				break;
			case HelpSystem.LOG_DEBUG :
				radioButtonDebug.setSelection(true);
				break;
		}

		if (!System.getProperty("os.name").startsWith("Win")) {
			textBrowserPath.setText(store.getString(BROWSER_PATH_KEY));
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
		IPreferenceStore store = getPreferenceStore();

		int choice = 0;
		if (radioButtonClient.getSelection())
			choice = 1;
		//else if (radioButtonServer.getSelection()) choice = 2;
		store.setValue(INSTALL_OPTION_KEY, choice);

		choice = 0;
		if (radioButtonLocalServerManual.getSelection())
			choice = 1;
		store.setValue(LOCAL_SERVER_CONFIG, choice);

		store.setValue(LOCAL_SERVER_ADDRESS_KEY, textServerAddr.getText());
		store.setValue(LOCAL_SERVER_PORT_KEY, textServerPort.getText());
		store.setValue(SERVER_PATH_KEY, textServerPath.getText());

		choice = 0;
		if (radioButtonWarning.getSelection())
			choice = 1;
		else
			if (radioButtonDebug.getSelection())
				choice = 2;
		store.setValue(LOG_LEVEL_KEY, choice);
		HelpSystem.setDebugLevel(choice);

		if (!System.getProperty("os.name").startsWith("Win")) {
			store.setValue(BROWSER_PATH_KEY, textBrowserPath.getText());
		}
		// set the value in the current session 
		WorkbenchHelpPlugin.getDefault().initializeFromStore();
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
