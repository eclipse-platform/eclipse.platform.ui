/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IUserInfo;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class CVSProjectPropertiesPage extends PropertyPage {
	IProject project;
	ICVSRepositoryLocation oldLocation;
	ICVSRepositoryLocation newLocation = null;

	private static final int TABLE_HEIGHT_HINT = 150;
	private static final int TABLE_WIDTH_HINT = 300;
	
	// Widgets
	Label methodLabel;
	Label userLabel;
	Label hostLabel;
	Label pathLabel;
	Label moduleLabel;
	Label portLabel;
	Label tagLabel;
	
	IUserInfo info;
	CVSTeamProvider provider;

	private class RepositorySelectionDialog extends Dialog {
		ICVSRepositoryLocation[] locations;
		ICVSRepositoryLocation location;
		
		TableViewer viewer;
		Button okButton;
		public RepositorySelectionDialog(Shell shell) {
			super(shell);
		}
		protected void createButtonsForButtonBar(Composite parent) {
			// create OK and Cancel buttons by default
			okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			okButton.setEnabled(false);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}
		protected Control createDialogArea(Composite parent) {
			parent.getShell().setText(Policy.bind("CVSProjectPropertiesPage.Select_a_Repository_1")); //$NON-NLS-1$
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
			createLabel(composite, Policy.bind("CVSProjectPropertiesPage.Select_a_CVS_repository_location_to_share_the_project_with__2"), 1); //$NON-NLS-1$
			Table table = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			GridData data = new GridData();
			data.widthHint = TABLE_WIDTH_HINT;
			data.heightHint = TABLE_HEIGHT_HINT;
			table.setLayoutData(data);
			viewer = new TableViewer(table);
			viewer.setLabelProvider(new WorkbenchLabelProvider());
			viewer.setContentProvider(new WorkbenchContentProvider() {
				public Object[] getElements(Object inputElement) {
					return locations;
				}
			});
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
					if (selection.isEmpty()) {
						location = null;
						okButton.setEnabled(false);
					} else {
						location = (ICVSRepositoryLocation)selection.getFirstElement();
						okButton.setEnabled(true);
					}
				}
			});
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					okPressed();
				}
			});
			viewer.setInput(locations);
			return composite;
		}
		protected void cancelPressed() {
			location = null;
			super.cancelPressed();
		}
		public void setLocations(ICVSRepositoryLocation[] locations) {
			this.locations = locations;
		}
		public ICVSRepositoryLocation getLocation() {
			return location;
		}
	};
	
	/*
	 * @see PreferencesPage#createContents
	 */
	protected Control createContents(Composite parent) {
		initialize();
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		Label label = createLabel(composite, Policy.bind("CVSPropertiesPage.connectionType"), 1); //$NON-NLS-1$
		methodLabel = createLabel(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.user"), 1); //$NON-NLS-1$
		userLabel = createLabel(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.host"), 1); //$NON-NLS-1$
		hostLabel = createLabel(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.port"), 1); //$NON-NLS-1$
		portLabel = createLabel(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.path"), 1); //$NON-NLS-1$
		pathLabel = createLabel(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.module"), 1); //$NON-NLS-1$
		moduleLabel = createLabel(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.tag"), 1); //$NON-NLS-1$
		tagLabel = createLabel(composite, "", 1); //$NON-NLS-1$
		
		createLabel(composite, "", 1); //$NON-NLS-1$
		createLabel(composite, "", 1); //$NON-NLS-1$
		createLabel(composite, "", 1); //$NON-NLS-1$
		createLabel(composite, "", 1); //$NON-NLS-1$
		
		label = new Label(composite, SWT.WRAP);
		label.setText(Policy.bind("CVSProjectPropertiesPage.You_can_change_the_sharing_of_this_project_to_another_repository_location._However,_this_is_only_possible_if_the_new_location_is___compatible___(on_the_same_host_with_the_same_repository_path)._1")); //$NON-NLS-1$
		GridData data = new GridData();
		data.widthHint = 300;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		Button changeButton = new Button(composite, SWT.PUSH);
		changeButton.setText(Policy.bind("CVSProjectPropertiesPage.Change_Sharing_5")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, changeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		changeButton.setLayoutData(data);
		changeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				// Find out which repo locations are appropriate
				ICVSRepositoryLocation[] locations = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownRoots();
				List compatibleLocations = new ArrayList();
				for (int i = 0; i < locations.length; i++) {
					ICVSRepositoryLocation location = locations[i];
					// Only locations with the same host and root are eligible
					if (!location.getHost().equals(hostLabel.getText())) continue;
					if (!location.getRootDirectory().equals(pathLabel.getText())) continue;
					if (location.equals(oldLocation)) continue;
					compatibleLocations.add(location);
				}
				RepositorySelectionDialog dialog = new RepositorySelectionDialog(getShell());
				dialog.setLocations((ICVSRepositoryLocation[])compatibleLocations.toArray(new ICVSRepositoryLocation[compatibleLocations.size()]));
				dialog.open();
				ICVSRepositoryLocation location = dialog.getLocation();
				if (location == null) return;
				newLocation = location;
				initializeValues(newLocation);
			}
		});
		
		initializeValues(oldLocation);
		WorkbenchHelp.setHelp(composite, IHelpContextIds.PROJECT_PROPERTY_PAGE);
		return composite;
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
	 * Initializes the page
	 */
	private void initialize() {
		// Get the project that is the source of this property page
		project = null;
		IAdaptable element = getElement();
		if (element instanceof IProject) {
			project = (IProject)element;
		} else {
			Object adapter = element.getAdapter(IProject.class);
			if (adapter instanceof IProject) {
				project = (IProject)adapter;
			}
		}
		// Do some pre-checks to ensure we're in a good state
		provider = (CVSTeamProvider)RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId());
		if (provider == null) return;
		CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();
		try {
			oldLocation = cvsRoot.getRemoteLocation();
		} catch (TeamException e) {
			handle(e);
		}
	}
	/**
	 * Set the initial values of the widgets
	 */
	private void initializeValues(ICVSRepositoryLocation location) {
		if (provider == null) return;
		CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();
		ICVSFolder folder = cvsRoot.getLocalRoot();
		
		try {
			if (!folder.isCVSFolder()) return;
			methodLabel.setText(location.getMethod().getName());
			info = location.getUserInfo(true);
			userLabel.setText(info.getUsername());
			hostLabel.setText(location.getHost());
			int port = location.getPort();
			if (port == ICVSRepositoryLocation.USE_DEFAULT_PORT) {
				portLabel.setText(Policy.bind("CVSPropertiesPage.defaultPort")); //$NON-NLS-1$
			} else {
				portLabel.setText("" + port); //$NON-NLS-1$
			}
			pathLabel.setText(location.getRootDirectory());
			FolderSyncInfo syncInfo = folder.getFolderSyncInfo();
			if (syncInfo == null) return;
			moduleLabel.setText(syncInfo.getRepository());
		} catch (TeamException e) {
			handle(e);
		}
		
		initializeTag();
	}
	
	private void initializeTag() {
		provider = (CVSTeamProvider)RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId());
		if (provider == null) return;
		CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();
		try {
			ICVSFolder local = cvsRoot.getCVSFolderFor(project);
			CVSTag tag = local.getFolderSyncInfo().getTag();
			String tagName;
			if (tag == null) {
				tagName = CVSTag.DEFAULT.getName();
			} else {
				tagName = tag.getName();
			}
			tagLabel.setText(tagName);
		} catch (TeamException e) {
			handle(e);
		}
	}
	/*
	 * @see PreferencesPage#performOk
	 */
	public boolean performOk() {
		if (newLocation == null) {
			return true;
		}
		try {
			new ProgressMonitorDialog(getShell()).run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						provider.setRemoteRoot(newLocation, monitor);
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
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
			return false;
		}
					
		return true;
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

