/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IUserInfo;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ui.DetailsDialogWithProjects;
import org.eclipse.ui.dialogs.PropertyPage;

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
		
		initializeValues();
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
		
		String[] methods = CVSProviderPlugin.getProvider().getSupportedConnectionMethods();
		for (int i = 0; i < methods.length; i++) {
			methodType.add(methods[i]);
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
	}
	
	/*
	 * @see PreferencesPage#performOk
	 */
	public boolean performOk() {
		if (!connectionInfoChanged && !passwordChanged) {
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
			new ProgressMonitorDialog(getShell()).run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
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
								true);
							int r = dialog.open();
							if (r != dialog.OK) {
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
						CVSProviderPlugin.getProvider().disposeRepository(location);
						
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
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof TeamException) {
				handle((TeamException)t);
			} else if (t instanceof CoreException) {
				handle(((CoreException)t).getStatus());
			} else {
				IStatus status = new Status(IStatus.ERROR, CVSUIPlugin.ID, 1, Policy.bind("internal"), t); //$NON-NLS-1$
				handle(status);
				CVSUIPlugin.log(status);
			}
		} catch (InterruptedException e) {
		}
					
		return result[0];
	}
	/**
	 * Shows the given errors to the user.
	 */
	protected void handle(TeamException e) {
		handle(e.getStatus());
	}
	
	protected void handle(IStatus status) {
		if (!status.isOK()) {
			IStatus toShow = status;
			if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				if (children.length == 1) {
					toShow = children[0];
				}
			}
			ErrorDialog.openError(getShell(), status.getMessage(), null, toShow);
		}
	}
}

