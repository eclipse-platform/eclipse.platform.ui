/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * The WizardProjectsImportPage is the page that allows the 
 * user to import projects from a particular location.
 * 
 */
public class WizardProjectsImportPage extends WizardPage {

	private class ProjectRecord {
		File projectFile;

		String projectName;

		IProjectDescription description;

		/**
		 * Create a record for a project based on the 
		 * info in the file.
		 * @param file
		 */
		ProjectRecord(File file) {
			projectFile = file;
			setProjectName();
		}

		/**
		 * Set the name of the project based on the projectFile.
		 */
		private void setProjectName() {

			IPath path = new Path(projectFile.getPath());

			IProjectDescription newDescription = null;

			try {
				newDescription = IDEWorkbenchPlugin.getPluginWorkspace().loadProjectDescription(
						path);
			} catch (CoreException exception) {
				//no good couldn't get the name
			}

			if (newDescription == null) {
				this.description = null;
				projectName = "";  //$NON-NLS-1$
			} else {
				this.description = newDescription;
				projectName = this.description.getName();
			}
		}

		/**
		 * Get the name of the project
		 * @return String
		 */
		public String getProjectName() {
			return projectName;
		}
	}

	private Text locationPathField;

	private CheckboxTreeViewer projectsList;

	private ProjectRecord[] selectedProjects = new ProjectRecord[0];

	//	Keep track of the directory that we browsed to last time
	//the wizard was invoked.
	private static String previouslyBrowsedDirectory = "";  //$NON-NLS-1$

	/**
	 * Creates a new project creation wizard page.
	 *
	 */
	public WizardProjectsImportPage() {
		super("wizardExternalProjectsPage");  //$NON-NLS-1$
		setPageComplete(false);
		setTitle(DataTransferMessages.getString("WizardProjectsImportPage.ImportProjectsTitle"));  //$NON-NLS-1$
		setDescription(DataTransferMessages.getString("WizardProjectsImportPage.ImportProjectsDescription"));  //$NON-NLS-1$

	}

	/**
	 * Create a new instance of the receiver.
	 * @param pageName
	 */
	public WizardProjectsImportPage(String pageName) {
		super(pageName);
	}

	/**
	 * Create a new instance of the receiver.
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public WizardProjectsImportPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		Composite workArea = new Composite(parent, SWT.NONE);
		setControl(workArea);

		workArea.setLayout(new GridLayout());
		workArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL));

		createProjectsRoot(workArea);
		createProjectsList(workArea);
		Dialog.applyDialogFont(workArea);

	}

	/**
	 * Create the checkbox list for the found projects.
	 * @param workArea
	 */
	private void createProjectsList(Composite workArea) {

		Label title = new Label(workArea, SWT.NONE);
		title.setText(DataTransferMessages.getString("WizardProjectsImportPage.ProjectsListTitle"));  //$NON-NLS-1$

		Composite listComposite = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.makeColumnsEqualWidth = false;
		listComposite.setLayout(layout);

		listComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
				| GridData.FILL_BOTH));

		projectsList = new CheckboxTreeViewer(listComposite, SWT.BORDER);
		GridData listData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
				| GridData.FILL_BOTH);
		projectsList.getControl().setLayoutData(listData);
		
		

		projectsList.setContentProvider(new ITreeContentProvider() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
			 */
			public Object[] getChildren(Object parentElement) {
				return null;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return selectedProjects;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
			 */
			public boolean hasChildren(Object element) {
				return false;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
			 */
			public Object getParent(Object element) {
				return null;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {

			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});

		projectsList.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((ProjectRecord) element).getProjectName();
			}
		});

		projectsList.setInput(this);
		createSelectionButtons(listComposite);

	}

	/**
	 * Create the selection buttons in the listComposite.
	 * @param listComposite
	 */
	private void createSelectionButtons(Composite listComposite) {
		Composite buttonsComposite = new Composite(listComposite, SWT.NONE);
		buttonsComposite.setLayout(new GridLayout());
		
		buttonsComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		Button selectAll = new Button(buttonsComposite, SWT.PUSH);
		selectAll.setText(DataTransferMessages.getString("DataTransfer.selectAll"));  //$NON-NLS-1$
		selectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				projectsList.setCheckedElements(selectedProjects);
			}
		});
		
		setButtonLayoutData(selectAll);
		

		Button deselectAll = new Button(buttonsComposite, SWT.PUSH);
		deselectAll.setText(DataTransferMessages.getString("DataTransfer.deselectAll"));  //$NON-NLS-1$
		deselectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				projectsList.setCheckedElements(new Object[0]);
			}
		});
		
		setButtonLayoutData(deselectAll);

	}

	/**
	 * Create the area where you select the root directory
	 * for the projects.
	 * @param workArea Composite
	 */
	private void createProjectsRoot(Composite workArea) {

		// project specification group
		Composite projectGroup = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectContentsLabel = new Label(projectGroup, SWT.NONE);
		projectContentsLabel.setText(DataTransferMessages.getString("WizardProjectsImportPage.RootSelectTitle")); //$NON-NLS-1$

		// project location entry field
		this.locationPathField = new Text(projectGroup, SWT.BORDER);

		this.locationPathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL));
		// browse button
		Button browseButton = new Button(projectGroup, SWT.PUSH);
		browseButton.setText(DataTransferMessages.getString("DataTransfer.browse")); //$NON-NLS-1$
		setButtonLayoutData(browseButton);

		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
			}

		});

		locationPathField.addModifyListener(new ModifyListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			public void modifyText(ModifyEvent e) {
				updateProjectsList(locationPathField.getText().trim());
			}
		});

	}

	/**
	 * Update the list of projects based on path
	 * @param path
	 */
	protected void updateProjectsList(String path) {

		File directory = new File(path);
		selectedProjects = new ProjectRecord[0];
		if (directory.isDirectory()) {
			Collection files = new ArrayList();
			collectProjectFiles(files, directory);
			Iterator filesIterator = files.iterator();
			selectedProjects = new ProjectRecord[files.size()];
			int index = 0;
			while (filesIterator.hasNext()) {
				File file = (File) filesIterator.next();
				selectedProjects[index] = new ProjectRecord(file);
				index++;
			}
		}
		
		projectsList.refresh(true);
		projectsList.setCheckedElements(selectedProjects);
		setPageComplete(selectedProjects.length > 0);
	}

	/**
	 * Collect the list of .project files that are under
	 * directory into files.
	 * @param files
	 * @param directory
	 */
	private void collectProjectFiles(Collection files, File directory) {
		File[] contents = directory.listFiles();
		for (int i = 0; i < contents.length; i++) {
			File file = contents[i];
			if (file.isDirectory())
				collectProjectFiles(files, file);
			else {
				if (file.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME))
					files.add(file);
			}
		}

	}

	/**
	 * The browse button has been selected. Select the
	 * location.
	 */
	protected void handleLocationBrowseButtonPressed() {

		DirectoryDialog dialog = new DirectoryDialog(locationPathField.getShell());
		dialog.setMessage(DataTransferMessages.getString("WizardProjectsImportPage.SelectDialogTitle")); //$NON-NLS-1$

		String dirName = locationPathField.getText().trim();
		if (dirName.length() == 0)
			dirName = previouslyBrowsedDirectory;

		if (dirName.length() == 0) 
			dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation()
					.toOSString());
		else {
			File path = new File(dirName);
			if (path.exists())
				dialog.setFilterPath(new Path(dirName).toOSString());
		}

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			previouslyBrowsedDirectory = selectedDirectory;
			locationPathField.setText(previouslyBrowsedDirectory);
		}

	}

	/**
	 * Create the selected projects
	 * @return boolean <code>true</code> if all project
	 * creations were successful.
	 */
	public boolean createProjects() {
		for (int i = 0; i < selectedProjects.length; i++) {
			ProjectRecord record = selectedProjects[i];
			if (!createExistingProject(record))
				return false;
		}
		return true;
	}

	/**
	 * Create the project described in record. If it is 
	 * successful return true.
	 * @param record
	 * @return boolean <code>true</code> of successult
	 */
	private boolean createExistingProject(final ProjectRecord record) {

		String projectName = record.getProjectName();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		if (record.description == null) {
			record.description = workspace.newProjectDescription(projectName);
			IPath locationPath = new Path(record.projectFile.getAbsolutePath());
			//If it is under the root use the default location
			if (Platform.getLocation().isPrefixOf(locationPath))
				record.description.setLocation(null);
			else
				record.description.setLocation(locationPath);
		} else
			record.description.setName(projectName);

		// create the new project operation
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask("", 2000);  //$NON-NLS-1$
				project.create(record.description, new SubProgressMonitor(monitor, 1000));
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));

			}
		};

		// run the new project creation operation
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			// ie.- one of the steps resulted in a core exception	
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				if (((CoreException) t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
					MessageDialog.openError(getShell(), DataTransferMessages.getString("WizardExternalProjectImportPage.errorMessage "),  //$NON-NLS-1$
							DataTransferMessages.getString("WizardProjectsImportPage.CaseVariantError")  //$NON-NLS-1$
					);
				} else {
					ErrorDialog.openError(getShell(), DataTransferMessages.getString("WizardExternalProjectImportPage.errorMessage "),  //$NON-NLS-1$
							null, ((CoreException) t).getStatus());
				}
			}
			return false;
		}

		return true;

	}

}
