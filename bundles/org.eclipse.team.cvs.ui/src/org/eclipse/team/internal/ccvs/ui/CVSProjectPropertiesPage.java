/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.IUserInfo;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class CVSProjectPropertiesPage extends CVSPropertiesPage {
	IProject project;
	ICVSRepositoryLocation oldLocation;
	ICVSRepositoryLocation newLocation = null;

	private static final int TABLE_HEIGHT_HINT = 150;
	
	// Widgets
	Text methodText;
	Text userText;
	Text hostText;
	Text pathText;
	Text moduleText;
	Text portText;
	Text tagText;
	private Button fetchButton;
	private Button watchEditButton;
	
	IUserInfo info;
	CVSTeamProvider provider;
	private boolean fetch;
	private boolean watchEdit;

	public static boolean isCompatible(ICVSRepositoryLocation location, ICVSRepositoryLocation oldLocation) {
		if (!location.getHost().equals(oldLocation.getHost())) return false;
		if (!location.getRootDirectory().equals(oldLocation.getRootDirectory())) return false;
		if (location.equals(oldLocation)) return false;
		return true;
	}
	
	private class RepositorySelectionDialog extends Dialog {
		ICVSRepositoryLocation[] allLocations;
		ICVSRepositoryLocation[] compatibleLocatons;
		ICVSRepositoryLocation selectedLocation;
		
		TableViewer viewer;
		Button okButton;
		boolean showCompatible = true;
		
		public RepositorySelectionDialog(Shell shell, ICVSRepositoryLocation oldLocation) {
			super(shell);
			initialize(oldLocation);
		}
		private void initialize(ICVSRepositoryLocation oldLocation) {
			allLocations = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownRepositoryLocations();
			List locations = new ArrayList();
			for (int i = 0; i < allLocations.length; i++) {
				ICVSRepositoryLocation location = allLocations[i];
				if (isCompatible(location, oldLocation)) {
					locations.add(location);
				}
			}
			compatibleLocatons = (ICVSRepositoryLocation[]) locations.toArray(new ICVSRepositoryLocation[locations.size()]);
		}
		protected void createButtonsForButtonBar(Composite parent) {
			// create OK and Cancel buttons by default
			okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			okButton.setEnabled(false);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}
		protected Control createDialogArea(Composite parent) {
			parent.getShell().setText(Policy.bind("CVSProjectPropertiesPage.Select_a_Repository_1")); //$NON-NLS-1$
			Composite composite = (Composite) super.createDialogArea(parent);
		
			createLabel(composite, Policy.bind("CVSProjectPropertiesPage.Select_a_CVS_repository_location_to_share_the_project_with__2"), 1); //$NON-NLS-1$
			Table table = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.heightHint = TABLE_HEIGHT_HINT;
			table.setLayoutData(data);
			viewer = new TableViewer(table);
			viewer.setLabelProvider(new WorkbenchLabelProvider());
			viewer.setContentProvider(new WorkbenchContentProvider() {
				public Object[] getElements(Object inputElement) {
					if (showCompatible) {
						return compatibleLocatons;
					} else {
						return allLocations;
					}
				}
			});
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
					if (selection.isEmpty()) {
						selectedLocation = null;
						okButton.setEnabled(false);
					} else {
						selectedLocation = (ICVSRepositoryLocation)selection.getFirstElement();
						okButton.setEnabled(true);
					}
				}
			});
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					okPressed();
				}
			});
			viewer.setInput(compatibleLocatons);
			
			final Button compatibleButton = createCheckBox(composite, Policy.bind("CVSProjectPropertiesPage.31")); //$NON-NLS-1$
			compatibleButton.setSelection(showCompatible);
			compatibleButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					showCompatible = compatibleButton.getSelection();
					viewer.refresh();
				}
			});

			Dialog.applyDialogFont(parent);
			
			return composite;
		}
		protected void cancelPressed() {
			selectedLocation = null;
			super.cancelPressed();
		}
		public ICVSRepositoryLocation getLocation() {
			return selectedLocation;
		}
	}
	
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
		
		Label label = createLabel(composite, Policy.bind("CVSProjectPropertiesPage.connectionType"), 1); //$NON-NLS-1$
		methodText = createReadOnlyText(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSProjectPropertiesPage.user"), 1); //$NON-NLS-1$
		userText = createReadOnlyText(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.host"), 1); //$NON-NLS-1$
		hostText = createReadOnlyText(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.port"), 1); //$NON-NLS-1$
		portText = createReadOnlyText(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.path"), 1); //$NON-NLS-1$
		pathText = createReadOnlyText(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.module"), 1); //$NON-NLS-1$
		moduleText = createReadOnlyText(composite, "", 1); //$NON-NLS-1$
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.tag"), 1); //$NON-NLS-1$
		tagText = createReadOnlyText(composite, "", 1); //$NON-NLS-1$
		
		createLabel(composite, "", 1); //$NON-NLS-1$
		
		// Should absent directories be fetched on update
		fetchButton = createCheckBox(composite, Policy.bind("CVSProjectPropertiesPage.fetchAbsentDirectoriesOnUpdate")); //$NON-NLS-1$
		fetchButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				fetch = fetchButton.getSelection();
			}
		});
		
		// Should the project be configured for watch/edit
		watchEditButton = createCheckBox(composite, Policy.bind("CVSProjectPropertiesPage.configureForWatchEdit")); //$NON-NLS-1$
		watchEditButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				watchEdit = watchEditButton.getSelection();
			}
		});
		
		createLabel(composite, "", 1); //$NON-NLS-1$
		createLabel(composite, "", 1); //$NON-NLS-1$
		createLabel(composite, "", 1); //$NON-NLS-1$
		createLabel(composite, "", 1); //$NON-NLS-1$
		
		label = new Label(composite, SWT.WRAP);
		label.setText(Policy.bind("CVSProjectPropertiesPage.You_can_change_the_sharing_of_this_project_to_another_repository_location._However,_this_is_only_possible_if_the_new_location_is___compatible___(on_the_same_host_with_the_same_repository_path)._1")); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 200;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		Button changeButton = new Button(composite, SWT.PUSH);
		changeButton.setText(Policy.bind("CVSProjectPropertiesPage.Change_Sharing_5")); //$NON-NLS-1$
		changeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				RepositorySelectionDialog dialog = new RepositorySelectionDialog(getShell(), oldLocation);
				dialog.open();
				ICVSRepositoryLocation location = dialog.getLocation();
				if (location == null) return;
				newLocation = location;
				initializeValues(newLocation);
			}
		});
		
		initializeValues(oldLocation);
		WorkbenchHelp.setHelp(getControl(), IHelpContextIds.PROJECT_PROPERTY_PAGE);
        Dialog.applyDialogFont(parent);
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
	 * Utility method that creates a text instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new text
	 * @return the new text
	 */
	protected Text createReadOnlyText(Composite parent, String text, int span) {
		Text txt = new Text(parent, SWT.LEFT | SWT.READ_ONLY);
		txt.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		txt.setLayoutData(data);
		return txt;
	}
	/**
	 * Creates a new checkbox instance and sets the default layout data.
	 *
	 * @param group  the composite in which to create the checkbox
	 * @param label  the string to set into the checkbox
	 * @return the new checkbox
	 */ 
	protected Button createCheckBox(Composite group, String label) {
		Button button = new Button(group, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		button.setLayoutData(data);
		return button;
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
			fetch = provider.getFetchAbsentDirectories();
			watchEdit = provider.isWatchEditEnabled();
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
			methodText.setText(location.getMethod().getName());
			info = location.getUserInfo(true);
			userText.setText(info.getUsername());
			hostText.setText(location.getHost());
			int port = location.getPort();
			if (port == ICVSRepositoryLocation.USE_DEFAULT_PORT) {
                portText.setText(Policy.bind("CVSPropertiesPage.defaultPort")); //$NON-NLS-1$
			} else {
				portText.setText("" + port); //$NON-NLS-1$
			}
			pathText.setText(location.getRootDirectory());
			FolderSyncInfo syncInfo = folder.getFolderSyncInfo();
			if (syncInfo == null) return;
			String label = syncInfo.getRepository();
			if (label.equals(FolderSyncInfo.VIRTUAL_DIRECTORY)) {
				label = Policy.bind("CVSPropertiesPage.virtualModule", label); //$NON-NLS-1$
			}
			moduleText.setText(label);
			fetchButton.setSelection(fetch);
			watchEditButton.setSelection(watchEdit);
		} catch (TeamException e) {
			handle(e);
		}
		
		initializeTag();
	}
	
	private void initializeTag() {
		provider = (CVSTeamProvider)RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId());
		if (provider == null) return;
		try {
			ICVSFolder local = CVSWorkspaceRoot.getCVSFolderFor(project);
			CVSTag tag = local.getFolderSyncInfo().getTag();
			
			tagText.setText(getTagLabel(tag));

		} catch (TeamException e) {
			handle(e);
		}
	}
	/*
	 * @see PreferencesPage#performOk
	 */
	public boolean performOk() {
		final boolean[] changeReadOnly = { false };
		try {
			if (fetch != provider.getFetchAbsentDirectories())
				provider.setFetchAbsentDirectories(fetch);
			if (watchEdit != provider.isWatchEditEnabled()) {
				provider.setWatchEditEnabled(watchEdit);
				changeReadOnly[0] = true;
			}
		} catch (CVSException e) {
			handle(e);
		}
		if (newLocation == null && !changeReadOnly[0]) {
			return true;
		}
		try {
			if (newLocation != null && !isCompatible(newLocation, oldLocation)) {
				if (!MessageDialog.openQuestion(getShell(), Policy.bind("CVSProjectPropertiesPage.32"), Policy.bind("CVSProjectPropertiesPage.33"))) { //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
			}
			new ProgressMonitorDialog(getShell()).run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask(Policy.bind("CVSProjectPropertiesPage.progressTaskName"),  //$NON-NLS-1$
						((newLocation == null)?0:100) + (changeReadOnly[0]?100:0));
						if (newLocation != null)
							provider.setRemoteRoot(newLocation, Policy.subMonitorFor(monitor, 100));
						if (changeReadOnly[0])
							setReadOnly(watchEdit, Policy.infiniteSubMonitorFor(monitor, 100));
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
			newLocation = null;
			if (changeReadOnly[0]) {
				CVSUIPlugin.broadcastPropertyChange(new PropertyChangeEvent(this, CVSUIPlugin.P_DECORATORS_CHANGED, null, null));
			}
		} catch (InvocationTargetException e) {
			handle(e);
		} catch (InterruptedException e) {
			return false;
		}
			
		return true;
	}
	/**
	 * @param watchEdit
	 */
	protected void setReadOnly(final boolean watchEdit, final IProgressMonitor monitor) throws CVSException {
		monitor.beginTask(null, 512);
		String taskName = watchEdit?
			Policy.bind("CVSProjectPropertiesPage.setReadOnly"): //$NON-NLS-1$
			Policy.bind("CVSProjectPropertiesPage.clearReadOnly"); //$NON-NLS-1$
		monitor.subTask(taskName);
		ICVSFolder root = CVSWorkspaceRoot.getCVSFolderFor(project);
		root.accept(new ICVSResourceVisitor() {
			public void visitFile(ICVSFile file) throws CVSException {
				// only change managed, unmodified files
				if (file.isManaged() && !file.isModified(null))
					file.setReadOnly(watchEdit);
				monitor.worked(1);
			}

			public void visitFolder(ICVSFolder folder) throws CVSException {
				folder.acceptChildren(this);
			}
		});
		monitor.done();
	}
	/**
	 * Shows the given errors to the user.
	 */
	protected void handle(Throwable t) {
		CVSUIPlugin.openError(getShell(), null, null, t);
	}
}

