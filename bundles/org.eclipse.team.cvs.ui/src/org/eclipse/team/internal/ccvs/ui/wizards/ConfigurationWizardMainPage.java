package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Wizard page for entering information about a CVS repository location.
 */
public class ConfigurationWizardMainPage extends CVSWizardPage {
	private boolean showValidate;
	private boolean validate;
	
	// Widgets
	
	// Connection Method
	private Combo connectionMethodCombo;
	// User
	private Combo userCombo;
	// Password
	private Text passwordText;
	// Port
	private Text portText;
	private Button useDefaultPort;
	private Button useCustomPort;
	// Host
	private Combo hostCombo;
	// Repository Path
	private Combo repositoryPathCombo;
	// Validation
	private Button validateButton;
	
	private static final int COMBO_HISTORY_LENGTH = 5;
	
	private Properties properties = null;
	
	// Dialog store id constants
	private static final String STORE_USERNAME_ID =
		"ConfigurationWizardMainPage.STORE_USERNAME_ID";//$NON-NLS-1$
	private static final String STORE_HOSTNAME_ID =
		"ConfigurationWizardMainPage.STORE_HOSTNAME_ID";//$NON-NLS-1$
	private static final String STORE_PATH_ID =
		"ConfigurationWizardMainPage.STORE_PATH_ID";//$NON-NLS-1$
	private static final String STORE_DONT_VALIDATE_ID =
		"ConfigurationWizardMainPage.STORE_DONT_VALIDATE_ID";//$NON-NLS-1$
	
	// In case the page was launched from a different wizard	
	private IDialogSettings settings;
	
	/**
	 * ConfigurationWizardMainPage constructor.
	 * 
	 * @param pageName  the name of the page
	 * @param title  the title of the page
	 * @param titleImage  the image for the page
	 */
	public ConfigurationWizardMainPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	/**
	 * Adds an entry to a history, while taking care of duplicate history items
	 * and excessively long histories.  The assumption is made that all histories
	 * should be of length <code>ConfigurationWizardMainPage.COMBO_HISTORY_LENGTH</code>.
	 *
	 * @param history the current history
	 * @param newEntry the entry to add to the history
	 * @return the history with the new entry appended
	 */
	private String[] addToHistory(String[] history, String newEntry) {
		ArrayList l = new ArrayList(Arrays.asList(history));
		addToHistory(l, newEntry);
		String[] r = new String[l.size()];
		l.toArray(r);
		return r;
	}
	protected IDialogSettings getDialogSettings() {
		return settings;
	}
	protected void setDialogSettings(IDialogSettings settings) {
		this.settings = settings;
	}
	/**
	 * Adds an entry to a history, while taking care of duplicate history items
	 * and excessively long histories.  The assumption is made that all histories
	 * should be of length <code>ConfigurationWizardMainPage.COMBO_HISTORY_LENGTH</code>.
	 *
	 * @param history the current history
	 * @param newEntry the entry to add to the history
	 */
	private void addToHistory(List history, String newEntry) {
		history.remove(newEntry);
		history.add(0,newEntry);
	
		// since only one new item was added, we can be over the limit
		// by at most one item
		if (history.size() > COMBO_HISTORY_LENGTH)
			history.remove(COMBO_HISTORY_LENGTH);
	}
	/**
	 * Creates the UI part of the page.
	 * 
	 * @param parent  the parent of the created widgets
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		// set F1 help
		// WorkbenchHelp.setHelp(composite, new DialogPageContextComputer (this, ITeamHelpContextIds.REPO_CONNECTION_MAIN_PAGE));

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				updateWidgetEnablements();
			}
		};
		
		Group g = createGroup(composite, Policy.bind("ConfigurationWizardMainPage.Location_1")); //$NON-NLS-1$
		
		// Host name
		createLabel(g, Policy.bind("ConfigurationWizardMainPage.host")); //$NON-NLS-1$
		hostCombo = createEditableCombo(g);
		hostCombo.addListener(SWT.Selection, listener);
		hostCombo.addListener(SWT.Modify, listener);
		
		// Repository Path
		createLabel(g, Policy.bind("ConfigurationWizardMainPage.repositoryPath")); //$NON-NLS-1$
		repositoryPathCombo = createEditableCombo(g);
		repositoryPathCombo.addListener(SWT.Selection, listener);
		repositoryPathCombo.addListener(SWT.Modify, listener);

		g = createGroup(composite, Policy.bind("ConfigurationWizardMainPage.Authentication_2")); //$NON-NLS-1$
		
		// User name
		createLabel(g, Policy.bind("ConfigurationWizardMainPage.userName")); //$NON-NLS-1$
		userCombo = createEditableCombo(g);
		userCombo.addListener(SWT.Selection, listener);
		userCombo.addListener(SWT.Modify, listener);
		
		// Password
		createLabel(g, Policy.bind("ConfigurationWizardMainPage.password")); //$NON-NLS-1$
		passwordText = createTextField(g);
		passwordText.setEchoChar('*');

		g = createGroup(composite, Policy.bind("ConfigurationWizardMainPage.Connection_3")); //$NON-NLS-1$
		
		// Connection type
		createLabel(g, Policy.bind("ConfigurationWizardMainPage.connection")); //$NON-NLS-1$
		connectionMethodCombo = createCombo(g);

		// Port number
		useDefaultPort = createRadioButton(g, Policy.bind("ConfigurationWizardMainPage.useDefaultPort"), 2); //$NON-NLS-1$
		useCustomPort = createRadioButton(g, Policy.bind("ConfigurationWizardMainPage.usePort"), 1); //$NON-NLS-1$
		useCustomPort.addListener(SWT.Selection, listener);
		portText = createTextField(g);
		portText.addListener(SWT.Selection, listener);
		
		// create a composite to ensure the validate button is in its own tab group
		if (showValidate) {
			Composite validateButtonTabGroup = new Composite(composite, SWT.NONE);
			GridData data = new GridData();
			data.horizontalSpan = 2;
			validateButtonTabGroup.setLayoutData(data);
			validateButtonTabGroup.setLayout(new FillLayout());

			validateButton = new Button(validateButtonTabGroup, SWT.CHECK);
			validateButton.setText(Policy.bind("ConfigurationWizardAutoconnectPage.validate")); //$NON-NLS-1$
			validateButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					validate = validateButton.getSelection();
				}
			});
		}
		
		initializeValues();
		updateWidgetEnablements();
		hostCombo.setFocus();
		
		setControl(composite);
	}
	/**
	 * Utility method to create an editable combo box
	 * 
	 * @param parent  the parent of the combo box
	 * @return the created combo
	 */
	protected Combo createEditableCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.NULL);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		combo.setLayoutData(data);
		return combo;
	}
	
	protected Group createGroup(Composite parent, String text) {
		Group group = new Group(parent, SWT.NULL);
		group.setText(text);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		//data.widthHint = GROUP_WIDTH;
		
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		return group;
	}
	
	/**
	 * @see CVSWizardPage#finish
	 */
	public boolean finish(IProgressMonitor monitor) {
		// Set the result to be the current values
		Properties result = new Properties();
		result.setProperty("connection", connectionMethodCombo.getText()); //$NON-NLS-1$
		result.setProperty("user", userCombo.getText()); //$NON-NLS-1$
		result.setProperty("password", passwordText.getText()); //$NON-NLS-1$
		result.setProperty("host", hostCombo.getText()); //$NON-NLS-1$
		if (useCustomPort.getSelection()) {
			result.setProperty("port", portText.getText()); //$NON-NLS-1$
		}
		result.setProperty("root", repositoryPathCombo.getText()); //$NON-NLS-1$
		this.properties = result;
		
		saveWidgetValues();
		
		return true;
	}
	/**
	 * Returns the properties for the repository connection
	 * 
	 * @return the properties or null
	 */
	public Properties getProperties() {
		return properties;
	}
	/**
	 * Initializes states of the controls.
	 */
	private void initializeValues() {
		// Set remembered values
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] hostNames = settings.getArray(STORE_HOSTNAME_ID);
			if (hostNames != null) {
				for (int i = 0; i < hostNames.length; i++) {
					hostCombo.add(hostNames[i]);
				}
			}
			String[] paths = settings.getArray(STORE_PATH_ID);
			if (paths != null) {
				for (int i = 0; i < paths.length; i++) {
					repositoryPathCombo.add(paths[i]);
				}
			}
			String[] userNames = settings.getArray(STORE_USERNAME_ID);
			if (userNames != null) {
				for (int i = 0; i < userNames.length; i++) {
					userCombo.add(userNames[i]);
				}
			}
			if (showValidate) {
				validate = !settings.getBoolean(STORE_DONT_VALIDATE_ID);
				validateButton.setSelection(validate);
			}
		}
		
		// Initialize other values and widget states
		String[] methods = CVSProviderPlugin.getProvider().getSupportedConnectionMethods();
		for (int i = 0; i < methods.length; i++) {
			connectionMethodCombo.add(methods[i]);
		}
		
		connectionMethodCombo.select(0);
		useDefaultPort.setSelection(true);
		
		if(properties != null) {
			String method = (String)properties.getProperty("connection"); //$NON-NLS-1$
			if (method == null) {
				connectionMethodCombo.select(0);
			} else {
				connectionMethodCombo.select(connectionMethodCombo.indexOf(method));
			}
	
			String user = (String)properties.getProperty("user"); //$NON-NLS-1$
			if (user != null) {
				userCombo.setText(user);
			}
	
			String password = (String)properties.getProperty("password"); //$NON-NLS-1$
			if (password != null) {
				passwordText.setText(password);
			}
	
			String host = (String)properties.getProperty("host"); //$NON-NLS-1$
			if (host != null) {
				hostCombo.setText(host);
			}
	
			String port = (String)properties.getProperty("port"); //$NON-NLS-1$
			if (port != null) {
				useCustomPort.setSelection(true);
				portText.setText(port);
			}
	
			String repositoryPath = (String)properties.getProperty("root"); //$NON-NLS-1$
			if (repositoryPath != null) {
				repositoryPathCombo.setText(repositoryPath);
			}
		}
	}
	/**
	 * Saves the widget values
	 */
	private void saveWidgetValues() {
		// Update history
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] userNames = settings.getArray(STORE_USERNAME_ID);
			if (userNames == null) userNames = new String[0];
			userNames = addToHistory(userNames, userCombo.getText());
			settings.put(STORE_USERNAME_ID, userNames);

			String[] hostNames = settings.getArray(STORE_HOSTNAME_ID);
			if (hostNames == null) hostNames = new String[0];
			hostNames = addToHistory(hostNames, hostCombo.getText());
			settings.put(STORE_HOSTNAME_ID, hostNames);

			String[] paths = settings.getArray(STORE_PATH_ID);
			if (paths == null) paths = new String[0];
			paths = addToHistory(paths, repositoryPathCombo.getText());
			settings.put(STORE_PATH_ID, paths);

			if (showValidate) {
				settings.put(STORE_DONT_VALIDATE_ID, !validate);
			}
		}
	}

	public void setShowValidate(boolean showValidate) {
		this.showValidate = showValidate;
	}
	
	/**
	 * Sets the properties for the repository connection
	 * 
	 * @param properties  the properties or null
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;		
	}
	
	/**
	 * Updates widget enablements and sets error message if appropriate.
	 */
	protected void updateWidgetEnablements() {
		if (useDefaultPort.getSelection()) {
			portText.setEnabled(false);
		} else {
			portText.setEnabled(true);
		}

		validateFields();
	}
	/**
	 * Validates the contents of the editable fields and set page completion 
	 * and error messages appropriately.
	 */
	private void validateFields() {
		String user = userCombo.getText();
		if (user.length() == 0) {
			setErrorMessage(null);
			setPageComplete(false);
			return;
		}
		if ((user.indexOf('@') != -1) || (user.indexOf(':') != -1)) {
			setErrorMessage(Policy.bind("ConfigurationWizardMainPage.invalidUserName")); //$NON-NLS-1$
			setPageComplete(false);
			return;
		}

		String host = hostCombo.getText();
		if (host.length() == 0) {
			setErrorMessage(null);
			setPageComplete(false);
			return;
		}
		if (host.indexOf(':') != -1) {
			setErrorMessage(Policy.bind("ConfigurationWizardMainPage.invalidHostName")); //$NON-NLS-1$
			setPageComplete(false);
			return;
		}

		if (portText.isEnabled()) {
			if (portText.getText().length() == 0) {
				setErrorMessage(null);
				setPageComplete(false);
				return;
			}
			try {
				Integer.parseInt(portText.getText());
			} catch (NumberFormatException e) {
				setErrorMessage(Policy.bind("ConfigurationWizardMainPage.invalidPort")); //$NON-NLS-1$
				setPageComplete(false);
				return;
			}
		}

		if (repositoryPathCombo.getText().length() == 0) {
			setErrorMessage(null);
			setPageComplete(false);
			return;
		} else {
			String pathString = repositoryPathCombo.getText();
			IPath path = new Path(pathString);
			String[] segments = path.segments();
			for (int i = 0; i < segments.length; i++) {
				String string = segments[i];
				if (string.charAt(0) == ' ' || string.charAt(string.length() -1) == ' ') {
					setErrorMessage(Policy.bind("ConfigurationWizardMainPage.invalidPathWithSpaces")); //$NON-NLS-1$
					setPageComplete(false);
					return;
				}
			}
			// look for // and inform the user that we support use of C:\cvs\root instead of /c//cvs/root
			if (pathString.indexOf("//") != -1) { //$NON-NLS-1$
				if (pathString.indexOf("//") == 2) { //$NON-NLS-1$
					// The user is probably trying to specify a CVSNT path
					setErrorMessage(Policy.bind("ConfigurationWizardMainPage.useNTFormat")); //$NON-NLS-1$
				} else {
					setErrorMessage(Policy.bind("ConfigurationWizardMainPage.invalidPathWithSlashes")); //$NON-NLS-1$
				}
				setPageComplete(false);
				return;
			}
		}
		setErrorMessage(null);
		setPageComplete(true);
	}
	
	public boolean getValidate() {
		return validate;
	}
}
