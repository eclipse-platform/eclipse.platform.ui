/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IConnectionMethod;
import org.eclipse.team.internal.ccvs.core.IUserInfo;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.dialogs.DetailsDialogWithProjects;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;

public class CVSRepositoryPropertiesPage extends PropertyPage {
	ICVSRepositoryLocation location;
	
	// Widgets
	Text userText;
	Text passwordText;
	Combo methodType;
	Label hostLabel;
	Label pathLabel;
	Label portLabel;
	
	boolean passwordChanged;
	boolean connectionInfoChanged;

	IUserInfo info;

	// Program Name
	private Text programNameText;
	private Button useDefaultProgramName;
	private Button useCustomProgramName;
	// Label
	private Button useLocationAsLabel;
	private Button useCustomLabel;
	private Text labelText;
	// Read/write access
	private Button useDefaultReadWriteLocations;
	private Button useCustomReadWriteLocations;
	private Combo readLocation;
	private Combo writeLocation;
			
	/*
	 * @see PreferencesPage#createContents
	 */
	protected Control createContents(Composite parent) {
		initialize();
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		// Repository Label
		// create a composite to ensure the radio buttons come in the correct order
		Composite labelGroup = new Composite(composite, SWT.NONE);
		GridData data = new GridData();
		data.horizontalSpan = 3;
		labelGroup.setLayoutData(data);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		labelGroup.setLayout(layout);
		Listener labelListener = new Listener() {
			public void handleEvent(Event event) {
				updateWidgetEnablements();
			}
		};
		useLocationAsLabel = createRadioButton(labelGroup, Policy.bind("CVSRepositoryPropertiesPage.useLocationAsLabel"), 3); //$NON-NLS-1$
		useCustomLabel = createRadioButton(labelGroup, Policy.bind("CVSRepositoryPropertiesPage.useCustomLabel"), 1); //$NON-NLS-1$
		useCustomLabel.addListener(SWT.Selection, labelListener);
		labelText = createTextField(labelGroup);
		labelText.addListener(SWT.Modify, labelListener);
		
		// Add some extra space
		createLabel(composite, "", 3); //$NON-NLS-1$
		
		createLabel(composite, Policy.bind("CVSPropertiesPage.connectionType"), 1); //$NON-NLS-1$
		methodType = createCombo(composite);
		
		createLabel(composite, Policy.bind("CVSPropertiesPage.user"), 1); //$NON-NLS-1$
		userText = createTextField(composite);
		
		createLabel(composite, Policy.bind("CVSPropertiesPage.password"), 1); //$NON-NLS-1$
		passwordText = createPasswordField(composite);
			
		createLabel(composite, Policy.bind("CVSPropertiesPage.host"), 1); //$NON-NLS-1$
		hostLabel = createLabel(composite, "", 2); //$NON-NLS-1$
		
		createLabel(composite, Policy.bind("CVSPropertiesPage.port"), 1); //$NON-NLS-1$
		portLabel = createLabel(composite, "", 2); //$NON-NLS-1$
		
		createLabel(composite, Policy.bind("CVSPropertiesPage.path"), 1); //$NON-NLS-1$
		pathLabel = createLabel(composite, "", 2); //$NON-NLS-1$

		// Add some extra space
		createLabel(composite, "", 3); //$NON-NLS-1$

		// Remote CVS program name
		// create a composite to ensure the radio buttons come in the correct order
		Composite programNameGroup = createRadioGroupComposite(composite);
		Listener programNameListener = new Listener() {
			public void handleEvent(Event event) {
				updateWidgetEnablements();
			}
		};
		useDefaultProgramName = createRadioButton(programNameGroup, Policy.bind("CVSRepositoryPropertiesPage.useDefaultProgramName"), 3); //$NON-NLS-1$
		useCustomProgramName = createRadioButton(programNameGroup, Policy.bind("CVSRepositoryPropertiesPage.useProgramName"), 1); //$NON-NLS-1$
		useCustomProgramName.addListener(SWT.Selection, programNameListener);
		programNameText = createTextField(programNameGroup);
		programNameText.addListener(SWT.Modify, programNameListener);
		
		// Add some extra space
		createLabel(composite, "", 3); //$NON-NLS-1$
		
		createReadWriteAccessComposite(composite);
		
		initializeValues();
		updateWidgetEnablements();
		passwordText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				passwordChanged = true;
			}
		});
		userText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				connectionInfoChanged = true;
			}
		});
		methodType.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				connectionInfoChanged = true;
			}
		});
		useDefaultReadWriteLocations.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateWidgetEnablements();

			}
		});
		useCustomReadWriteLocations.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateWidgetEnablements();
	
			}
		});
		
		WorkbenchHelp.setHelp(getControl(), IHelpContextIds.REPOSITORY_LOCATION_PROPERTY_PAGE);
		return composite;
	}
	/**
	 * @param composite
	 */
	private void createReadWriteAccessComposite(Composite composite) {
		Composite radioGroup = createRadioGroupComposite(composite);
		useDefaultReadWriteLocations = createRadioButton(radioGroup, "Use this location's connection information for all connections", 3);
		useCustomReadWriteLocations = createRadioButton(radioGroup, "Use the following locations for read and write access", 3);
		createLabel(composite, "Read:", 1);
		readLocation = createCombo(composite);
		createLabel(composite, "Write:", 1);
		writeLocation = createCombo(composite);
	}
	/**
	 * @param composite
	 */
	private Composite createRadioGroupComposite(Composite composite) {
		Composite radioGroup = new Composite(composite, SWT.NONE);
		GridData data = new GridData();
		data.horizontalSpan = 3;
		radioGroup.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		radioGroup.setLayout(layout);
		return radioGroup;
	}
	
	/**
	 * Utility method that creates a combo box
	 *
	 * @param parent  the parent for the new label
	 * @return the new widget
	 */
	protected Combo createCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.horizontalSpan = 2;
		combo.setLayoutData(data);
		return combo;
	}
	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	protected Label createLabel(Composite parent, String text, int span) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	/**
	 * Create a text field specific for this application
	 *
	 * @param parent  the parent of the new text field
	 * @return the new text field
	 */
	protected Text createTextField(Composite parent) {
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		return layoutTextField(text);
	}
	/**
	 * Create a password field specific for this application
	 *
	 * @param parent  the parent of the new text field
	 * @return the new text field
	 */
	protected Text createPasswordField(Composite parent) {
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		return layoutTextField(text);
	}
	/**
	 * Layout a text or password field specific for this application
	 *
	 * @param parent  the parent of the new text field
	 * @return the new text field
	 */
	protected Text layoutTextField(Text text) {
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.horizontalSpan = 2;
		text.setLayoutData(data);
		return text;
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
		GridData data = new GridData();
		data.horizontalSpan = span;
		button.setLayoutData(data);
		return button;
	}
	/**
	 * Initializes the page
	 */
	private void initialize() {
		location = null;
		IAdaptable element = getElement();
		if (element instanceof ICVSRepositoryLocation) {
			location = (ICVSRepositoryLocation)element;
		} else {
			Object adapter = element.getAdapter(ICVSRepositoryLocation.class);
			if (adapter instanceof ICVSRepositoryLocation) {
				location = (ICVSRepositoryLocation)adapter;
			}
		}
	}
	/**
	 * Set the initial values of the widgets
	 */
	private void initializeValues() {
		passwordChanged = false;
		
		IConnectionMethod[] methods = CVSRepositoryLocation.getPluggedInConnectionMethods();
		for (int i = 0; i < methods.length; i++) {
			methodType.add(methods[i].getName());
		}
		String method = location.getMethod().getName();
		methodType.select(methodType.indexOf(method));
		info = location.getUserInfo(true);
		userText.setText(info.getUsername());
		passwordText.setText("*********"); //$NON-NLS-1$
		hostLabel.setText(location.getHost());
		int port = location.getPort();
		if (port == ICVSRepositoryLocation.USE_DEFAULT_PORT) {
			portLabel.setText(Policy.bind("CVSPropertiesPage.defaultPort")); //$NON-NLS-1$
		} else {
			portLabel.setText("" + port); //$NON-NLS-1$
		}
		pathLabel.setText(location.getRootDirectory());
		
		// get the program name
		String programName = ((CVSRepositoryLocation)location).getRemoteCVSProgramName();
		programNameText.setText(programName);
		useDefaultProgramName.setSelection(programName == CVSRepositoryLocation.DEFAULT_REMOTE_CVS_PROGRAM_NAME);
		useCustomProgramName.setSelection(!useDefaultProgramName.getSelection());
		
		// get the repository label
		String label = null;
		RepositoryRoot root = CVSUIPlugin.getPlugin().getRepositoryManager().getRepositoryRootFor(location);
		label = root.getName();
		useLocationAsLabel.setSelection(label == null);
		useCustomLabel.setSelection(!useLocationAsLabel.getSelection());
		if (label == null) {
			label = location.getLocation();
		}
		labelText.setText(label);
		
		// Fill in read/write repo locations
		String currentReadLocation = ((CVSRepositoryLocation)root.getRoot()).getReadLocation();
		String currentWriteLocation = ((CVSRepositoryLocation)root.getRoot()).getWriteLocation();
		try {
			// Ensure the read and write locations are listed
			if (currentReadLocation != null) {
				CVSProviderPlugin.getPlugin().getRepository(currentReadLocation);
			}
			if (currentWriteLocation != null) {
				CVSProviderPlugin.getPlugin().getRepository(currentWriteLocation);
			}
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
		}

		ICVSRepositoryLocation[] locations = CVSProviderPlugin.getPlugin().getKnownRepositories();
		for (int i = 0; i < locations.length; i++) {
			ICVSRepositoryLocation location = locations[i];
			readLocation.add(location.getLocation());
			writeLocation.add(location.getLocation());
		}
		readLocation.setText(currentReadLocation == null ? root.getRoot().getLocation() : currentReadLocation);
		writeLocation.setText(currentWriteLocation == null ? root.getRoot().getLocation() : currentWriteLocation);
		if (currentReadLocation == null && currentWriteLocation == null) {
			useDefaultReadWriteLocations.setSelection(true);
			useCustomReadWriteLocations.setSelection(false);
		} else {
			useDefaultReadWriteLocations.setSelection(false);
			useCustomReadWriteLocations.setSelection(true);
		}
	}
	
	private boolean performConnectionInfoChanges() {
		// Don't do anything if there wasn't a password or connection change
		if (!passwordChanged && !connectionInfoChanged) return true;
		
		try {
			// Check if the password was the only thing to change.
			if (passwordChanged && !connectionInfoChanged) {
				CVSRepositoryLocation oldLocation = (CVSRepositoryLocation)location;
				oldLocation.setPassword(getNewPassword());
				oldLocation.updateCache();
				passwordChanged = false;
				return true;
			}
		
			// Otherwise change the connection info and the password
			// This operation is done inside a workspace operation in case the sharing
			// info for existing projects is changed
			final boolean[] result = new boolean[] { false };
			new ProgressMonitorDialog(getShell()).run(false, false, new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						// Create a new repository location with the new information
						CVSRepositoryLocation newLocation = CVSRepositoryLocation.fromString(location.getLocation());
						newLocation.setMethod(methodType.getText());
						info.setUsername(userText.getText());
						if (passwordChanged) {
							info.setPassword(getNewPassword());
						}
						newLocation.setUserInfo(info);
						
						try {
							// For each project shared with the old location, set connection info to the new one
							List projects = new ArrayList();
							IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
							for (int i = 0; i < allProjects.length; i++) {
								RepositoryProvider teamProvider = RepositoryProvider.getProvider(allProjects[i], CVSProviderPlugin.getTypeId());
								if (teamProvider != null) {
									CVSTeamProvider cvsProvider = (CVSTeamProvider)teamProvider;
									if (cvsProvider.getCVSWorkspaceRoot().getRemoteLocation().equals(location)) {
										projects.add(allProjects[i]);
									}
								}
							}
							if (projects.size() > 0) {
								// To do: warn the user
								DetailsDialogWithProjects dialog = new DetailsDialogWithProjects(
									getShell(), 
									Policy.bind("CVSRepositoryPropertiesPage.Confirm_Project_Sharing_Changes_1"), //$NON-NLS-1$
									Policy.bind("CVSRepositoryPropertiesPage.There_are_projects_in_the_workspace_shared_with_this_repository_2"), //$NON-NLS-1$
									Policy.bind("CVSRepositoryPropertiesPage.sharedProject", location.toString()), //$NON-NLS-1$
									(IProject[]) projects.toArray(new IProject[projects.size()]),
									true,
									DetailsDialogWithProjects.DLG_IMG_WARNING);
								int r = dialog.open();
								if (r != DetailsDialogWithProjects.OK) {
									result[0] = false;
									return;
								}
								monitor.beginTask(null, 1000 * projects.size());
								try {
									Iterator it = projects.iterator();
									while (it.hasNext()) {
										IProject project = (IProject)it.next();
										RepositoryProvider teamProvider = RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId());
										CVSTeamProvider cvsProvider = (CVSTeamProvider)teamProvider;
										cvsProvider.setRemoteRoot(newLocation, Policy.subMonitorFor(monitor, 1000));
									}
								} finally {
									monitor.done();
								}
							}
							
							// Dispose the old repository location
							CVSUIPlugin.getPlugin().getRepositoryManager().replaceRepositoryLocation(location, newLocation);
							
						} finally {
							// Even if we failed, ensure that the new location appears in the repo view.
							newLocation = (CVSRepositoryLocation)CVSProviderPlugin.getPlugin().getRepository(newLocation.getLocation());
							newLocation.updateCache();
						}
						
						// Set the location of the page to the new location in case Apply was chosen
						location = newLocation;
						connectionInfoChanged = false;
						passwordChanged = false;
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
					result[0] = true;
				}
			});
			return result[0];
		} catch (InvocationTargetException e) {
			handle(e);
		} catch (InterruptedException e) {
		} catch (CVSException e) {
			handle(e);
		}
		return false; /* we only get here if an exception occurred */
	}
	
	private void performNonConnectionInfoChanges() {
		recordNewProgramName((CVSRepositoryLocation)location);
		recordNewLabel((CVSRepositoryLocation)location);
		recordReadWriteLocations((CVSRepositoryLocation)location);
	}
	/*
	 * @see PreferencesPage#performOk
	 */
	public boolean performOk() {
		if (performConnectionInfoChanges()) {
			performNonConnectionInfoChanges();
			return true;
		}
		return false;
	}
	/**
	 * Shows the given errors to the user.
	 */
	protected void handle(Throwable e) {
		CVSUIPlugin.openError(getShell(), null, null, e);
	}
	
	/**
	 * Updates widget enablements and sets error message if appropriate.
	 */
	protected void updateWidgetEnablements() {
		if (useDefaultProgramName.getSelection()) {
			programNameText.setEnabled(false);
		} else {
			programNameText.setEnabled(true);
		}
		if (useLocationAsLabel.getSelection()) {
			labelText.setEnabled(false);
		} else {
			labelText.setEnabled(true);
		}
		if (useDefaultReadWriteLocations.getSelection()) {
			readLocation.setEnabled(false);
			writeLocation.setEnabled(false);
		} else {
			readLocation.setEnabled(true);
			writeLocation.setEnabled(true);
		}
		validateFields();
	}
	
	private void validateFields() {
		if (programNameText.isEnabled()) {
			if (programNameText.getText().length() == 0) {
				setValid(false);
				return;
			}
		}
		if (labelText.isEnabled()) {
			if (labelText.getText().length() == 0) {
				setValid(false);
				return;
			}
		}
		setValid(true);
	}
	
	private void recordNewProgramName(CVSRepositoryLocation location) {
		String newProgramName = getNewProgramName();
		if (getOldProgramName(location).equals(newProgramName)) return;
		CVSProviderPlugin.getPlugin().setCVSProgramName(location, newProgramName);
	}
	private String getOldProgramName(CVSRepositoryLocation location) {
		return location.getRemoteCVSProgramName();
	}
	private String getNewProgramName() {
		// Set the remote program name if appropriate
		String newProgramName;
		if (useDefaultProgramName.getSelection()) {
			newProgramName = CVSRepositoryLocation.DEFAULT_REMOTE_CVS_PROGRAM_NAME;
		} else {
			newProgramName = programNameText.getText();
		}
		return newProgramName;
	}
	private void recordNewLabel(CVSRepositoryLocation location) {
		String newLabel = getNewLabel(location);
		if (newLabel == null) {
			String oldLabel = getOldLabel(location);
			if (oldLabel == null || oldLabel.equals(location.getLocation())) {
				return;
			}
		} else if (newLabel.equals(getOldLabel(location))) {
			return;
		}
		try {
			CVSUIPlugin.getPlugin().getRepositoryManager().setLabel(location, newLabel);
		} catch (CVSException e) {
			CVSUIPlugin.log(e);
		}
	}
	private String getOldLabel(CVSRepositoryLocation location) {
		return CVSUIPlugin.getPlugin().getRepositoryManager().getRepositoryRootFor(location).getName();
	}
	private String getNewLabel(CVSRepositoryLocation location) {
		String label = null;
		if (useCustomLabel.getSelection()) {
			label = labelText.getText();
			if (label.equals(location.getLocation())) {
				label = null;
			}
		}
		return label;
	}
	/* internal use only */ String getNewPassword() {
		return passwordText.getText();
	}
	private void recordReadWriteLocations(CVSRepositoryLocation location) {
		location.setReadLocation(useDefaultReadWriteLocations.getSelection() ? null : readLocation.getText());
		location.setWriteLocation(useDefaultReadWriteLocations.getSelection() ? null : writeLocation.getText());
		// TODO: These will be lost if a crash occurres before shutdown
	}
}

