package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Wizard page for configuring a project with a CVS repository location.
 * 
 * This is a multi-purpose wizard page. Its contents are determined by a
 * bitmask of capabilities set with setStyle().
 */
public class ConfigurationWizardMainPage extends CVSWizardPage {
	public static final int CONNECTION_METHOD = 1 << 0;
	public static final int USER = 1 << 1;
	public static final int PASSWORD = 1 << 2;
	public static final int HOST = 1 << 3;
	public static final int PORT = 1 << 4;
	public static final int REPOSITORY_PATH = 1 << 5;
	public static final int MODULE_TEXT = 1 << 6;
	public static final int MODULE_RADIO = 1 << 7;
	public static final int PROJECT_NAME = 1 << 8;
	public static final int TAG = 1 << 9;
	public static final int PROJECT_LOCATION = 1 << 10;
	
	// The mode for the page
	int style;
	
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
	// Module combo
	private Combo moduleCombo;
	// Module radio
	private Button moduleIsProject;
	private Button moduleIsCustom;
	private Text moduleText;
	// Project name
	private Button projectIsModule;
	private Button projectIsCustom;
	private Text projectText;
	// Tag
	private Button checkoutHead;
	private Button checkoutTag;
	private Combo tagCombo;

	private static final int COMBO_HISTORY_LENGTH = 5;
	
	private Properties properties = new Properties();
	
	// Dialog store id constants
	private static final String STORE_USERNAME_ID =
		"ConfigurationWizardMainPage.STORE_USERNAME_ID";//$NON-NLS-1$
	private static final String STORE_HOSTNAME_ID =
		"ConfigurationWizardMainPage.STORE_HOSTNAME_ID";//$NON-NLS-1$
	private static final String STORE_PATH_ID =
		"ConfigurationWizardMainPage.STORE_PATH_ID";//$NON-NLS-1$
	private static final String STORE_MODULE_ID =
		"ConfigurationWizardMainPage.STORE_MODULE_ID";//$NON-NLS-1$
	private static final String STORE_TAG_ID =
		"ConfigurationWizardMainPage.STORE_TAG_ID";//$NON-NLS-1$
	
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
		
		if ((style & CONNECTION_METHOD) != 0) {
			createLabel(composite, Policy.bind("ConfigurationWizardMainPage.connection"));
			connectionMethodCombo = createCombo(composite);
		}
		if ((style & USER) != 0) {
			createLabel(composite, Policy.bind("ConfigurationWizardMainPage.userName"));
			userCombo = createEditableCombo(composite);
		}
		if ((style & PASSWORD) != 0) {
			createLabel(composite, Policy.bind("ConfigurationWizardMainPage.password"));
			passwordText = createTextField(composite);
			passwordText.setEchoChar('*');
		}
		if ((style & HOST) != 0) {
			createLabel(composite, Policy.bind("ConfigurationWizardMainPage.host"));
			hostCombo = createEditableCombo(composite);
		}
		if ((style & PORT) != 0) {
			useDefaultPort = createRadioButton(composite, Policy.bind("ConfigurationWizardMainPage.useDefaultPort"), 2);
			useCustomPort = createRadioButton(composite, Policy.bind("ConfigurationWizardMainPage.usePort"), 1);
			portText = createTextField(composite);
		}
		if ((style & REPOSITORY_PATH) != 0) {
			createLabel(composite, Policy.bind("ConfigurationWizardMainPage.repositoryPath"));
			repositoryPathCombo = createEditableCombo(composite);
		}
		if ((style & MODULE_TEXT) != 0) {
			createLabel(composite, Policy.bind("ConfigurationWizardMainPage.module"));
			moduleCombo = createEditableCombo(composite);
		} else if ((style & MODULE_RADIO) != 0) {
			Composite radioComposite = createRadioComposite(composite);
			moduleIsProject = createRadioButton(radioComposite, Policy.bind("ConfigurationWizardMainPage.moduleIsProject"), 2);
			moduleIsCustom = createRadioButton(radioComposite, Policy.bind("ConfigurationWizardMainPage.module"), 1);
			moduleText = createTextField(radioComposite);
		}
		if ((style & PROJECT_NAME) != 0) {
			Composite radioComposite = createRadioComposite(composite);
			projectIsModule = createRadioButton(radioComposite, Policy.bind("ConfigurationWizardMainPage.projectIsModule"), 2);
			projectIsCustom = createRadioButton(radioComposite, Policy.bind("ConfigurationWizardMainPage.projectName"), 1);
			projectText = createTextField(radioComposite);			
		}
		if ((style & TAG) != 0) {
			createLabel(composite, "");
			createLabel(composite, "");
			Composite radioComposite = createRadioComposite(composite);	
			checkoutHead = createRadioButton(radioComposite, Policy.bind("ConfigurationWizardMainPage.checkoutHead"), 2);
			checkoutTag = createRadioButton(radioComposite, Policy.bind("ConfigurationWizardMainPage.checkoutTag"), 1);
			tagCombo = createEditableCombo(radioComposite);
		}
		
		initializeValues();
		updateWidgetEnablements();
		if (userCombo != null) {
			userCombo.setFocus();
		}
		
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
		combo.addListener(SWT.Modify, this);
		return combo;
	}
	/**
	 * Utility method to create a radio button
	 * 
	 * @param parent  the parent of the radio button
	 * @param label  the label of the radio button
	 * @param span  the number of columns to span
	 * @return the created radio button
	 */
	protected Button createRadioButton(Composite parent, String label, int span) {
		Button button = new Button(parent, SWT.RADIO);
		button.setText(label);
		button.addListener(SWT.Selection, this);
		GridData data = new GridData();
		data.horizontalSpan = span;
		button.setLayoutData(data);
		return button;
	}
	/**
	 * Utility method to create a composite for radio buttons
	 * 
	 * @param composite  the parent
	 * @return the created composite
	 */
	protected Composite createRadioComposite(Composite composite) {
		Composite comboComposite = new Composite(composite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comboComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		comboComposite.setLayoutData(data);
		return comboComposite;
	}
	/**
	 * @see CVSWizardPage#finish
	 */
	public boolean finish(IProgressMonitor monitor) {
		// Set the result to be the current values
		Properties result = new Properties();
		if ((style & CONNECTION_METHOD) != 0) {
			result.setProperty("connection", connectionMethodCombo.getText());
		}
		if ((style & USER) != 0) {
			result.setProperty("user", userCombo.getText());
		}
		if ((style & PASSWORD) != 0) {
			result.setProperty("password", passwordText.getText());
		}
		if ((style & HOST) != 0) {
			result.setProperty("host", hostCombo.getText());
		}
		if ((style & PORT) != 0) {
			if (useCustomPort.getSelection()) {
				result.setProperty("port", portText.getText());
			}
		}
		if ((style & REPOSITORY_PATH) != 0) {
			result.setProperty("root", repositoryPathCombo.getText());
		}
		if ((style & TAG) != 0) {
			if (checkoutTag.getSelection()) {
				result.setProperty("tag", tagCombo.getText());
			}
		}
		if ((style & PROJECT_NAME) != 0) {
			if (projectIsCustom.getSelection()) {
				result.setProperty("project", projectText.getText());
			}
		}
		if ((style & MODULE_RADIO) != 0) {
			if (moduleIsCustom.getSelection()) {
				result.setProperty("module", moduleText.getText());
			}
		} else if ((style & MODULE_TEXT) != 0) {
			result.setProperty("module", moduleCombo.getText());
		}
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
			if ((style & HOST) != 0) {
				String[] hostNames = settings.getArray(STORE_HOSTNAME_ID);
				if (hostNames != null) {
					for (int i = 0; i < hostNames.length; i++) {
						hostCombo.add(hostNames[i]);
					}
				}
			}
			if ((style & REPOSITORY_PATH) != 0) {
				String[] paths = settings.getArray(STORE_PATH_ID);
				if (paths != null) {
					for (int i = 0; i < paths.length; i++) {
						repositoryPathCombo.add(paths[i]);
					}
				}
			}
			if ((style & USER) != 0) {
				String[] userNames = settings.getArray(STORE_USERNAME_ID);
				if (userNames != null) {
					for (int i = 0; i < userNames.length; i++) {
						userCombo.add(userNames[i]);
					}
				}
			}
			if ((style & MODULE_TEXT) != 0) {
				String[] moduleNames = settings.getArray(STORE_MODULE_ID);
				if (moduleNames != null) {
					for (int i = 0; i < moduleNames.length; i++) {
						moduleCombo.add(moduleNames[i]);
					}
				}
			}
			if ((style & TAG) != 0) {
				String[] tags = settings.getArray(STORE_TAG_ID);
				if (tags != null) {
					for (int i = 0; i < tags.length; i++) {
						tagCombo.add(tags[i]);
					}
				}
			}
		}
		
		// Initialize other values and widget states
		if ((style & CONNECTION_METHOD) != 0) {
			String[] methods = CVSProviderPlugin.getProvider().getSupportedConnectionMethods();
			for (int i = 0; i < methods.length; i++) {
				connectionMethodCombo.add(methods[i]);
			}
			String method = (String)properties.getProperty("connection");
			if (method == null) {
				connectionMethodCombo.select(0);
			} else {
				connectionMethodCombo.select(connectionMethodCombo.indexOf(method));
			}
		}
		if ((style & USER) != 0) {
			String user = (String)properties.getProperty("user");
			if (user != null) {
				userCombo.setText(user);
			}
		}
		if ((style & PASSWORD) != 0) {
			String password = (String)properties.getProperty("password");
			if (password != null) {
				passwordText.setText(password);
			}
		}
		if ((style & HOST) != 0) {
			String host = (String)properties.getProperty("host");
			if (host != null) {
				hostCombo.setText(host);
			}
		}
		if ((style & PORT) != 0) {
			String port = (String)properties.getProperty("port");
			if (port == null) {
				useDefaultPort.setSelection(true);
			} else {
				useCustomPort.setSelection(true);
				portText.setText(port);
			}
		}
		if ((style & REPOSITORY_PATH) != 0) {
			String repositoryPath = (String)properties.getProperty("root");
			if (repositoryPath != null) {
				repositoryPathCombo.setText(repositoryPath);
			}
		}
		if ((style & MODULE_RADIO) != 0) {
			String module = (String)properties.getProperty("module");
			if (module == null) {
				moduleIsProject.setSelection(true);
			} else {
				moduleIsCustom.setSelection(true);
				moduleText.setText(module);
			}
		} else if ((style & MODULE_TEXT) != 0) {
			String module = (String)properties.getProperty("module");
			if (module != null) {
				moduleCombo.setText(module);
			}
		}
		if ((style & PROJECT_NAME) != 0) {
			String project = (String)properties.getProperty("project");
			if (project == null) {
				projectIsModule.setSelection(true);
			} else {
				projectIsCustom.setSelection(true);
				projectText.setText(project);
			}
		}
		if ((style & TAG) != 0) {
			String tag = (String)properties.getProperty("tag");
			if (tag == null) {
				checkoutHead.setSelection(true);
			} else {
				checkoutTag.setSelection(true);
				tagCombo.setText(tag);
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
			if ((style & USER) != 0) {
				String[] userNames = settings.getArray(STORE_USERNAME_ID);
				if (userNames == null) userNames = new String[0];
				userNames = addToHistory(userNames, userCombo.getText());
				settings.put(STORE_USERNAME_ID, userNames);
			}
			if ((style & HOST) != 0) {
				String[] hostNames = settings.getArray(STORE_HOSTNAME_ID);
				if (hostNames == null) hostNames = new String[0];
				hostNames = addToHistory(hostNames, hostCombo.getText());
				settings.put(STORE_HOSTNAME_ID, hostNames);
			}
			if ((style & REPOSITORY_PATH) != 0) {
				String[] paths = settings.getArray(STORE_PATH_ID);
				if (paths == null) paths = new String[0];
				paths = addToHistory(paths, repositoryPathCombo.getText());
				settings.put(STORE_PATH_ID, paths);
			}
			if ((style & MODULE_TEXT) != 0) {
				String[] modules = settings.getArray(STORE_MODULE_ID);
				if (modules == null) modules = new String[0];
				modules = addToHistory(modules, moduleCombo.getText());
				settings.put(STORE_MODULE_ID, modules);
			}
			if ((style & TAG) != 0) {
				String tag = tagCombo.getText();
				if (!tag.equals("")) {
					String[] tags = settings.getArray(STORE_TAG_ID);
					if (tags == null) tags = new String[0];
					tags = addToHistory(tags, tag);
					settings.put(STORE_TAG_ID, tags);
				}
			}
		}
	}
	/**
	 * Set the style for the wizard page
	 * 
	 * @param style  the style for the page
	 */
	public void setStyle(int style) {
		this.style = style;
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
		if ((style & PORT) != 0) {
			if (useDefaultPort.getSelection()) {
				portText.setEnabled(false);
			} else {
				portText.setEnabled(true);
			}
		}
		if ((style & PROJECT_NAME) != 0) {
			if (projectIsModule.getSelection()) {
				projectText.setEnabled(false);
			} else {
				projectText.setEnabled(true);
			}
		}
		if ((style & MODULE_RADIO) != 0) {
			if (moduleIsProject.getSelection()) {
				moduleText.setEnabled(false);
			} else {
				moduleText.setEnabled(true);
			}
		}
		if ((style & TAG) != 0) {
			if (checkoutHead.getSelection()) {
				tagCombo.setEnabled(false);
			} else {
				tagCombo.setEnabled(true);
			}
		}
		validateFields();
	}
	/**
	 * Validates the contents of the editable fields and set page completion 
	 * and error messages appropriately.
	 */
	private void validateFields() {
		if ((style & USER) != 0) {
			String user = userCombo.getText();
			if (user.length() == 0) {
				setErrorMessage(null);
				setPageComplete(false);
				return;
			}
			if ((user.indexOf('@') != -1) || (user.indexOf(':') != -1)) {
				setErrorMessage(Policy.bind("ConfigurationWizardMainPage.invalidUserName"));
				setPageComplete(false);
				return;
			}
		}
		if ((style & HOST) != 0) {
			String host = hostCombo.getText();
			if (host.length() == 0) {
				setErrorMessage(null);
				setPageComplete(false);
				return;
			}
			if (host.indexOf(':') != -1) {
				setErrorMessage(Policy.bind("ConfigurationWizardMainPage.invalidHostName"));
				setPageComplete(false);
				return;
			}
		}
		if ((style & PORT) != 0) {
			if (portText.isEnabled()) {
				if (portText.getText().length() == 0) {
					setErrorMessage(null);
					setPageComplete(false);
					return;
				}
				try {
					Integer.parseInt(portText.getText());
				} catch (NumberFormatException e) {
					setErrorMessage(Policy.bind("ConfigurationWizardMainPage.invalidPort"));
					setPageComplete(false);
					return;
				}
			}
		}
		if ((style & REPOSITORY_PATH) != 0) {
			if (repositoryPathCombo.getText().length() == 0) {
				setErrorMessage(null);
				setPageComplete(false);
				return;
			}
		}
		if ((style & MODULE_RADIO) != 0) {
			if (moduleIsCustom.getSelection()) {
				if (moduleText.getText().length() == 0) {
					setErrorMessage(null);
					setPageComplete(false);
					return;
				}
			}
		}
		if ((style & MODULE_TEXT) != 0) {
			if (moduleCombo.getText().length() == 0) {
				setErrorMessage(null);
				setPageComplete(false);
				return;
			}
		}
		if ((style & PROJECT_NAME) != 0) {
			if (projectIsCustom.getSelection()) {
				if (projectText.getText().length() == 0) {
					setErrorMessage(null);
					setPageComplete(false);
					return;
				}
			}
		}
		setErrorMessage(null);
		setPageComplete(true);
	}
}
