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
import org.eclipse.team.internal.ui.DetailsDialogWithProjects;
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
	boolean programNameChanged;
	boolean labelChanged;

	IUserInfo info;

	// Program Name
	private Text programNameText;
	private Button useDefaultProgramName;
	private Button useCustomProgramName;
	// Label
	private Button useLocationAsLabel;
	private Button useCustomLabel;
	private Text labelText;
			
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
				labelChanged = true;
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
		
		Label label = createLabel(composite, Policy.bind("CVSPropertiesPage.connectionType"), 1); //$NON-NLS-1$
		methodType = createCombo(composite);
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.user"), 1); //$NON-NLS-1$
		userText = createTextField(composite);
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.password"), 1); //$NON-NLS-1$
		passwordText = createTextField(composite);
		passwordText.setEchoChar('*');
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.host"), 1); //$NON-NLS-1$
		hostLabel = createLabel(composite, "", 2); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.port"), 1); //$NON-NLS-1$
		portLabel = createLabel(composite, "", 2); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.path"), 1); //$NON-NLS-1$
		pathLabel = createLabel(composite, "", 2); //$NON-NLS-1$

		// Add some extra space
		createLabel(composite, "", 3); //$NON-NLS-1$

		// Remote CVS program name
		// create a composite to ensure the radio buttons come in the correct order
		Composite programNameGroup = new Composite(composite, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 3;
		programNameGroup.setLayoutData(data);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		programNameGroup.setLayout(layout);
		Listener programNameListener = new Listener() {
			public void handleEvent(Event event) {
				programNameChanged = true;
				updateWidgetEnablements();
			}
		};
		useDefaultProgramName = createRadioButton(programNameGroup, Policy.bind("CVSRepositoryPropertiesPage.useDefaultProgramName"), 3); //$NON-NLS-1$
		useCustomProgramName = createRadioButton(programNameGroup, Policy.bind("CVSRepositoryPropertiesPage.useProgramName"), 1); //$NON-NLS-1$
		useCustomProgramName.addListener(SWT.Selection, programNameListener);
		programNameText = createTextField(programNameGroup);
		programNameText.addListener(SWT.Modify, programNameListener);
				
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
		WorkbenchHelp.setHelp(composite, IHelpContextIds.REPOSITORY_LOCATION_PROPERTY_PAGE);
		return composite;
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
	}
	
	/*
	 * @see PreferencesPage#performOk
	 */
	public boolean performOk() {
		if (!connectionInfoChanged && !passwordChanged) {
			if (programNameChanged) {
				recordNewProgramName((CVSRepositoryLocation)location);
			}
			if (labelChanged) {
				recordNewLabel((CVSRepositoryLocation)location);
			}
			return true;
		}
		info.setUsername(userText.getText());
		if (passwordChanged) {
			info.setPassword(passwordText.getText());
		}
		final String type = methodType.getText();
		final String password = passwordText.getText();
		final boolean[] result = new boolean[] { false };
		try {
			// This operation is done inside a workspace operation in case the sharing
			// info for existing projects is changed
			new ProgressMonitorDialog(getShell()).run(false, false, new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						// Check if the password was the only thing to change.
						if (passwordChanged && !connectionInfoChanged) {
							CVSRepositoryLocation oldLocation = (CVSRepositoryLocation)location;
							oldLocation.setPassword(password);
							oldLocation.updateCache();
							passwordChanged = false;
							result[0] = true;
							return;
						}
						
						// Create a new repository location with the new information
						CVSRepositoryLocation newLocation = CVSRepositoryLocation.fromString(location.getLocation());
						newLocation.setMethod(type);
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
						
						if (programNameChanged) {
							recordNewProgramName((CVSRepositoryLocation)location);
						}
						if (labelChanged) {
							recordNewLabel((CVSRepositoryLocation)location);
						}
			
						connectionInfoChanged = false;
						passwordChanged = false;
						programNameChanged = false;
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
					result[0] = true;
				}
			});
		} catch (InvocationTargetException e) {
			handle(e);
		} catch (InterruptedException e) {
		}
					
		return result[0];
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
		// Set the remote program name if appropriate
		String newProgramName;
		if (useDefaultProgramName.getSelection()) {
			newProgramName = CVSRepositoryLocation.DEFAULT_REMOTE_CVS_PROGRAM_NAME;
		} else {
			newProgramName = programNameText.getText();
		}
		if (!location.getRemoteCVSProgramName().equals(newProgramName)) {
			CVSProviderPlugin.getPlugin().setCVSProgramName(location, newProgramName);
		}
	}
	
	private void recordNewLabel(CVSRepositoryLocation location) {
		String label = null;
		if (useCustomLabel.getSelection()) {
			label = labelText.getText();
			if (label.equals(location.getLocation())) {
				label = null;
			}
		}
		try {
			CVSUIPlugin.getPlugin().getRepositoryManager().setLabel(location, label);
		} catch (CVSException e) {
			CVSUIPlugin.log(e);
		}
	}
}

